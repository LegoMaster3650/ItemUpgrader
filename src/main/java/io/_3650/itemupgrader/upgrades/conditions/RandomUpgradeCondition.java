package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RandomUpgradeCondition extends UpgradeCondition {
	
	private final double chance;
	
	public RandomUpgradeCondition(IUpgradeInternals internals, boolean inverted, double chance) {
		super(internals, inverted, UpgradeEntrySet.create(builder -> {
			builder.require(UpgradeEntry.LEVEL);
		}));
		this.chance = chance;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		Level level = data.getEntry(UpgradeEntry.LEVEL);
		return level.getRandom().nextDouble() <= this.chance;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(ComponentHelper.SIGNED_PERCENT.format(this.chance)));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<RandomUpgradeCondition> {
		
		@Override
		public RandomUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			double chance = GsonHelper.getAsDouble(json, "chance");
			return new RandomUpgradeCondition(internals, inverted, chance);
		}
		
		@Override
		public void toNetwork(RandomUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeDouble(condition.chance);
		}
		
		@Override
		public RandomUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			double chance = buf.readDouble();
			return new RandomUpgradeCondition(internals, inverted, chance);
		}
		
	}
	
}