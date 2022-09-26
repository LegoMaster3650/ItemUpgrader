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
	private final EntryCategory<T> category;
	
	private UpgradeEntry(ResourceLocation id) {
		this.id = id;
		this.category = null;
	}
	
	/**
	 * Constructs a new {@linkplain UpgradeEntry} with the given name
	 * @param name The {@linkplain ResourceLocation} to identify this with
	 */
	private UpgradeEntry(ResourceLocation id, @Nonnull EntryCategory<T> category) {
		this.id = id;
		this.category = category;
		category.addEntry(this);
	}
	
	/**
	 * Gets the id of this entry
	 * @return The {@linkplain ResourceLocation} associated with this entry
	 */
	public ResourceLocation getId() {
		return this.id;
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
	
	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeResourceLocation(this.id);
	}
	
	private static final Factory FACTORY = new Factory(ItemUpgrader.MOD_ID);
	
	private static <T> UpgradeEntry<T> create(String name) {
		return FACTORY.create(name);
	}
	
	private static <T> UpgradeEntry<T> create(String name, EntryCategory<T> category) {
		return FACTORY.create(name, category);
	}
	
	private static <T> UpgradeEntry<T> create(String name, EntryCategory<T> category, boolean isCategoryDefault) {
		return FACTORY.create(name, category, isCategoryDefault);
	}
	
	public static final UpgradeEntry<LogicalSide> SIDE = create("side");
	public static final UpgradeEntry<EquipmentSlot> SLOT = create("slot");
	public static final UpgradeEntry<ItemStack> ITEM = create("itemstack", EntryCategory.ITEM, true);
	public static final UpgradeEntry<ItemStack> PREV_ITEM = create("previous_item");
	public static final UpgradeEntry<Entity> ENTITY = create("entity", EntryCategory.ENTITY, true);
	public static final UpgradeEntry<Vec3> POSITION = create("position", EntryCategory.POSITION, true);
	public static final UpgradeEntry<Level> LEVEL = create("level");
	public static final UpgradeEntry<Entity> DAMAGER_ENTITY = create("damager_entity", EntryCategory.ENTITY);
	public static final UpgradeEntry<Entity> DIRECT_DAMAGER = create("direct_damager", EntryCategory.ENTITY);
	public static final UpgradeEntry<DamageSource> DAMAGE_SOURCE = create("damager");
	public static final UpgradeEntry<Float> DAMAGE = create("damage", EntryCategory.FLOAT_VALUE);
	public static final UpgradeEntry<BlockState> BLOCK_STATE = create("block_state");
	public static final UpgradeEntry<BlockEntity> BLOCK_ENTITY = create("block_entity");
	public static final UpgradeEntry<BlockPos> BLOCK_POS = create("block_position", EntryCategory.BLOCK_POS, true);
	public static final UpgradeEntry<Direction> BLOCK_FACE = create("block_face");
	public static final UpgradeEntry<LivingEntity> LIVING = create("living_entity", EntryCategory.LIVING, true);
	public static final UpgradeEntry<Player> PLAYER = create("player", EntryCategory.PLAYER, true);
	public static final UpgradeEntry<Entity> TARGET_ENTITY = create("target_entity", EntryCategory.ENTITY);
	public static final UpgradeEntry<Vec3> TARGET_ENTITY_POS = create("target_entity_position", EntryCategory.POSITION);
	public static final UpgradeEntry<Vec3> INTERACTION_POS = create("interaction_position", EntryCategory.POSITION);
	public static final UpgradeEntry<ResourceLocation> UPGRADE_ID = create("upgrade_id", EntryCategory.UPGRADE_ID, true);
	public static final UpgradeEntry<ResourceLocation> PREV_UPGRADE_ID = create("prev_upgrade_id", EntryCategory.UPGRADE_ID);
	public static final UpgradeEntry<Integer> INT_VALUE = create("int", EntryCategory.INT_VALUE, true);
	public static final UpgradeEntry<Float> FLOAT_VALUE = create("float", EntryCategory.FLOAT_VALUE, true);
	
	/**<h1><b><u>Intended for use in results</u></b></h1>**/ public static final UpgradeEntry<Boolean> CANCELLED = create("cancelled");
	/**<h1><b><u>Intended for use in results</u></b></h1>**/ public static final UpgradeEntry<Boolean> CONSUMED = create("consumed");
	
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
		
		/**
		 * Constructs a new entry using the given name
		 * @param <T> The type to use for the entry
		 * @param name The name to use for the entry
		 * @return A new {@linkplain UpgradeEntry} with the given parameters
		 */
		public <T> UpgradeEntry<T> create(String name) {
			return new UpgradeEntry<>(new ResourceLocation(this.modId, name));
		}
		
		/**
		 * Constructs a new entry using the given category
		 * @param <T> The type to use for the entry
		 * @param name The name to use for the entry
		 * @param category The category this entry is assigned to
		 * 
		 * @return A new {@linkplain UpgradeEntry} with the given parameters
		 */
		public <T> UpgradeEntry<T> create(String name, EntryCategory<T> category) {
			return new UpgradeEntry<>(new ResourceLocation(this.modId, name), category);
		}
		
		/**
		 * Constructs a new entry using the given
		 * @param <T> The type to use for the entry
		 * @param name The name to use for the entry
		 * @param category The category this entry is assigned to
		 * @param isCategoryDefault If true, the category default is set to this value
		 * 
		 * @return A new {@linkplain UpgradeEntry} with the given parameters
		 */
		public <T> UpgradeEntry<T> create(String name, EntryCategory<T> category, boolean isCategoryDefault) {
			UpgradeEntry<T> entry = new UpgradeEntry<>(new ResourceLocation(this.modId, name), category);
			if (isCategoryDefault) category.setDefaultValue(entry);
			return entry;
		}
		
	}
	
}
