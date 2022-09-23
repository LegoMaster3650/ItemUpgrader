package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.upgrades.results.BlockParticleUpgradeResult;
import io._3650.itemupgrader.upgrades.results.CancelUpgradeResult;
import io._3650.itemupgrader.upgrades.results.EffectUpgradeResult;
import io._3650.itemupgrader.upgrades.results.RunCommandUpgradeResult;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeResults {
	
	public static final DeferredRegister<UpgradeResultSerializer<?>> RESULTS = DeferredRegister.create(ItemUpgraderRegistry.RESULTS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<CancelUpgradeResult.Serializer> CANCEL = RESULTS.register("cancel", () -> new CancelUpgradeResult.Serializer());
	public static final RegistryObject<EffectUpgradeResult.Serializer> EFFECT = RESULTS.register("effect", () -> new EffectUpgradeResult.Serializer());
	public static final RegistryObject<BlockParticleUpgradeResult.Serializer> BLOCK_PARTICLE = RESULTS.register("block_particle", () -> new BlockParticleUpgradeResult.Serializer());
	public static final RegistryObject<RunCommandUpgradeResult.Serializer> RUN_COMMAND = RESULTS.register("run_command", () -> new RunCommandUpgradeResult.Serializer());
	
}