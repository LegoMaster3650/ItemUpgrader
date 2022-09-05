package io.legom.itemupgrader.api;

import io.legom.itemupgrader.ItemUpgrader;
import net.minecraft.resources.ResourceLocation;

/**
 * Contains resource locations for the ItemUpgrader registries
 * @author legom
 *
 */
public class ItemUpgraderRegistry {
	
	public static final ResourceLocation ACTIONS = modRes("upgrade_types");
	public static final ResourceLocation CONDITIONS = modRes("condition_types");
	public static final ResourceLocation RESULTS = modRes("result_types");
	
	public static final ResourceLocation TYPED_CRITERIA = modRes("typed_criteria");
	
	public static final ResourceLocation UPGRADE_SLOTS = modRes("upgrade_slots");
	
	/**
	 * Mostly just public for internal use only for creating resource locations with the mod id easily<br>
	 * Please use your own Mod ID if possible instead of stealing mine with this method
	 * @param name The path to use for the resource location
	 * @return A ResourceLocation with the value itemupgrader:{@literal <name>}
	 */
	public static ResourceLocation modRes(String name) {
		return new ResourceLocation(ItemUpgrader.MOD_ID, name);
	}
	
}