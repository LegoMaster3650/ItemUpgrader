package io._3650.itemupgrader.api.data;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

/**
 * Main data holder for event data in ItemUpgrader<br>
 * This system was made due to the mod's datapack-oriented nature and the need to establish consistancy.<br>
 * <br>
 * There are two maps of entries present, the {@code entries} map and the {@code results} map.<br>
 * The {@code entries} map is immutable and contains the data passed into this event initially.<br>
 * The {@code results} map is meant to hold return values and thus can be modified. 
 * @author LegoMaster3650
 * 
 * @see Builder
 * @see io._3650.itemupgrader.api.ItemUpgraderApi
 */
public class UpgradeEventData {
	
	private final UpgradeEntrySet entrySet;
	private final Map<UpgradeEntry<?>, Object> entries;
	private final Map<UpgradeEntry<?>, Object> results;
	
	private UpgradeEventData(UpgradeEntrySet entrySet, Map<UpgradeEntry<?>, Object> entries, Map<UpgradeEntry<?>, Object> baseResults) {
		this.entrySet = entrySet;
		this.entries = ImmutableMap.copyOf(entries);
		this.results = baseResults;
	}
	
	/**
	 * Gets the entry set this event data was built with and verified against.
	 * @return This event data's entry set
	 */
	public UpgradeEntrySet getEntrySet() {
		return this.entrySet;
	}
	
	/**
	 * Checks if the event data contains an entry for this type
	 * @param entry The {@linkplain UpgradeEntry} type to check for
	 * @return If the entry type is present
	 */
	public boolean hasEntry(UpgradeEntry<?> entry) {
		return this.entries.containsKey(entry);
	}
	
	/**
	 * Gets an entry value for this event, erroring if null or missing
	 * @param <T> The data type provided by this entry
	 * @param entry The {@linkplain UpgradeEntry} type to get
	 * @return The value of the entry if present
	 * @throws NoSuchElementException If the entry isn't present
	 * @see #getEntryOrNull(UpgradeEntry)
	 * @see #getOptional(UpgradeEntry)
	 */
	@Nonnull
	public <T> T getEntry(UpgradeEntry<T> entry) throws NoSuchElementException {
		T t = this.getEntryOrNull(entry);
		if (t == null) {
			throw new NoSuchElementException("Upgrade event missing entry " + entry.getName().toString());
		} else {
			return t;
		}
	}
	
	/**
	 * Gets an entry value for this event, returning null if missing
	 * @param <T> The data type provided by this entry
	 * @param entry The {@linkplain UpgradeEntry} type to get
	 * @return The value of the entry if present, or {@code null} if not
	 */
	@SuppressWarnings("unchecked") //Pretty sure it's type safe enough so I'll use the forbidden suppression
	@Nullable
	public <T> T getEntryOrNull(UpgradeEntry<T> entry) {
		return (T) this.entries.get(entry);
	}
	
	/**
	 * Gets an optional of this entry's value which is empty if not present
	 * @param <T> The data type provided by this entry
	 * @param entry The {@linkplain UpgradeEntry} type to get
	 * @return An {@linkplain Optional} of the entry's value if present
	 */
	public <T> Optional<T> getOptional(UpgradeEntry<T> entry) {
		return Optional.ofNullable(this.getEntryOrNull(entry));
	}
	
	/**
	 * Checks if the event results allow this type
	 * @param type The {@linkplain UpgradeEntry} type to check for
	 * @return If the event results allow this type
	 */
	public boolean allowsResultType(UpgradeEntry<?> type) {
		return this.results.containsKey(type);
	}
	
	/**
	 * Sets a result for this event if it allows this type
	 * @param <T> The data type held by this result
	 * @param entry The {@linkplain UpgradeEntry} type to set
	 * @param result The data to store for this result
	 * @return Whether the result was permitted to be set or not
	 */
	public <T> boolean setResult(UpgradeEntry<T> entry, T result) {
		if (this.allowsResultType(entry)) {
			this.results.put(entry, result);
			return true;
		} else return false;
	}
	
	/**
	 * Checks if a VALUE is present for this entry type
	 * @param entry The {@linkplain UpgradeEntry} type to check
	 * @return Whether the result specified has a value
	 * @see #allowsResultType(UpgradeEntry)
	 */
	public boolean hasResultValue(UpgradeEntry<?> entry) {
		return this.results.get(entry) != null;
	}
	
	/**
	 * Gets a result value for this event, erroring if null or missing
	 * @param <T> The data type held by this result
	 * @param entry The {@linkplain UpgradeEntry} type to get
	 * @return The value of the result if present
	 * @throws NoSuchElementException If the result isn't present
	 * @see #getResultOrNull(UpgradeEntry)
	 * @see #getResultOptional(UpgradeEntry)
	 */
	@Nonnull
	public <T> T getResult(UpgradeEntry<T> entry) throws NoSuchElementException {
		T t = this.getResultOrNull(entry);
		if (t == null) {
			throw new NoSuchElementException("Upgrade event missing result " + entry.getName().toString());
		} else {
			return t;
		}
	}
	
	/**
	 * Gets a result value for this event, returning null if missing
	 * @param <T> The data type provided by this result
	 * @param entry The {@linkplain UpgradeEntry} type to get
	 * @return The value of the result if present, or {@code null} if not
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T getResultOrNull(UpgradeEntry<T> entry) {
		return (T) this.results.get(entry);
	}
	
	/**
	 * Gets an optional of this result's value which is empty if not present
	 * @param <T> The data type provided by this result
	 * @param entry The {@linkplain UpgradeEntry} type to get
	 * @return An {@linkplain Optional} of the result's value if present
	 */
	public <T> Optional<T> getResultOptional(UpgradeEntry<T> entry) {
		return Optional.ofNullable(this.getResultOrNull(entry));
	}
	
	/**
	 * A utility function to quickly check if this event can be cancelled
	 * @return If the event is cancellable
	 */
	public boolean isCancellable() {
		return this.allowsResultType(UpgradeEntry.CANCELLED);
	}
	
	/**
	 * A utility function to quickly cancel the event if it is cancellable
	 * @return If the cancelled value was successfully set
	 */
	public boolean cancel() {
		return this.setResult(UpgradeEntry.CANCELLED, true);
	}
	
	/**
	 * A utility function to quickly check if the event is cancelled
	 * @return If the event is cancelled
	 */
	public boolean isCancelled() {
		return this.getResultOptional(UpgradeEntry.CANCELLED).orElse(false);
	}
	
	/**
	 * The builder for {@linkplain UpgradeEventData}
	 * @author LegoMaster3650
	 *
	 */
	public static class Builder {
		
		private final Map<UpgradeEntry<?>, Object> entries = Maps.newIdentityHashMap();
		private final Map<UpgradeEntry<?>, Object> results = Maps.newIdentityHashMap();
		
		/**
		 * Constructs a builder with the {@link UpgradeEntry#ITEM} property set
		 * @param stack
		 */
		public Builder(ItemStack stack) {
			this.entry(UpgradeEntry.ITEM, stack);
		}
		
		/**
		 * Constructs a builder with following properties automatically determined:<br>
		 * {@linkplain UpgradeEntry#ITEM}<br>
		 * {@linkplain UpgradeEntry#SLOT}<br>
		 * {@linkplain UpgradeEntry#LIVING}<br>
		 * {@linkplain UpgradeEntry#ENTITY}<br>
		 * {@linkplain UpgradeEntry#ORIGIN}<br>
		 * {@linkplain UpgradeEntry#LEVEL}<br>
		 * {@linkplain UpgradeEntry#SIDE}
		 * @param living A {@linkplain LivingEntity} to use for context
		 * @param slot An {@linkplain EquipmentSlot} to use for context
		 */
		public Builder(LivingEntity living, EquipmentSlot slot) {
			Level level = living.getLevel();
			this.entry(UpgradeEntry.ITEM, living.hasItemInSlot(slot) ? living.getItemBySlot(slot) : ItemStack.EMPTY)
				.entry(UpgradeEntry.SLOT, slot)
				.entry(UpgradeEntry.LIVING, living)
				.entry(UpgradeEntry.ENTITY, living)
				.entry(UpgradeEntry.ORIGIN, living.position())
				.entry(UpgradeEntry.LEVEL, level)
				.entry(UpgradeEntry.SIDE, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
		}
		
		/**
		 * Constructs a builder with following properties automatically determined:<br>
		 * {@linkplain UpgradeEntry#LIVING}<br>
		 * {@linkplain UpgradeEntry#ENTITY}<br>
		 * {@linkplain UpgradeEntry#ORIGIN}<br>
		 * {@linkplain UpgradeEntry#LEVEL}<br>
		 * {@linkplain UpgradeEntry#SIDE}
		 * @param living A {@linkplain LivingEntity} to use for context
		 */
		public Builder(LivingEntity living) {
			Level level = living.getLevel();
			this.entry(UpgradeEntry.LIVING, living)
				.entry(UpgradeEntry.ENTITY, living)
				.entry(UpgradeEntry.ORIGIN, living.position())
				.entry(UpgradeEntry.LEVEL, level)
				.entry(UpgradeEntry.SIDE, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
		}
		
		/**
		 * Constructs a builder with following properties automatically determined:<br>
		 * {@linkplain UpgradeEntry#ENTITY}<br>
		 * {@linkplain UpgradeEntry#ORIGIN}<br>
		 * {@linkplain UpgradeEntry#LEVEL}<br>
		 * {@linkplain UpgradeEntry#SIDE}
		 * @param entity An {@linkplain Entity} to use for context
		 */
		public Builder(Entity entity) {
			Level level = entity.getLevel();
			this.entry(UpgradeEntry.ENTITY, entity)
				.entry(UpgradeEntry.ORIGIN, entity.position())
				.entry(UpgradeEntry.LEVEL, level)
				.entry(UpgradeEntry.SIDE, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
		}
		
		/**
		 * Constructs a builder with following properties automatically determined:<br>
		 * {@linkplain UpgradeEntry#LEVEL}<br>
		 * {@linkplain UpgradeEntry#SIDE}
		 * @param level A {@linkplain Level} to use for context
		 */
		public Builder(Level level) {
			this.entry(UpgradeEntry.LEVEL, level)
				.entry(UpgradeEntry.SIDE, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
		}
		
		/**
		 * Constructs an empty builder
		 */
		public Builder() {}
		
		/**
		 * Adds the given entry to the builder
		 * @param <T> The data type provided by this entry
		 * @param entry The {@linkplain UpgradeEntry} type to set
		 * @param value The data to store for this entry
		 * @return This builder
		 */
		public <T> Builder entry(UpgradeEntry<T> entry, T value) {
			this.entries.put(entry, value);
			return this;
		}
		
		/**
		 * Adds the given entry to the builder if not null
		 * @param <T> The data type provided by this entry
		 * @param entry The {@linkplain UpgradeEntry} type to set
		 * @param value The data to store for this entry
		 * @return This builder
		 */
		public <T> Builder optionalEntry(UpgradeEntry<T> entry, T value) {
			if (value == null) this.entries.remove(entry);
			else this.entries.put(entry, value);
			return this;
		}
		
		/**
		 * Adds the given result to the builder
		 * @param <T> The data type provided by this result
		 * @param entry The {@linkplain UpgradeEntry} type to set
		 * @param value The data to store for this result
		 * @return This builder
		 */
		public <T> Builder result(UpgradeEntry<T> entry, T defaultValue) {
			this.results.put(entry, defaultValue);
			return this;
		}
		
		/**
		 * Adds the given result to the builder if a condition is met
		 * @param <T> The data type provided by this result
		 * @param condition A boolean determining whether or not to add the result
		 * @param entry The {@linkplain UpgradeEntry} type to set
		 * @param value The data to store for this result
		 * @return This builder
		 */
		public <T> Builder resultIf(boolean condition, UpgradeEntry<T> entry, T defaultValue) {
			if (condition) this.results.put(entry, defaultValue);
			return this;
		}
		
		/**
		 * Utility function to quickly add the cancellable result to this event
		 * @return This builder
		 */
		public Builder cancellable() {
			return this.cancellable(false);
		}
		
		/**
		 * Utility function to quickly add the cancellable result to this event if {@code condition} is true
		 * @param condition A boolean determining whether or not to add the result
		 * @return This builder
		 */
		public Builder cancellableIf(boolean condition) {
			return this.cancellableIf(condition, false);
		}
		
		/**
		 * Utility function to quickly add the cancellable result with a default value to this event
		 * @param defaultValue Whether or not the event is cancelled by default
		 * @return This builder
		 */
		public Builder cancellable(boolean defaultValue) {
			return this.result(UpgradeEntry.CANCELLED, defaultValue);
		}
		
		/**
		 * Utility function to quickly add the cancellable result to this event with a default value if {@code condition} is true
		 * @param condition A boolean determining whether or not to add the result
		 * @param defaultValue Whether or not the event is cancelled by default
		 * @return This builder
		 */
		public Builder cancellableIf(boolean condition, boolean defaultValue) {
			return this.resultIf(condition, UpgradeEntry.CANCELLED, defaultValue);
		}
		
		/**
		 * Builds the builder against the given entry set, erroring if the entry set's required parameters aren't present
		 * @param entrySet An {@linkplain UpgradeEntrySet} of the required parameters for this entry
		 * @return The resulting {@linkplain UpgradeEventData} if no errors occur
		 * @throws IllegalStateException If one or more required entries in the entry set are not present
		 */
		public UpgradeEventData build(UpgradeEntrySet entrySet) throws IllegalStateException {
			Set<UpgradeEntry<?>> test = Sets.difference(entrySet.getRequired(), this.entries.keySet());
			if (!test.isEmpty()) {
				Iterator<UpgradeEntry<?>> iter = test.iterator();
				String errStr = "";
				while (iter.hasNext()) errStr = errStr + ", " + iter.next();
				throw new IllegalStateException("Missing required entries: [" + errStr + "]");
			} else {
				return new UpgradeEventData(entrySet, this.entries, this.results);
			}
		}
		
	}
	
	/**
	 * Constructs an empty builder
	 * @return An empty {@linkplain Builder}
	 */
	public static Builder builder() {
		return new Builder();
	}
	
}