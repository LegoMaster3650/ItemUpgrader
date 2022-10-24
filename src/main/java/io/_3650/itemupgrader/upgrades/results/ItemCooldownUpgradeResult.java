package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemCooldownUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Player> playerEntry;
	private final UpgradeEntry<ItemStack> itemEntry;
	private final int cooldownTicks;
	
	public ItemCooldownUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Player> playerEntry,
			UpgradeEntry<ItemStack> itemEntry, int cooldownTicks) {
		super(internals, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.PLAYER, playerEntry);
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.playerEntry = playerEntry;
		this.itemEntry = itemEntry;
		this.cooldownTicks = cooldownTicks;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		Player player = data.getEntry(this.playerEntry);
		ItemStack stack = data.getEntry(this.itemEntry);
		player.getCooldowns().addCooldown(stack.getItem(), this.cooldownTicks);
	}
	
	private final Serializer instance = new Serializer();
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.cooldownTicks / 20.0D)));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<ItemCooldownUpgradeResult> {
		
		@Override
		public ItemCooldownUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			int cooldownTicks = GsonHelper.getAsInt(json, "cooldown");
			return new ItemCooldownUpgradeResult(internals, playerEntry, itemEntry, cooldownTicks);
		}
		
		@Override
		public void toNetwork(ItemCooldownUpgradeResult result, FriendlyByteBuf buf) {
			result.playerEntry.toNetwork(buf);
			result.itemEntry.toNetwork(buf);
			buf.writeInt(result.cooldownTicks);
		}
		
		@Override
		public ItemCooldownUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			int cooldownTicks = buf.readInt();
			return new ItemCooldownUpgradeResult(internals, playerEntry, itemEntry, cooldownTicks);
		}
		
	}
	
}