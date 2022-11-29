package io._3650.itemupgrader.network;

import java.util.function.Supplier;

import io._3650.itemupgrader.client.ClientStuff;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record PickupItemPacket(int item, int target, int amount) {
	
	public static void encode(PickupItemPacket packet, FriendlyByteBuf buffer) {
		buffer.writeInt(packet.item).writeInt(packet.target).writeInt(packet.amount);
	}
	
	public static PickupItemPacket decode(FriendlyByteBuf buffer) {
		return new PickupItemPacket(buffer.readInt(), buffer.readInt(), buffer.readInt());
	}
	
	public static void handle(PickupItemPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientStuff.displayItemPickup(packet.item, packet.target, packet.amount));
		});
		ctx.get().setPacketHandled(true);
	}
	
}