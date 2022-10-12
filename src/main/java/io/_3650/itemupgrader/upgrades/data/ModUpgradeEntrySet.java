package io._3650.itemupgrader.upgrades.data;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;

public class ModUpgradeEntrySet {
	
	public static final UpgradeEntrySet ATTRIBUTES = UpgradeEntrySet.SLOT_ITEM.with(builder -> {
		builder
		.require(ModUpgradeEntry.ATTRIBUTES)
		.require(ModUpgradeEntry.ATTRIBUTE_ADDITIONS)
		.require(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS);
	});
	
}