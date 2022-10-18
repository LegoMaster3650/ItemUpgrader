package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

/**
 * hasEntity<br>
 * hasDirectEntity<br>
 * isBypassArmor<br>
 * isBypassInvul<br>
 * isBypassMagic<br>
 * isCreativePlayer<br>
 * isDamageHelmet<br>
 * isExplosion<br>
 * isFall<br>
 * isFire<br>
 * isMagic<br>
 * isNoAggro<br>
 * isProjectile
 */
public class DamageSourceUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<DamageSource> sourceEntry;
	private final boolean hasEntity;
	private final boolean hasDirectEntity;
	private final boolean isBypassArmor;
	private final boolean isBypassInvul;
	private final boolean isBypassMagic;
	private final boolean isCreativePlayer;
	private final boolean isDamageHelmet;
	private final boolean isExplosion;
	private final boolean isFall;
	private final boolean isFire;
	private final boolean isMagic;
	private final boolean isNoAggro;
	private final boolean isProjectile;
	
	public DamageSourceUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<DamageSource> sourceEntry,
			boolean hasEntity, boolean hasDirectEntity, boolean isBypassArmor,
			boolean isBypassInvul, boolean isBypassMagic, boolean isCreativePlayer,
			boolean isDamageHelmet, boolean isExplosion, boolean isFall, boolean isFire,
			boolean isMagic, boolean isNoAggro, boolean isProjectile) {
		super(internals, inverted, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.DAMAGE_SOURCE, sourceEntry);
		}));
		this.sourceEntry = sourceEntry;
		this.hasEntity = hasEntity;
		this.hasDirectEntity = hasDirectEntity;
		this.isBypassArmor = isBypassArmor;
		this.isBypassInvul = isBypassInvul;
		this.isBypassMagic = isBypassMagic;
		this.isCreativePlayer = isCreativePlayer;
		this.isDamageHelmet = isDamageHelmet;
		this.isExplosion = isExplosion;
		this.isFall = isFall;
		this.isFire = isFire;
		this.isMagic = isMagic;
		this.isNoAggro = isNoAggro;
		this.isProjectile = isProjectile;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		DamageSource damage = data.getEntry(this.sourceEntry);
		boolean result = true;
		if (this.hasEntity) result &= damage.getEntity() != null;
		if (this.hasDirectEntity) result &= damage.getDirectEntity() != null;
		if (this.isBypassArmor) result &= damage.isBypassArmor();
		if (this.isBypassInvul) result &= damage.isBypassInvul();
		if (this.isBypassMagic) result &= damage.isBypassMagic();
		if (this.isCreativePlayer) result &= damage.isCreativePlayer();
		if (this.isDamageHelmet) result &= damage.isDamageHelmet();
		if (this.isExplosion) result &= damage.isExplosion();
		if (this.isFall) result &= damage.isFall();
		if (this.isFire) result &= damage.isFire();
		if (this.isMagic) result &= damage.isMagic();
		if (this.isNoAggro) result &= damage.isNoAggro();
		if (this.isProjectile) result &= damage.isProjectile();
		return result;
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
	
	public static class Serializer extends UpgradeConditionSerializer<DamageSourceUpgradeCondition> {
		
		@Override
		public DamageSourceUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromJson(json);
			boolean hasEntity = GsonHelper.getAsBoolean(json, "hasEntity");
			boolean hasDirectEntity = GsonHelper.getAsBoolean(json, "hasDirectEntity");
			boolean isBypassArmor = GsonHelper.getAsBoolean(json, "isBypassArmor");
			boolean isBypassInvul = GsonHelper.getAsBoolean(json, "isBypassInvul");
			boolean isBypassMagic = GsonHelper.getAsBoolean(json, "isBypassMagic");
			boolean isCreativePlayer = GsonHelper.getAsBoolean(json, "isCreativePlayer");
			boolean isDamageHelmet = GsonHelper.getAsBoolean(json, "isDamageHelmet");
			boolean isExplosion = GsonHelper.getAsBoolean(json, "isExplosion");
			boolean isFall = GsonHelper.getAsBoolean(json, "isFall");
			boolean isFire = GsonHelper.getAsBoolean(json, "isFire");
			boolean isMagic = GsonHelper.getAsBoolean(json, "isMagic");
			boolean isNoAggro = GsonHelper.getAsBoolean(json, "isNoAggro");
			boolean isProjectile = GsonHelper.getAsBoolean(json, "isProjectile");
			return new DamageSourceUpgradeCondition(internals, inverted, sourceEntry,
					hasEntity, hasDirectEntity, isBypassArmor, isBypassInvul,
					isBypassMagic, isCreativePlayer, isDamageHelmet, isExplosion,
					isFall, isFire, isMagic, isNoAggro, isProjectile);
		}
		
		@Override
		public void toNetwork(DamageSourceUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.sourceEntry.toNetwork(buf);
			buf.writeBoolean(condition.hasEntity);
			buf.writeBoolean(condition.hasDirectEntity);
			buf.writeBoolean(condition.isBypassArmor);
			buf.writeBoolean(condition.isBypassInvul);
			buf.writeBoolean(condition.isBypassMagic);
			buf.writeBoolean(condition.isCreativePlayer);
			buf.writeBoolean(condition.isDamageHelmet);
			buf.writeBoolean(condition.isExplosion);
			buf.writeBoolean(condition.isFall);
			buf.writeBoolean(condition.isFire);
			buf.writeBoolean(condition.isMagic);
			buf.writeBoolean(condition.isNoAggro);
			buf.writeBoolean(condition.isProjectile);
		}
		
		@Override
		public DamageSourceUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromNetwork(buf);
			boolean hasEntity = buf.readBoolean();
			boolean hasDirectEntity = buf.readBoolean();
			boolean isBypassArmor = buf.readBoolean();
			boolean isBypassInvul = buf.readBoolean();
			boolean isBypassMagic = buf.readBoolean();
			boolean isCreativePlayer = buf.readBoolean();
			boolean isDamageHelmet = buf.readBoolean();
			boolean isExplosion = buf.readBoolean();
			boolean isFall = buf.readBoolean();
			boolean isFire = buf.readBoolean();
			boolean isMagic = buf.readBoolean();
			boolean isNoAggro = buf.readBoolean();
			boolean isProjectile = buf.readBoolean();
			return new DamageSourceUpgradeCondition(internals, inverted, sourceEntry,
					hasEntity, hasDirectEntity, isBypassArmor, isBypassInvul,
					isBypassMagic, isCreativePlayer, isDamageHelmet, isExplosion,
					isFall, isFire, isMagic, isNoAggro, isProjectile);		}
		
	}
	
}
