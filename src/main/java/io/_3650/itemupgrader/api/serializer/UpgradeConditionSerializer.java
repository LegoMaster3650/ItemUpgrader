package io._3650.itemupgrader.api.serializer;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class UpgradeConditionSerializer<T extends UpgradeCondition> extends ForgeRegistryEntry<UpgradeConditionSerializer<T>> {
	
	public abstract T fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json);
	
	public abstract void toNetwork(T condition, FriendlyByteBuf buf);
	
	public abstract T fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf);
	
}