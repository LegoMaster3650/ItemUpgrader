package io.legom.itemupgrader.event;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.network.NetworkHandler;
import io.legom.itemupgrader.network.UpdateItemUpgradesPacket;
import io.legom.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemUpgrader.MOD_ID)
public class DataEvents {
	
	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener(ItemUpgradeManager.INSTANCE);
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