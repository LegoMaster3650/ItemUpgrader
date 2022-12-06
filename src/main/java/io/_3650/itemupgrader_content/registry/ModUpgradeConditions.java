package io._3650.itemupgrader_content.registry;

import io._3650.itemupgrader_content.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader_content.upgrades.conditions.special.CanReboundUpgradeCondition;
import io._3650.itemupgrader_content.upgrades.conditions.special.CanReflectUpgradeCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeConditions {
	
	public static final DeferredRegister<UpgradeConditionSerializer<?>> CONDITIONS = DeferredRegister.create(ItemUpgraderRegistry.CONDITIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<CanReflectUpgradeCondition.Serializer> CAN_PARRY = CONDITIONS.register("internal_can_parry", () -> new CanReflectUpgradeCondition.Serializer());
	public static final RegistryObject<CanReboundUpgradeCondition.Serializer> CAN_REBOUND = CONDITIONS.register("internal_can_rebound", () -> new CanReboundUpgradeCondition.Serializer());
	
	
}