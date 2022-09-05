package io.legom.itemupgrader.integration.jei;

import io.legom.itemupgrader.ItemUpgrader;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class ItemUpgraderJEIPlugin implements IModPlugin {
	
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ItemUpgrader.MOD_ID, "upgrades");
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(RecipeTypes.SMITHING, ItemUpgradeRecipeMaker.getRecipes());
	}

}