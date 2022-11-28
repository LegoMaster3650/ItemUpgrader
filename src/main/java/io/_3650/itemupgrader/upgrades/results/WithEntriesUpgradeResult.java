package io._3650.itemupgrader.upgrades.results;

import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeJsonHelper;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class WithEntriesUpgradeResult extends UpgradeResult {
	
	private final ImmutableSet<UpgradeEntry<?>> entries;
	private final ImmutableMultimap<UpgradeEntry<?>, UpgradeEntry<?>> convert;
	private final UpgradeResult result;
	
	public WithEntriesUpgradeResult(IUpgradeInternals internals, Set<UpgradeEntry<?>> entries, SetMultimap<UpgradeEntry<?>, UpgradeEntry<?>> convert, UpgradeResult result) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.combine(result.getRequiredData(), entry -> !entries.contains(entry) && !convert.containsValue(entry));
			for (var conversion : convert.entries()) if (!entries.contains(conversion.getKey())) builder.require(conversion.getKey());
		}));
		this.entries = ImmutableSet.copyOf(entries);
		this.convert = ImmutableMultimap.copyOf(convert);
		this.result = result;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		boolean pass = true;
		for (var entry : this.entries) pass &= data.hasEntry(entry);
		if (pass && this.convert.size() > 0) for (var conversion : this.convert.entries()) {
			Object value = data.getEntry(conversion.getKey());
			if (value != null && conversion.getValue().getType().isInstance(value)) {
				UpgradeEventData.InternalStuffIgnorePlease.forceSetEntry(data, conversion.getValue(), convertUnsafe(value));
			} else pass = false;
		}
		if (pass) result.execute(data);
		return pass;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(UpgradeTooltipHelper.result(this.result, stack));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<WithEntriesUpgradeResult> {
		
		@Override
		public WithEntriesUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			Set<UpgradeEntry<?>> entries = Sets.newIdentityHashSet();
			UpgradeJsonHelper.collectObjects(json.get("entries"), entryJsonMain -> {
				var entryJson = entryJsonMain.entrySet().iterator().next();
				ResourceLocation categoryId = new ResourceLocation(entryJson.getKey());
				EntryCategory<?> category = ItemUpgraderApi.getCategory(categoryId);
				if (category == null) throw new NoSuchElementException("Category " + categoryId + " does not exist.");
				if (!GsonHelper.isStringValue(entryJson.getValue())) throw new IllegalArgumentException("Category " + categoryId + " value is not a valid string: " + entryJson.getValue().toString());
				entries.add(category.getEntry(entryJson.getValue().getAsString()));
				return null;
			});
			SetMultimap<UpgradeEntry<?>, UpgradeEntry<?>> convert = MultimapBuilder.hashKeys().hashSetValues().build();
			if (json.has("convert")) UpgradeJsonHelper.collectObjects(json.get("convert"), conversion -> {
				JsonObject source = GsonHelper.getAsJsonObject(conversion, "source");
				String sourceString = source.keySet().iterator().next();
				ResourceLocation sourceKey = new ResourceLocation(sourceString);
				EntryCategory<?> sourceCategory = ItemUpgraderApi.getCategory(sourceKey);
				if (sourceCategory == null) throw new NoSuchElementException("Category " + sourceKey + " does not exist.");
				UpgradeEntry<?> sourceEntry = sourceCategory.getEntry(GsonHelper.getAsString(source, sourceString));
				JsonObject target = GsonHelper.getAsJsonObject(conversion, "target");
				String targetString = target.keySet().iterator().next();
				ResourceLocation targetKey = new ResourceLocation(targetString);
				EntryCategory<?> targetCategory = ItemUpgraderApi.getCategory(targetKey);
				if (targetCategory == null) throw new NoSuchElementException("Category " + targetKey + " does not exist.");
				UpgradeEntry<?> targetEntry = sourceCategory.getEntry(GsonHelper.getAsString(target, targetString));
				if (!targetCategory.zCheckIfSubclass(sourceEntry)) throw new IllegalArgumentException("Cannot convert from " + sourceEntry + " to " + targetEntry);
				convert.put(sourceEntry, targetEntry);
				return null;
			});
			UpgradeResult result = UpgradeSerializer.result(json.get("result"));
			return new WithEntriesUpgradeResult(internals, entries, convert, result);
		}
		
		@Override
		public void toNetwork(WithEntriesUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeInt(result.entries.size());
			for (var entry : result.entries) {
				buf.writeResourceLocation(entry.getCategory().getId());
				entry.toNetwork(buf);
			}
			buf.writeInt(result.convert.keySet().size());
			for (var source : result.convert.keySet()) {
				buf.writeResourceLocation(source.getCategory().getId());
				source.toNetwork(buf);
				var targets = result.convert.get(source);
				buf.writeInt(targets.size());
				for (var target : targets) {
					buf.writeResourceLocation(target.getCategory().getId());
					target.toNetwork(buf);
				}
			}
			UpgradeSerializer.resultToNetwork(result.result, buf);
		}
		
		@Override
		public WithEntriesUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			int entriesSize = buf.readInt();
			Set<UpgradeEntry<?>> entries = Sets.newIdentityHashSet();
			for (int i = 0; i < entriesSize; i++) {
				EntryCategory<?> category = ItemUpgraderApi.getCategory(buf.readResourceLocation());
				entries.add(category.fromNetwork(buf));
			}
			int convertSize = buf.readInt();
			SetMultimap<UpgradeEntry<?>, UpgradeEntry<?>> convert = MultimapBuilder.hashKeys().hashSetValues().build();
			for (int i = 0; i < convertSize; i++) {
				EntryCategory<?> sourceCategory = ItemUpgraderApi.getCategory(buf.readResourceLocation());
				UpgradeEntry<?> source = sourceCategory.fromNetwork(buf);
				int targetCount = buf.readInt();
				for (int j = 0; j < targetCount; j++) {
					EntryCategory<?> targetCategory = ItemUpgraderApi.getCategory(buf.readResourceLocation());
					UpgradeEntry<?> target = targetCategory.fromNetwork(buf);
					convert.put(source, target);
				}
			}
			UpgradeResult result = UpgradeSerializer.resultFromNetwork(buf);
			return new WithEntriesUpgradeResult(internals, entries, convert, result);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T convertUnsafe(Object value) {
		return (T) value;
	}
	
}