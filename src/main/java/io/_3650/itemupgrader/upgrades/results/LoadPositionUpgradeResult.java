package io._3650.itemupgrader.upgrades.results;

import java.util.Set;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LoadPositionUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final UpgradeEntry<Player> playerEntry;
	private final String tagName;
	private final boolean dimension;
	private final boolean discard;
	
	public LoadPositionUpgradeResult(IUpgradeInternals internals, UpgradeEntry<ItemStack> itemEntry, UpgradeEntry<Player> playerEntry, String tagName, boolean dimension, boolean discard) {
		super(internals, UpgradeEntrySet.PLAYER_ITEM.fillCategories(mapper -> {
			mapper.set(EntryCategory.PLAYER, playerEntry).set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.playerEntry = playerEntry;
		this.tagName = tagName;
		this.dimension = dimension;
		this.discard = discard;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		if (!(data.getEntry(this.playerEntry) instanceof ServerPlayer player)) return;
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag.contains(this.tagName, CompoundTag.TAG_COMPOUND)) {
				CompoundTag posTag = tag.getCompound(this.tagName);
				if (posTag.contains("x", CompoundTag.TAG_DOUBLE) && posTag.contains("y", CompoundTag.TAG_DOUBLE) && posTag.contains("z", CompoundTag.TAG_DOUBLE)) {
					double x = posTag.getDouble("x");
					double y = posTag.getDouble("y");
					double z = posTag.getDouble("z");
					boolean dimensionValid = this.dimension && posTag.contains("dim", CompoundTag.TAG_STRING);
					ResourceKey<Level> dimension = player.level.dimension();
					if (dimensionValid) { //first pass - validate dimension value
						String dimKeyStr = posTag.getString("dim");
						if (ResourceLocation.isValidResourceLocation(dimKeyStr) && player.level instanceof ServerLevel level) {
							dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimKeyStr));
							if (!level.dimension().equals(dimension)) {
								boolean pass = false;
								Set<ResourceKey<Level>> levelKeys = level.getServer().levelKeys();
								for (var key : levelKeys) {
									if (key.equals(dimension)) {
										pass = true;
										break;
									}
								}
								dimensionValid = pass;
							} else dimensionValid = false;
						} else dimensionValid = false;
					}
					if (dimensionValid) { //second pass - actual logic
						ServerLevel level = player.server.getLevel(dimension);
						player.teleportTo(level, x, y, z, 0, 0.5F);
					} else {
						player.teleportTo(x, y, z);
					}
					if (this.discard) {
						tag.remove(this.tagName);
						stack.setTag(tag);
					}
				}
			}
		}
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(this.tagName));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<LoadPositionUpgradeResult> {
		
		@Override
		public LoadPositionUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag");
			boolean dimension = GsonHelper.getAsBoolean(json, "dimension", true);
			boolean discard = GsonHelper.getAsBoolean(json, "discard", false);
			return new LoadPositionUpgradeResult(internals, itemEntry, playerEntry, tagName, dimension, discard);
		}
		
		@Override
		public void toNetwork(LoadPositionUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
			result.playerEntry.toNetwork(buf);
			buf.writeUtf(result.tagName);
			buf.writeBoolean(result.dimension);
			buf.writeBoolean(result.discard);
		}
		
		@Override
		public LoadPositionUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			String tagName = buf.readUtf();
			boolean dimension = buf.readBoolean();
			boolean discard = buf.readBoolean();
			return new LoadPositionUpgradeResult(internals, itemEntry, playerEntry, tagName, dimension, discard);
		}
		
	}
	
}