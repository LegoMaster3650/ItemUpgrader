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
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a category for {@linkplain UpgradeEntry entries}
 * @author LegoMaster3650
 * 
 * @param <T> The type that this category corresponds to
 * @see EntryCategorySet
 * @see UpgradeEntry
 */
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
	
	/**
	 * Gets the id of this category
	 * @return The {@linkplain ResourceLocation} associated with this category
	 */
	public ResourceLocation getId() {
		return this.id;
	}
	
	/**
	 * Sets the default value of this category
	 * @param defaultValue The default {@linkplain UpgradeEntry} to use for this category
	 */
	public void setDefaultValue(@Nonnull UpgradeEntry<T> defaultValue) {
		if (this.defaultValue == null) this.defaultValue = defaultValue;
		else throw new IllegalStateException("Trying to set default value for " + this + ", but it already has a default value " + this.defaultValue + "!");
	}
	
	/**
	 * Gets the default value for this category, throwing an error if it is undefined
	 * @return The default {@linkplain UpgradeEntry} for this category
	 * @throws NullPointerException If the default value for this category is undefined
	 */
	@Nonnull
	public UpgradeEntry<T> getDefaultValue() throws NullPointerException {
		if (this.defaultValue == null) throw new NullPointerException(this + " has no default value defined! Report this to the mod author!");
		return this.defaultValue;
	}
	
	/**
	 * Checks if this category has a parent category
	 * @return If this category has a parent {@linkplain EntryCategory}
	 */
	public boolean hasParent() {
		return this.parent != null;
	}
	
	/**
	 * Gets this category's parent category or null if not present
	 * @return This category's parent {@linkplain EntryCategory}
	 */
	@Nullable
	public EntryCategory<? super T> getParent() {
		return this.parent;
	}
	
	/**
	 * Adds an entry to this category and all of its parent categories
	 * @param entry The {@linkplain UpgradeEntry} to add to this category
	 * @return Whether or not any of the categories the entry was added to already had it
	 */
	@SuppressWarnings("unchecked")
	public boolean addEntry(UpgradeEntry<? extends T> entry) {
		boolean pass = !this.entries.containsKey(entry.getId());
		this.entries.put(entry.getId(), (UpgradeEntry<T>) entry);
		//I know this isn't the best idea but it's a compromise for user friendliness
		ResourceLocation basicEntryId = new ResourceLocation(entry.getId().getPath()); //only using first registered value
		if (!this.entries.containsKey(basicEntryId)) this.entries.put(basicEntryId, (UpgradeEntry<T>) entry);
		if (this.hasParent()) pass = this.parent.addEntry(entry) && pass;
		return pass;
	}
	
	/**
	 * Checks if this category contains the given entry
	 * @param entry The {@linkplain UpgradeEntry} to check
	 * @return If this category contains the given entry
	 */
	public boolean hasEntry(UpgradeEntry<T> entry) {
		return this.entries.containsKey(entry.getId());
	}
	
	/**
	 * Checks if this category contains the given entry id
	 * @param id The {@linkplain ResourceLocation} of the {@linkplain UpgradeEntry} to check for
	 * @return If this category contains the given entry id
	 */
	public boolean hasEntry(ResourceLocation id) {
		return this.entries.containsKey(id);
	}
	
	/**
	 * Gets the given entry if present, or null if not
	 * @param id The {@linkplain String} id of the entry to get, checking for default values if no namespace is given
	 * @return The {@linkplain UpgradeEntry} for the given id, or {@code null} if invalid
	 */
	@Nullable
	public UpgradeEntry<T> getEntry(String id) {
		try {
			return this.entries.get(new ResourceLocation(id));
		} catch (ResourceLocationException e) {
			return null;
		}
	}
	
	/**
	 * Gets the given entry if present, or null if not
	 * @param id The {@linkplain ResourceLocation} id of the entry to get, checking for default values if the default namespace {@code (minecraft)} is used
	 * @return The {@linkplain UpgradeEntry} for the given id, or {@code null} if invalid
	 */
	@Nullable
	public UpgradeEntry<T> getEntry(ResourceLocation id) {
		return this.entries.get(id);
	}
	
	@Override
	public String toString() {
		return "<category:" + this.id + ">";
	}
	
	/**
	 * Checks if the given json contains a value for this entry
	 * @param json The {@linkplain JsonObject} to check
	 * @return If the given json contains a value for this entry
	 */
	public boolean jsonHasValue(JsonObject json) {
		String val = categoryKeyFromJson(json, this.getId().getPath());
		if (val == null) val = categoryKeyFromJson(json, this.getId().toString());
		if (val == null) return false;
		return this.getEntry(val) != null;
	}
	
	/**
	 * Checks if the given json contains a value for this entry
	 * @param json The {@linkplain JsonObject} to check
	 * @param keyOverride An override for what json key to use for this entry
	 * @return If the given json contains a value for this entry
	 */
	public boolean jsonHasValue(JsonObject json, String keyOverride) {
		String val = categoryKeyFromJson(json, keyOverride);
		if (val == null) return false;
		return this.getEntry(val) != null;
	}
	
	/**
	 * Gets the entry associated with this category from the given json
	 * @param json The {@linkplain JsonObject} to get the entry from
	 * @return The {@linkplain UpgradeEntry} associated with this category, or the category's default value if not present and valid
	 */
	@Nonnull
	public UpgradeEntry<T> fromJson(JsonObject json) {
		String val = categoryKeyFromJson(json, this.getId().getPath());
		if (val == null) val = categoryKeyFromJson(json, this.getId().toString());
		if (val == null) return this.defaultValue;
		else return Objects.requireNonNullElse(this.getEntry(val), this.defaultValue);
	}
	
	@Nullable
	private static String categoryKeyFromJson(JsonObject json, String category) {
		if (GsonHelper.isStringValue(json, category)) {
			return GsonHelper.getAsString(json, category);
		} else return null;
	}
	
	/**
	 * Gets the entry associated with this category from the given json
	 * @param json The {@linkplain JsonObject} to get the entry from
	 * @param keyOverride An override for what json key to use for this entry
	 * @return The {@linkplain UpgradeEntry} associated with this category, or the category's default value if not present and valid
	 */
	@Nonnull
	public UpgradeEntry<T> fromJson(JsonObject json, String keyOverride) {
		String val = categoryKeyFromJson(json, keyOverride);
		if (val == null) return this.defaultValue;
		return Objects.requireNonNullElse(this.getEntry(val), this.defaultValue);
	}
	
	/**
	 * Reads an entry from the given network buffer
	 * @param buf The {@linkplain FriendlyByteBuf} to read the entry from
	 * @return The {@linkplain UpgradeEntry} from the buffer
	 */
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
	/**Entity &gt; Living*/
	public static final EntryCategory<LivingEntity> LIVING = create("living", ENTITY);
	/**Entity &gt; Living &gt; Player*/
	public static final EntryCategory<Player> PLAYER = create("player", LIVING);
	
	/**Vec3*/
	public static final EntryCategory<Vec3> POSITION = create("position");
	/**BlockPos*/
	public static final EntryCategory<BlockPos> BLOCK_POS = create("block_pos");
	/**Direction*/
	public static final EntryCategory<Direction> DIRECTION = create("direction");
	
	/**ItemStack*/
	public static final EntryCategory<ItemStack> ITEM = create("item");
	
	/**Upgrade Entry ID*/
	public static final EntryCategory<ResourceLocation> UPGRADE_ID = create("upgrade_id");
	
	/**Integer Value*/
	public static final EntryCategory<Integer> INT_VALUE = create("int");
	
	/**Float Value*/
	public static final EntryCategory<Float> FLOAT_VALUE = create("float");
	
	/**Boolean Value*/
	public static final EntryCategory<Boolean> BOOL_VALUE = create("boolean");
	
	/**Damage Source*/
	public static final EntryCategory<DamageSource> DAMAGE_SOURCE = create("damage_source");
	
	/**
	 * A factory for upgrade categories which automatically ain your mod id for simplicity
	 */
	public static final class Factory {
		
		private final String modId;
		
		/**
		 * Constructs a new factory using the given mod id
		 * @param modId The mod id to use
		 */
		public Factory(String modId) {
			this.modId = modId;
		}
		
		/**
		 * Constructs a new category using the given name
		 * @param <T> The type to use for the category
		 * @param name The name to use for the category
		 * @return A new {@linkplain EntryCategory} with the given parameters
		 */
		public <T> EntryCategory<T> create(String name) {
			return new EntryCategory<>(new ResourceLocation(modId, name));
		}
		
		/**
		 * Constructs a new category using the given name and parent
		 * @param <T> The type to use for the category
		 * @param name The name to use for the category
		 * @param parent The parent of the new category
		 * @return A new {@linkplain EntryCategory} with the given parameters
		 */
		public <T> EntryCategory<T> create(String name, EntryCategory<? super T> parent) {
			return new EntryCategory<>(new ResourceLocation(modId, name), parent);
		}
	}
	
}