package io._3650.itemupgrader.upgrades;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import io._3650.itemupgrader.api.data.EntryCategory;
import net.minecraft.resources.ResourceLocation;

public class EntryCategoryManager {
	
	private static final Map<ResourceLocation, EntryCategory<?>> categories = Maps.newHashMap();
	
	public static void addCategory(ResourceLocation id, EntryCategory<?> category) {
		categories.put(id, category);
		ResourceLocation basicId = new ResourceLocation(id.getPath());
		if (!categories.containsKey(basicId)) categories.put(basicId, category); //only using first registered value
	}
	
	@Nullable
	public static EntryCategory<?> getCategory(ResourceLocation id) {
		return categories.get(id);
	}
	
}