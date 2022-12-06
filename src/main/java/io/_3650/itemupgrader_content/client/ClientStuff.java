package io._3650.itemupgrader_content.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class ClientStuff {
	
	@SuppressWarnings("resource")
	public static void playerActionBar(Component msg) {
		Minecraft.getInstance().player.displayClientMessage(msg, true);
	}
	
	@SuppressWarnings("resource")
	public static void displayItemPickup(int itemId, int targetId, int amount) {
		ClientLevel level = Minecraft.getInstance().level;
		Entity itemE = level.getEntity(itemId);
		if (itemE instanceof ItemEntity item) {
			Entity target = level.getEntity(targetId);
			level.playLocalSound(item.getX(), item.getY(), item.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (level.random.nextFloat() - level.random.nextFloat()) * 1.4F + 2.0F, false);
			Minecraft.getInstance().particleEngine.add(new ItemPickupParticle(Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().renderBuffers(), level, item, target));
			ItemStack stack = item.getItem();
			stack.shrink(amount);
			if (stack.isEmpty()) level.removeEntity(itemId, Entity.RemovalReason.DISCARDED);
		}
	}
	
}