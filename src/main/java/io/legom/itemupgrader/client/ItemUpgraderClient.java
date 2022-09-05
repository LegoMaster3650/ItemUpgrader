package io.legom.itemupgrader.client;

import io.legom.itemupgrader.ItemUpgrader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ItemUpgraderClient {
	
	public ItemUpgraderClient() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::clientSetup);
	}
	
	public void clientSetup(FMLClientSetupEvent event) {
		ItemUpgrader.LOGGER.info("Item Upgrader client init");
		ModKeybinds.init();
	}
	
}