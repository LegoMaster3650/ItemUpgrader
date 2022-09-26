package io._3650.itemupgrader.events;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.network.NetworkHandler;
import io._3650.itemupgrader.network.PlayerLeftClickEmptyPacket;
import io._3650.itemupgrader.network.PlayerRightClickEmptyPacket;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import io._3650.itemupgrader.upgrades.actions.AttributeUpgradeAction.AttributeReplacement;
import io._3650.itemupgrader.upgrades.data.ModUpgradeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemUpgrader.MOD_ID)
public class ModEvents {
	
	/*
	 * TICKING
	 */
	
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent event) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			UpgradeEventData.Builder builder = new UpgradeEventData.Builder(event.player, slot);
			ItemUpgraderApi.runActions(event.phase == TickEvent.Phase.END ? ModUpgradeActions.PLAYER_TICK_POST : ModUpgradeActions.PLAYER_TICK_PRE, builder);
		}
	}
	
	@SubscribeEvent
	public static void livingTick(LivingUpdateEvent event) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			UpgradeEventData.Builder builder = new UpgradeEventData.Builder(event.getEntityLiving(), slot);
			ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_TICK, builder);
		}
	}
	
	/*
	 * ATTRIBUTES
	 */
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void attributeModifiers(ItemAttributeModifierEvent event) {
		UpgradeEventData.Builder builder = UpgradeEventData.builder()
				.entry(UpgradeEntry.ITEM, event.getItemStack())
				.entry(UpgradeEntry.SLOT, event.getSlotType())
				.entry(ModUpgradeEntry.ATTRIBUTES, MultimapBuilder.hashKeys().hashSetValues().build(event.getOriginalModifiers()))
				.result(ModUpgradeEntry.ATTRIBUTE_ADDITIONS, MultimapBuilder.hashKeys().hashSetValues().build())
				.result(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS, Sets.newHashSet());
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ATTRIBUTE, builder);
		Set<AttributeReplacement> replacements = data.getResult(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS);
		if (!replacements.isEmpty()) {
			replacements.forEach(replacement -> {
				event.removeModifier(replacement.target(), replacement.oldAttribute());
				event.addModifier(replacement.target(), replacement.newAttribute());
			});
		}
		SetMultimap<Attribute, AttributeModifier> additions = data.getResult(ModUpgradeEntry.ATTRIBUTE_ADDITIONS);
		if (!additions.isEmpty()) {
			additions.forEach((attribute, modifier) -> {
				event.addModifier(attribute, modifier);
			});
		}
	}
	
	/*
	 * RIGHT CLICK (USE)
	 */
	
	@SubscribeEvent
	public static void interactEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		Entity targetEntity = event.getTarget();
		Vec3 targetPos = targetEntity.position();
		Vec3 interactionPos = targetPos.add(event.getLocalPos());
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.TARGET_ENTITY, targetEntity)
				.entry(UpgradeEntry.TARGET_ENTITY_POS, targetPos)
				.entry(UpgradeEntry.INTERACTION_POS, interactionPos)
				.result(UpgradeEntry.CONSUMED, false)
				.cancellable();
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENTITY_INTERACT_SPECIFIC, builder);
		if (data.getBoolResult(UpgradeEntry.CONSUMED)) {
			event.setCancellationResult(InteractionResult.CONSUME);
			event.setCanceled(true);
		} else if (data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void interactEntity(PlayerInteractEvent.EntityInteract event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		Entity targetEntity = event.getTarget();
		Vec3 targetPos = targetEntity.position();
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.TARGET_ENTITY, targetEntity)
				.entry(UpgradeEntry.TARGET_ENTITY_POS, targetPos)
				.entry(UpgradeEntry.INTERACTION_POS, targetPos)
				.cancellable();
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENTITY_INTERACT, builder);
		if (data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void playerUseBlock(PlayerInteractEvent.RightClickBlock event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.BLOCK_POS, pos)
				.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
				.entry(UpgradeEntry.BLOCK_STATE, state)
				.entry(UpgradeEntry.INTERACTION_POS, event.getHitVec().getLocation())
				.result(UpgradeEntry.CONSUMED, false)
				.cancellable();
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_BLOCK, builder, event.getItemStack());
		if (data.getBoolResult(UpgradeEntry.CONSUMED)) {
			event.setCancellationResult(InteractionResult.CONSUME);
			event.setCanceled(true);
		}
		if (data.isCancelled()) {
			event.setCanceled(true);
			return;
		}
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			UpgradeEventData data1 = ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_BLOCK_EFFECT, new UpgradeEventData.Builder(player, slot1)
					.entry(UpgradeEntry.BLOCK_POS, pos)
					.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
					.entry(UpgradeEntry.BLOCK_STATE, state)
					.entry(UpgradeEntry.INTERACTION_POS, Vec3.atCenterOf(pos))
					.result(UpgradeEntry.CONSUMED, false));
			if (data1.getBoolResult(UpgradeEntry.CONSUMED)) {
				event.setCancellationResult(InteractionResult.CONSUME);
				event.setCanceled(true);
				return;
			}
		}
		rightClickBase(slot, player, event.getItemStack());
	}
	
	@SubscribeEvent
	public static void playerUseItem(PlayerInteractEvent.RightClickItem event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		UpgradeEventData data = rightClickBaseData(slot, player, event.getItemStack().isEmpty());
		if (data != null && data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void playerUseEmpty(PlayerInteractEvent.RightClickEmpty event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		if (event.getSide().isClient()) NetworkHandler.sendToServer(new PlayerRightClickEmptyPacket(slot));
		rightClickBase(slot, player, event.getItemStack());
	}
	
	public static void rightClickBase(EquipmentSlot slot, Player player, ItemStack stack) {
		if (!stack.isEmpty()) ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK, new UpgradeEventData.Builder(player, slot), stack);
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_EFFECT, new UpgradeEventData.Builder(player, slot1));
		}
	}
	
	@Nullable
	public static UpgradeEventData rightClickBaseData(EquipmentSlot slot, Player player, boolean emptyStack) {
		UpgradeEventData data = null;
		if (!emptyStack) data = ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK, new UpgradeEventData.Builder(player, slot).cancellable());
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_EFFECT, new UpgradeEventData.Builder(player, slot1));
		}
		return data;
	}
	
	/*
	 * LEFT CLICK (CLICK)
	 */
	
	@SubscribeEvent
	public static void playerClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.BLOCK_POS, pos)
				.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
				.entry(UpgradeEntry.BLOCK_STATE, state)
				.entry(UpgradeEntry.INTERACTION_POS, Vec3.atCenterOf(pos))
				.result(UpgradeEntry.CONSUMED, false);
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_BLOCK, builder, event.getItemStack());
		if (data.getBoolResult(UpgradeEntry.CONSUMED)) return;
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			UpgradeEventData data1 = ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_BLOCK_EFFECT, new UpgradeEventData.Builder(player, slot1)
					.entry(UpgradeEntry.BLOCK_POS, pos)
					.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
					.entry(UpgradeEntry.BLOCK_STATE, state)
					.entry(UpgradeEntry.INTERACTION_POS, Vec3.atCenterOf(pos))
					.result(UpgradeEntry.CONSUMED, false));
			if (data1.getBoolResult(UpgradeEntry.CONSUMED)) return;
			
		}
		leftClickBase(slot, player, event.getItemStack());
	}
	
	@SubscribeEvent
	public static void playerClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		ItemStack stack = event.getItemStack();
		if (event.getSide().isClient()) NetworkHandler.sendToServer(new PlayerLeftClickEmptyPacket(slot, stack.isEmpty()));
		leftClickBase(slot, player, stack);
	}
	
	public static void leftClickBase(EquipmentSlot slot, Player player, ItemStack stack) {
		if (!stack.isEmpty()) ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK, new UpgradeEventData.Builder(player, slot), stack);
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_EFFECT, new UpgradeEventData.Builder(player, slot1));
		}
	}
	
	private static EquipmentSlot slotFromHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
	}
	
	/*
	 * ATTACK/DAMAGE
	 */
	
	@SubscribeEvent
	public static void playerAttack(AttackEntityEvent event) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.PLAYER_ATTACK, new UpgradeEventData.Builder(event.getPlayer(), EquipmentSlot.MAINHAND)
				.entry(UpgradeEntry.TARGET_ENTITY, event.getTarget())
				.entry(UpgradeEntry.TARGET_ENTITY_POS, event.getTarget().position())
				.entry(UpgradeEntry.INTERACTION_POS, event.getTarget().position())
				.cancellable());
		if (data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void livingHurt(LivingHurtEvent event) {
		LivingEntity living = event.getEntityLiving();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (living.hasItemInSlot(slot)) {
				UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_HURT, new UpgradeEventData.Builder(living, slot)
						.entry(UpgradeEntry.DAMAGE_SOURCE, event.getSource())
						.entry(UpgradeEntry.DAMAGE, event.getAmount())
						.result(UpgradeEntry.DAMAGE, event.getAmount())
						.cancellable());
				if (data.isCancelled()) {
					event.setCanceled(true);
					return;
				} else {
					float resAmount = data.getResult(UpgradeEntry.DAMAGE);
					if (resAmount != event.getAmount()) event.setAmount(resAmount);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void livingDamage(LivingDamageEvent event) {
		LivingEntity living = event.getEntityLiving();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (living.hasItemInSlot(slot)) {
				UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_DAMAGE, new UpgradeEventData.Builder(living, slot)
						.entry(UpgradeEntry.DAMAGE_SOURCE, event.getSource())
						.entry(UpgradeEntry.DAMAGE, event.getAmount())
						.result(UpgradeEntry.DAMAGE, event.getAmount())
						.cancellable());
				if (data.isCancelled()) {
					event.setCanceled(true);
					return;
				} else {
					float resAmount = data.getResult(UpgradeEntry.DAMAGE);
					if (resAmount != event.getAmount()) event.setAmount(resAmount);
				}
			}
		}
	}
	
}