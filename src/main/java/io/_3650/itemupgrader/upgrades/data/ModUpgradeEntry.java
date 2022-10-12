package io._3650.itemupgrader.upgrades.data;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ModUpgradeEntry {
	
	private static final UpgradeEntry.Factory FACTORY = new UpgradeEntry.Factory(ItemUpgrader.MOD_ID);
	
	public static final UpgradeEntry<Multimap<Attribute, AttributeModifier>> ATTRIBUTES = FACTORY.create("attributes");
	
	public static final UpgradeEntry<SetMultimap<Attribute, AttributeModifier>> ATTRIBUTE_ADDITIONS = FACTORY.create("attribute_additions");
	public static final UpgradeEntry<Set<AttributeReplacement>> ATTRIBUTE_REPLACEMENTS = FACTORY.create("attribute_replacements");
	
}