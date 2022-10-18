package io._3650.itemupgrader.recipes.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.registry.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class BasePackEnabledCondition implements ICondition {
	
	private static final ResourceLocation ID = ItemUpgraderRegistry.modRes("base_pack_enabled");
	
	private final boolean recipeOnly;
	
	public BasePackEnabledCondition(boolean recipeOnly) {
		this.recipeOnly = recipeOnly;
	}
	
	@Override
	public ResourceLocation getID() {
		return ID;
	}
	
	@Override
	public boolean test() {
		return Config.SERVER.basePackEnabled.get() && !(this.recipeOnly && !Config.SERVER.basePackRecipes.get());
	}
	
	public static class Serializer implements IConditionSerializer<BasePackEnabledCondition> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(JsonObject json, BasePackEnabledCondition value) {
			json.addProperty("recipe_only", value.recipeOnly);
		}
		
		@Override
		public BasePackEnabledCondition read(JsonObject json) {
			return new BasePackEnabledCondition(GsonHelper.getAsBoolean(json, "recipe_only", false));
		}
		
		@Override
		public ResourceLocation getID() {
			return BasePackEnabledCondition.ID;
		}
		
	}
	
}