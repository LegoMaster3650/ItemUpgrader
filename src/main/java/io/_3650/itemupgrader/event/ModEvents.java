package io._3650.itemupgrader.event;

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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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
		Vec3 targetPos = targetEntity.position().add(event.getLocalPos());
		UpgradeEventData.Builder builder = new UpgradeEventData.Builder(player, slot)
				.entry(UpgradeEntry.TARGET_ENTITY, targetEntity)
				.entry(UpgradeEntry.TARGET_ENTITY_POS, targetPos)
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
				.result(UpgradeEntry.CONSUMED, false)
				.cancellable();
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_BLOCK, builder);
		if (data.isCancelled()) {
			event.setCanceled(true);
			return;
		}
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			UpgradeEventData data1 = ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK_BLOCK_EFFECT, new UpgradeEventData.Builder(player, slot)
					.entry(UpgradeEntry.BLOCK_POS, pos)
					.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
					.entry(UpgradeEntry.BLOCK_STATE, state)
					.result(UpgradeEntry.CONSUMED, false));
			if (data1.getBoolResult(UpgradeEntry.CONSUMED)) {
				event.setCancellationResult(InteractionResult.CONSUME);
				event.setCanceled(true);
				return;
			}
		}
		if (!data.getBoolResult(UpgradeEntry.CONSUMED)) {
			boolean emptyStack = event.getItemStack().isEmpty();
			rightClickBase(slot, player, emptyStack);
		}
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
		rightClickBase(slot, player, true);
	}
	
	public static void rightClickBase(EquipmentSlot slot, Player player, boolean emptyStack) {
		if (!emptyStack) ItemUpgraderApi.runActions(ModUpgradeActions.RIGHT_CLICK, new UpgradeEventData.Builder(player, slot));
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
				.result(UpgradeEntry.CONSUMED, false);
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_BLOCK, builder);
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_BLOCK_EFFECT, data);
			if (data.getBoolResult(UpgradeEntry.CONSUMED)) return;
		}
		if (!data.getBoolResult(UpgradeEntry.CONSUMED)) {
			boolean emptyStack = event.getItemStack().isEmpty();
			leftClickBase(slot, player, emptyStack);
		}
	}
	
	@SubscribeEvent
	public static void playerClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		Player player = event.getPlayer();
		boolean emptyStack = event.getItemStack().isEmpty();
		if (event.getSide().isClient()) NetworkHandler.sendToServer(new PlayerLeftClickEmptyPacket(slot, emptyStack));
		leftClickBase(slot, player, emptyStack);
	}
	
	public static void leftClickBase(EquipmentSlot slot, Player player, boolean emptyStack) {
		if (!emptyStack) ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK, new UpgradeEventData.Builder(player, slot));
		for (var slot1 : EquipmentSlot.values()) {
			if (slot1 == slot) continue;
			ItemUpgraderApi.runActions(ModUpgradeActions.LEFT_CLICK_EFFECT, new UpgradeEventData.Builder(player, slot1));
		}
	}
	
	private static EquipmentSlot slotFromHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
	}
	
	/*
	 * ATTACK
	 */
	
	public static void playerAttack(AttackEntityEvent event) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.PLAYER_ATTACK, new UpgradeEventData.Builder(event.getPlayer(), EquipmentSlot.MAINHAND)
				.entry(UpgradeEntry.TARGET_ENTITY, event.getTarget())
				.entry(UpgradeEntry.TARGET_ENTITY_POS, event.getTarget().position())
				.cancellable());
		if (data.isCancelled()) event.setCanceled(true);
	}
	
}