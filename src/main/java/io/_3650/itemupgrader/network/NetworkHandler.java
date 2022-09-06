package io._3650.itemupgrader.network;

import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
	
	private static final String PROTOCOL_VERSION = "1.0.0";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		ItemUpgraderRegistry.modRes("network"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals);
	
	private static int id = 0;
	
	public static void init() {
		INSTANCE.registerMessage(id++, UpdateItemUpgradesPacket.class, UpdateItemUpgradesPacket::encode, UpdateItemUpgradesPacket::decode, UpdateItemUpgradesPacket::handle);
	}
	
	public static <MSG> void sendToPlayer(ServerPlayer player, MSG msg) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
	}
	
}