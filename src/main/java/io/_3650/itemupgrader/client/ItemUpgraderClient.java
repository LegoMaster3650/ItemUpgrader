package io._3650.itemupgrader.client;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ItemUpgraderClient {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public ItemUpgraderClient() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::clientSetup);
	}
	
	public void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			LOGGER.debug("Item Upgrader client init");
			ModKeybinds.init();
		});
	}
	
}