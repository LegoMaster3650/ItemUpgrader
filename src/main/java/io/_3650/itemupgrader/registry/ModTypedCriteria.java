package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ingredient.TypedCriteria;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTypedCriteria {
	
	public static final DeferredRegister<TypedCriteria> CRITERIA = DeferredRegister.create(ItemUpgraderRegistry.TYPED_CRITERIA, ItemUpgrader.MOD_ID);
	
	//Always True or False criteria
	public static final RegistryObject<TypedCriteria> TRUE = CRITERIA.register("true", TypedCriteria.TRUE);
	public static final RegistryObject<TypedCriteria> FALSE = CRITERIA.register("false", TypedCriteria.FALSE);
	
	//Tool criteria
	public static final RegistryObject<TypedCriteria> SWORD = CRITERIA.register("sword", TypedCriteria.of(stack -> stack.getItem() instanceof SwordItem));
	public static final RegistryObject<TypedCriteria> AXE = CRITERIA.register("axe", TypedCriteria.of(stack -> stack.getItem() instanceof AxeItem));
	public static final RegistryObject<TypedCriteria> PICKAXE = CRITERIA.register("pickaxe", TypedCriteria.of(stack -> stack.getItem() instanceof PickaxeItem));
	public static final RegistryObject<TypedCriteria> SHOVEL = CRITERIA.register("shovel", TypedCriteria.of(stack -> stack.getItem() instanceof ShovelItem));
	public static final RegistryObject<TypedCriteria> HOE = CRITERIA.register("hoe", TypedCriteria.of(stack -> stack.getItem() instanceof HoeItem));
	public static final RegistryObject<TypedCriteria> FISHING_ROD = CRITERIA.register("fishing_rod", TypedCriteria.of(stack -> stack.getItem() instanceof FishingRodItem));
	public static final RegistryObject<TypedCriteria> BOW = CRITERIA.register("bow", TypedCriteria.of(stack -> stack.getItem() instanceof BowItem));
	public static final RegistryObject<TypedCriteria> CROSSBOW = CRITERIA.register("crossbow", TypedCriteria.of(stack -> stack.getItem() instanceof CrossbowItem));
	public static final RegistryObject<TypedCriteria> TRIDENT = CRITERIA.register("trident", TypedCriteria.of(stack -> stack.getItem() instanceof TridentItem));
	
	public static final RegistryObject<TypedCriteria> SHIELD = CRITERIA.register("shield", TypedCriteria.of(stack -> stack.getItem().canPerformAction(stack, ToolActions.SHIELD_BLOCK)));
	
	//Equipment Slot criteria
	public static final RegistryObject<TypedCriteria> HEAD = CRITERIA.register("head", TypedCriteria.of(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD));
	public static final RegistryObject<TypedCriteria> CHEST = CRITERIA.register("chest", TypedCriteria.of(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST));
	public static final RegistryObject<TypedCriteria> LEGS = CRITERIA.register("legs", TypedCriteria.of(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.LEGS));
	public static final RegistryObject<TypedCriteria> FEET = CRITERIA.register("feet", TypedCriteria.of(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.FEET));
	public static final RegistryObject<TypedCriteria> MAINHAND = CRITERIA.register("mainhand", TypedCriteria.of(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.MAINHAND));
	public static final RegistryObject<TypedCriteria> OFFHAND = CRITERIA.register("offhand", TypedCriteria.of(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.OFFHAND));
	
	//Armor criteria (only accepts actual armor, not elytras)
	public static final RegistryObject<TypedCriteria> HELMET = CRITERIA.register("helmet", TypedCriteria.of(stack -> armorCheck(stack.getItem(), EquipmentSlot.HEAD)));
	public static final RegistryObject<TypedCriteria> CHESTPLATE = CRITERIA.register("chestplate", TypedCriteria.of(stack -> armorCheck(stack.getItem(), EquipmentSlot.CHEST)));
	public static final RegistryObject<TypedCriteria> LEGGINGS = CRITERIA.register("leggings", TypedCriteria.of(stack -> armorCheck(stack.getItem(), EquipmentSlot.LEGS)));
	public static final RegistryObject<TypedCriteria> BOOTS = CRITERIA.register("boots", TypedCriteria.of(stack -> armorCheck(stack.getItem(), EquipmentSlot.FEET)));
	
	//Misc.
	public static final RegistryObject<TypedCriteria> BOOK = CRITERIA.register("book", TypedCriteria.of(stack -> stack.getItem() instanceof BookItem));
	public static final RegistryObject<TypedCriteria> ENCHANTABLE = CRITERIA.register("enchantable", TypedCriteria.of(stack -> stack.getItem().getEnchantmentValue() > 0));
	public static final RegistryObject<TypedCriteria> UNBRAKING_ENCHANTABLE = CRITERIA.register("unbreaking_enchantable", TypedCriteria.of(stack -> Enchantments.UNBREAKING.canEnchant(stack)));
	public static final RegistryObject<TypedCriteria> EFFICIENCY_ENCHANTABLE = CRITERIA.register("efficiency_enchantable", TypedCriteria.of(stack -> Enchantments.BLOCK_EFFICIENCY.canEnchant(stack)));
	public static final RegistryObject<TypedCriteria> FORTUNE_ENCHANTABLE = CRITERIA.register("fortune_enchantable", TypedCriteria.of(stack -> Enchantments.BLOCK_FORTUNE.canEnchant(stack)));
	public static final RegistryObject<TypedCriteria> SHARPNESS_ENCHANTABLE = CRITERIA.register("sharpness_enchantable", TypedCriteria.of(stack -> Enchantments.SHARPNESS.canEnchant(stack)));
	public static final RegistryObject<TypedCriteria> LOOTING_ENCHANTABLE = CRITERIA.register("looting_enchantable", TypedCriteria.of(stack -> Enchantments.MOB_LOOTING.canEnchant(stack)));
	
	//Utility functions
	private static boolean armorCheck(Item item, EquipmentSlot slot) {
		return item instanceof ArmorItem armor ? armor.getSlot() == slot : false;
	}
	
}