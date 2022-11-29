package io._3650.itemupgrader.registry.types;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;

public class ModUpgradeEntrySet {
	
	public static final UpgradeEntrySet ATTRIBUTES = UpgradeEntrySet.SLOT_ITEM.with(builder -> {
		builder
		.require(ModUpgradeEntry.ATTRIBUTES)
		.modifiable(ModUpgradeEntry.ATTRIBUTE_ADDITIONS)
		.modifiable(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS);
	});
	
}