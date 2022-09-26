package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.upgrades.conditions.CompareNumbersCondition;
import io._3650.itemupgrader.upgrades.conditions.EyesInFluidUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.HasUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.PredicateUpgradeCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeConditions {
	
	public static final DeferredRegister<UpgradeConditionSerializer<?>> CONDITIONS = DeferredRegister.create(ItemUpgraderRegistry.CONDITIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<PredicateUpgradeCondition.Serializer> PREDICATE = CONDITIONS.register("predicate", () -> new PredicateUpgradeCondition.Serializer());
	public static final RegistryObject<EyesInFluidUpgradeCondition.Serializer> EYES_IN_FLUID = CONDITIONS.register("eyes_in_fluid", () -> new EyesInFluidUpgradeCondition.Serializer());
	public static final RegistryObject<HasUpgradeCondition.Serializer> HAS_UPGRADE = CONDITIONS.register("has_upgrade", () -> new HasUpgradeCondition.Serializer());
	public static final RegistryObject<CompareNumbersCondition.Serializer> COMPARE_NUMBERS = CONDITIONS.register("compare", () -> new CompareNumbersCondition.Serializer());
	
}