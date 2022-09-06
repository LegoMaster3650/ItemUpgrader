package io._3650.itemupgrader.api.data;

import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
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
 * @param <T> The type that this entry type corresponds to
 * @see UpgradeEventData
 * @see UpgradeEntrySet
 */
public class UpgradeEntry<T> {
	
	private final ResourceLocation name;
	
	/**
	 * Constructs a new {@linkplain UpgradeEntry} with the given name
	 * @param name The {@linkplain ResourceLocation} to identify this with
	 */
	public UpgradeEntry(ResourceLocation name) {
		this.name = name;
	}
	
	/**
	 * Gets the name of this entry
	 * @return The {@linkplain ResourceLocation} associated with this entry
	 */
	public ResourceLocation getName() {
		return this.name;
	}
	
	/**
	 * Just your usual {@linkplain Object#toString()} override
	 */
	@Override
	public String toString() {
		return "<upgrade entry " + this.name + ">";
	}
	
	public static final UpgradeEntry<LogicalSide> SIDE = create("dist");
	public static final UpgradeEntry<ItemStack> ITEM = create("itemstack");
	public static final UpgradeEntry<Entity> ENTITY = create("entity");
	public static final UpgradeEntry<Vec3> ORIGIN = create("origin");
	public static final UpgradeEntry<Level> LEVEL = create("level");
	public static final UpgradeEntry<EquipmentSlot> SLOT = create("equipment_slot");
	public static final UpgradeEntry<Entity> DAMAGER_ENTITY = create("damager_entity");
	public static final UpgradeEntry<Entity> DIRECT_DAMAGER = create("direct_damager");
	public static final UpgradeEntry<DamageSource> DAMAGE_SOURCE = create("damager");
	public static final UpgradeEntry<BlockState> BLOCK_STATE = create("block_state");
	public static final UpgradeEntry<BlockEntity> BLOCK_ENTITY = create("block_entity");
	public static final UpgradeEntry<Float> EXPLOSION_RADIUS = create("explosion_radius"); //for the sake of predicates and stuff
	public static final UpgradeEntry<LivingEntity> LIVING = create("living_entity");
	public static final UpgradeEntry<Player> PLAYER = create("player");
	public static final UpgradeEntry<ResourceLocation> UPGRADE_ID = create("upgrade_id");
	public static final UpgradeEntry<ResourceLocation> PREV_UPGRADE_ID = create("prev_upgrade_id");
	
	/**<h1><b><u>Intended for use in results</u></b></h1>**/ public static final UpgradeEntry<Boolean> CANCELLED = create("cancelled");
	
	private static <T> UpgradeEntry<T> create(String name) {
		return new UpgradeEntry<T>(ItemUpgraderRegistry.modRes(name));
	}
	
	/**
	 * Constructs a new entry using the given type, mod id, and name
	 * @param <T> The type to use for the entry
	 * @param modid The mod id to use for the entry
	 * @param name The name to use for the entry
	 * @return A new {@linkplain UpgradeEntry} with the given parameters
	 */
	public static <T> UpgradeEntry<T> create(String modid, String name) {
		return new UpgradeEntry<T>(new ResourceLocation(modid, name));
	}
	
}
