package io._3650.itemupgrader.registry.types;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.registry.RegistryHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ModUpgradeEntry {
	
	private static final UpgradeEntry.Factory FACTORY = new UpgradeEntry.Factory(ItemUpgrader.MOD_ID);
	
	public static final UpgradeEntry<Multimap<Attribute, AttributeModifier>> ATTRIBUTES = FACTORY.create("attributes", RegistryHelper.fixStupidClass(Multimap.class));
	
	public static final UpgradeEntry<SetMultimap<Attribute, AttributeModifier>> ATTRIBUTE_ADDITIONS = FACTORY.create("attribute_additions", RegistryHelper.fixStupidClass(SetMultimap.class));
	public static final UpgradeEntry<Set<AttributeReplacement>> ATTRIBUTE_REPLACEMENTS = FACTORY.create("attribute_replacements", RegistryHelper.fixStupidClass(Set.class));
	
}