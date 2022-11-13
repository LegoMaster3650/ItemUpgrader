package io._3650.itemupgrader.recipes;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * Inspired by Fast Furnace (using this to help with lag from instaminers hopefully this doesnt add a dupe glitch)
 */
public class SmeltingRecipeGetter {
	
	public static SmeltingRecipe cachedRecipe = null;
	
	public static Optional<SmeltingRecipe> getRecipe(ItemStack stack, ServerLevel level) {
		if (cachedRecipe != null && cachedRecipe.matches(new SimpleContainer(stack), level)) return Optional.of(cachedRecipe);
		else {
			Optional<SmeltingRecipe> optionalRecipe = level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream().filter(recipe -> recipe.getIngredients().get(0).test(stack)).findFirst();
			if (optionalRecipe.isPresent()) {
				cachedRecipe = optionalRecipe.get();
				return optionalRecipe;
			} else {
				return Optional.empty();
			}
		}
	}
	
}