package io._3650.itemupgrader.api.data;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * Represents a {@linkplain Set} of Upgrade Entries for use in UpgradeEventData
 * @author LegoMaster3650
 * 
 * @see UpgradeEventData
 * @see UpgradeEntry
 */
public class UpgradeEntrySet {
	
	private final ImmutableSet<UpgradeEntry<?>> required;
	private final ImmutableSet<UpgradeEntry<?>> provided;
	private final ImmutableSet<UpgradeEntry<?>> modified;
	
	private final ImmutableSet<UpgradeEntry<?>> forced;
	
	private UpgradeEntrySet(Set<UpgradeEntry<?>> required, Set<UpgradeEntry<?>> provided, Set<UpgradeEntry<?>> modified, Set<UpgradeEntry<?>> additionalProvided) {
		this.required = ImmutableSet.copyOf(required);
		this.provided = ImmutableSet.copyOf(provided);
		this.modified = ImmutableSet.copyOf(modified);
		this.forced = ImmutableSet.copyOf(additionalProvided);
	}
	
	/**
	 * @return The entries required by this entry set, which will cause errors to be thrown if not present
	 */
	public ImmutableSet<UpgradeEntry<?>> getRequired() {
		return this.required;
	}
	
	/**
	 * @return The entries provided by this entry set, which will cause errors to be thrown if not present
	 */
	public ImmutableSet<UpgradeEntry<?>> getProvided() {
		return this.provided;
	}
	
	/**
	 * @return The entries listed as modifiable by this entry set, which will cause errors to be thrown if not present
	 */
	public ImmutableSet<UpgradeEntry<?>> getModified() {
		return this.modified;
	}
	
	/**
	 * @return The entries forcefully provided by this entry set
	 */
	public Set<UpgradeEntry<?>> getForcedProvided() {
		return Set.copyOf(this.forced);
	}
	
	private static final Map<UpgradeEntrySet, Boolean> VERIFICATION_CACHE = Maps.newHashMap();
	
	/**
	 * Verifies the target entry set against this one<br>
	 * <b>The target entry set is required to provide at least every required entry in this entry set.</b>
	 * @param provided The target entry set to verify
	 * @return If the target entry set contains every entry in this set
	 */
	public boolean verify(@Nonnull UpgradeEntrySet provided) {
		if (VERIFICATION_CACHE.containsKey(provided)) return VERIFICATION_CACHE.get(provided);
		else {
			boolean diff = Sets.difference(this.required, provided.provided).isEmpty();
			diff = diff && Sets.difference(this.modified, provided.modified).isEmpty();
			VERIFICATION_CACHE.put(provided, diff);
			return diff;
		}
	}
	
	/**
	 * Verifies the target entry set against this one, ignoring cache and simply verifying difference<br>
	 * <b>The target entry set is required to provide at least every required entry in this entry set.</b>
	 * @param provided The target entry set to verify
	 * @return A {@linkplain SetView} of every missing required {@linkplain UpgradeEntry}
	 */
	public SetView<UpgradeEntry<?>> verifyDifference(UpgradeEntrySet provided) {
		return Sets.difference(this.required, provided.provided);
	}
	
	/**
	 * Constructs a new UpgradeEntrySet with a new Builder pre-loaded this set's current required set
	 * @param consumer A consumer of a {@linkplain Builder} to add new required entries to
	 * @return An {@linkplain UpgradeEntrySet} with the builder applied
	 */
	public UpgradeEntrySet with(Consumer<Builder> consumer) {
		Builder builder = new Builder(this);
		consumer.accept(builder);
		return builder.build();
	}
	
	/**
	 * Constructs a new UpgradeEntrySet combining this one with the provided one
	 * @param set The {@linkplain UpgradeEntrySet} to combine with the target
	 * @return A new {@linkplain UpgradeEntrySet} with the two sets combined
	 */
	public UpgradeEntrySet with(UpgradeEntrySet set) {
		return this.with(builder -> builder.combine(set));
	}
	
	/**
	 * Constructs a new UpgradeEntrySet combining this one with the provided ones
	 * @param sets Any number of {@linkplain UpgradeEntrySet}s to combine with the target
	 * @return A new {@linkplain UpgradeEntrySet} with all of the sets combined
	 */
	public UpgradeEntrySet withAll(UpgradeEntrySet... sets) {
		return this.with(builder -> {
			for (var set : sets) builder.combine(set);
		});
	}
	
	/**
	 * Fills in category values using a category set mapper
	 * @param mapperConsumer A {@linkplain Consumer} of a {@linkplain EntryCategorySet.Mapper Mapper} that will define all the values
	 * @return A new {@linkplain UpgradeEntrySet} with the categories defined.
	 */
	public UpgradeEntrySet fillCategories(Consumer<EntryCategorySet.Mapper> mapperConsumer) {
		EntryCategorySet.Mapper mapper = new EntryCategorySet.Mapper();
		mapperConsumer.accept(mapper);
		EntryCategorySet categories = mapper.freeze();
		return this.with(builder -> {
			for (var category : categories.getCategoryMap().keySet()) {
				var entry = categories.getEntry(category);
				if (entry != null) {
					if (categories.isRequired(category)) builder.require(entry);
					else builder.provide(entry);
				}
			}
		});
	}
	
	/**
	 * The builder for {@linkplain UpgradeEntrySet}s
	 */
	public static class Builder {
		
		private final Set<UpgradeEntry<?>> required = Sets.newIdentityHashSet();
		private final Set<UpgradeEntry<?>> provided = Sets.newIdentityHashSet();
		private final Set<UpgradeEntry<?>> modified = Sets.newIdentityHashSet();
		
		private final Set<UpgradeEntry<?>> additionalProvided = Sets.newIdentityHashSet();
		
		/**Constructs a new empty builder*/
		public Builder() {}
		
		/**Reverts the given entry set back to a builder*/
		private Builder(UpgradeEntrySet preset) {
			this.combine(preset);
		}
		/**
		 * Adds all the data from the given entry to this one
		 * @return The {@linkplain Builder} to chain more statements on
		 */
		public Builder combine(UpgradeEntrySet preset) {
			this.required.addAll(preset.required);
			this.provided.addAll(preset.provided);
			this.modified.addAll(preset.modified);
			this.additionalProvided.addAll(preset.forced);
			return this;
		}
		
		/**
		 * Adds {@code entry} to the builder's required AND provided list<br>
		 * Requires the given entry to be present in the target when calling {@linkplain UpgradeEntrySet#verify(UpgradeEntrySet)}<br>
		 * @param entry The {@linkplain UpgradeEntry} to append to the builder's requirements
		 * @return The {@linkplain Builder} to chain more statements on
		 * @see #provide(UpgradeEntry)
		 */
		public Builder require(UpgradeEntry<?> entry) {
			this.required.add(entry);
			this.provided.add(entry);
			return this;
		}
		
		/**
		 * Adds {@code entry} to the builder's provided set<br>
		 * Provides without requiring the entry for verification as the target in {@linkplain UpgradeEntrySet#verify(UpgradeEntrySet)}
		 * @param entry The {@linkplain UpgradeEntry} to append to the builder's provided entry set
		 * @return The {@linkplain Builder} to chain more statements on
		 */
		public Builder provide(UpgradeEntry<?> entry) {
			this.provided.add(entry);
			return this;
		}
		
		/**
		 * Adds {@code entry} to the builder's modified set<br>
		 * The modified list requires certain entries to be modifiable, useful for return data or cancellation
		 * @param entry The {@linkplain UpgradeEntry} to append to the builder's provided and modified entry sets
		 * @return The {@linkplain Builder} to chain more statements on
		 */
		public Builder modifiable(UpgradeEntry<?> entry) {
			this.modified.add(entry);
			return this;
		}
		
		/**
		 * Adds {@code entry} to the builder's forced and provided sets<br>
		 * The forced list will be ignored in upgrade event data builder verification, allowing data that will be added in later to be marked as present<br>
		 * @param entry The {@linkplain UpgradeEntry} to append builder's provided and forced entry sets
		 * @return The {@linkplain Builder} to chain more statements on
		 * @see #provide(UpgradeEntry)
		 */
		public Builder provideForce(UpgradeEntry<?> entry) {
			this.additionalProvided.add(entry);
			return this.provide(entry);
		}
		
		/**
		 * Builds the {@linkplain Builder} and finalizes its' contents (unless you use {@linkplain UpgradeEntrySet#with(Consumer)} or {@linkplain UpgradeEntrySet#with(UpgradeEntrySet)}
		 * @return A new {@linkplain UpgradeEntrySet} with this builder's required list made immutable
		 */
		public UpgradeEntrySet build() {
			return new UpgradeEntrySet(this.required, this.provided, this.modified, this.additionalProvided);
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
	/**NOTHING*/
	public static final UpgradeEntrySet EMPTY = builder().build();
	/**Position*/
	public static final UpgradeEntrySet POSITION = create(builder -> {
		builder.provide(UpgradeEntry.POSITION);
	});
	/**Interaction Position*/
	public static final UpgradeEntrySet INTERACTION_POS = create(builder -> {
		builder.require(UpgradeEntry.INTERACTION_POS);
	});
	/**[Item]*/
	public static final UpgradeEntrySet ITEM = create(builder -> {
		builder.provide(UpgradeEntry.ITEM);
	});
	/**Slot*/
	public static final UpgradeEntrySet SLOT = create(builder -> {
		builder.require(UpgradeEntry.SLOT);
	});
	/**Slot, [Item]*/
	public static final UpgradeEntrySet SLOT_ITEM = ITEM.with(builder -> {
		builder.require(UpgradeEntry.SLOT);
	});
	/**Side*/
	public static final UpgradeEntrySet SIDED = create(builder -> {
		builder.require(UpgradeEntry.SIDE);
	});
	/**Side, Level*/
	public static final UpgradeEntrySet LEVEL = SIDED.with(builder -> {
		builder.require(UpgradeEntry.LEVEL);
	});
	/**Side, Level, [Position]*/
	public static final UpgradeEntrySet LEVEL_POSITION = LEVEL.with(POSITION);
	/**Damage*/
	public static final UpgradeEntrySet DAMAGE = create(builder -> {
		builder.require(UpgradeEntry.DAMAGE);
	});
	/**Damage Source*/
	public static final UpgradeEntrySet DAMAGE_SOURCE = create(builder -> {
		builder.require(UpgradeEntry.DAMAGE_SOURCE);
	});
	/**Damage, Damage Source*/
	public static final UpgradeEntrySet DAMAGE_EVENT = DAMAGE.with(DAMAGE_SOURCE);
	/**Enchantment ID*/
	public static final UpgradeEntrySet ENCHANTMENT_ID = create(builder -> {
		builder.require(UpgradeEntry.ENCHANTMENT_ID);
	});
	/**Enchantment Level*/
	public static final UpgradeEntrySet ENCHANTMENT_LEVEL = create(builder -> {
		builder.require(UpgradeEntry.ENCHANTMENT_LEVEL);
	});
	/**Enchantment ID, Enchantment Level*/
	public static final UpgradeEntrySet ENCHANTMENT = ENCHANTMENT_ID.with(ENCHANTMENT_LEVEL);
	/**[Item], Enchantment ID, Enchantment Level*/
	public static final UpgradeEntrySet ITEM_ENCHANTMENT = ITEM.with(ENCHANTMENT);
	
	/* ==== ENTITY STUFF ==== */
	/**Side, Level, [Position], [Entity]*/
	public static final UpgradeEntrySet ENTITY = LEVEL_POSITION.with(builder -> {
		builder.provide(UpgradeEntry.ENTITY);
	});
	/**Side, Level, [Position], [Entity], [Item]*/
	public static final UpgradeEntrySet ENTITY_ITEM = ENTITY.with(ITEM);
	/**Side, Level, [Position], [Entity], Slot*/
	public static final UpgradeEntrySet ENTITY_SLOT = ENTITY.with(SLOT);
	/**Side, Level, [Position], [Entity], Slot, [Item]*/
	public static final UpgradeEntrySet ENTITY_SLOT_ITEM = ENTITY_SLOT.with(ITEM);
	/**Side, Level, [Position], [Entity], [Living]*/
	public static final UpgradeEntrySet LIVING = ENTITY.with(builder -> {
		builder.provide(UpgradeEntry.LIVING);
	});
	/**Side, Level, [Position], [Entity], [Living], [Item]*/
	public static final UpgradeEntrySet LIVING_ITEM = LIVING.with(ITEM);
	/**Side, Level, [Position], [Entity], [Living], Slot*/
	public static final UpgradeEntrySet LIVING_SLOT = LIVING.with(SLOT);
	/**Side, Level, [Position], [Entity], [Living], Slot, [Item]*/
	public static final UpgradeEntrySet LIVING_SLOT_ITEM = LIVING_SLOT.with(ITEM);
	/**Side, Level, [Position], [Entity], [Living], [Player]*/
	public static final UpgradeEntrySet PLAYER = LIVING.with(builder -> {
		builder.provide(UpgradeEntry.PLAYER);
	});
	/**Side, Level, [Position], [Entity], [Living], [Player], [Item]*/
	public static final UpgradeEntrySet PLAYER_ITEM = PLAYER.with(ITEM);
	/**Side, Level, [Position], [Entity], [Living], [Player], Slot*/
	public static final UpgradeEntrySet PLAYER_SLOT = PLAYER.with(SLOT);
	/**Side, Level, [Position], [Entity], [Living], [Player], Slot, [Item]*/
	public static final UpgradeEntrySet PLAYER_SLOT_ITEM = PLAYER_SLOT.with(ITEM);
	
	/**Target Entity, Target Entity Position, Interaction Position*/
	public static final UpgradeEntrySet TARGET_ENTITY = create(builder -> {
		builder.require(UpgradeEntry.TARGET_ENTITY).require(UpgradeEntry.TARGET_ENTITY_POS).require(UpgradeEntry.INTERACTION_POS);
	});
	/**Side, Level, [Position], Target Entity, Target Entity Position, Interaction Position*/
	public static final UpgradeEntrySet TARGET_ENTITY_EXTENDED = LEVEL_POSITION.with(TARGET_ENTITY);
	
	/* ==== UPGRADE STUFF ==== */
	/**Upgrade ID*/
	public static final UpgradeEntrySet UPGRADE_ID = create(builder -> {
		builder.require(UpgradeEntry.UPGRADE_ID);
	});
	/**Upgrade ID, Item*/
	public static final UpgradeEntrySet ITEM_UPGRADE_ID = ITEM.with(UPGRADE_ID);
	/**Previous Upgrade ID*/
	public static final UpgradeEntrySet PREV_UPGRADE_ID = create(builder -> {
		builder.require(UpgradeEntry.PREV_UPGRADE_ID);
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
		builder.require(UpgradeEntry.BLOCK_POS);
	});
	/**Block State*/
	public static final UpgradeEntrySet BLOCK_STATE = create(builder -> {
		builder.require(UpgradeEntry.BLOCK_STATE);
	});
	/**Block Entity*/
	public static final UpgradeEntrySet BLOCK_ENTITY = create(builder -> {
		builder.require(UpgradeEntry.BLOCK_ENTITY);
	});
	/**Block Face*/
	public static final UpgradeEntrySet BLOCK_FACE = create(builder -> {
		builder.require(UpgradeEntry.BLOCK_FACE);
	});
	/**[Block Pos], Block State*/
	public static final UpgradeEntrySet BLOCK_POS_STATE = BLOCK_POS.with(BLOCK_STATE);
	/**[Block Pos], Block Entity*/
	public static final UpgradeEntrySet BLOCK_POS_ENTITY = BLOCK_POS.with(BLOCK_ENTITY);
	/**Block State, Block Entity*/
	public static final UpgradeEntrySet BLOCK_STATE_ENTITY = BLOCK_STATE.with(BLOCK_ENTITY);
	/**[Block Pos], Block State, Block Entity*/
	public static final UpgradeEntrySet BLOCK_ENTITY_FULL = BLOCK_POS_STATE.with(BLOCK_ENTITY);
	/**Side, Level, [Block Pos], Block State*/
	public static final UpgradeEntrySet LEVEL_BLOCK = BLOCK_POS_STATE.with(LEVEL);
	/**Side, Level, [Block Pos], Block State, Block Entity*/
	public static final UpgradeEntrySet LEVEL_BLOCK_ENTITY = BLOCK_ENTITY_FULL.with(LEVEL);
	
	/* ==== AMALGAMATIONS ==== */
	/**Side, Level, Origin, [Entity], [Living], [Player], [Block Pos], Block State*/
	public static final UpgradeEntrySet PLAYER_LEVEL_BLOCK = PLAYER.with(LEVEL_BLOCK);
	/**Side, Level, Origin, [Entity], [Living], [Player], Slot, [Item], [Block Pos], Block State, Block Face, Interaction Position*/
	public static final UpgradeEntrySet PLAYER_BLOCK_INTERACTION = PLAYER_SLOT_ITEM.withAll(LEVEL_BLOCK, BLOCK_FACE, INTERACTION_POS);
	/**Side, Level, Origin, [Entity], [Living], [Player], Slot, [Item], Target Entity, Entity Position*/
	public static final UpgradeEntrySet PLAYER_ENTITY_INTERACTION = PLAYER_SLOT_ITEM.with(TARGET_ENTITY);
	/**Side, Level, [Position], [Entity], [Living], Slot, [Item], Damage, Damage Source*/
	public static final UpgradeEntrySet LIVING_DAMAGE = LIVING_SLOT_ITEM.with(DAMAGE_EVENT);
	/**Side, Level, [Position], Entity, Item, Block Pos, Block State, Block Drops*/
	public static final UpgradeEntrySet BLOCK_DROPS = UpgradeEntrySet.ENTITY.withAll(UpgradeEntrySet.ITEM, UpgradeEntrySet.BLOCK_POS_STATE).with(builder -> {
		builder.require(UpgradeEntry.ENTITY).require(UpgradeEntry.BLOCK_POS).require(UpgradeEntry.BLOCK_DROPS);
	});
	//TEMPLATE: public static final UpgradeEntrySet 
}