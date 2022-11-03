package io._3650.itemupgrader.upgrades.actions;

import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.type.ConditionalUpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.upgrades.data.ModUpgradeEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class BreakSpeedUpgradeAction extends ConditionalUpgradeAction {
	
	private final double modifier;
	
	public BreakSpeedUpgradeAction(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, List<UpgradeCondition> conditions, double modifier) {
		super(internals, validSlots, conditions);
		this.modifier = modifier;
	}
	
	@Override
	public MutableComponent applyResultTooltip(MutableComponent tooltip, ItemStack stack) {
		return tooltip.append(new TranslatableComponent("action.itemupgrader.mining_speed.tooltip" + (this.modifier < 0 ? ".take" : ".plus"), new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.modifier * 100.0D))).withStyle(this.modifier < 0 ? ChatFormatting.RED : ChatFormatting.BLUE));
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		float breakSpeed = data.getEntry(ModUpgradeEntry.BREAKING_SPEED);
		data.setModifiableEntry(ModUpgradeEntry.BREAKING_SPEED, breakSpeed * (1.0F + (float) this.modifier));
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
	
	public static class Serializer extends ConditionalUpgradeActionSerializer<BreakSpeedUpgradeAction> {
		
		@Override
		public UpgradeEntrySet getProvidedData() {
			return UpgradeEntrySet.PLAYER_SLOT_ITEM
					.with(UpgradeEntrySet.BLOCK_POS_STATE)
					.with(builder -> builder.require(ModUpgradeEntry.BREAKING_SPEED));
		}
		
		@Override
		public BreakSpeedUpgradeAction fromJson(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, JsonObject json) {
			List<UpgradeCondition> conditions = this.conditionsFromJson(json);
			double modifier = GsonHelper.getAsDouble(json, "amount");
			return new BreakSpeedUpgradeAction(internals, validSlots, conditions, modifier);
		}
		
		@Override
		public void toNetwork(BreakSpeedUpgradeAction action, FriendlyByteBuf buf) {
			this.conditionsToNetwork(action, buf);
			buf.writeDouble(action.modifier);
		}
		
		@Override
		public BreakSpeedUpgradeAction fromNetwork(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, FriendlyByteBuf buf) {
			List<UpgradeCondition> conditions = this.conditionsFromNetwork(buf);
			double modifier = buf.readDouble();
			return new BreakSpeedUpgradeAction(internals, validSlots, conditions, modifier);
		}
		
	}
	
}
