package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.upgrades.data.ModDamageSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class DamageUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Entity> entityEntry;
	private final float damage;
	private final String damageSource;
	
	public DamageUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Entity> entityEntry, float damage, String damageSource) {
		super(internals, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.ENTITY, entityEntry);
		}));
		this.entityEntry = entityEntry;
		this.damage = damage;
		this.damageSource = damageSource;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Entity entity = data.getEntry(this.entityEntry);
		entity.hurt(getDamageSource(this.damageSource), this.damage);
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TextComponent("" + this.damage), new TextComponent(this.damageSource), new TranslatableComponent(this.entityEntry.getDescriptionId())};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<DamageUpgradeResult> {
		
		@Override
		public DamageUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			float damage = GsonHelper.getAsFloat(json, "amount");
			String damageSource = GsonHelper.getAsString(json, "source", "magic");
			return new DamageUpgradeResult(internals, entityEntry, damage, damageSource);
		}
		
		@Override
		public void toNetwork(DamageUpgradeResult result, FriendlyByteBuf buf) {
			result.entityEntry.toNetwork(buf);
			buf.writeFloat(result.damage);
			buf.writeUtf(result.damageSource);
		}
		
		@Override
		public DamageUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			float damage = buf.readFloat();
			String damageSource = buf.readUtf();
			return new DamageUpgradeResult(internals, entityEntry, damage, damageSource);
		}
		
	}
	
	private static DamageSource getDamageSource(String sourceName) {
		switch (sourceName) {
		case "simple":
			return ModDamageSource.MOD;
		case "generic":
			return DamageSource.GENERIC;
		default:
		case "magic":
			return DamageSource.MAGIC;
		case "void":
			return DamageSource.OUT_OF_WORLD;
		}
	}
	
}