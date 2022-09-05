package io.legom.itemupgrader.api.ingredient;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.legom.itemupgrader.api.ItemUpgraderApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class UpgradeIngredient extends AbstractIngredient {
	
	private final ResourceLocation upgradeId;
	
	public UpgradeIngredient(ResourceLocation upgradeId) {
		super();
		this.upgradeId = upgradeId;
	}
	
	@Override
	public boolean test(@Nullable ItemStack stack) {
		if (stack == null) return false;
		if (!ItemUpgraderApi.hasUpgrade(stack)) return false;
		return ItemUpgraderApi.getUpgradeKey(stack).equals(this.upgradeId);
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}
	
	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("upgrade", this.upgradeId.toString());
		return json;
	}
	
	public static class Serializer implements IIngredientSerializer<UpgradeIngredient> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public UpgradeIngredient parse(FriendlyByteBuf buf) {
			ResourceLocation netUpgradeId = buf.readResourceLocation();
			return new UpgradeIngredient(netUpgradeId);
		}

		@Override
		public UpgradeIngredient parse(JsonObject json) {
			ResourceLocation upgradeId = new ResourceLocation(GsonHelper.getAsString(json, "upgrade"));
			return new UpgradeIngredient(upgradeId);
		}

		@Override
		public void write(FriendlyByteBuf buf, UpgradeIngredient ingredient) {
			buf.writeResourceLocation(ingredient.upgradeId);
		}
		
	}

}
