package io._3650.itemupgrader.network;

import java.text.DecimalFormat;
import java.util.function.Supplier;

import io._3650.itemupgrader.client.ClientStuff;
import io._3650.itemupgrader.registry.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record TellPlayerTimePacket(long worldTime) {
	
	private static final DecimalFormat TIME_FORMAT = new DecimalFormat("00");
	
	public static void encode(TellPlayerTimePacket packet, FriendlyByteBuf buffer) {
		buffer.writeLong(packet.worldTime);
	}
	
	public static TellPlayerTimePacket decode(FriendlyByteBuf buffer) {
		return new TellPlayerTimePacket(buffer.readLong());
	}
	
	public static void handle(TellPlayerTimePacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			int settings = Config.CLIENT.timeDisplayMode.get();
			int day = (int)(packet.worldTime / 24000L);
			int dayTime = (int)(packet.worldTime % 24000L);
			int hours = dayTime / 1000;
			int minutes = Mth.ceil((double)((dayTime % 1000) * 60) / 1000.0);
			MutableComponent msg;
			if (settings % 2 == 0) msg = new TranslatableComponent("tooltip.itemupgrader.internal_tell_time.time", TIME_FORMAT.format(hours), TIME_FORMAT.format(minutes));
			else {
				String ampm;
				if (hours < 12) ampm = "tooltip.itemupgrader.internal_tell_time.am";
				else {
					hours -= 12;
					ampm = "tooltip.itemupgrader.internal_tell_time.pm";
				}
				if (hours == 0) hours = 12;
				msg = new TranslatableComponent("tooltip.itemupgrader.internal_tell_time.time", TIME_FORMAT.format(hours), TIME_FORMAT.format(minutes)).append(new TranslatableComponent(ampm));
			}
			if (settings < 3) msg = new TranslatableComponent("tooltip.itemupgrader.internal_tell_time.day", day).append(msg);
			final MutableComponent finalMsg = msg; //have to make it final ;-;
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientStuff.playerActionBar(finalMsg));
		});
		ctx.get().setPacketHandled(true);
	}
	
}