package io.legom.itemupgrader.registry;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.api.ItemUpgraderRegistry;
import io.legom.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io.legom.itemupgrader.upgrades.conditions.EyesInFluidUpgradeCondition;
import io.legom.itemupgrader.upgrades.conditions.PredicateUpgradeCondition;
import io.legom.itemupgrader.upgrades.conditions.HasUpgradeCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeConditions {
	
	public static final DeferredRegister<UpgradeConditionSerializer<?>> CONDITIONS = DeferredRegister.create(ItemUpgraderRegistry.CONDITIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<PredicateUpgradeCondition.Serializer> PREDICATE = CONDITIONS.register("predicate", () -> new PredicateUpgradeCondition.Serializer());
	public static final RegistryObject<EyesInFluidUpgradeCondition.Serializer> EYES_IN_FLUID = CONDITIONS.register("eyes_in_fluid", () -> new EyesInFluidUpgradeCondition.Serializer());
	public static final RegistryObject<HasUpgradeCondition.Serializer> HAS_UPGRADE = CONDITIONS.register("has_upgrade", () -> new HasUpgradeCondition.Serializer());
	
}