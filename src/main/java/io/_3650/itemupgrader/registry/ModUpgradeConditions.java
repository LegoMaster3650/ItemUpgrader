package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.upgrades.conditions.CompareNumbersUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.EyesInFluidUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.HasUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.ItemCooldownUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.PredicateUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.TagVarBoolUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.TagVarFloatUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.TagVarIntUpgradeCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeConditions {
	
	public static final DeferredRegister<UpgradeConditionSerializer<?>> CONDITIONS = DeferredRegister.create(ItemUpgraderRegistry.CONDITIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<PredicateUpgradeCondition.Serializer> PREDICATE = CONDITIONS.register("predicate", () -> new PredicateUpgradeCondition.Serializer());
	public static final RegistryObject<EyesInFluidUpgradeCondition.Serializer> EYES_IN_FLUID = CONDITIONS.register("eyes_in_fluid", () -> new EyesInFluidUpgradeCondition.Serializer());
	public static final RegistryObject<HasUpgradeCondition.Serializer> HAS_UPGRADE = CONDITIONS.register("has_upgrade", () -> new HasUpgradeCondition.Serializer());
	public static final RegistryObject<CompareNumbersUpgradeCondition.Serializer> COMPARE_NUMBERS = CONDITIONS.register("compare", () -> new CompareNumbersUpgradeCondition.Serializer());
	public static final RegistryObject<ItemCooldownUpgradeCondition.Serializer> COOLDOWN = CONDITIONS.register("cooldown", () -> new ItemCooldownUpgradeCondition.Serializer());
	public static final RegistryObject<TagVarBoolUpgradeCondition.Serializer> TAGVAR_BOOLEAN = CONDITIONS.register("tag_boolean", () -> new TagVarBoolUpgradeCondition.Serializer());
	public static final RegistryObject<TagVarIntUpgradeCondition.Serializer> TAGVAR_INT = CONDITIONS.register("tag_int", () -> new TagVarIntUpgradeCondition.Serializer());
	public static final RegistryObject<TagVarFloatUpgradeCondition.Serializer> TAGVAR_FLOAT = CONDITIONS.register("tag_float", () -> new TagVarFloatUpgradeCondition.Serializer());
	
	
}