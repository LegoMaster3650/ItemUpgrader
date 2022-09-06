package io._3650.itemupgrader.api.type;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;

public abstract class UpgradeCondition extends IUpgradeType {
	
	private final boolean inverted;
	
	public UpgradeCondition(IUpgradeInternals internals, boolean inverted) {
		super(internals);
		this.inverted = inverted;
	}
	
	public abstract UpgradeEntrySet requiredData();
	
	public abstract boolean test(UpgradeEventData data);
	
	/**
	 * Use this to return your class's serializer instance.<br>
	 * Ensure the return type is an UpgradeConditionSerializer<<b>This Class</b>> in some form, whether just that or a subclass of that, just please make sure it's not the default Wildcard ? type
	 * @return Your own serializer instance
	 */
	public abstract UpgradeConditionSerializer<?> getSerializer();
	
	public final boolean isInverted() {
		return this.inverted;
	}
	
}