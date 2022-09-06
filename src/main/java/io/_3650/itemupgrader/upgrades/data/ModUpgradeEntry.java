package io._3650.itemupgrader.upgrades.data;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.upgrades.actions.AttributeUpgradeAction.AttributeReplacement;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ModUpgradeEntry {
	
	public static final UpgradeEntry<Multimap<Attribute, AttributeModifier>> ATTRIBUTES = create("attributes");
	
	public static final UpgradeEntry<SetMultimap<Attribute, AttributeModifier>> ATTRIBUTE_ADDITIONS = create("attribute_additions");
	public static final UpgradeEntry<Set<AttributeReplacement>> ATTRIBUTE_REPLACEMENTS = create("attribute_replacements");
	
	private static <T> UpgradeEntry<T> create(String name) {
		return new UpgradeEntry<T>(ItemUpgraderRegistry.modRes(name));
	}
	
}