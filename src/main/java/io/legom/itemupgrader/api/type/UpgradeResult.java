package io.legom.itemupgrader.api.type;

import io.legom.itemupgrader.api.data.UpgradeEntrySet;
import io.legom.itemupgrader.api.data.UpgradeEventData;
import io.legom.itemupgrader.api.serializer.UpgradeResultSerializer;

public abstract class UpgradeResult extends IUpgradeType {
	
	public UpgradeResult(IUpgradeInternals internals) {
		super(internals);
	}
	
	public abstract UpgradeEntrySet requiredData();
	
	public abstract void execute(UpgradeEventData data);
	
	/**
	 * Use this to return your class's serializer instance.<br>
	 * Ensure the return type is an UpgradeConditionSerializer<<b>This Class</b>> in some form, whether just that or a subclass of that, just please make sure it's not the default Wildcard ? type
	 * @return Your own serializer instance
	 */
	public abstract UpgradeResultSerializer<?> getSerializer();
	
}