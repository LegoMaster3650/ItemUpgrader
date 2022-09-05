package io.legom.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io.legom.itemupgrader.api.ItemUpgraderApi;
import io.legom.itemupgrader.api.data.UpgradeEntry;
import io.legom.itemupgrader.api.data.UpgradeEntrySet;
import io.legom.itemupgrader.api.data.UpgradeEventData;
import io.legom.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io.legom.itemupgrader.api.type.UpgradeCondition;
import io.legom.itemupgrader.api.util.ComponentHelper;
import io.legom.itemupgrader.upgrades.ItemUpgrade;
import io.legom.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class HasUpgradeCondition extends UpgradeCondition {
	
	private final EquipmentSlot slot;
	private final ResourceLocation upgradeId;
	
	public HasUpgradeCondition(IUpgradeInternals internals, boolean inverted, EquipmentSlot slot, ResourceLocation upgradeId) {
		super(internals, inverted);
		this.slot = slot;
		this.upgradeId = upgradeId;
	}

	@Override
	public UpgradeEntrySet requiredData() {
		return UpgradeEntrySet.LIVING;
	}

	@Override
	public boolean test(UpgradeEventData data) {
		ItemStack stack = data.getEntry(UpgradeEntry.LIVING).getItemBySlot(this.slot);
		if (!ItemUpgraderApi.hasUpgrade(stack)) return false;
		return ItemUpgraderApi.getUpgradeKey(stack).equals(this.upgradeId);
	}

	@Override
	public Serializer getSerializer() {
		return new Serializer();
	}

	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		ItemUpgrade upgrade = ItemUpgradeManager.INSTANCE.getUpgrade(this.upgradeId);
		MutableComponent upgradeComponent = new TranslatableComponent("upgrade." + ComponentHelper.keyFormat(this.upgradeId));
		if (upgrade != null) upgradeComponent = ComponentHelper.applyColor(upgrade.getColor(), upgradeComponent);
		MutableComponent slotComponent = ComponentHelper.slotInOn(this.slot);
		return new MutableComponent[] {upgradeComponent, slotComponent};
	}

	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<HasUpgradeCondition> {

		@Override
		public HasUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			EquipmentSlot slot = EquipmentSlot.byName(GsonHelper.getAsString(json, "slot"));
			ResourceLocation upgradeId = new ResourceLocation(GsonHelper.getAsString(json, "upgrade"));
			return new HasUpgradeCondition(internals, inverted, slot, upgradeId);
		}

		@Override
		public void toNetwork(HasUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeEnum(condition.slot);
			buf.writeResourceLocation(condition.upgradeId);
		}

		@Override
		public HasUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			EquipmentSlot netSlot = buf.readEnum(EquipmentSlot.class);
			ResourceLocation netUpgradeId = buf.readResourceLocation();
			return new HasUpgradeCondition(internals, inverted, netSlot, netUpgradeId);
		}
		
	}
	
}