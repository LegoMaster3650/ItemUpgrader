package io._3650.itemupgrader.api.type;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;

/**
 * Base class for Upgrade Conditions (Used to gate the execution of results in an action behind a condition)
 * @author LegoMaster3650
 */
public abstract class UpgradeCondition extends IUpgradeType {
	
	private final boolean inverted;
	
	/**
	 * Constructs an {@linkplain IUpgradeType} using the given internals
	 * @param internals {@linkplain UpgradeCondition} containing information for this type
	 * @param inverted Whether or not the condition is inverted
	 */
	public UpgradeCondition(IUpgradeInternals internals, boolean inverted) {
		super(internals);
		this.inverted = inverted;
	}
	
	/**
	 * Gets the entry data required by this condition to function properly
	 * @return An {@linkplain UpgradeEntrySet} of every {@linkplain UpgradeEntry} required by this condition
	 */
	public abstract UpgradeEntrySet requiredData();
	
	/**
	 * Tests this condition against the provided data which is verified against the required entry set
	 * @param data The {@linkplain UpgradeEventData} containing the current action data
	 * @return Whether or not the condition passes
	 */
	public abstract boolean test(UpgradeEventData data);
	
	/**
	 * Use this to return your class's serializer instance.<br>
	 * Ensure the return type is an UpgradeConditionSerializer<<b>This Class</b>> in some form, whether just that or a subclass of that, just please make sure it's not the default Wildcard ? type
	 * @return Your own serializer instance
	 */
	public abstract UpgradeConditionSerializer<?> getSerializer();
	
	/**
	 * Gets whether this condition is inverted.
	 * @return Whether or not the condition is inverted
	 */
	public final boolean isInverted() {
		return this.inverted;
	}
	
}