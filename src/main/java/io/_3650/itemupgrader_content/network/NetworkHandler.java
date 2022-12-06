package io._3650.itemupgrader_content.network;

import io._3650.itemupgrader_content.ItemUpgrader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
	
	private static final String PROTOCOL_VERSION = "1.0.0";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(ItemUpgrader.MOD_ID, "network"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals);
	
	private static int id = 0;
	
	public static void init() {
		INSTANCE.registerMessage(id++, TellPlayerCoordsPacket.class, TellPlayerCoordsPacket::encode, TellPlayerCoordsPacket::decode, TellPlayerCoordsPacket::handle);
		INSTANCE.registerMessage(id++, TellPlayerTimePacket.class, TellPlayerTimePacket::encode, TellPlayerTimePacket::decode, TellPlayerTimePacket::handle);
		INSTANCE.registerMessage(id++, PickupItemPacket.class, PickupItemPacket::encode, PickupItemPacket::decode, PickupItemPacket::handle);
	}
	
	public static <MSG> void sendToPlayer(ServerPlayer player, MSG msg) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
	}
	
	public static <MSG> void sendToServer(MSG msg) {
		INSTANCE.sendToServer(msg);
	}
	
}