package io._3650.itemupgrader.integration.jei;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ItemUpgrade;
import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.recipes.ItemUpgradeRecipe;
import io._3650.itemupgrader.registry.ModRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;

public class ItemUpgradeRecipeMaker {
	
	public static List<UpgradeRecipe> getRecipes() {
		@SuppressWarnings("resource")
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return Collections.emptyList();
		List<UpgradeRecipe> recipes = Lists.newArrayList();
		
		level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING).stream()
			.filter(recipe -> recipe.getSerializer() == ModRecipes.ITEM_UPGRADE.get())
			.map(recipe -> (ItemUpgradeRecipe) recipe)
			.forEach(recipe -> {
				ItemUpgrade upgrade = recipe.getUpgrade();
				if (upgrade != null) {
					ResourceLocation id = new ResourceLocation(ItemUpgrader.MOD_ID, "jei.upgrade." + ComponentHelper.keyFormat(upgrade.getId()));
					
					upgrade.getValidItems().forEach(stack -> {
						ItemStack withUpgrade = ItemUpgraderApi.applyUpgradeNoUpdate(stack, upgrade.getId());
						recipes.add(new UpgradeRecipe(id, Ingredient.of(stack.getItem()), recipe.getCatalyst(), withUpgrade));
					});
				}
			});
			
		return recipes;
	}
	
}