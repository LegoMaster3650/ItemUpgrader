package io.legom.itemupgrader.network;

import java.util.Map;
import java.util.function.Supplier;

import io.legom.itemupgrader.upgrades.ItemUpgrade;
import io.legom.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record UpdateItemUpgradesPacket (Map<ResourceLocation, ItemUpgrade> upgrades) {
	
	public static void encode(UpdateItemUpgradesPacket packet, FriendlyByteBuf buffer) {
		buffer.writeMap(packet.upgrades, FriendlyByteBuf::writeResourceLocation, (buf, upgrade) -> upgrade.toNetwork(buf));
	}
	
	public static UpdateItemUpgradesPacket decode(FriendlyByteBuf buffer) {
		return new UpdateItemUpgradesPacket(buffer.readMap(FriendlyByteBuf::readResourceLocation, ItemUpgrade::fromNetwork));
	}
	
	public static void handle(UpdateItemUpgradesPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ItemUpgradeManager.INSTANCE.setUpgrades(packet.upgrades));
		});
		ctx.get().setPacketHandled(true);
	}
	
}