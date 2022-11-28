package io._3650.itemupgrader.recipes;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.ItemUpgrade;
import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.registry.ModRecipes;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemUpgradeRecipe extends UpgradeRecipe {
	
	private final Ingredient catalyst;
	private final ResourceLocation upgradeId;
	
	private ItemUpgrade upgrade;
	
	public ItemUpgradeRecipe(ResourceLocation id, Ingredient catalyst, ResourceLocation upgradeId) {
		super(id, Ingredient.EMPTY, catalyst, ItemStack.EMPTY);
		this.catalyst = catalyst;
		this.upgradeId = upgradeId;
		this.getUpgrade(); //auto sets upgrade
	}
	
	@Override
	public boolean matches(Container inv, Level level) {
		if (this.getUpgrade() == null) return false;
		return this.getUpgrade().isValidItem(inv.getItem(0)) && this.catalyst.test(inv.getItem(1));
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		if (this.getUpgrade() == null) return ItemStack.EMPTY;
		ItemStack result = inv.getItem(0).copy();
		result.setCount(1);
		return ItemUpgraderApi.applyUpgrade(result, this.upgradeId);
	}
	
	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}
	
	public Ingredient getCatalyst() {
		return this.catalyst;
	}
	
	public ItemUpgrade getUpgrade() {
		if (this.upgrade == null) this.upgrade = ItemUpgradeManager.INSTANCE.getUpgrade(this.upgradeId);
		return this.upgrade;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.ITEM_UPGRADE.get();
	}
	
	@Override
	public boolean isAdditionIngredient(ItemStack addition) {
		return catalyst.test(addition);
	}
	
	@Override
	public boolean isIncomplete() {
		return net.minecraftforge.common.ForgeHooks.hasNoElements(this.catalyst);
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ItemUpgradeRecipe> {
		
		@Override
		public ItemUpgradeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient catalyst = Ingredient.fromJson(GsonHelper.isArrayNode(json, "catalyst") ? GsonHelper.getAsJsonArray(json, "catalyst") : GsonHelper.getAsJsonObject(json, "catalyst"));
			ResourceLocation upgradeId = new ResourceLocation(GsonHelper.getAsString(json, "upgrade"));
			return new ItemUpgradeRecipe(recipeId, catalyst, upgradeId);
		}
		
		@Override
		public void toNetwork(FriendlyByteBuf buf, ItemUpgradeRecipe recipe) {
			recipe.catalyst.toNetwork(buf);
			buf.writeResourceLocation(recipe.upgradeId);
		}
		
		@Override
		public ItemUpgradeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
			Ingredient catalyst = Ingredient.fromNetwork(buf);
			ResourceLocation upgradeId = buf.readResourceLocation();
			return new ItemUpgradeRecipe(recipeId, catalyst, upgradeId);
		}
		
	}
	
}