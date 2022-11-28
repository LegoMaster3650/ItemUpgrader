package io._3650.itemupgrader.api.data;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.registry.RegistryHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;

/**
 * Represents an upgrade entry type for UpgradeEventData
 * @author LegoMaster3650
 * 
 * @param <T> The type that this entry corresponds to
 * @see UpgradeEventData
 * @see UpgradeEntrySet
 */
public class UpgradeEntry<T> {
	
	/**Don't worry about this, it does nothing on its own, it's just to get java to load the static stuff*/
	public static final void init() {}
	
	private final ResourceLocation id;
	private final Class<T> type;
	private final boolean nullable;
	private final EntryCategory<T> category;
	
	/**
	 * Constructs a new {@linkplain UpgradeEntry} with the given name
	 * @param id The {@linkplain ResourceLocation} to identify this with
	 * @param nullable Whether or not the value of this entry may be null
	 * @param category The category this entry belongs to (or {@code null} if none)
	 */
	private UpgradeEntry(ResourceLocation id, Class<T> type, boolean nullable, @Nonnull EntryCategory<T> category) {
		this.id = id;
		this.type = type;
		this.nullable = nullable;
		this.category = category;
	}
	
	/**
	 * Gets the id of this entry
	 * @return The {@linkplain ResourceLocation} associated with this entry
	 */
	public ResourceLocation getId() {
		return this.id;
	}
	
	@Nullable
	private String descriptionId;
	
	/**
	 * Gets the unlocalized descriptor id for translation
	 * @return The unlocalized descriptor id for translation
	 */
	@Nonnull
	public String getDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("upgradeEntry", this.id);
		}
		return this.descriptionId;
	}
	
	public Class<T> getType() {
		return this.type;
	}
	
	/**
	 * Checks if the value of this entry may be null
	 * @return Whether the value of this entry is allowed to be null
	 */
	public boolean isNullable() {
		return this.nullable;
	}
	
	/**
	 * Gets the category of this entry
	 * @return The {@linkplain EntryCategory} for this entry (MAY BE NULL)
	 */
	@Nullable
	public EntryCategory<T> getCategory() {
		return this.category;
	}
	
	@Override
	public String toString() {
		return "<entry:" + this.id + ">";
	}
	
	/**
	 * Writes this entry to the given network buffer
	 * @param buf The {@linkplain FriendlyByteBuf} to write this entry to
	 */
	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeResourceLocation(this.id);
	}
	
	private static final Factory FACTORY = new Factory(ItemUpgrader.MOD_ID);
	
	public static final UpgradeEntry<LogicalSide> SIDE = FACTORY.create("side", LogicalSide.class);
	public static final UpgradeEntry<EquipmentSlot> SLOT = FACTORY.create("slot", EquipmentSlot.class, true);
	public static final UpgradeEntry<ItemStack> ITEM = FACTORY.createDefault("item", ItemStack.class, EntryCategory.ITEM);
	public static final UpgradeEntry<ItemStack> PREV_ITEM = FACTORY.create("previous_item", ItemStack.class, EntryCategory.ITEM);
	public static final UpgradeEntry<ItemStack> USED_ITEM = FACTORY.create("used_item", ItemStack.class, EntryCategory.ITEM);
	public static final UpgradeEntry<Entity> ENTITY = FACTORY.createDefault("entity", Entity.class, EntryCategory.ENTITY);
	public static final UpgradeEntry<Vec3> POSITION = FACTORY.createDefault("position", Vec3.class, EntryCategory.POSITION);
	public static final UpgradeEntry<Level> LEVEL = FACTORY.create("level", Level.class);
	public static final UpgradeEntry<Entity> DAMAGER_ENTITY = FACTORY.create("damager_entity", Entity.class, EntryCategory.ENTITY);
	public static final UpgradeEntry<Entity> DIRECT_DAMAGER = FACTORY.create("direct_damager", Entity.class, EntryCategory.ENTITY);
	public static final UpgradeEntry<DamageSource> DAMAGE_SOURCE = FACTORY.createDefault("damager", DamageSource.class, EntryCategory.DAMAGE_SOURCE);
	public static final UpgradeEntry<Float> DAMAGE = FACTORY.create("damage", Float.class, EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<Float> DAMAGE_MULT = FACTORY.create("damage_multiplier", Float.class, EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<Float> FALL_DIST = FACTORY.create("fall_distance", Float.class, EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<BlockState> BLOCK_STATE = FACTORY.create("block_state", BlockState.class);
	public static final UpgradeEntry<BlockEntity> BLOCK_ENTITY = FACTORY.create("block_entity", BlockEntity.class);
	public static final UpgradeEntry<BlockPos> BLOCK_POS = FACTORY.createDefault("block_position", BlockPos.class, EntryCategory.BLOCK_POS);
	public static final UpgradeEntry<Direction> BLOCK_FACE = FACTORY.create("block_face", Direction.class, EntryCategory.DIRECTION);
	public static final UpgradeEntry<LivingEntity> LIVING = FACTORY.createDefault("living", LivingEntity.class, EntryCategory.LIVING);
	public static final UpgradeEntry<Player> PLAYER = FACTORY.createDefault("player", Player.class, EntryCategory.PLAYER);
	public static final UpgradeEntry<Entity> TARGET_ENTITY = FACTORY.create("target_entity", Entity.class, EntryCategory.ENTITY);
	public static final UpgradeEntry<Vec3> TARGET_ENTITY_POS = FACTORY.create("target_entity_position", Vec3.class, EntryCategory.POSITION);
	public static final UpgradeEntry<Vec3> INTERACTION_POS = FACTORY.create("interaction_position", Vec3.class, EntryCategory.POSITION);
	public static final UpgradeEntry<ResourceLocation> UPGRADE_ID = FACTORY.createDefault("upgrade_id", ResourceLocation.class, EntryCategory.UPGRADE_ID);
	public static final UpgradeEntry<ResourceLocation> PREV_UPGRADE_ID = FACTORY.create("prev_upgrade_id", ResourceLocation.class, EntryCategory.UPGRADE_ID);
	public static final UpgradeEntry<Integer> INT_VALUE = FACTORY.createDefault("int", Integer.class, EntryCategory.INT_VALUE);
	public static final UpgradeEntry<Float> FLOAT_VALUE = FACTORY.createDefault("float", Float.class, EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<Boolean> BOOL_VALUE = FACTORY.createDefault("boolean", Boolean.class, EntryCategory.BOOL_VALUE);
	public static final UpgradeEntry<ResourceLocation> ENCHANTMENT_ID = FACTORY.create("enchantment", ResourceLocation.class);
	public static final UpgradeEntry<Integer> ENCHANTMENT_LEVEL = FACTORY.create("enchantment_level", Integer.class, EntryCategory.INT_VALUE);
	public static final UpgradeEntry<Integer> ENCHANTABILITY = FACTORY.create("enchantability", Integer.class, EntryCategory.INT_VALUE);
	public static final UpgradeEntry<Float> BREAKING_SPEED = FACTORY.create("breaking_speed", Float.class, EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<List<ItemStack>> BLOCK_DROPS = FACTORY.create("block_drops", RegistryHelper.fixStupidClass(List.class));
	public static final UpgradeEntry<Boolean> DO_SHIELD_DAMAGE = FACTORY.create("do_shield_damage", Boolean.class, EntryCategory.BOOL_VALUE);
	public static final UpgradeEntry<Projectile> PROJECTILE = FACTORY.create("projectile", Projectile.class);
	
	/**<b><u>Intended for use in results</u></b>**/
	public static final UpgradeEntry<Boolean> CANCELLED = FACTORY.create("cancelled", Boolean.class); //twitter entry    bottom text
	/**<b><u>Intended for use in results</u></b>**/
	public static final UpgradeEntry<Boolean> CONSUMED = FACTORY.create("consumed", Boolean.class);
	
	/**
	 * A factory for upgrade entries which automatically adds in your mod id for simplicity
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
		
		public <T> UpgradeEntry<T> create(String name, Class<T> type) {
			return create(name, type, false, null);
		}
		
		public <T> UpgradeEntry<T> create(String name, Class<T> type, boolean nullable) {
			return create(name, type, nullable, null);
		}
		
		public <T> UpgradeEntry<T> create(String name, Class<T> type, @Nullable EntryCategory<T> category) {
			return create(name, type, false, category);
		}
		
		public <T> UpgradeEntry<T> create(String name, Class<T> type, boolean nullable, @Nullable EntryCategory<T> category) {
			UpgradeEntry<T> entry = new UpgradeEntry<>(new ResourceLocation(this.modId, name), type, nullable, category);
			if (category != null) {
				category.addEntry(entry);
			}
			return entry;
		}
		
		public <T> UpgradeEntry<T> createDefault(String name, Class<T> type, @Nullable EntryCategory<T> category) {
			UpgradeEntry<T> entry = create(name, type, false, category);
			category.setDefaultValue(entry);
			return entry;
		}
		
		public <T> UpgradeEntry<T> createDefault(String name, Class<T> type, boolean nullable, @Nullable EntryCategory<T> category) {
			UpgradeEntry<T> entry = create(name, type, nullable, category);
			category.setDefaultValue(entry);
			return entry;
		}
		
	}
	
}
