package io.legom.itemupgrader.registry;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.api.ItemUpgraderRegistry;
import io.legom.itemupgrader.api.serializer.UpgradeResultSerializer;
import io.legom.itemupgrader.upgrades.results.EffectUpgradeResult;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeResults {
	
	public static final DeferredRegister<UpgradeResultSerializer<?>> RESULTS = DeferredRegister.create(ItemUpgraderRegistry.RESULTS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<UpgradeResultSerializer<EffectUpgradeResult>> EFFECT = RESULTS.register("effect", () -> new EffectUpgradeResult.Serializer());
	
}