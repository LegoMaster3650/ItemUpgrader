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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ReflectProjectileUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<LivingEntity> livingEntry;
	private final float reflectPower;
	private final double facingBias;
	
	public ReflectProjectileUpgradeResult(IUpgradeInternals internals, UpgradeEntry<LivingEntity> livingEntry, float reflectPower, double facingBias) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.requireAll(UpgradeEntry.PROJECTILE, livingEntry);
		}));
		this.livingEntry = livingEntry;
		this.reflectPower = reflectPower;
		this.facingBias = facingBias;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Projectile projectile = data.getEntry(UpgradeEntry.PROJECTILE);
		LivingEntity living = data.getEntry(UpgradeEntry.LIVING);
		
		Vec3 returnAngle = projectile.getDeltaMovement().scale(-1.0D);
		Vec3 lookAngle = living.getLookAngle();
		double magnitude = Math.sqrt(Math.pow(lookAngle.x, 2) + Math.pow(lookAngle.y, 2) + Math.pow(lookAngle.z, 2)); //normalize does sqrt anyways
		Vec3 rotationBias = lookAngle.scale(magnitude * this.facingBias);
		returnAngle = returnAngle.scale(1.0 - this.facingBias).add(rotationBias);
		projectile.shoot(returnAngle.x, returnAngle.y, returnAngle.z, reflectPower, 0.1F);
		projectile.hurtMarked = true;
		
		if (!living.level.isClientSide && living instanceof ServerPlayer sPlayer) {
			sPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
		}
		
		if (projectile instanceof AbstractHurtingProjectile hurtProj) {
			hurtProj.xPower = returnAngle.x * 0.1D;
			hurtProj.yPower = returnAngle.y * 0.1D;
			hurtProj.zPower = returnAngle.z * 0.1D;
			if (hurtProj instanceof LargeFireball && living instanceof Player) hurtProj.setOwner(living);
		}
		
		InteractionHand hand = living.getUsedItemHand();
		living.getUseItem().hurtAndBreak(1, living, living1 -> living1.broadcastBreakEvent(hand));
		
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
	
	public static class Serializer extends UpgradeResultSerializer<ReflectProjectileUpgradeResult> {
		
		@Override
		public ReflectProjectileUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			float reflectPower = GsonHelper.getAsFloat(json, "power", 1.0F);
			double facingBias = GsonHelper.getAsDouble(json, "facing_bias", 0.3F);
			return new ReflectProjectileUpgradeResult(internals, livingEntry, reflectPower, facingBias);
		}
		
		@Override
		public void toNetwork(ReflectProjectileUpgradeResult result, FriendlyByteBuf buf) {
			result.livingEntry.toNetwork(buf);
			buf.writeFloat(result.reflectPower);
			buf.writeDouble(result.facingBias);
		}
		
		@Override
		public ReflectProjectileUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			float reflectPower = buf.readFloat();
			double facingBias = buf.readDouble();
			return new ReflectProjectileUpgradeResult(internals, livingEntry, reflectPower, facingBias);
		}
		
	}
	
}