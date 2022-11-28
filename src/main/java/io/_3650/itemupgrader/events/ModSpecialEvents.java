package io._3650.itemupgrader.events;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

//For "events" that don't use forge's event system
public class ModSpecialEvents {
	
	/*
	 * ENCHANTMENTS
	 */
	
	public static int lootEnchantmentBonus(Enchantment enchantment, int enchantmentLevel, ItemStack dropStack, LootContext context) {
		if (context.hasParam(LootContextParams.TOOL)) {
			UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.LOOT_ENCHANTMENT, new UpgradeEventData.Builder(context.getParam(LootContextParams.TOOL))
					.entry(UpgradeEntry.ENCHANTMENT_ID, enchantment.getRegistryName())
					.modifiableEntry(UpgradeEntry.ENCHANTMENT_LEVEL, enchantmentLevel));
			return data.getEntry(UpgradeEntry.ENCHANTMENT_LEVEL);
		} else return enchantmentLevel;
	}
	
	public static int riptideBonus(ItemStack stack, LivingEntity living, int riptide) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENCHANTMENT_BONUS, new UpgradeEventData.Builder(living)
				.entry(UpgradeEntry.ITEM, stack)
				.entry(UpgradeEntry.ENCHANTMENT_ID, Enchantments.RIPTIDE.getRegistryName())
				.modifiableEntry(UpgradeEntry.ENCHANTMENT_LEVEL, riptide));
		return data.getEntry(UpgradeEntry.ENCHANTMENT_LEVEL);
	}
	
	public static byte loyaltyBonus(ItemStack stack, LivingEntity living, int loyalty) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENCHANTMENT_BONUS, new UpgradeEventData.Builder(living)
				.entry(UpgradeEntry.ITEM, stack)
				.entry(UpgradeEntry.ENCHANTMENT_ID, Enchantments.LOYALTY.getRegistryName())
				.modifiableEntry(UpgradeEntry.ENCHANTMENT_LEVEL, loyalty));
		return data.getEntry(UpgradeEntry.ENCHANTMENT_LEVEL).byteValue();
	}
	
	public static int unbreakingBonus(ItemStack stack, int unbreaking) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENCHANTMENT_BONUS, new UpgradeEventData.Builder(stack)
				.entry(UpgradeEntry.ENCHANTMENT_ID, Enchantments.UNBREAKING.getRegistryName())
				.modifiableEntry(UpgradeEntry.ENCHANTMENT_LEVEL, unbreaking));
		return data.getEntry(UpgradeEntry.ENCHANTMENT_LEVEL);
	}
	
	public static int itemEnchantability(ItemStack stack, int originalValue) {
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.ENCHANTABILITY, new UpgradeEventData.Builder(stack)
				.modifiableEntry(UpgradeEntry.ENCHANTABILITY, originalValue));
		return data.getEntry(UpgradeEntry.ENCHANTABILITY);
	}
	
}