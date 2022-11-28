package io._3650.itemupgrader.integration.jei;

import java.util.List;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.recipes.ItemUpgradeRecipe;
import io._3650.itemupgrader.registry.config.Config;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class ItemUpgraderJEIPlugin implements IModPlugin {
	
	public static final RecipeType<ItemUpgradeRecipe> UPGRADE_TYPE = RecipeType.create(ItemUpgrader.MOD_ID, "item_upgrade", ItemUpgradeRecipe.class);
	
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ItemUpgrader.MOD_ID, "upgrades");
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new ItemUpgraderJEICategory(registration.getJeiHelpers().getGuiHelper()));
	}
	
	@SuppressWarnings("resource")
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		List<ItemUpgradeRecipe> recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING)
				.stream()
				.filter(ItemUpgradeRecipe.class::isInstance)
				.map(ItemUpgradeRecipe.class::cast)
				.filter(recipe -> recipe.getUpgrade() != null)
				.toList();
		registration.addRecipes(UPGRADE_TYPE, recipes);
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), UPGRADE_TYPE);
	}
	
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(SmithingScreen.class,
				Config.CLIENT.recipeClickAreaLeftX.get(),
				Config.CLIENT.recipeClickAreaTopY.get(),
				Config.CLIENT.recipeClickAreaWidth.get(),
				Config.CLIENT.recipeClickAreaHeight.get(), UPGRADE_TYPE);
		//hammer - 17, 7, 30, 30
		//plus - 53, 49, 13, 13
	}

}