package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ingredient.TypedCriteria;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTypedCriteria {
	
	public static final DeferredRegister<TypedCriteria> TYPED_CRITERIA = DeferredRegister.create(ItemUpgraderRegistry.TYPED_CRITERIA, ItemUpgrader.MOD_ID);
	
	//Always True or False criteria
	public static final RegistryObject<TypedCriteria> TRUE = TYPED_CRITERIA.register("true", TypedCriteria.TRUE);
	public static final RegistryObject<TypedCriteria> FALSE = TYPED_CRITERIA.register("false", TypedCriteria.FALSE);
	
	//Tool criteria
	public static final RegistryObject<TypedCriteria> SWORD = TYPED_CRITERIA.register("sword", () -> new TypedCriteria(item -> item instanceof SwordItem));
	public static final RegistryObject<TypedCriteria> AXE = TYPED_CRITERIA.register("axe", () -> new TypedCriteria(item -> item instanceof AxeItem));
	public static final RegistryObject<TypedCriteria> PICKAXE = TYPED_CRITERIA.register("pickaxe", () -> new TypedCriteria(item -> item instanceof PickaxeItem));
	public static final RegistryObject<TypedCriteria> SHOVEL = TYPED_CRITERIA.register("shovel", () -> new TypedCriteria(item -> item instanceof ShovelItem));
	public static final RegistryObject<TypedCriteria> HOE = TYPED_CRITERIA.register("hoe", () -> new TypedCriteria(item -> item instanceof HoeItem));
	
	public static final RegistryObject<TypedCriteria> SHIELD = TYPED_CRITERIA.register("shield", () -> new TypedCriteria(item -> item.canPerformAction(new ItemStack(item), ToolActions.SHIELD_BLOCK)));
	
	//Equipment Slot criteria
	public static final RegistryObject<TypedCriteria> HEAD = TYPED_CRITERIA.register("head", () -> new TypedCriteria(item -> LivingEntity.getEquipmentSlotForItem(new ItemStack(item)) == EquipmentSlot.HEAD));
	public static final RegistryObject<TypedCriteria> CHEST = TYPED_CRITERIA.register("chest", () -> new TypedCriteria(item -> LivingEntity.getEquipmentSlotForItem(new ItemStack(item)) == EquipmentSlot.CHEST));
	public static final RegistryObject<TypedCriteria> LEGS = TYPED_CRITERIA.register("legs", () -> new TypedCriteria(item -> LivingEntity.getEquipmentSlotForItem(new ItemStack(item)) == EquipmentSlot.LEGS));
	public static final RegistryObject<TypedCriteria> FEET = TYPED_CRITERIA.register("feet", () -> new TypedCriteria(item -> LivingEntity.getEquipmentSlotForItem(new ItemStack(item)) == EquipmentSlot.FEET));
	public static final RegistryObject<TypedCriteria> MAINHAND = TYPED_CRITERIA.register("mainhand", () -> new TypedCriteria(item -> LivingEntity.getEquipmentSlotForItem(new ItemStack(item)) == EquipmentSlot.MAINHAND));
	public static final RegistryObject<TypedCriteria> OFFHAND = TYPED_CRITERIA.register("offhand", () -> new TypedCriteria(item -> LivingEntity.getEquipmentSlotForItem(new ItemStack(item)) == EquipmentSlot.OFFHAND));
	
	//Armor criteria (only accepts actual armor, not elytras)
	public static final RegistryObject<TypedCriteria> HELMET = TYPED_CRITERIA.register("helmet", () -> new TypedCriteria(item -> armorCheck(item, EquipmentSlot.HEAD)));
	public static final RegistryObject<TypedCriteria> CHESTPLATE = TYPED_CRITERIA.register("chestplate", () -> new TypedCriteria(item -> armorCheck(item, EquipmentSlot.CHEST)));
	public static final RegistryObject<TypedCriteria> LEGGINGS = TYPED_CRITERIA.register("leggings", () -> new TypedCriteria(item -> armorCheck(item, EquipmentSlot.LEGS)));
	public static final RegistryObject<TypedCriteria> BOOTS = TYPED_CRITERIA.register("boots", () -> new TypedCriteria(item -> armorCheck(item, EquipmentSlot.FEET)));
	
	//Utility functions
	private static boolean armorCheck(Item item, EquipmentSlot slot) {
		return item instanceof ArmorItem armor ? armor.getSlot() == slot : false;
	}
	
}