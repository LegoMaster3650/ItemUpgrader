package io._3650.itemupgrader.event;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.network.NetworkHandler;
import io._3650.itemupgrader.network.UpdateItemUpgradesPacket;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.tags.TagManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemUpgrader.MOD_ID)
public class DataEvents {
	
	//This dumb hack seems like the best way to do it and I'll just hope my dumb thing never gets called without context if it refuses to listen without context
	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		java.util.Iterator<TagManager> iterator = event.getServerResources().listeners().stream().filter(listener -> listener instanceof TagManager).map(listener -> (TagManager) listener).iterator();
		if (iterator.hasNext()) {
			ItemUpgradeManager.INSTANCE.setContext(new net.minecraftforge.common.crafting.conditions.ConditionContext(iterator.next()));
			event.addListener(ItemUpgradeManager.INSTANCE);
		}
	}
	
	@SubscribeEvent
	public static void syncDatapack(OnDatapackSyncEvent event) {
		if (event.getPlayer() != null) {
			NetworkHandler.sendToPlayer(event.getPlayer(), new UpdateItemUpgradesPacket(ItemUpgradeManager.INSTANCE.getUpgrades()));
		} else {
			event.getPlayerList().getPlayers().forEach((player) -> {
				NetworkHandler.sendToPlayer(player, new UpdateItemUpgradesPacket(ItemUpgradeManager.INSTANCE.getUpgrades()));
			});
		}
	}
	
}