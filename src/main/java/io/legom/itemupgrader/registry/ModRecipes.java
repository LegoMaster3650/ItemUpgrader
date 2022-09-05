package io.legom.itemupgrader.registry;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.recipes.ItemUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
	
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<RecipeSerializer<ItemUpgradeRecipe>> ITEM_UPGRADE = SERIALIZERS.register("item_upgrade", () -> new ItemUpgradeRecipe.Serializer());
	
}