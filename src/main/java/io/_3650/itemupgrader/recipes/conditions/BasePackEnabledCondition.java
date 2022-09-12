package io._3650.itemupgrader.recipes.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.registry.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class BasePackEnabledCondition implements ICondition {
	
	private static final ResourceLocation ID = ItemUpgraderRegistry.modRes("base_pack_enabled");
	
	@Override
	public ResourceLocation getID() {
		return ID;
	}
	
	@Override
	public boolean test() {
		return Config.SERVER.basePackEnabled.get();
	}
	
	public static class Serializer implements IConditionSerializer<BasePackEnabledCondition> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(JsonObject json, BasePackEnabledCondition value) {
			//does nothing
		}
		
		@Override
		public BasePackEnabledCondition read(JsonObject json) {
			return new BasePackEnabledCondition();
		}
		
		@Override
		public ResourceLocation getID() {
			return BasePackEnabledCondition.ID;
		}
		
	}
	
}