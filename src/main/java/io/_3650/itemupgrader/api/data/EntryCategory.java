package io._3650.itemupgrader.api.data;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.ItemUpgrader;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntryCategory<T> {
	
	/**Don't worry about this, it does nothing on its own, it's just to get java to load the static stuff*/
	public static final void init() {}
	
	private final ResourceLocation id;
	private final EntryCategory<? super T> parent;
	private final Map<ResourceLocation, UpgradeEntry<T>> entries = Maps.newHashMap();
	
	private UpgradeEntry<T> defaultValue = null;
	
	/**
	 * Constructs a new upgrade entry category
	 * @param id A {@linkplain ResourceLocation} to use as your category's id
	 */
	private EntryCategory(ResourceLocation id) {
		this.id = id;
		this.parent = null;
	}
	
	/**
	 * Constructs a new upgrade entry category with a parent category
	 * @param id A {@linkplain ResourceLocation} to use as your category's id
	 * @param parent The parent {@linkplain UpgradeEntry}
	 */
	private EntryCategory(ResourceLocation id, EntryCategory<? super T> parent) {
		this.id = id;
		this.parent = parent;
	}
	
	public ResourceLocation getId() {
		return this.id;
	}
	
	public void setDefaultValue(@Nonnull UpgradeEntry<T> defaultValue) {
		if (this.defaultValue != defaultValue) this.defaultValue = defaultValue;
	}
	
	@Nonnull
	public UpgradeEntry<T> getDefaultValue() {
		if (this.defaultValue == null) throw new NullPointerException(this + " has no default value defined! Report this to the mod author!");
		return this.defaultValue;
	}
	
	public boolean hasParent() {
		return this.parent != null;
	}
	
	@Nullable
	public EntryCategory<? super T> getParent() {
		return this.parent;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addChild(UpgradeEntry<? extends T> entry) {
		boolean pass = !this.entries.containsKey(entry.getId());
		this.entries.put(entry.getId(), (UpgradeEntry<T>) entry);
		this.entries.put(new ResourceLocation(entry.getId().getPath()), (UpgradeEntry<T>) entry);
		if (this.hasParent()) pass = this.parent.addChild(entry) && pass;
		return pass;
	}
	
	public boolean isChild(UpgradeEntry<T> entry) {
		return this.entries.containsKey(entry.getId());
	}
	
	public boolean isEntry(ResourceLocation id) {
		return this.entries.containsKey(id);
	}
	
	@Nullable
	public UpgradeEntry<T> getEntry(String id) {
		try {
			return this.getEntry(new ResourceLocation(id));
		} catch (ResourceLocationException e) {
			return null;
		}
	}
	
	@Nullable
	public UpgradeEntry<T> getEntry(ResourceLocation id) {
		return this.entries.get(id);
	}
	
	@Override
	public String toString() {
		return "<category:" + this.id + ">";
	}
	
	@Nonnull
	public UpgradeEntry<T> fromJson(JsonObject json) {
		String val = categoryKeyFromJson(json, this.getId().toString());
		if (val == null) val = categoryKeyFromJson(json, this.getId().getPath());
		if (val == null) return this.defaultValue;
		else return Objects.requireNonNullElse(this.getEntry(val), this.defaultValue);
	}
	
	@Nullable
	private static String categoryKeyFromJson(JsonObject json, String category) {
		if (GsonHelper.isStringValue(json, category)) {
			return GsonHelper.getAsString(json, category);
		} else return null;
	}
	
	@Nonnull
	public UpgradeEntry<T> fromJson(JsonObject json, String keyOverride) {
		String val = categoryKeyFromJson(json, keyOverride);
		if (val == null) return this.defaultValue;
		return Objects.requireNonNullElse(this.getEntry(val), this.defaultValue);
	}
	
	public UpgradeEntry<T> fromNetwork(FriendlyByteBuf buf) {
		return this.getEntry(buf.readResourceLocation());
	}
	
	private static final Factory FACTORY = new Factory(ItemUpgrader.MOD_ID);
	
	private static <T> EntryCategory<T> create(String name) {
		return FACTORY.create(name);
	}
	
	private static <T> EntryCategory<T> create(String name, EntryCategory<? super T> parent) {
		return FACTORY.create(name, parent);
	}
	
	/**Entity*/
	public static final EntryCategory<Entity> ENTITY = create("entity");
	/**Entity &gt Living*/
	public static final EntryCategory<LivingEntity> LIVING = create("living", ENTITY);
	/**Entity &gt Living &gt Player*/
	public static final EntryCategory<Player> PLAYER = create("player", LIVING);
	
	/**Vec3*/
	public static final EntryCategory<Vec3> POSITION = create("position");
	/**BlockPos*/
	public static final EntryCategory<BlockPos> BLOCK_POS = create("block_pos");
	
	/**Item*/
	public static final EntryCategory<ItemStack> ITEM = create("item");
	
	/**Upgrade Entry ID*/
	public static final EntryCategory<ResourceLocation> UPGRADE_ID = create("upgrade_id");
	
	public static final class Factory {
		private final String modId;
		public Factory(String modId) {
			this.modId = modId;
		}
		public <T> EntryCategory<T> create(String name) {
			return new EntryCategory<>(new ResourceLocation(modId, name));
		}
		public <T> EntryCategory<T> create(String name, EntryCategory<? super T> parent) {
			return new EntryCategory<>(new ResourceLocation(modId, name), parent);
		}
	}
	
}