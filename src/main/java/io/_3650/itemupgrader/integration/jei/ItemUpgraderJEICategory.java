package io._3650.itemupgrader.integration.jei;

import java.util.List;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.recipes.ItemUpgradeRecipe;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class ItemUpgraderJEICategory implements IRecipeCategory<ItemUpgradeRecipe> {
	
	//why is this in the top secret jei
	private static final ResourceLocation JEI_RECIPE_GUI_VANILLA = new ResourceLocation(ModIds.JEI_ID, "textures/gui/gui_vanilla.png");
	
	private final IDrawable background;
	private final IDrawable icon;
	
	public ItemUpgraderJEICategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(JEI_RECIPE_GUI_VANILLA, 0, 168, 125, 18);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
	}
	
	@Override
	public Component getTitle() {
		return new TranslatableComponent("itemupgrader.jei.category");
	}
	
	@Override
	public IDrawable getBackground() {
		return this.background;
	}
	
	@Override
	public IDrawable getIcon() {
		return this.icon;
	}
	
	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getUid() {
		return this.getRecipeType().getUid();
	}
	
	@SuppressWarnings("removal")
	@Override
	public Class<? extends ItemUpgradeRecipe> getRecipeClass() {
		return ItemUpgradeRecipe.class;
	}
	
	@Override
	public RecipeType<ItemUpgradeRecipe> getRecipeType() {
		return ItemUpgraderJEIPlugin.UPGRADE_TYPE;
	}
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ItemUpgradeRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> validItems = recipe.getUpgrade().getValidItems();
		ResourceLocation upgradeId = recipe.getUpgrade().getId();
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addItemStacks(validItems);
		builder.addSlot(RecipeIngredientRole.INPUT, 50, 1).addIngredients(recipe.getCatalyst());
		List<ItemStack> upgradedItems = validItems.stream().map(stack -> ItemUpgraderApi.applyUpgradeNoUpdate(stack.copy(), upgradeId)).toList();
		builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1).addItemStacks(upgradedItems);
	}
	
	@Override
	public boolean isHandled(ItemUpgradeRecipe recipe) {
		return recipe.getUpgrade() != null;
	}
	
}