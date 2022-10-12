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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemCooldownUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<Player> playerEntry;
	private final UpgradeEntry<ItemStack> itemEntry;
	
	public ItemCooldownUpgradeCondition(
			IUpgradeInternals internals,
			boolean inverted,
			UpgradeEntry<Player> playerEntry,
			UpgradeEntry<ItemStack> itemEntry) {
		super(internals, inverted, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.PLAYER, playerEntry);
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.playerEntry = playerEntry;
		this.itemEntry = itemEntry;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		Player player = data.getEntry(this.playerEntry);
		ItemStack stack = data.getEntry(this.itemEntry);
		return !player.getCooldowns().isOnCooldown(stack.getItem());
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
	
	public static class Serializer extends UpgradeConditionSerializer<ItemCooldownUpgradeCondition> {
		
		@Override
		public ItemCooldownUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			return new ItemCooldownUpgradeCondition(internals, inverted, playerEntry, itemEntry);
		}
		
		@Override
		public void toNetwork(ItemCooldownUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.playerEntry.toNetwork(buf);
			condition.itemEntry.toNetwork(buf);
		}
		
		@Override
		public ItemCooldownUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			return new ItemCooldownUpgradeCondition(internals, inverted, playerEntry, itemEntry);
		}
		
	}
	
}