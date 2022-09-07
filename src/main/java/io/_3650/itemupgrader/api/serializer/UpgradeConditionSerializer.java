package io._3650.itemupgrader.api.serializer;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Serializer class for upgrade conditions
 * @author LegoMaster3650
 *
 * @param <T> The {@linkplain UpgradeCondition} subclass serialized by this serializer
 */
public abstract class UpgradeConditionSerializer<T extends UpgradeCondition> extends ForgeRegistryEntry<UpgradeConditionSerializer<T>> {
	
	/**
	 * Constructs your subclass T using the given internals and json
	 * @param internals Will recieve {@linkplain IUpgradeInternals} which bundles together a bunch of data required for the condition
	 * @param inverted Whether or not the condition is inverted
	 * @param json Will recieve a {@linkplain JsonObject} containing the entire contents of the condition in json
	 * @return The newly constructed {@linkplain UpgradeCondition} subclass T
	 */
	public abstract T fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json);
	
	/**
	 * Serializes your condition to a network buffer for synchronizing between server and client
	 * @param condition The {@linkplain UpgradeCondition} to serialize to the network
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize the condition to
	 */
	public abstract void toNetwork(T condition, FriendlyByteBuf buf);
	
	/**
	 * Deseralizes your condition from a network buffer for synchronizing between server and client
	 * @param internals Will recieve {@linkplain IUpgradeInternals} which bundles together a bunch of data required for the condition
	 * @param inverted Whether or not the condition is inverted
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize the condition from
	 * @return The newly constructed {@linkplain UpgradeCondition} subclass T
	 */
	public abstract T fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf);
	
}