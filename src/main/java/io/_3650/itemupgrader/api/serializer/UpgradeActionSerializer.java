package io._3650.itemupgrader.api.serializer;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class UpgradeActionSerializer<T extends UpgradeAction> extends ForgeRegistryEntry<UpgradeActionSerializer<T>> {
	
	public abstract UpgradeEntrySet providedData();
	
	public abstract T fromJson(IUpgradeInternals internals, JsonObject json);
	
	public abstract void toNetwork(T action, FriendlyByteBuf buf);
	
	public abstract T fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf);
	
}