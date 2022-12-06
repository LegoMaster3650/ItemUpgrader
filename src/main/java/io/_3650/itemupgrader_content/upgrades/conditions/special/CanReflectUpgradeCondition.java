package io._3650.itemupgrader_content.upgrades.conditions.special;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CanReflectUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<LivingEntity> livingEntry;
	
	public CanReflectUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<LivingEntity> livingEntry) {
		super(internals, inverted, UpgradeEntrySet.create(builder -> {
			builder.requireAll(UpgradeEntry.PROJECTILE, livingEntry);
		})); //requiring projectile is just bonus check that a projectile is here to be blocked
		this.livingEntry = livingEntry;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		LivingEntity living = data.getEntry(this.livingEntry);
		return living.isBlocking() && living.getTicksUsingItem() < Config.COMMON.shieldParryDuration.get();
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Config.COMMON.shieldParryDuration.get() / 20.0D)));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<CanReflectUpgradeCondition> {
		
		@Override
		public CanReflectUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			return new CanReflectUpgradeCondition(internals, inverted, livingEntry);
		}
		
		@Override
		public void toNetwork(CanReflectUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.livingEntry.toNetwork(buf);
		}
		
		@Override
		public CanReflectUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			return new CanReflectUpgradeCondition(internals, inverted, livingEntry);
		}
		
	}
	
}