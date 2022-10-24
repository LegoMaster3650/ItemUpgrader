package io._3650.itemupgrader.upgrades.actions;

import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.type.ConditionalUpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.registry.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class LootEnchantUpgradeAction extends ConditionalUpgradeAction {
	
	private final ResourceLocation enchantId;
	private final int modifier;
	
	public LootEnchantUpgradeAction(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, List<UpgradeCondition> conditions, ResourceLocation enchantId, int modifier) {
		super(internals, validSlots, conditions);
		this.enchantId = enchantId;
		this.modifier = modifier;
	}
	
	@Override
	public MutableComponent getResultTooltip(ItemStack stack) {
		return new TranslatableComponent("action.itemupgrader.loot_enchantment.tooltip" + (this.modifier < 0 ? ".decrease" : this.modifier == 1 ? ".one" : ""), Config.CLIENT.useRomanNumerals.get() ? new TranslatableComponent("enchantment.level." + this.modifier) : new TextComponent("" + Mth.abs(this.modifier)), new TranslatableComponent("enchantment." + ComponentHelper.keyFormat(this.enchantId)));
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		if (data.getEntry(UpgradeEntry.ENCHANTMENT_ID).equals(this.enchantId)) {
			int enchLevel = data.getEntry(UpgradeEntry.ENCHANTMENT_LEVEL);
			data.setModifiableEntry(UpgradeEntry.ENCHANTMENT_LEVEL, enchLevel + this.modifier);
		}
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends ConditionalUpgradeActionSerializer<LootEnchantUpgradeAction> {
		
		@Override
		public UpgradeEntrySet getProvidedData() {
			return UpgradeEntrySet.ITEM_ENCHANTMENT;
		}
		
		@Override
		public LootEnchantUpgradeAction fromJson(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, JsonObject json) {
			List<UpgradeCondition> conditions = this.conditionsFromJson(json);
			ResourceLocation enchantId = new ResourceLocation(GsonHelper.getAsString(json, "enchantment"));
			int modifier = GsonHelper.getAsInt(json, "amount");
			return new LootEnchantUpgradeAction(internals, validSlots, conditions, enchantId, modifier);
		}
		
		@Override
		public void toNetwork(LootEnchantUpgradeAction action, FriendlyByteBuf buf) {
			this.conditionsToNetwork(action, buf);
			buf.writeResourceLocation(action.enchantId);
			buf.writeInt(action.modifier);
		}
		
		@Override
		public LootEnchantUpgradeAction fromNetwork(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, FriendlyByteBuf buf) {
			List<UpgradeCondition> conditions = this.conditionsFromNetwork(buf);
			ResourceLocation enchantId = buf.readResourceLocation();
			int modifier = buf.readInt();
			return new LootEnchantUpgradeAction(internals, validSlots, conditions, enchantId, modifier);
		}
		
	}
	
}
