package io._3650.itemupgrader.api.data;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents a {@linkplain Set} of Upgrade Entries for use in UpgradeEventData
 * @author LegoMaster3650
 * 
 * @see UpgradeEventData
 * @see UpgradeEntry
 */
public class UpgradeEntrySet {
	
	private final Set<UpgradeEntry<?>> required;
	
	private UpgradeEntrySet(Set<UpgradeEntry<?>> required) {
		this.required = ImmutableSet.copyOf(required);
	}
	
	/**
	 * @return The entries required by this entry set, which will cause errors to be thrown if not present
	 */
	public Set<UpgradeEntry<?>> getRequired() {
		return this.required;
	}
	
	private static final record VerifierCacheKey(UpgradeEntrySet holder, UpgradeEntrySet target) {}
	private static final Map<VerifierCacheKey, Boolean> VERIFICATION_CACHE = Maps.newHashMap();
	
	/**
	 * Verifies the target entry set against this one<br>
	 * <b>The target entry set is required to at least have every entry in this entry set.</b>
	 * @param target The target entry set to verify
	 * @return If the target entry set contains every entry in this set
	 */
	public boolean verify(@Nonnull UpgradeEntrySet target) {
		VerifierCacheKey verifierKey = new VerifierCacheKey(this, target);
		if (VERIFICATION_CACHE.containsKey(verifierKey)) return VERIFICATION_CACHE.get(verifierKey);
		else return Sets.difference(this.getRequired(), target.getRequired()).isEmpty();
	}
	
	/**
	 * Constructs a new UpgradeEntrySet with a new Builder pre-loaded this set's current required set
	 * @param consumer A consumer of a {@linkplain Builder} to add new required entries to
	 * @return An {@linkplain UpgradeEntrySet} with the builder applied
	 */
	public UpgradeEntrySet with(Consumer<Builder> consumer) {
		Builder builder = new Builder();
		builder.required.addAll(this.required);
		consumer.accept(builder);
		return builder.build();
	}
	
	/**
	 * Constructs a new UpgradeEntrySet combining this one with the provided one
	 * @param set The {@linkplain UpgradeEntrySet} to combine with the target
	 * @return A new {@linkplain UpgradeEntrySet} with the two sets combined
	 */
	public UpgradeEntrySet with(UpgradeEntrySet set) {
		Builder builder = new Builder();
		builder.required.addAll(this.required);
		builder.required.addAll(set.required);
		return builder.build();
	}
	
	/**
	 * Constructs a new UpgradeEntrySet combining this one with the provided ones
	 * @param sets Any number of {@linkplain UpgradeEntrySet}s to combine with the target
	 * @return A new {@linkplain UpgradeEntrySet} with all of the sets combined
	 */
	public UpgradeEntrySet withAll(UpgradeEntrySet... sets) {
		Builder builder = new Builder();
		builder.required.addAll(this.required);
		for (var set : sets) {
			builder.required.addAll(set.required);
		}
		return builder.build();
	}
	
	/**
	 * The builder for UpgradeEntrySets
	 * @author legom
	 *
	 */
	public static class Builder {
		
		private final Set<UpgradeEntry<?>> required = Sets.newIdentityHashSet();
		
		/**
		 * Constructs a new empty builder
		 */
		public Builder() {}
		
		/**
		 * Adds {@code entry} to the builder's required list
		 * @param entry The {@linkplain UpgradeEntry} to append to the builder's requirements
		 * @return The {@linkplain Builder} to chain more statements on
		 */
		public Builder add(UpgradeEntry<?> entry) {
			this.required.add(entry);
			return this;
		}
		
		/**
		 * Builds the {@linkplain Builder} and finalizes its' contents (unless you use {@linkplain UpgradeEntrySet#with(Consumer)} or {@linkplain UpgradeEntrySet#with(UpgradeEntrySet)}
		 * @return A new {@linkplain UpgradeEntrySet} with this builder's required list made immutable
		 */
		public UpgradeEntrySet build() {
			return new UpgradeEntrySet(this.required);
		}
		
	}
	
	/**
	 * Constructs a new empty builder
	 * @return A new empty {@linkplain Builder}
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Utility function for quickly creating a new builder and applying it
	 * @param consumer A consumer of a {@linkplain Builder} to add entries to
	 * @return The resulting {@linkplain UpgradeEntrySet} from the builder
	 */
	public static UpgradeEntrySet create(Consumer<Builder> consumer) {
		Builder builder = builder();
		consumer.accept(builder);
		return builder.build();
	}
	
	/* ==== SOME BASE STUFF ==== */
	/**Origin*/
	public static final UpgradeEntrySet ORIGIN = create(builder -> {
		builder.add(UpgradeEntry.ORIGIN);
	});
	/**Item*/
	public static final UpgradeEntrySet ITEM = create(builder -> {
		builder.add(UpgradeEntry.ITEM);
	});
	/**Slot*/
	public static final UpgradeEntrySet SLOT = create(builder -> {
		builder.add(UpgradeEntry.SLOT);
	});
	/**Slot, Item*/
	public static final UpgradeEntrySet SLOT_ITEM = ITEM.with(builder -> {
		builder.add(UpgradeEntry.SLOT);
	});
	/**Side*/
	public static final UpgradeEntrySet SIDED = create(builder -> {
		builder.add(UpgradeEntry.SIDE);
	});
	/**Side, Level*/
	public static final UpgradeEntrySet LEVEL = SIDED.with(builder -> {
		builder.add(UpgradeEntry.LEVEL);
	});
	/**Side, Level, Origin*/
	public static final UpgradeEntrySet LEVEL_ORIGIN = LEVEL.with(ORIGIN);
	
	/* ==== ENTITY STUFF ==== */
	/**Side, Level, Origin, Entity*/
	public static final UpgradeEntrySet ENTITY = LEVEL_ORIGIN.with(builder -> {
		builder.add(UpgradeEntry.ENTITY);
	});
	/**Side, Level, Origin, Entity, Item*/
	public static final UpgradeEntrySet ENTITY_ITEM = ENTITY.with(ITEM);
	/**Side, Level, Origin, Entity, Slot*/
	public static final UpgradeEntrySet ENTITY_SLOT = ENTITY.with(SLOT);
	/**Side, Level, Origin, Entity, Slot, Item*/
	public static final UpgradeEntrySet ENTITY_SLOT_ITEM = ENTITY_SLOT.with(ITEM);
	/**Side, Level, Origin, Entity, Living*/
	public static final UpgradeEntrySet LIVING = ENTITY.with(builder -> {
		builder.add(UpgradeEntry.LIVING);
	});
	/**Side, Level, Origin, Entity, Living, Item*/
	public static final UpgradeEntrySet LIVING_ITEM = LIVING.with(ITEM);
	/**Side, Level, Origin, Entity, Living, Slot*/
	public static final UpgradeEntrySet LIVING_SLOT = LIVING.with(SLOT);
	/**Side, Level, Origin, Entity, Living, Slot, Item*/
	public static final UpgradeEntrySet LIVING_SLOT_ITEM = LIVING_SLOT.with(ITEM);
	/**Side, Level, Origin, Entity, Living, Player*/
	public static final UpgradeEntrySet PLAYER = LIVING.with(builder -> {
		builder.add(UpgradeEntry.PLAYER);
	});
	/**Side, Level, Origin, Entity, Living, Player, Item*/
	public static final UpgradeEntrySet PLAYER_ITEM = PLAYER.with(ITEM);
	/**Side, Level, Origin, Entity, Living, Player, Slot*/
	public static final UpgradeEntrySet PLAYER_SLOT = PLAYER.with(SLOT);
	/**Side, Level, Origin, Entity, Living, Player, Slot, Item*/
	public static final UpgradeEntrySet PLAYER_SLOT_ITEM = PLAYER_SLOT.with(ITEM);
	
	/**Target Entity, Target Entity Position*/
	public static final UpgradeEntrySet TARGET_ENTITY = create(builder -> {
		builder.add(UpgradeEntry.TARGET_ENTITY).add(UpgradeEntry.TARGET_ENTITY_POS);
	});
	/**Side, Level, Origin, Target Entity, Target Entity Position*/
	public static final UpgradeEntrySet TARGET_ENTITY_EXTENDED = LEVEL_ORIGIN.with(TARGET_ENTITY);
	
	/* ==== UPGRADE STUFF ==== */
	/**Upgrade ID*/
	public static final UpgradeEntrySet UPGRADE_ID = create(builder -> {
		builder.add(UpgradeEntry.UPGRADE_ID);
	});
	/**Upgrade ID, Item*/
	public static final UpgradeEntrySet ITEM_UPGRADE_ID = ITEM.with(UPGRADE_ID);
	/**Previous Upgrade ID*/
	public static final UpgradeEntrySet PREV_UPGRADE_ID = create(builder -> {
		builder.add(UpgradeEntry.PREV_UPGRADE_ID);
	});
	/**Previous Upgrade ID, Item*/
	public static final UpgradeEntrySet ITEM_PREV_UPGRADE_ID = ITEM.with(PREV_UPGRADE_ID);
	/**Upgrade ID, Previous Upgrade ID*/
	public static final UpgradeEntrySet REPLACE_UPGRADE_IDS = UPGRADE_ID.with(PREV_UPGRADE_ID);
	/**Upgrade ID, Previous Upgrade ID*/
	public static final UpgradeEntrySet ITEM_REPLACE_UPGRADE_IDS = ITEM.with(REPLACE_UPGRADE_IDS);
	
	/* ==== BLOCK STUFF ==== */
	/**Block Position*/
	public static final UpgradeEntrySet BLOCK_POS = create(builder -> {
		builder.add(UpgradeEntry.BLOCK_POS);
	});
	/**Block State*/
	public static final UpgradeEntrySet BLOCK_STATE = create(builder -> {
		builder.add(UpgradeEntry.BLOCK_STATE);
	});
	/**Block Entity*/
	public static final UpgradeEntrySet BLOCK_ENTITY = create(builder -> {
		builder.add(UpgradeEntry.BLOCK_ENTITY);
	});
	/**Block Face*/
	public static final UpgradeEntrySet BLOCK_FACE = create(builder -> {
		builder.add(UpgradeEntry.BLOCK_FACE);
	});
	/**Block Pos, Block State*/
	public static final UpgradeEntrySet BLOCK_POS_STATE = BLOCK_POS.with(BLOCK_STATE);
	/**Block Pos, Block Entity*/
	public static final UpgradeEntrySet BLOCK_POS_ENTITY = BLOCK_POS.with(BLOCK_ENTITY);
	/**Block State, Block Entity*/
	public static final UpgradeEntrySet BLOCK_STATE_ENTITY = BLOCK_STATE.with(BLOCK_ENTITY);
	/**Block Pos, Block State, Block Entity*/
	public static final UpgradeEntrySet BLOCK_ENTITY_FULL = BLOCK_POS_STATE.with(BLOCK_ENTITY);
	/**Side, Level, Block Pos, Block State*/
	public static final UpgradeEntrySet LEVEL_BLOCK = BLOCK_POS_STATE.with(LEVEL);
	/**Side, Level, Block Pos, Block State, Block Entity*/
	public static final UpgradeEntrySet LEVEL_BLOCK_ENTITY = BLOCK_ENTITY_FULL.with(LEVEL);
	
	/* ==== AMALGAMATIONS ==== */
	/**Side, Level, Origin, Entity, Living, Player, Block Pos, Block State*/
	public static final UpgradeEntrySet PLAYER_LEVEL_BLOCK = PLAYER.with(LEVEL_BLOCK);
	/**Side, Level, Origin, Entity, Living, Player, Slot, Item, Block Pos, Block State, Block Face*/
	public static final UpgradeEntrySet PLAYER_BLOCK_INTERACTION = PLAYER_SLOT_ITEM.withAll(LEVEL_BLOCK, BLOCK_FACE);
	/**Side, Level, Origin, Entity, Living, Player, Slot, Item, Target Entity, Target Entity Position*/
	public static final UpgradeEntrySet PLAYER_ENTITY_INTERACTION = PLAYER_SLOT_ITEM.with(TARGET_ENTITY);
	
	//TEMPLATE: public static final UpgradeEntrySet 
}