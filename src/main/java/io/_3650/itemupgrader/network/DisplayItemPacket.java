package io._3650.itemupgrader.network;

import java.util.function.Supplier;

import io._3650.itemupgrader.client.ClientStuff;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record DisplayItemPacket(ItemStack stack) {
	
	public static void encode(DisplayItemPacket packet, FriendlyByteBuf buffer) {
		buffer.writeItemStack(packet.stack, false);
	}
	
	public static DisplayItemPacket decode(FriendlyByteBuf buffer) {
		return new DisplayItemPacket(buffer.readItem());
	}
	
	public static void handle(DisplayItemPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientStuff.displayItemActivation(packet.stack));
		});
		ctx.get().setPacketHandled(true);
	}
	
}