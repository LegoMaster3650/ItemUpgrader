package io._3650.itemupgrader.events;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.event.UpgradeEvent;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemUpgrader.MOD_ID)
public class UpgraderEvents {
	
	@SubscribeEvent
	public static void upgradeApplyPre(UpgradeEvent.Apply.Pre event) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.UPGRADE_APPLY_PRE, new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.UPGRADE_ID, event.upgradeId)
				.cancellable());
		if (data.isCancelled() && event.isCancelable()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void upgradeApplyPost(UpgradeEvent.Apply.Post event) {
		ItemUpgraderApi.runActions(ModUpgradeActions.UPGRADE_APPLY_POST, new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.UPGRADE_ID, event.upgradeId));
	}
	
	@SubscribeEvent
	public static void upgradeRemove(UpgradeEvent.Remove event) {
		ItemUpgraderApi.runActions(ModUpgradeActions.UPGRADE_REMOVE, new UpgradeEventData.Builder(event.stack)
				.entry(UpgradeEntry.PREV_UPGRADE_ID, event.upgradeId));
	}
	
}