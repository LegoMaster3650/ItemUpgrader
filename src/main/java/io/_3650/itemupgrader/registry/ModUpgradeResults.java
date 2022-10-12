package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.upgrades.results.AttributeUpgradeResult;
import io._3650.itemupgrader.upgrades.results.BlockParticleUpgradeResult;
import io._3650.itemupgrader.upgrades.results.CancelUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ConsumeUpgradeResult;
import io._3650.itemupgrader.upgrades.results.EffectUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ItemCooldownUpgradeResult;
import io._3650.itemupgrader.upgrades.results.RunCommandUpgradeResult;
import io._3650.itemupgrader.upgrades.results.TagVarBoolUpgradeResult;
import io._3650.itemupgrader.upgrades.results.TagVarFloatUpgradeResult;
import io._3650.itemupgrader.upgrades.results.TagVarIntUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.FallToFoodUpgradeResult;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeResults {
	
	public static final DeferredRegister<UpgradeResultSerializer<?>> RESULTS = DeferredRegister.create(ItemUpgraderRegistry.RESULTS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<CancelUpgradeResult.Serializer> CANCEL = RESULTS.register("cancel", () -> new CancelUpgradeResult.Serializer());
	public static final RegistryObject<ConsumeUpgradeResult.Serializer> CONSUME = RESULTS.register("consume", () -> new ConsumeUpgradeResult.Serializer());
	public static final RegistryObject<AttributeUpgradeResult.Serializer> ATTRIBUTE = RESULTS.register("attribute", () -> new AttributeUpgradeResult.Serializer());
	public static final RegistryObject<EffectUpgradeResult.Serializer> EFFECT = RESULTS.register("effect", () -> new EffectUpgradeResult.Serializer());
	public static final RegistryObject<BlockParticleUpgradeResult.Serializer> BLOCK_PARTICLE = RESULTS.register("block_particle", () -> new BlockParticleUpgradeResult.Serializer());
	public static final RegistryObject<RunCommandUpgradeResult.Serializer> RUN_COMMAND = RESULTS.register("command", () -> new RunCommandUpgradeResult.Serializer());
	public static final RegistryObject<ItemCooldownUpgradeResult.Serializer> COOLDOWN = RESULTS.register("cooldown", () -> new ItemCooldownUpgradeResult.Serializer());
	public static final RegistryObject<TagVarBoolUpgradeResult.Serializer> TAGVAR_BOOLEAN = RESULTS.register("tag_boolean", () -> new TagVarBoolUpgradeResult.Serializer());
	public static final RegistryObject<TagVarIntUpgradeResult.Serializer> TAGVAR_INT = RESULTS.register("tag_int", () -> new TagVarIntUpgradeResult.Serializer());
	public static final RegistryObject<TagVarFloatUpgradeResult.Serializer> TAGVAR_FLOAT = RESULTS.register("tag_float", () -> new TagVarFloatUpgradeResult.Serializer());
	
	public static final RegistryObject<FallToFoodUpgradeResult.Serializer> SPECIAL_FALL_TO_FOOD = RESULTS.register("internal_fall_to_food", () -> new FallToFoodUpgradeResult.Serializer());
	
}