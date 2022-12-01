package io._3650.itemupgrader.upgrades.results.special;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.mixin.LivingEntityInvoker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

public class FallToFoodUpgradeResult extends UpgradeResult {
	
	private final double multiplier;
	
	public FallToFoodUpgradeResult(IUpgradeInternals internals, double multiplier) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.requireAll(UpgradeEntry.PLAYER, UpgradeEntry.DAMAGE_MULT).modifiable(UpgradeEntry.FALL_DIST);
		}));
		this.multiplier = multiplier;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Player player = data.getEntry(UpgradeEntry.PLAYER);
		FoodData food = player.getFoodData();
		float dmg = ((LivingEntityInvoker)player).callCalculateFallDamage(data.getEntry(UpgradeEntry.FALL_DIST), data.getEntry(UpgradeEntry.DAMAGE_MULT));
		if (dmg <= 0.0F) return false;
		else dmg *= this.multiplier;
		dmg = ((LivingEntityInvoker)player).callGetDamageAfterMagicAbsorb(DamageSource.FALL, dmg);
		float totalFood = food.getFoodLevel() + food.getSaturationLevel();
		if (food.getFoodLevel() > 6 && dmg <= totalFood * 4.0F) {
			// Not needed after mixin, keeping around in case of future uses
//			float newExhaustion = food.getExhaustionLevel() + dmg;
//			while (newExhaustion >= 40.0F) {
//				newExhaustion -= 4.0F;
//				if (food.getSaturationLevel() > 0.0F) {
//					food.setSaturation(Math.max(food.getSaturationLevel() - 1.0F, 0.0F));
//				} else {
//					food.setFoodLevel(Math.max(food.getFoodLevel() - 1, 0));
//				}
//			}
			food.addExhaustion(dmg);
			data.setModifiableEntry(UpgradeEntry.FALL_DIST, 0.0F);
			return true;
		}
		return false;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.empty();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<FallToFoodUpgradeResult> {
		
		@Override
		public FallToFoodUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			double multiplier = GsonHelper.getAsDouble(json, "multiplier", 3.0D);
			return new FallToFoodUpgradeResult(internals, multiplier);
		}
		
		@Override
		public void toNetwork(FallToFoodUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeDouble(result.multiplier);
		}
		
		@Override
		public FallToFoodUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			double multiplier = buf.readDouble();
			return new FallToFoodUpgradeResult(internals, multiplier);
		}
		
	}
	
}