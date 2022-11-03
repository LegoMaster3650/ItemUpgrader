package io._3650.itemupgrader.client;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientStuff {
	
	@Nullable
	public static Component getSubtitle(ResourceLocation soundId) {
		WeighedSoundEvents sound = Minecraft.getInstance().getSoundManager().getSoundEvent(soundId);
		if (sound != null) return sound.getSubtitle();
		else return null;
	}
	
	@SuppressWarnings("resource")
	public static void playerActionBar(Component msg) {
		Minecraft.getInstance().player.displayClientMessage(msg, true);
	}
	
}