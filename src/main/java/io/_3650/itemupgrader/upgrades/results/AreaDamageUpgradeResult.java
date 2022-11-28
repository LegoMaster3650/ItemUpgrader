package io._3650.itemupgrader.upgrades.results;

import java.util.List;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AreaDamageUpgradeResult extends UpgradeResult {
	
	private final float damage;
	private final UpgradeEntry<Vec3> posEntry;
	private final UpgradeEntry<Entity> directSourceEntry;
	private final UpgradeEntry<Entity> sourceEntry;
	private final String damageSourceName;
	private final boolean bypassArmor;
	private final boolean isMagic;
	private final boolean isFire;
	private final double range;
	private final double rangeSquared;
	private final double cubeRange;
	
	public AreaDamageUpgradeResult(float damage, IUpgradeInternals internals, UpgradeEntry<Vec3> posEntry,
			UpgradeEntry<Entity> directSourceEntry,UpgradeEntry<Entity> sourceEntry, String damageSourceName,
			boolean bypassArmor, boolean isMagic, boolean isFire, double range) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(UpgradeEntry.LEVEL);
			builder.require(UpgradeEntry.ITEM);
			builder.require(posEntry);
		}));
		this.damage = damage;
		this.posEntry = posEntry;
		this.directSourceEntry = directSourceEntry;
		this.sourceEntry = sourceEntry;
		this.damageSourceName = damageSourceName;
		this.bypassArmor = bypassArmor;
		this.isMagic = isMagic;
		this.isFire = isFire;
		this.range = range;
		this.rangeSquared = range * range;
		this.cubeRange = Math.sqrt(this.rangeSquared * 2);
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return false;
		Vec3 pos = data.getEntry(this.posEntry);
		Entity directSource = data.getEntryOrNull(this.directSourceEntry);
		Entity source = data.getEntryOrNull(this.sourceEntry);
		DamageSource damageSource;
		if (source == null) damageSource = new DamageSource(this.damageSourceName);
		else if (directSource == null) damageSource = new EntityDamageSource(damageSourceName, source);
		else damageSource = new IndirectEntityDamageSource(damageSourceName, directSource, source);
		if (this.bypassArmor) damageSource.bypassArmor();
		if (this.isMagic) damageSource.setMagic();
		if (this.isFire) damageSource.setIsFire();
		
		AABB aabb = new AABB(pos.x() + this.cubeRange, pos.y() + this.cubeRange, pos.z() + this.cubeRange, pos.x() - this.cubeRange, pos.y() - this.cubeRange, pos.z() - this.cubeRange);
		List<LivingEntity> targets = level.getEntities(EntityTypeTest.forClass(LivingEntity.class), aabb, entity -> {
			return entity.position().distanceToSqr(pos) <= this.rangeSquared;
		});
		
		for (var living : targets) living.hurt(damageSource, this.damage);
		
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.damage)), new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.range))};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<AreaDamageUpgradeResult> {
		
		@Override
		public AreaDamageUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			float damage = GsonHelper.getAsFloat(json, "damage");
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json);
			UpgradeEntry<Entity> directSourceEntry = GsonHelper.isStringValue(json, "direct_source") ? EntryCategory.ENTITY.fromJson(json, "direct_source") : UpgradeEntry.DIRECT_DAMAGER;
			UpgradeEntry<Entity> sourceEntry = GsonHelper.isStringValue(json, "source") ? EntryCategory.ENTITY.fromJson(json, "source") : UpgradeEntry.DAMAGER_ENTITY;
			String sourceName = "itemUpgrader";
			boolean bypassArmor = false;
			boolean isMagic = false;
			boolean isFire = false;
			if (GsonHelper.isObjectNode(json, "damage_source")) {
				JsonObject sourceJson = GsonHelper.getAsJsonObject(json, "damage_source");
				sourceName = GsonHelper.getAsString(sourceJson, "name", "itemUpgrader");
				bypassArmor = GsonHelper.getAsBoolean(sourceJson, "bypass_armor", false);
				isMagic = GsonHelper.getAsBoolean(sourceJson, "magic", false);
				isFire = GsonHelper.getAsBoolean(sourceJson, "fire", false);
			}
			double range = GsonHelper.getAsDouble(json, "range", 4.0D);
			return new AreaDamageUpgradeResult(damage, internals, posEntry, directSourceEntry, sourceEntry, sourceName, bypassArmor, isMagic, isFire, range);
		}
		
		@Override
		public void toNetwork(AreaDamageUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeFloat(result.damage);
			result.posEntry.toNetwork(buf);
			result.directSourceEntry.toNetwork(buf);
			result.sourceEntry.toNetwork(buf);
			buf.writeUtf(result.damageSourceName);
			buf.writeBoolean(result.bypassArmor);
			buf.writeBoolean(result.isMagic);
			buf.writeBoolean(result.isFire);
			buf.writeDouble(result.range);
		}
		
		@Override
		public AreaDamageUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			float damage = buf.readFloat();
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			UpgradeEntry<Entity> directSourceEntry = EntryCategory.ENTITY.fromNetwork(buf);
			UpgradeEntry<Entity> sourceEntry = EntryCategory.ENTITY.fromNetwork(buf);
			String sourceName = buf.readUtf();
			boolean bypassArmor = buf.readBoolean();
			boolean isMagic = buf.readBoolean();
			boolean isFire = buf.readBoolean();
			double range = buf.readDouble();
			return new AreaDamageUpgradeResult(damage, internals, posEntry, directSourceEntry, sourceEntry, sourceName, bypassArmor, isMagic, isFire, range);
		}
		
	}
	
}