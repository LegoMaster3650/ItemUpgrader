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
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ReboundEntityUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<DamageSource> sourceEntry;
	private final UpgradeEntry<LivingEntity> livingEntry;
	private final double power;
	
	public ReboundEntityUpgradeResult(IUpgradeInternals internals, UpgradeEntry<DamageSource> sourceEntry, UpgradeEntry<LivingEntity> livingEntry, double power) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.requireAll(sourceEntry, livingEntry);
		}));
		this.sourceEntry = sourceEntry;
		this.livingEntry = livingEntry;
		this.power = power;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		DamageSource source = data.getEntry(this.sourceEntry);
		if (!(source.getDirectEntity() instanceof LivingEntity target)) return false;
		LivingEntity living = data.getEntry(this.livingEntry);
		Vec3 dir = living.getLookAngle();
		dir = new Vec3(dir.x, 0.3, dir.z).scale(this.power);
		target.setDeltaMovement(target.getDeltaMovement().add(dir));
		target.hasImpulse = true;
		
		if (!living.level.isClientSide && living instanceof ServerPlayer sPlayer) {
			sPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
		}
		
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
	
	public static class Serializer extends UpgradeResultSerializer<ReboundEntityUpgradeResult> {
		
		@Override
		public ReboundEntityUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromJson(json);
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			double power = GsonHelper.getAsDouble(json, "power", 1.0D);
			return new ReboundEntityUpgradeResult(internals, sourceEntry, livingEntry, power);
		}
		
		@Override
		public void toNetwork(ReboundEntityUpgradeResult result, FriendlyByteBuf buf) {
			result.sourceEntry.toNetwork(buf);
			result.livingEntry.toNetwork(buf);
			buf.writeDouble(result.power);
		}
		
		@Override
		public ReboundEntityUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromNetwork(buf);
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			double power = buf.readDouble();
			return new ReboundEntityUpgradeResult(internals, sourceEntry, livingEntry, power);
		}
		
	}
	
}