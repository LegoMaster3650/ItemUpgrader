package io._3650.itemupgrader_content.upgrades.results.special;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class TotemParticlesUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<LivingEntity> livingEntry;
	
	public TotemParticlesUpgradeResult(IUpgradeInternals internals, UpgradeEntry<LivingEntity> livingEntry) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(livingEntry);
		}));
		this.livingEntry = livingEntry;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		LivingEntity living = data.getEntry(this.livingEntry);
		if (!(living.level instanceof ServerLevel level)) return false;
		level.broadcastEntityEvent(living, (byte) 35);
		return true;
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
	
	public static class Serializer extends UpgradeResultSerializer<TotemParticlesUpgradeResult> {
		
		@Override
		public TotemParticlesUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			return new TotemParticlesUpgradeResult(internals, livingEntry);
		}
		
		@Override
		public void toNetwork(TotemParticlesUpgradeResult result, FriendlyByteBuf buf) {
			result.livingEntry.toNetwork(buf);
		}
		
		@Override
		public TotemParticlesUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			return new TotemParticlesUpgradeResult(internals, livingEntry);
		}
		
	}
	
}