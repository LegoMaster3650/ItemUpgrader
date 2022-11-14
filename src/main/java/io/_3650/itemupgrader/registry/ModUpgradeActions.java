package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.type.SimpleUpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.upgrades.actions.AttributeUpgradeAction;
import io._3650.itemupgrader.upgrades.actions.LootEnchantUpgradeAction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeActions {
	
	public static final DeferredRegister<UpgradeActionSerializer<? extends UpgradeAction>> ACTIONS = DeferredRegister.create(ItemUpgraderRegistry.ACTIONS, ItemUpgrader.MOD_ID);
	
	// Custom Actions
	public static final RegistryObject<AttributeUpgradeAction.Serializer> ATTRIBUTE = ACTIONS.register("attribute", () -> new AttributeUpgradeAction.Serializer());
	public static final RegistryObject<LootEnchantUpgradeAction.Serializer> LOOT_ENCHANTMENT = ACTIONS.register("loot_enchantment", () -> new LootEnchantUpgradeAction.Serializer());
	
	// Simple
	public static final RegistryObject<SimpleUpgradeAction.Serializer> BREAKING_SPEED = ACTIONS.register("breaking_speed", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_SLOT_ITEM.with(UpgradeEntrySet.BLOCK_POS_STATE).with(builder -> builder.modifiable(UpgradeEntry.BREAKING_SPEED))));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_APPLY_PRE = ACTIONS.register("upgrade_apply_pre", SimpleUpgradeAction.of(UpgradeEntrySet.ITEM_UPGRADE_ID.with(builder -> builder.cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_APPLY_POST = ACTIONS.register("upgrade_apply_post", SimpleUpgradeAction.of(UpgradeEntrySet.ITEM_UPGRADE_ID));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> UPGRADE_REMOVE = ACTIONS.register("upgrade_remove", SimpleUpgradeAction.of(UpgradeEntrySet.ITEM_PREV_UPGRADE_ID));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_TICK = ACTIONS.register("living_tick", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_SLOT_ITEM));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> ENTITY_INTERACT = ACTIONS.register("entity_interact", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_ENTITY_INTERACTION.with(builder -> builder.cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> ENTITY_INTERACT_SPECIFIC = ACTIONS.register("entity_interact_specific", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_ENTITY_INTERACTION.with(builder -> builder.cancellable().consumable())));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> RIGHT_CLICK = ACTIONS.register("right_click", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_SLOT_ITEM.with(builder -> builder.cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> RIGHT_CLICK_EFFECT = ACTIONS.register("right_click_effect", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_SLOT_ITEM.with(builder -> builder.provide(UpgradeEntry.USED_ITEM).consumable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> RIGHT_CLICK_BLOCK = ACTIONS.register("right_click_block", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_BLOCK_INTERACTION.with(builder -> builder.cancellable().consumable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> RIGHT_CLICK_BLOCK_EFFECT = ACTIONS.register("right_click_block_effect", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_BLOCK_INTERACTION.with(builder -> builder.provide(UpgradeEntry.USED_ITEM).consumable())));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LEFT_CLICK = ACTIONS.register("left_click", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_SLOT_ITEM));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LEFT_CLICK_EFFECT = ACTIONS.register("left_click_effect", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_SLOT_ITEM.with(builder -> builder.provide(UpgradeEntry.USED_ITEM))));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LEFT_CLICK_BLOCK = ACTIONS.register("left_click_block", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_BLOCK_INTERACTION.with(builder -> builder.consumable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LEFT_CLICK_BLOCK_EFFECT = ACTIONS.register("left_click_block_effect", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_BLOCK_INTERACTION.with(builder -> builder.provide(UpgradeEntry.USED_ITEM).consumable())));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> PLAYER_ATTACK = ACTIONS.register("player_attack", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_ENTITY_INTERACTION.with(builder -> builder.cancellable().consumable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_PRE_HURT = ACTIONS.register("pre_hurt", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_DAMAGE.with(builder -> builder.cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_HURT = ACTIONS.register("hurt", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_DAMAGE.with(builder -> builder.modifiable(UpgradeEntry.DAMAGE).cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_DAMAGE = ACTIONS.register("damage", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_DAMAGE.with(builder -> builder.modifiable(UpgradeEntry.DAMAGE).cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_FALL = ACTIONS.register("fall", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_SLOT_ITEM.with(builder -> builder.modifiableAll(UpgradeEntry.DAMAGE_MULT, UpgradeEntry.FALL_DIST).cancellable().consumable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_TOTEM_PRE = ACTIONS.register("totem", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_SLOT_ITEM.with(builder -> builder.provide(UpgradeEntry.DAMAGE_SOURCE).cancellable())));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> LIVING_TOTEM_POST = ACTIONS.register("after_totem", SimpleUpgradeAction.of(UpgradeEntrySet.LIVING_ITEM.with(builder -> builder.provide(UpgradeEntry.DAMAGE_SOURCE))));
	public static final RegistryObject<SimpleUpgradeAction.Serializer> PLAYER_SHIELD_BLOCK = ACTIONS.register("shield_block", SimpleUpgradeAction.of(UpgradeEntrySet.PLAYER_ITEM.with(builder -> builder.provide(UpgradeEntry.DAMAGE_SOURCE).modifiableAll(UpgradeEntry.DAMAGE, UpgradeEntry.DO_SHIELD_DAMAGE).cancellable())));
	
	public static final RegistryObject<SimpleUpgradeAction.Serializer> BLOCK_DROPS = ACTIONS.register("block_drops", SimpleUpgradeAction.of(UpgradeEntrySet.BLOCK_DROPS));
	
}