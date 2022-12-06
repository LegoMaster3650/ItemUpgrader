package io._3650.itemupgrader_content.network;

import java.util.Locale;
import java.util.function.Supplier;

import io._3650.itemupgrader_content.client.ClientStuff;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record TellPlayerCoordsPacket(Vec3 pos) {
	
	public static void encode(TellPlayerCoordsPacket packet, FriendlyByteBuf buffer) {
		buffer.writeDouble(packet.pos.x).writeDouble(packet.pos.y).writeDouble(packet.pos.z);
	}
	
	public static TellPlayerCoordsPacket decode(FriendlyByteBuf buffer) {
		return new TellPlayerCoordsPacket(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
	}
	
	public static void handle(TellPlayerCoordsPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			int settings = Config.CLIENT.coordinateDisplayMode.get();
			String format = (settings < 3 ? "XYZ: " : "") + (settings % 2 == 0 ? "%.3f, %.5f, %.3f" : "%.3f / %.5f / %.3f");
			final Component msg = Component.literal(String.format(Locale.ROOT, format, packet.pos.x(), packet.pos.y(), packet.pos.z()));
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientStuff.playerActionBar(msg));
		});
		ctx.get().setPacketHandled(true);
	}
	
}