package io._3650.itemupgrader.api.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io._3650.itemupgrader.ItemUpgrader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
	private final boolean nullable;
	private final EntryCategory<T> category;
	
	/**
	 * Constructs a new {@linkplain UpgradeEntry} with the given name
	 * @param id The {@linkplain ResourceLocation} to identify this with
	 * @param nullable Whether or not the value of this entry may be null
	 * @param category The category this entry belongs to (or {@code null} if none)
	 */
	private UpgradeEntry(ResourceLocation id, boolean nullable, @Nonnull EntryCategory<T> category) {
		this.id = id;
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
	
	public static final UpgradeEntry<LogicalSide> SIDE = FACTORY.create("side");
	public static final UpgradeEntry<EquipmentSlot> SLOT = FACTORY.create("slot", true);
	public static final UpgradeEntry<ItemStack> ITEM = FACTORY.createDefault("itemstack", EntryCategory.ITEM);
	public static final UpgradeEntry<ItemStack> PREV_ITEM = FACTORY.create("previous_item", EntryCategory.ITEM);
	public static final UpgradeEntry<Entity> ENTITY = FACTORY.createDefault("entity", EntryCategory.ENTITY);
	public static final UpgradeEntry<Vec3> POSITION = FACTORY.createDefault("position", EntryCategory.POSITION);
	public static final UpgradeEntry<Level> LEVEL = FACTORY.create("level");
	public static final UpgradeEntry<Entity> DAMAGER_ENTITY = FACTORY.create("damager_entity", EntryCategory.ENTITY);
	public static final UpgradeEntry<Entity> DIRECT_DAMAGER = FACTORY.create("direct_damager", EntryCategory.ENTITY);
	public static final UpgradeEntry<DamageSource> DAMAGE_SOURCE = FACTORY.createDefault("damager", EntryCategory.DAMAGE_SOURCE);
	public static final UpgradeEntry<Float> DAMAGE = FACTORY.create("damage", EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<Float> DAMAGE_MULT = FACTORY.create("damage_multiplier", EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<Float> FALL_DIST = FACTORY.create("fall_distance", EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<BlockState> BLOCK_STATE = FACTORY.create("block_state");
	public static final UpgradeEntry<BlockEntity> BLOCK_ENTITY = FACTORY.create("block_entity");
	public static final UpgradeEntry<BlockPos> BLOCK_POS = FACTORY.createDefault("block_position", EntryCategory.BLOCK_POS);
	public static final UpgradeEntry<Direction> BLOCK_FACE = FACTORY.create("block_face", EntryCategory.DIRECTION);
	public static final UpgradeEntry<LivingEntity> LIVING = FACTORY.createDefault("living_entity", EntryCategory.LIVING);
	public static final UpgradeEntry<Player> PLAYER = FACTORY.createDefault("player", EntryCategory.PLAYER);
	public static final UpgradeEntry<Entity> TARGET_ENTITY = FACTORY.create("target_entity", EntryCategory.ENTITY);
	public static final UpgradeEntry<Vec3> TARGET_ENTITY_POS = FACTORY.create("target_entity_position", EntryCategory.POSITION);
	public static final UpgradeEntry<Vec3> INTERACTION_POS = FACTORY.create("interaction_position", EntryCategory.POSITION);
	public static final UpgradeEntry<ResourceLocation> UPGRADE_ID = FACTORY.createDefault("upgrade_id", EntryCategory.UPGRADE_ID);
	public static final UpgradeEntry<ResourceLocation> PREV_UPGRADE_ID = FACTORY.create("prev_upgrade_id", EntryCategory.UPGRADE_ID);
	public static final UpgradeEntry<Integer> INT_VALUE = FACTORY.createDefault("int", EntryCategory.INT_VALUE);
	public static final UpgradeEntry<Float> FLOAT_VALUE = FACTORY.createDefault("float", EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<ResourceLocation> ENCHANTMENT_ID = FACTORY.create("enchantment");
	public static final UpgradeEntry<Integer> ENCHANTMENT_LEVEL = FACTORY.create("enchantment_level", EntryCategory.INT_VALUE);
	
	/**<b><u>Intended for use in results</u></b>**/
	public static final UpgradeEntry<Boolean> CANCELLED = FACTORY.create("cancelled");
	/**<b><u>Intended for use in results</u></b>**/
	public static final UpgradeEntry<Boolean> CONSUMED = FACTORY.create("consumed");
	
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
		
		public <T> UpgradeEntry<T> create(String name) {
			return create(name, false, null);
		}
		
		public <T> UpgradeEntry<T> create(String name, boolean nullable) {
			return create(name, nullable, null);
		}
		
		public <T> UpgradeEntry<T> create(String name, @Nullable EntryCategory<T> category) {
			return create(name, false, category);
		}
		
		public <T> UpgradeEntry<T> create(String name, boolean nullable, @Nullable EntryCategory<T> category) {
			UpgradeEntry<T> entry = new UpgradeEntry<>(new ResourceLocation(this.modId, name), nullable, category);
			if (category != null) {
				category.addEntry(entry);
			}
			return entry;
		}
		
		public <T> UpgradeEntry<T> createDefault(String name, @Nullable EntryCategory<T> category) {
			UpgradeEntry<T> entry = create(name, false, category);
			category.setDefaultValue(entry);
			return entry;
		}
		
		public <T> UpgradeEntry<T> createDefault(String name, boolean nullable, @Nullable EntryCategory<T> category) {
			UpgradeEntry<T> entry = create(name, nullable, category);
			category.setDefaultValue(entry);
			return entry;
		}
		
	}
	
}
