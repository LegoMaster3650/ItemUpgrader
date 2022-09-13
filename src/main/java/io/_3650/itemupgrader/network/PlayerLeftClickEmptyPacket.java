package io._3650.itemupgrader.network;

import java.util.function.Supplier;

import io._3650.itemupgrader.event.ModEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;

public record PlayerLeftClickEmptyPacket(EquipmentSlot slot, boolean emptyStack) {
	
	public static void encode(PlayerLeftClickEmptyPacket packet, FriendlyByteBuf buffer) {
		buffer.writeEnum(packet.slot).writeBoolean(packet.emptyStack);
	}
	
	public static PlayerLeftClickEmptyPacket decode(FriendlyByteBuf buffer) {
		return new PlayerLeftClickEmptyPacket(buffer.readEnum(EquipmentSlot.class), buffer.readBoolean());
	}
	
	public static void handle(PlayerLeftClickEmptyPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			ModEvents.leftClickBase(packet.slot, player, packet.emptyStack);
		});
		ctx.get().setPacketHandled(true);
	}
	
}