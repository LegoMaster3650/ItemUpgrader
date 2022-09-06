package io._3650.itemupgrader.client;

import com.mojang.blaze3d.platform.InputConstants;

import io._3650.itemupgrader.ItemUpgrader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;

public class ModKeybinds {
	
	public static KeyMapping showTooltipKey;
	
	public static void init() {
		showTooltipKey = registerKey("show_tooltip", KeyConflictContext.GUI, InputConstants.KEY_LSHIFT, MAIN_CATEGORY);
	}
	
	public static boolean isKeyPressed(KeyMapping key) {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue());
	}
	
	private static KeyMapping registerKey(String name, KeyConflictContext context, int keycode, String category) {
		KeyMapping keyMap = new KeyMapping("key." + ItemUpgrader.MOD_ID + "." + name, context, InputConstants.Type.KEYSYM, keycode, category);
		ClientRegistry.registerKeyBinding(keyMap);
		return keyMap;
	}
	
	private static final String MAIN_CATEGORY = "key.categories." + ItemUpgrader.MOD_ID;
	
}