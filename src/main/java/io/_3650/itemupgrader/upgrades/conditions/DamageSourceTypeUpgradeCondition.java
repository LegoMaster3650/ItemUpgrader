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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

public class DamageSourceTypeUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<DamageSource> sourceEntry;
	private final String sourceType;
	
	public DamageSourceTypeUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<DamageSource> sourceEntry, String sourceType) {
		super(internals, inverted, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.DAMAGE_SOURCE, sourceEntry);
		}));
		this.sourceEntry = sourceEntry;
		this.sourceType = sourceType;
		
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		DamageSource damage = data.getEntry(this.sourceEntry);
		return damage.getMsgId().equals(this.sourceType);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(this.sourceType));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<DamageSourceTypeUpgradeCondition> {
		
		@Override
		public DamageSourceTypeUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromJson(json);
			String sourceType = GsonHelper.getAsString(json, "source");
			return new DamageSourceTypeUpgradeCondition(internals, inverted, sourceEntry, sourceType);
		}
		
		@Override
		public void toNetwork(DamageSourceTypeUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.sourceEntry.toNetwork(buf);
			buf.writeUtf(condition.sourceType);
		}
		
		@Override
		public DamageSourceTypeUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<DamageSource> sourceEntry = EntryCategory.DAMAGE_SOURCE.fromNetwork(buf);
			String sourceType = buf.readUtf();
			return new DamageSourceTypeUpgradeCondition(internals, inverted, sourceEntry, sourceType);
		}
		
	}
	
}
