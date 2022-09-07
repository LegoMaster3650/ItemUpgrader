package io._3650.itemupgrader.api.type;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;

/**
 * Base class for Upgrade Results (The actions performed when all conditions pass in a simple upgrade action)
 * @author LegoMaster3650
 */
public abstract class UpgradeResult extends IUpgradeType {
	
	/**
	 * Constructs an {@linkplain IUpgradeType} using the given internals
	 * @param internals {@linkplain UpgradeResult} containing information for this type
	 */
	public UpgradeResult(IUpgradeInternals internals) {
		super(internals);
	}
	
	/**
	 * Gets the entry data required by this result to function properly
	 * @return An {@linkplain UpgradeEntrySet} of every {@linkplain UpgradeEntry} required by this result
	 */
	public abstract UpgradeEntrySet requiredData();
	
	/**
	 * Runs this condition with the provided data which is verified against the required entry set
	 * @param data The {@linkplain UpgradeEventData} containing the current action data
	 */
	public abstract void execute(UpgradeEventData data);
	
	/**
	 * Use this to return your class's serializer instance.<br>
	 * Ensure the return type is an UpgradeConditionSerializer<<b>This Class</b>> in some form, whether just that or a subclass of that, just please make sure it's not the default Wildcard ? type
	 * @return Your own serializer instance
	 */
	public abstract UpgradeResultSerializer<?> getSerializer();
	
}