package io._3650.itemupgrader_content.events;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader_content.ItemUpgrader;
import io._3650.itemupgrader_content.registry.ModUpgradeActions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemUpgrader.MOD_ID)
public class ModEvents {
	
	private static final String SOULBOUND_SAVED_TAG = "SoulboundSavedItems";
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingDrops(LivingDropsEvent event) {
		if (event.getEntity() instanceof Player player1) {
			Player player = (Player) player1;
			if (player instanceof FakePlayer || player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
			CompoundTag persist = player.getPersistentData();
			ListTag saved = new ListTag();
			var items = event.getDrops().iterator();
			while (items.hasNext()) {
				ItemStack stack = items.next().getItem();
				if (ItemUpgraderApi.runActions(ModUpgradeActions.PLAYER_DEATH_DROP, new UpgradeEventData.Builder(player)
						.entry(UpgradeEntry.ITEM, stack).cancellable()).isCancelled()) {
					items.remove();
					saved.add(stack.serializeNBT());
				}
			}
			if (saved.size() > 0) persist.put(SOULBOUND_SAVED_TAG, saved);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		Player player = event.getEntity();
		CompoundTag persist = event.getOriginal().getPersistentData();
		if (persist.contains(SOULBOUND_SAVED_TAG, CompoundTag.TAG_LIST)) {
			ListTag load = persist.getList(SOULBOUND_SAVED_TAG, CompoundTag.TAG_COMPOUND);
			for (var itemNbt : load) if (itemNbt instanceof CompoundTag tag) {
				ItemStack stack = ItemStack.of(tag);
				if (!stack.isEmpty() && !player.getInventory().add(stack)) {
					player.drop(stack, true, false);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onEndermanLook(EnderManAngerEvent event) {
		if (ItemUpgraderApi.runActions(ModUpgradeActions.ENDERMAN_LOOK, new UpgradeEventData.Builder(event.getPlayer(), EquipmentSlot.HEAD).cancellable()).isCancelled()) {
			event.setCanceled(true);
		}
	}
	
}