package io._3650.itemupgrader_content;

import com.mojang.logging.LogUtils;

import io._3650.itemupgrader_content.network.NetworkHandler;
import io._3650.itemupgrader_content.recipes.conditions.BasePackEnabledCondition;
import io._3650.itemupgrader_content.registry.ModUpgradeConditions;
import io._3650.itemupgrader_content.registry.ModUpgradeResults;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ItemUpgrader.MOD_ID)
public class ItemUpgrader {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public static final String MOD_ID = "itemupgrader_content";
	
	public ItemUpgrader() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
//		ModUpgradeActions.ACTIONS.register(bus);
		ModUpgradeConditions.CONDITIONS.register(bus);
		ModUpgradeResults.RESULTS.register(bus);
		
		bus.addListener(this::setup);
		
//		ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_SPEC, MOD_ID + "-server.toml");
		ModLoadingContext.get().registerConfig(Type.COMMON, Config.COMMON_SPEC, MOD_ID + "-common.toml");
		ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_SPEC, MOD_ID + "-client.toml");
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			LOGGER.debug("Item Upgrader common setup");
			//actual common stuff
			CraftingHelper.register(BasePackEnabledCondition.Serializer.INSTANCE);
			NetworkHandler.init();
		});
	}
}