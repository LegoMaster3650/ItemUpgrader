package io._3650.itemupgrader.api.data;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * A collection of entry category values.<br>
 * You might be looking for the {@linkplain Mapper} variant.
 * 
 * @author LegoMaster3650
 * @see Mapper
 * @see EntryCategory
 */
public class EntryCategorySet {
	
	private final ImmutableMap<EntryCategory<?>, UpgradeEntry<?>> categories;
	private final ImmutableMap<EntryCategory<?>, Boolean> required;
	
	private EntryCategorySet(Map<EntryCategory<?>, UpgradeEntry<?>> categories, Map<EntryCategory<?>, Boolean> required) {
		this.categories = ImmutableMap.copyOf(categories);
		this.required = ImmutableMap.copyOf(required);
	}
	
	/**
	 * Checks if this category set is empty
	 * @return If this category set is empty
	 */
	public boolean isEmpty() {
		return this.categories.isEmpty();
	}
	
	/**
	 * Checks if this category set contains the given category
	 * @param category The {@linkplain EntryCategory} to check for
	 * @return If this category set contains the given category
	 */
	public boolean hasEntry(EntryCategory<?> category) {
		return this.categories.get(category) != null;
	}
	
	/**
	 * Gets the mapped entry for the given category
	 * @param <T> The type that the category corresponds to
	 * @param category The category to get the entry for
	 * @return The mapped entry if present, or the category default value if not
	 */
	@SuppressWarnings("unchecked") //its probably fine
	public <T> UpgradeEntry<T> getEntry(EntryCategory<T> category) {
		UpgradeEntry<T> entry = (UpgradeEntry<T>) this.categories.get(category);
		return entry == null ? category.getDefaultValue() : entry;
	}
	
	/**
	 * Checks if the given category is marked as required or not, defaulting to true if not specified
	 * @param category The {@linkplain EntryCategory} to check for
	 * @return If the given category is required
	 */
	public boolean isRequired(EntryCategory<?> category) {
		return this.required.getOrDefault(category, true);
	}
	
	/**
	 * Gets the internal immutable map of categories to entries
	 * @return The internal {@linkplain ImmutableMap}
	 */
	public ImmutableMap<EntryCategory<?>, UpgradeEntry<?>> getCategoryMap() {
		return this.categories;
	}
	
	/**
	 * A utility class for mapping {@linkplain EntryCategory categories} to {@linkplain UpgradeEntry entries}.
	 * @author LegoMaster3650
	 */
	public static class Mapper {
		
		private final Map<EntryCategory<?>, UpgradeEntry<?>> categories = Maps.newIdentityHashMap();
		private final Map<EntryCategory<?>, Boolean> required = Maps.newIdentityHashMap();
		
		/**
		 * Constructs a new empty mapper
		 */
		public Mapper() {}
		
		/**
		 * Sets the given category's value to the given entry
		 * @param <T> The type that the category corresponds to
		 * @param category The {@linkplain EntryCategory} to set the value for
		 * @param entry The {@linkplain UpgradeEntry} to use as the value
		 * @return The {@linkplain Mapper}
		 */
		public <T> Mapper set(EntryCategory<T> category, UpgradeEntry<? extends T> entry) {
			this.categories.put(category, entry == null ? category.getDefaultValue() : entry);
			if (category.hasParent()) this.set(category.getParent(), entry);
			return this;
		}
		
		/**
		 * Sets the given category's value to the given entry, marking it as provided rather than required
		 * @param <T> The type that the category corresponds to
		 * @param category The {@linkplain EntryCategory} to set the value for
		 * @param entry The {@linkplain UpgradeEntry} to use as the value
		 * @return The {@linkplain Mapper}
		 */
		public <T> Mapper setOptional(EntryCategory<T> category, UpgradeEntry<? extends T> entry) {
			this.categories.put(category, entry == null ? category.getDefaultValue() : entry);
			this.required.put(category, false);
			return this;
		}
		
		/**
		 * Freezes this mapper's values
		 * @return A new {@linkplain EntryCategorySet} with this {@linkplain Mapper}'s values frozen as {@linkplain ImmutableMap}s
		 */
		public EntryCategorySet freeze() {
			return new EntryCategorySet(this.categories, this.required);
		}
		
	}
	
}