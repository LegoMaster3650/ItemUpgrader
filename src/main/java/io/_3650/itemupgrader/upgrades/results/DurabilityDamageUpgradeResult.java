package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.BoolHolder;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class DurabilityDamageUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final UpgradeEntry<LivingEntity> livingEntry;
	private final int amount;
	
	public DurabilityDamageUpgradeResult(IUpgradeInternals internals, UpgradeEntry<ItemStack> itemEntry, UpgradeEntry<LivingEntity> livingEntry, int amount) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.requireAll(itemEntry, livingEntry);
		}));
		this.itemEntry = itemEntry;
		this.livingEntry = livingEntry;
		this.amount = amount;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		LivingEntity living = data.getEntry(this.livingEntry);
		if (living.level.isClientSide) return false;
		ItemStack stack = data.getEntry(this.itemEntry);
		BoolHolder broke = new BoolHolder(false);
		stack.hurtAndBreak(this.amount, living, livingEntity -> {
			EquipmentSlot slot = data.getEntryOrNull(UpgradeEntry.SLOT);
			if (slot != null) livingEntity.broadcastBreakEvent(slot);
			broke.value = true;
		});
		return broke.value;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent("" + this.amount));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<DurabilityDamageUpgradeResult> {
		
		@Override
		public DurabilityDamageUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json);
			int amount = GsonHelper.getAsInt(json, "amount");
			return new DurabilityDamageUpgradeResult(internals, itemEntry, livingEntry, amount);
		}
		
		@Override
		public void toNetwork(DurabilityDamageUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
			result.livingEntry.toNetwork(buf);
			buf.writeInt(result.amount);
		}
		
		@Override
		public DurabilityDamageUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			int amount = buf.readInt();
			return new DurabilityDamageUpgradeResult(internals, itemEntry, livingEntry, amount);
		}
		
	}
	
}