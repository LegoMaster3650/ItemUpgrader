package io._3650.itemupgrader.upgrades.results;

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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class DamageUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Entity> entityEntry;
	private final float damage;
	private final UpgradeEntry<Entity> directSourceEntry;
	private final UpgradeEntry<Entity> sourceEntry;
	private final String damageSourceName;
	private final boolean bypassArmor;
	private final boolean isMagic;
	private final boolean isFire;
	
	public DamageUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Entity> entityEntry, float damage,
			UpgradeEntry<Entity> directSourceEntry,UpgradeEntry<Entity> sourceEntry, String damageSourceName,
			boolean bypassArmor, boolean isMagic, boolean isFire) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(entityEntry);
		}));
		this.entityEntry = entityEntry;
		this.damage = damage;
		this.directSourceEntry = directSourceEntry;
		this.sourceEntry = sourceEntry;
		this.damageSourceName = damageSourceName;
		this.bypassArmor = bypassArmor;
		this.isMagic = isMagic;
		this.isFire = isFire;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Entity entity = data.getEntry(this.entityEntry);
		if (entity.level.isClientSide) return false;

		Entity directSource = data.getEntryOrNull(this.directSourceEntry);
		Entity source = data.getEntryOrNull(this.sourceEntry);
		DamageSource damageSource;
		if (source == null) damageSource = new DamageSource(this.damageSourceName);
		else if (directSource == null) damageSource = new EntityDamageSource(damageSourceName, source);
		else damageSource = new IndirectEntityDamageSource(damageSourceName, directSource, source);
		if (this.bypassArmor) damageSource.bypassArmor();
		if (this.isMagic) damageSource.setMagic();
		if (this.isFire) damageSource.setIsFire();
		
		entity.hurt(damageSource, this.damage);
		
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.damage)), new TextComponent(this.damageSourceName), new TranslatableComponent(this.entityEntry.getDescriptionId())};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<DamageUpgradeResult> {
		
		@Override
		public DamageUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			float damage = GsonHelper.getAsFloat(json, "damage");
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
			return new DamageUpgradeResult(internals, entityEntry, damage, directSourceEntry, sourceEntry, sourceName, bypassArmor, isMagic, isFire);
		}
		
		@Override
		public void toNetwork(DamageUpgradeResult result, FriendlyByteBuf buf) {
			result.entityEntry.toNetwork(buf);
			buf.writeFloat(result.damage);
			result.directSourceEntry.toNetwork(buf);
			result.sourceEntry.toNetwork(buf);
			buf.writeUtf(result.damageSourceName);
			buf.writeBoolean(result.bypassArmor);
			buf.writeBoolean(result.isMagic);
			buf.writeBoolean(result.isFire);
		}
		
		@Override
		public DamageUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			float damage = buf.readFloat();
			UpgradeEntry<Entity> directSourceEntry = EntryCategory.ENTITY.fromNetwork(buf);
			UpgradeEntry<Entity> sourceEntry = EntryCategory.ENTITY.fromNetwork(buf);
			String sourceName = buf.readUtf();
			boolean bypassArmor = buf.readBoolean();
			boolean isMagic = buf.readBoolean();
			boolean isFire = buf.readBoolean();
			return new DamageUpgradeResult(internals, entityEntry, damage, directSourceEntry, sourceEntry, sourceName, bypassArmor, isMagic, isFire);
		}
		
	}
	
}