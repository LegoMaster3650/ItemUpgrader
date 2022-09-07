package io._3650.itemupgrader.api.serializer;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Serializer for upgrade results
 * @author LegoMaster3650
 *
 * @param <T> The {@linkplain UpgradeResult} subclass serialized by this serializer
 */
public abstract class UpgradeResultSerializer<T extends UpgradeResult> extends ForgeRegistryEntry<UpgradeResultSerializer<T>> {
	
	/**
	 * Constructs your subclass T using the given internals and json
	 * @param internals Will recieve {@linkplain IUpgradeInternals} which bundles together a bunch of data required for the result
	 * @param json Will recieve a {@linkplain JsonObject} containing the entire contents of the result in json
	 * @return The newly constructed {@linkplain UpgradeResult} subclass T
	 */
	public abstract T fromJson(IUpgradeInternals internals, JsonObject json);
	
	/**
	 * Serializes your result to a network buffer for synchronizing between server and client
	 * @param result The {@linkplain UpgradeResult} to serialize to the network
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize the result to
	 */
	public abstract void toNetwork(T result, FriendlyByteBuf buf);
	
	/**
	 * Deseralizes your result from a network buffer for synchronizing between server and client
	 * @param internals Will recieve {@linkplain IUpgradeInternals} which bundles together a bunch of data required for the result
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize the result from
	 * @return The newly constructed {@linkplain UpgradeResult} subclass T
	 */
	public abstract T fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf);
	
}