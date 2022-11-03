package io._3650.itemupgrader.registry.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config {
	
	public static class Server {
		
		
		
		Server(ForgeConfigSpec.Builder builder) {
			
		}
		
	}
	
	public static class Common {

		public final BooleanValue basePackEnabled;
		public final BooleanValue basePackRecipes;
		
		Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Strangely buggy when in server config so this is in common config instead.").push("datapack");
			
			basePackEnabled = builder.comment("Is the mod's default upgrade datapack enabled?","Disable this if you only want to use custom upgrade datapacks.","[Default: true]").define("basePackEnabled", true);
			basePackRecipes = builder.comment("Should the mod's default upgrade datapack recipes be enabled?", "Disable this to easily use your own recipes.", "[Default: true]").define("basePackRecipes", true);
			
			builder.pop();
		}
		
	}
	
	public static class Client {
		
		public final BooleanValue requiresKeyHeld;
		public final BooleanValue showUpgradeID;
		public final BooleanValue useRomanNumerals;
		
		public final IntValue coordinateDisplayMode;
		public final IntValue timeDisplayMode;
		
		Client(ForgeConfigSpec.Builder builder) {
			builder.push("tooltip");
			
			requiresKeyHeld = builder.comment("Does the upgrade tooltip require a key to be held to expand?", "[Default: true]").define("requiresKeyHeld", true);
			showUpgradeID = builder.comment("Show an item's upgrade id when advanced tooltips are enabled", "[Default: false]").define("showUpgradeID", false);
			useRomanNumerals = builder.comment("Uses roman numerals instead of numbers when describing enchantments.", "[Default: false]").define("useRomanNumerals", false);
			
			builder.pop();
			///////////////////////////////////////////////////////////
			builder.push("information_format");
			
			coordinateDisplayMode = builder.comment(
					"The display mode for any Reveal coordinates upgrades.",
					"1 - XYZ: X.XXX / Y.YYYYY / Z.ZZZ",
					"2 - XYZ: X.XXX, Y.YYYYY, Z.ZZZ",
					"3 - X.XXX / Y.YYYYY / Z.ZZZ",
					"4 - X.XXX, Y.YYYYY, Z.ZZZ",
					"[Default: 1]").defineInRange("coordinateDisplayMode", 1, 1, 4);
			timeDisplayMode = builder.comment(
					"The display mode for any Reveal time upgrades.",
					"1 - Day #, HH:MM AM/PM",
					"2 - Day #, HH:MM",
					"3 - HH:MM AM/PM",
					"4 - HH:MM",
					"[Default: 1]").defineInRange("timeDisplayMode", 1, 1, 4);
			
			builder.pop();
		}
		
	}
	
//	public static final ForgeConfigSpec SERVER_SPEC;
//	public static final Server SERVER;
	
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final Common COMMON;
	
	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final Client CLIENT;
	
	static {
//		final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
//		SERVER_SPEC = serverSpecPair.getRight();
//		SERVER = serverSpecPair.getLeft();
		
		final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = commonSpecPair.getRight();
		COMMON = commonSpecPair.getLeft();
		
		final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();
	}
	
}