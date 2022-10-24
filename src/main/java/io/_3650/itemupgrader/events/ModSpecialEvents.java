package io._3650.itemupgrader.events;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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
	
}