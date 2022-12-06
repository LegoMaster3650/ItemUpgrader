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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CanReboundUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<DamageSource> sourceEntry;
	private final UpgradeEntry<LivingEntity> livingEntry;
	
	public CanReboundUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<DamageSource> sourceEntry, UpgradeEntry<LivingEntity> livingEntry) {
		super(internals, inverted, UpgradeEntrySet.create(builder -> {
			builder.require(sourceEntry);
		}));
		this.sourceEntry = sourceEntry;
		this.livingEntry = livingEntry;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		DamageSource source = data.getEntry(this.sourceEntry);
		LivingEntity living = data.getEntry(this.livingEntry);
		return source.getDirectEntity() instanceof LivingEntity && living.getTicksUsingItem() < Config.COMMON.shieldParryDuration.get() * 2;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Config.COMMON.shieldParryDuration.get() / 10.0D)));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<CanReboundUpgradeCondition> {
		
		@Override
		public CanReboundUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromJson(json);
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			return new CanReboundUpgradeCondition(internals, inverted, sourceEntry, livingEntry);
		}
		
		@Override
		public void toNetwork(CanReboundUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.sourceEntry.toNetwork(buf);
			condition.livingEntry.toNetwork(buf);
		}
		
		@Override
		public CanReboundUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromNetwork(buf);
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			return new CanReboundUpgradeCondition(internals, inverted, sourceEntry, livingEntry);
		}
		
	}
	
}