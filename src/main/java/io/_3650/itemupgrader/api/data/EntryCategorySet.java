package io._3650.itemupgrader.api.data;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EntryCategorySet {
	
	private final ImmutableMap<EntryCategory<?>, UpgradeEntry<?>> categories;
	private final ImmutableMap<EntryCategory<?>, Boolean> required;
	
	private EntryCategorySet(Map<EntryCategory<?>, UpgradeEntry<?>> categories, Map<EntryCategory<?>, Boolean> required) {
		this.categories = ImmutableMap.copyOf(categories);
		this.required = ImmutableMap.copyOf(required);
	}
	
	public boolean isEmpty() {
		return this.categories.isEmpty();
	}
	
	public boolean hasEntry(EntryCategory<?> category) {
		return this.categories.get(category) != null;
	}
	
	@SuppressWarnings("unchecked") //its probably fine
	public <T> UpgradeEntry<T> getEntry(EntryCategory<T> category) {
		UpgradeEntry<T> entry = (UpgradeEntry<T>) this.categories.get(category);
		return entry == null ? category.getDefaultValue() : entry;
	}
	
	public boolean isRequired(EntryCategory<?> category) {
		return this.required.getOrDefault(category, true);
	}
	
	public ImmutableMap<EntryCategory<?>, UpgradeEntry<?>> getCategoryMap() {
		return this.categories;
	}
	
	public static class Builder {
		
		private final Set<EntryCategory<?>> categories = Sets.newIdentityHashSet();
		
		public Builder() {}
		
		public boolean isEmpty() {
			return this.categories.isEmpty();
		}
		
		public Builder add(EntryCategory<?> category) {
			this.categories.add(category);
			return this;
		}
		
		public Builder addAll(Builder builder) {
			this.categories.addAll(builder.categories);
			return this;
		}
		
		public static Builder copyOf(Builder oldBuilder) {
			Builder builder = new Builder();
			builder.categories.addAll(oldBuilder.categories);
			return builder;
		}
		
		public Set<EntryCategory<?>> getCategories() {
			return this.categories;
		}
		
		public Mapper build() {
			return new Mapper();
		}
		
	}
	
	public static class Mapper {
		
		private final Map<EntryCategory<?>, UpgradeEntry<?>> categories = Maps.newIdentityHashMap();
		private final Map<EntryCategory<?>, Boolean> required = Maps.newIdentityHashMap();
		
		public Mapper() {}
		
		public <T> Mapper set(EntryCategory<T> category, UpgradeEntry<? extends T> entry) {
			this.categories.put(category, entry == null ? category.getDefaultValue() : entry);
			return this;
		}
		
		public <T> Mapper setOptional(EntryCategory<T> category, UpgradeEntry<? extends T> entry) {
			this.categories.put(category, entry == null ? category.getDefaultValue() : entry);
			this.required.put(category, false);
			return this;
		}
		
		public EntryCategorySet freeze() {
			return new EntryCategorySet(this.categories, this.required);
		}
		
	}
	
}