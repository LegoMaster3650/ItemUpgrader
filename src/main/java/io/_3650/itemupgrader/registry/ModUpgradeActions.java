package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.type.SimpleUpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.upgrades.actions.AttributeUpgradeAction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeActions {
	
	public static final DeferredRegister<UpgradeActionSerializer<? extends UpgradeAction>> ACTIONS = DeferredRegister.create(ItemUpgraderRegistry.ACTIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_APPLY_PRE = ACTIONS.register("upgrade_apply_pre", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.ITEM_UPGRADE_ID));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_APPLY_POST = ACTIONS.register("upgrade_apply_post", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.ITEM_UPGRADE_ID));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_REMOVE = ACTIONS.register("upgrade_remove", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.ITEM_PREV_UPGRADE_ID));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_REPLACE = ACTIONS.register("upgrade_replace", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.ITEM_REPLACE_UPGRADE_IDS));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_TICK = ACTIONS.register("living_tick", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.LIVING_SLOT_ITEM));
	public static final RegistryObject<UpgradeActionSerializer<AttributeUpgradeAction>> ATTRIBUTE = ACTIONS.register("attribute", () -> new AttributeUpgradeAction.Serializer());
	public static final RegistryObject<SimpleUpgradeAction.Serializer> PLAYER_TICK_PRE = ACTIONS.register("player_tick_pre", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.PLAYER_SLOT_ITEM));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> PLAYER_TICK_POST = ACTIONS.register("player_tick_post", () -> new SimpleUpgradeAction.Serializer(UpgradeEntrySet.PLAYER_SLOT_ITEM));
	
}