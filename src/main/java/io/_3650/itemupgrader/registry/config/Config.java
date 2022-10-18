package io._3650.itemupgrader.registry.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class Config {
	
	public static class Server {
		
		public final BooleanValue basePackEnabled;
		public final BooleanValue basePackRecipes;
		
		Server(ForgeConfigSpec.Builder builder) {
			builder.push("datapack");
			
			basePackEnabled = builder.comment("Is the mod's default upgrade datapack enabled?","Disable this if you only want to use custom upgrade datapacks.","[Default: true]").define("basePackEnabled", true);
			basePackRecipes = builder.comment("Should the mod's default upgrade datapack recipes be enabled?", "Disable this to easily use your own recipes.", "[Default: true]").define("basePackRecipes", true);
			
			builder.pop();
		}
		
	}
	
	public static class Common {
		
	}
	
	public static class Client {
		
		public final BooleanValue showUpgradeID;
		public final BooleanValue requiresKeyHeld;
		
		Client(ForgeConfigSpec.Builder builder) {
			builder.push("tooltip");
			
			requiresKeyHeld = builder.comment("Does the upgrade tooltip require a key to be held to expand?", "[Default: true]").define("requiresKeyHeld", true);
			showUpgradeID = builder.comment("Show an item's upgrade id when advanced tooltips are enabled", "[Default: false]").define("showUpgradeID", false);
			
			builder.pop();
		}
		
	}
	
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Server SERVER;
	
	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final Client CLIENT;
	
	static {
		final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER_SPEC = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();
		
		final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();
	}
	
}