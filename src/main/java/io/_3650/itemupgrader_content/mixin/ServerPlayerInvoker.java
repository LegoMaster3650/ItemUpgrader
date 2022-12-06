package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public interface ServerPlayerInvoker {
	
	@Invoker("fudgeSpawnLocation")
	public abstract void callFudgeSpawnLocation(ServerLevel pLevel);
	
}