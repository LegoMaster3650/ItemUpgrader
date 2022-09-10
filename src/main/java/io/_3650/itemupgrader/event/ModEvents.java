package io._3650.itemupgrader.event;

import java.util.Set;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.event.UpgradeEvent;
import io._3650.itemupgrader.network.NetworkHandler;
import io._3650.itemupgrader.network.PlayerLeftClickEmptyPacket;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import io._3650.itemupgrader.upgrades.actions.AttributeUpgradeAction.AttributeReplacement;
import io._3650.itemupgrader.upgrades.data.ModUpgradeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemUpgrader.MOD_ID)
public class ModEvents {
	
	@SubscribeEvent
	public static void upgradeApplyPre(UpgradeEvent.Apply.Pre event) {
		UpgradeEventData data = new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.UPGRADE_ID, event.upgradeId)
				.cancellable()
				.build(UpgradeEntrySet.ITEM_UPGRADE_ID);
		ItemUpgraderApi.runAction(ModUpgradeActions.UPGRADE_APPLY_PRE.getId(), data, event.stack);
		if (data.isCancelled() && event.isCancelable()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void upgradeApplyPost(UpgradeEvent.Apply.Post event) {
		ItemUpgraderApi.runAction(ModUpgradeActions.UPGRADE_APPLY_POST.getId(), new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.UPGRADE_ID, event.upgradeId)
				.build(UpgradeEntrySet.ITEM_UPGRADE_ID), event.stack);
	}
	
	@SubscribeEvent
	public static void upgradeRemove(UpgradeEvent.Remove event) {
		ItemUpgraderApi.runAction(ModUpgradeActions.UPGRADE_REMOVE.getId(), new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.PREV_UPGRADE_ID, event.upgradeId)
				.build(UpgradeEntrySet.ITEM_PREV_UPGRADE_ID), event.stack);
	}
	
	@SubscribeEvent
	public static void upgradeReplace(UpgradeEvent.Replace event) {
		UpgradeEventData data = new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.UPGRADE_ID, event.upgradeId)
				.entry(UpgradeEntry.PREV_UPGRADE_ID, event.previousUpgradeId)
				.cancellable()
				.build(UpgradeEntrySet.ITEM_REPLACE_UPGRADE_IDS);
		ItemUpgraderApi.runAction(ModUpgradeActions.UPGRADE_REPLACE.getId(), data, event.stack);
		if (data.isCancelled() && event.isCancelable()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent event) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			UpgradeEventData data = new UpgradeEventData.Builder(event.player, slot)
					.build(UpgradeEntrySet.PLAYER_SLOT_ITEM);
			ItemUpgraderApi.runAction(event.phase == TickEvent.Phase.END ? ModUpgradeActions.PLAYER_TICK_POST.getId() : ModUpgradeActions.PLAYER_TICK_PRE.getId(), data);
		}
	}
	
	@SubscribeEvent
	public static void livingTick(LivingUpdateEvent event) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			UpgradeEventData data = new UpgradeEventData.Builder(event.getEntityLiving(), slot)
					.build(UpgradeEntrySet.LIVING_SLOT_ITEM);
			ItemUpgraderApi.runAction(ModUpgradeActions.LIVING_TICK.getId(), data);
		}
	}
	
	@SubscribeEvent
	public static void attributeModifiers(ItemAttributeModifierEvent event) {
		UpgradeEventData data = UpgradeEventData.builder()
				.entry(UpgradeEntry.ITEM, event.getItemStack())
				.entry(UpgradeEntry.SLOT, event.getSlotType())
				.entry(ModUpgradeEntry.ATTRIBUTES, MultimapBuilder.hashKeys().hashSetValues().build(event.getOriginalModifiers()))
				.result(ModUpgradeEntry.ATTRIBUTE_ADDITIONS, MultimapBuilder.hashKeys().hashSetValues().build())
				.result(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS, Sets.newHashSet())
				.build(UpgradeEntrySet.SLOT_ITEM);
		ItemUpgraderApi.runAction(ModUpgradeActions.ATTRIBUTE.getId(), data);
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
	
	@SubscribeEvent
	public static void playerUseItem(PlayerInteractEvent.RightClickItem event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		UpgradeEventData data = new UpgradeEventData.Builder(event.getPlayer(), slot)
				.cancellable()
				.build(UpgradeEntrySet.PLAYER_SLOT_ITEM);
		ItemUpgraderApi.runAction(ModUpgradeActions.PLAYER_USE.getId(), data);
		if (data.isCancelled()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void playerClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		UpgradeEventData data = new UpgradeEventData.Builder(event.getPlayer(), slot)
				.entry(UpgradeEntry.BLOCK_POS, pos)
				.entry(UpgradeEntry.BLOCK_FACE, event.getFace())
				.entry(UpgradeEntry.BLOCK_STATE, state)
				.cancellable()
				.build(UpgradeEntrySet.PLAYER_BLOCK_INTERACTION);
		ItemUpgraderApi.runAction(ModUpgradeActions.LEFT_CLICK_BLOCK.getId(), data);
		if (!data.isCancelled()) {
			UpgradeEventData data1 = new UpgradeEventData.Builder(event.getPlayer(), slot)
					.build(UpgradeEntrySet.PLAYER_SLOT_ITEM);
			ItemUpgraderApi.runAction(ModUpgradeActions.LEFT_CLICK.getId(), data1);
		}
	}
	
	@SubscribeEvent
	public static void playerClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		EquipmentSlot slot = slotFromHand(event.getHand());
		boolean emptyStack = event.getItemStack().isEmpty();
		NetworkHandler.sendToServer(new PlayerLeftClickEmptyPacket(slot, emptyStack));
		if (emptyStack) {
			for (var slot1 : EquipmentSlot.values()) {
				if (slot1 == slot) continue;
				UpgradeEventData data = new UpgradeEventData.Builder(event.getPlayer(), slot1)
						.build(UpgradeEntrySet.PLAYER_SLOT_ITEM);
				ItemUpgraderApi.runAction(ModUpgradeActions.LEFT_CLICK_EMPTY.getId(), data);
			}
		} else {
			UpgradeEventData data = new UpgradeEventData.Builder(event.getPlayer(), slot)
					.build(UpgradeEntrySet.PLAYER_SLOT_ITEM);
			ItemUpgraderApi.runAction(ModUpgradeActions.LEFT_CLICK.getId(), data);
		}
	}
	
	private static EquipmentSlot slotFromHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
	}
	
}