package io.legom.itemupgrader.api.serializer;

import com.google.gson.JsonObject;

import io.legom.itemupgrader.api.type.UpgradeResult;
import io.legom.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class UpgradeResultSerializer<T extends UpgradeResult> extends ForgeRegistryEntry<UpgradeResultSerializer<T>> {
	
	public abstract T fromJson(IUpgradeInternals internals, JsonObject json);
	
	public abstract void toNetwork(T result, FriendlyByteBuf buf);
	
	public abstract T fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf);
	
}