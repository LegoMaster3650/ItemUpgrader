package io._3650.itemupgrader_content.registry;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.type.SimpleUpgradeAction;
import io._3650.itemupgrader_content.ItemUpgrader;
import io._3650.itemupgrader_content.upgrades.actions.special.SoulboundChargesUpgradeAction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeActions {
	
	public static final DeferredRegister<UpgradeActionSerializer<?>> ACTIONS = DeferredRegister.create(ItemUpgraderRegistry.ACTIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<SoulboundChargesUpgradeAction.Serializer> SPECIAL_SOULBOUND_CHARGES = ACTIONS.register("internal_soulbound_charges", () -> new SoulboundChargesUpgradeAction.Serializer());
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> PLAYER_DEATH_DROP = ACTIONS.register("death_drop", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_ITEM.with(builder -> builder.cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> ENDERMAN_LOOK = ACTIONS.register("enderman_look", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_SLOT_ITEM.with(builder -> builder.cancellable())));
	
}