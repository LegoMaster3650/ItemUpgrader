package io._3650.itemupgrader.api.serializer;

import java.util.Set;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Serializer class for upgrade actions
 * @author LegoMaster3650
 *
 * @param <T> The {@linkplain UpgradeAction} subclass serialized by this serializer
 */
public abstract class UpgradeActionSerializer<T extends UpgradeAction> extends ForgeRegistryEntry<UpgradeActionSerializer<T>> {
	
	/**
	 * Gets the entry data guaranteed to be provided by the serialized action
	 * @return An {@linkplain UpgradeEntrySet} containing every {@linkplain UpgradeEntry} guaranteed to be provided in {@linkplain UpgradeEventData} sent by this action
	 */
	public abstract UpgradeEntrySet getProvidedData();
	
	/**
	 * Constructs your subclass T using the given internals and json
	 * @param internals Will recieve {@linkplain IUpgradeInternals} which bundles together a bunch of data required for the action
	 * @param validSlots Will recieve a {@linkplain Set} of {@linkplain EquipmentSlot}s that the action is valid for.
	 * @param json Will recieve a {@linkplain JsonObject} containing the entire contents of the action in json
	 * @return The newly constructed {@linkplain UpgradeAction} subclass T
	 */
	public abstract T fromJson(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, JsonObject json);
	
	/**
	 * Serializes your action to a network buffer for synchronizing between server and client
	 * @param action The {@linkplain UpgradeAction} to serialize to the network
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize the action to
	 */
	public abstract void toNetwork(T action, FriendlyByteBuf buf);
	
	/**
	 * Deseralizes your action from a network buffer for synchronizing between server and client
	 * @param internals Will recieve {@linkplain IUpgradeInternals} which bundles together a bunch of data required for the action
	 * @param validSlots Will recieve a {@linkplain Set} of {@linkplain EquipmentSlot}s that the action is valid for. <b>MAY BE NULL</b>
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize the action from
	 * @return The newly constructed {@linkplain UpgradeAction} subclass T
	 */
	public abstract T fromNetwork(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, FriendlyByteBuf buf);
	
}