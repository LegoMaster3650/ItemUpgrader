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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SetHealthUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<LivingEntity> livingEntry;
	private final float amount;
	
	public SetHealthUpgradeResult(IUpgradeInternals internals, UpgradeEntry<LivingEntity> livingEntry, float amount) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(livingEntry);
		}));
		this.livingEntry = livingEntry;
		this.amount = amount;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		LivingEntity living = data.getEntry(this.livingEntry);
		if (living.level.isClientSide) return false;
		living.setHealth(this.amount);
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TranslatableComponent(this.livingEntry.getDescriptionId()), new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amount))};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<SetHealthUpgradeResult> {
		
		@Override
		public SetHealthUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			float amount = GsonHelper.getAsFloat(json, "amount");
			return new SetHealthUpgradeResult(internals, livingEntry, amount);
		}
		
		@Override
		public void toNetwork(SetHealthUpgradeResult result, FriendlyByteBuf buf) {
			result.livingEntry.toNetwork(buf);
			buf.writeFloat(result.amount);
		}
		
		@Override
		public SetHealthUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			float amount = buf.readFloat();
			return new SetHealthUpgradeResult(internals, livingEntry, amount);
		}
		
	}
	
}