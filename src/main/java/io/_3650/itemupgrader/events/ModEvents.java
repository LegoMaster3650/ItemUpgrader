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
import io._3650.itemupgrader.api.event.LivingTotemEvent;
import io._3650.itemupgrader.network.NetworkHandler;
import io._3650.itemupgrader.network.PlayerLeftClickEmptyPacket;
import io._3650.itemupgrader.network.PlayerRightClickEmptyPacket;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import io._3650.itemupgrader.upgrades.data.AttributeReplacement;
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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
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
		Player player = event.player;
		if (player.level.isClientSide) return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot);
			ItemUpgraderApi.runActions(event.phase == TickEvent.Phase.END ? ModUpgradeActions.PLAYER_TICK_POST : ModUpgradeActions.PLAYER_TICK_PRE, builder);
		}
	}
	
	@SubscribeEvent
	public static void livingTick(LivingUpdateEvent event) {
		LivingEntity living = event.getEntityLiving();
		if (living.level.isClientSide) return;
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
				.entry(ModUpgradeEntry.ATTRIBUTES, event.getModifiers())
				.modifiableEntry(ModUpgradeEntry.ATTRIBUTE_ADDITIONS, MultimapBuilder.hashKeys().hashSetValues().build())
				.modifiableEntry(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS, Sets.newHashSet());
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ATTRIBUTE, builder);
		Set<AttributeReplacement> replacements = data.getEntry(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS);
		if (!replacements.isEmpty()) {
			for (var replacement : replacements) {
				event.removeModifier(replacement.target(), replacement.oldAttribute());
				event.addModifier(replacement.target(), replacement.newAttribute());
			}
		}
		SetMultimap<Attribute, AttributeModifier> additions = data.getEntry(ModUpgradeEntry.ATTRIBUTE_ADDITIONS);
		if (!additions.isEmpty()) {
			for (var entry : additions.entries()) {
				event.addModifier(entry.getKey(), entry.getValue());
			}
		}
		UpgradeEventData.Builder builder2 = UpgradeEventData.builder()
				.entry(UpgradeEntry.ITEM, event.getItemStack())
				.entry(UpgradeEntry.SLOT, event.getSlotType())
				.entry(ModUpgradeEntry.ATTRIBUTES, event.getModifiers())
				.modifiableEntry(ModUpgradeEntry.ATTRIBUTE_ADDITIONS, MultimapBuilder.hashKeys().hashSetValues().build())
				.modifiableEntry(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS, Sets.newHashSet())
				.modifiableEntry(UpgradeEntry.CONSUMED, false);
		UpgradeEventData data2 = ItemUpgraderApi.runActions(ModUpgradeActions.ATTRIBUTE_COMPLEX, builder2);
		if (data2.getBoolEntry(UpgradeEntry.CONSUMED)) return;
		Set<AttributeReplacement> replacements2 = data2.getEntry(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS);
		if (!replacements2.isEmpty()) {
			for (var replacement : replacements2) {
				event.removeModifier(replacement.target(), replacement.oldAttribute());
				event.addModifier(replacement.target(), replacement.newAttribute());
			}
		}
		SetMultimap<Attribute, AttributeModifier> additions2 = data2.getEntry(ModUpgradeEntry.ATTRIBUTE_ADDITIONS);
		if (!additions2.isEmpty()) {
			for (var entry : additions2.entries()) {
				event.addModifier(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/*
	 * RIGHT CLICK (USE)
	 */
	
	@SubscribeEvent
	public static void interactEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		if (player.level.isClientSide) return;
		Entity targetEntity = event.getTarget();
		Vec3 targetPos = targetEntity.position();
		Vec3 interactionPos = targetPos.add(event.getLocalPos());
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.TARGET_ENTITY, targetEntity)
				.entry(UpgradeEntry.TARGET_ENTITY_POS, targetPos)
				.entry(UpgradeEntry.INTERACTION_POS, interactionPos)
				.modifiableEntry(UpgradeEntry.CONSUMED, false)
				.cancellable();
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENTITY_INTERACT_SPECIFIC, builder);
		if (data.getBoolEntry(UpgradeEntry.CONSUMED)) {
			event.setCancellationResult(InteractionResult.CONSUME);
			event.setCanceled(true);
		} else if (data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void interactEntity(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getPlayer();
		if (player.level.isClientSide) return;
		EquipmentSlot slot = slotFromHand(event.getHand());
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
		Player player = event.getPlayer();
		if (player.level.isClientSide) return;
		EquipmentSlot slot = slotFromHand(event.getHand());
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.BLOCK_POS, pos)
				.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
				.entry(UpgradeEntry.BLOCK_STATE, state)
				.entry(UpgradeEntry.INTERACTION_POS, event.getHitVec().getLocation())
				.modifiableEntry(UpgradeEntry.CONSUMED, false)
				.cancellable();
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_BLOCK, builder, event.getItemStack());
		if (data.getBoolEntry(UpgradeEntry.CONSUMED)) {
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
					.modifiableEntry(UpgradeEntry.CONSUMED, false));
			if (data1.getBoolEntry(UpgradeEntry.CONSUMED)) {
				event.setCancellationResult(InteractionResult.CONSUME);
				event.setCanceled(true);
				return;
			}
		}
		rightClickBase(slot, player, event.getItemStack());
	}
	
	@SubscribeEvent
	public static void playerUseItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getPlayer();
		if (player.level.isClientSide) return;
		EquipmentSlot slot = slotFromHand(event.getHand());
		UpgradeEventData data = rightClickBaseData(slot, player, event.getItemStack().isEmpty());
		if (data != null && data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void playerUseEmpty(PlayerInteractEvent.RightClickEmpty event) {
		if (event.getSide().isClient()) NetworkHandler.sendToServer(new PlayerRightClickEmptyPacket(slotFromHand(event.getHand())));
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
		if (player.level.isClientSide) return;
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.BLOCK_POS, pos)
				.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
				.entry(UpgradeEntry.BLOCK_STATE, state)
				.entry(UpgradeEntry.INTERACTION_POS, Vec3.atCenterOf(pos))
				.modifiableEntry(UpgradeEntry.CONSUMED, false);
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_BLOCK, builder, event.getItemStack());
		if (data.getBoolEntry(UpgradeEntry.CONSUMED)) return;
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			UpgradeEventData data1 = ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_BLOCK_EFFECT, new UpgradeEventData.Builder(player, slot1)
					.entry(UpgradeEntry.BLOCK_POS, pos)
					.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
					.entry(UpgradeEntry.BLOCK_STATE, state)
					.entry(UpgradeEntry.INTERACTION_POS, Vec3.atCenterOf(pos))
					.modifiableEntry(UpgradeEntry.CONSUMED, false));
			if (data1.getBoolEntry(UpgradeEntry.CONSUMED)) return;
			
		}
		leftClickBase(slot, player, event.getItemStack());
	}
	
	@SubscribeEvent
	public static void playerClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		if (event.getSide().isClient()) NetworkHandler.sendToServer(new PlayerLeftClickEmptyPacket(slotFromHand(event.getHand()), event.getItemStack().isEmpty()));
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
		Player player = event.getPlayer();
		if (player.level.isClientSide) return;
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.PLAYER_ATTACK, new UpgradeEventData.Builder(player, EquipmentSlot.MAINHAND)
				.entry(UpgradeEntry.TARGET_ENTITY, event.getTarget())
				.entry(UpgradeEntry.TARGET_ENTITY_POS, event.getTarget().position())
				.entry(UpgradeEntry.INTERACTION_POS, event.getTarget().position())
				.cancellable());
		if (data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void livingPreHurt(LivingAttackEvent event) {
		LivingEntity living = event.getEntityLiving();
		if (living.level.isClientSide) return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (living.hasItemInSlot(slot)) {
				UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_PRE_HURT, new UpgradeEventData.Builder(living, slot)
						.entry(UpgradeEntry.DAMAGE_SOURCE, event.getSource())
						.entry(UpgradeEntry.DAMAGE, event.getAmount())
						.cancellable());
				if (data.isCancelled()) {
					event.setCanceled(true);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void livingHurt(LivingHurtEvent event) {
		LivingEntity living = event.getEntityLiving();
		if (living.level.isClientSide) return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (living.hasItemInSlot(slot)) {
				UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_HURT, new UpgradeEventData.Builder(living, slot)
						.entry(UpgradeEntry.DAMAGE_SOURCE, event.getSource())
						.modifiableEntry(UpgradeEntry.DAMAGE, event.getAmount())
						.cancellable());
				if (data.isCancelled()) {
					event.setCanceled(true);
					return;
				} else {
					float resAmount = data.getEntry(UpgradeEntry.DAMAGE);
					if (resAmount != event.getAmount()) event.setAmount(resAmount);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void livingDamage(LivingDamageEvent event) {
		LivingEntity living = event.getEntityLiving();
		if (living.level.isClientSide) return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (living.hasItemInSlot(slot)) {
				UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_DAMAGE, new UpgradeEventData.Builder(living, slot)
						.entry(UpgradeEntry.DAMAGE_SOURCE, event.getSource())
						.modifiableEntry(UpgradeEntry.DAMAGE, event.getAmount())
						.cancellable());
				if (data.isCancelled()) {
					event.setCanceled(true);
					return;
				} else {
					float resAmount = data.getEntry(UpgradeEntry.DAMAGE);
					if (resAmount != event.getAmount()) event.setAmount(resAmount);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void fallDamage(LivingFallEvent event) {
		LivingEntity living = event.getEntityLiving();
		if (living.level.isClientSide) return;
		for (var slot : EquipmentSlot.values()) {
			UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_FALL, new UpgradeEventData.Builder(living, slot)
					.modifiableEntry(UpgradeEntry.FALL_DIST, event.getDistance())
					.modifiableEntry(UpgradeEntry.DAMAGE_MULT, event.getDamageMultiplier())
					.modifiableEntry(UpgradeEntry.CONSUMED, false)
					.cancellable());
			if (data.isCancelled()) {
				event.setCanceled(true);
				return;
			} else if (data.getBoolEntry(UpgradeEntry.CONSUMED)) return;
			float fallDist = data.getEntry(UpgradeEntry.FALL_DIST);
			float damageMult = data.getEntry(UpgradeEntry.DAMAGE_MULT);
			if (fallDist != event.getDistance()) event.setDistance(fallDist);
			if (damageMult != event.getDamageMultiplier()) event.setDamageMultiplier(damageMult);
		}
	}
	
	//counts as a damage event enough to me
	//also I had to make this whole thing myself using mixins because forge has no totem event ;-;
	@SubscribeEvent
	public static void totemTriggerEventPre(LivingTotemEvent.Pre event) {
		LivingEntity living = event.living;
		if (living.level.isClientSide) return;
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_TOTEM_PRE, new UpgradeEventData.Builder(living)
				.entry(UpgradeEntry.ITEM, event.totem)
				.entry(UpgradeEntry.SLOT, slotFromHand(event.hand))
				.entry(UpgradeEntry.DAMAGE_SOURCE, event.damageSource)
				.cancellable());
		if (data.isCancelled()) event.setCanceled(true);
	}
	
	//fun fact I spent like 2 hours troubleshooting this cuz I forgot to add an @SubscribeEvent!! I LOVE PROGRAMMING PROGRAMMING IS FUN!!!
	@SubscribeEvent
	public static void totemTriggerEventPost(LivingTotemEvent.Post event) {
		LivingEntity living = event.living;
		if (living.level.isClientSide) return;
		ItemUpgraderApi.runActions(ModUpgradeActions.LIVING_TOTEM_POST, new UpgradeEventData.Builder(living)
				.entry(UpgradeEntry.ITEM, event.totem)
				.entry(UpgradeEntry.DAMAGE_SOURCE, event.damageSource));
	}
	
}