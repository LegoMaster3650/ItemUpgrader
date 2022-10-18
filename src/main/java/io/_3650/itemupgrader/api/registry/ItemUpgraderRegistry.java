package io._3650.itemupgrader.api.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ingredient.TypedCriteria;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.resources.ResourceLocation;

/**
 * Contains resource locations for the ItemUpgrader registries
 * @author LegoMaster3650
 *
 */
public class ItemUpgraderRegistry {
	
	/**The registry for {@linkplain UpgradeAction}*/
	public static final ResourceLocation ACTIONS = modRes("upgrade_types");
	/**The registry for {@linkplain UpgradeCondition}*/
	public static final ResourceLocation CONDITIONS = modRes("condition_types");
	/**The registry for {@linkplain UpgradeResult}*/
	public static final ResourceLocation RESULTS = modRes("result_types");
	/**The registry for {@linkplain TypedCriteria}*/
	public static final ResourceLocation TYPED_CRITERIA = modRes("typed_criteria");
	
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