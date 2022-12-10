package io._3650.itemupgrader_content.registry;

import io._3650.itemupgrader_content.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader_content.upgrades.results.special.AbsorbItemsUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.FallToFoodUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.PlayerDeathpointUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.PlayerSpawnpointUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.ReboundEntityUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.ReflectProjectileUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.SoulboundChargesUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.TellCoordsUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.TellTimeUpgradeResult;
import io._3650.itemupgrader_content.upgrades.results.special.TotemParticlesUpgradeResult;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeResults {
	
	public static final DeferredRegister<UpgradeResultSerializer<?>> RESULTS = DeferredRegister.create(ItemUpgraderRegistry.RESULTS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<FallToFoodUpgradeResult.Serializer> SPECIAL_FALL_TO_FOOD = RESULTS.register("internal_fall_to_food", () -> new FallToFoodUpgradeResult.Serializer());
	public static final RegistryObject<PlayerSpawnpointUpgradeResult.Serializer> SPECIAL_SPAWNPOINT = RESULTS.register("internal_spawnpoint", () -> new PlayerSpawnpointUpgradeResult.Serializer());
	public static final RegistryObject<PlayerDeathpointUpgradeResult.Serializer> SPECIAL_DEATHPOINT = RESULTS.register("internal_deathpoint", () -> new PlayerDeathpointUpgradeResult.Serializer());
	public static final RegistryObject<TellCoordsUpgradeResult.Serializer> SPECIAL_TELL_COORDS = RESULTS.register("internal_tell_coords", () -> new TellCoordsUpgradeResult.Serializer());
	public static final RegistryObject<TellTimeUpgradeResult.Serializer> SPECIAL_TELL_TIME = RESULTS.register("internal_tell_time", () -> new TellTimeUpgradeResult.Serializer());
	public static final RegistryObject<TotemParticlesUpgradeResult.Serializer> SPECIAL_TOTEM_PARTICLES = RESULTS.register("internal_totem_particles", () -> new TotemParticlesUpgradeResult.Serializer());
	public static final RegistryObject<ReflectProjectileUpgradeResult.Serializer> SPECIAL_PARRY = RESULTS.register("internal_parry", () -> new ReflectProjectileUpgradeResult.Serializer());
	public static final RegistryObject<ReboundEntityUpgradeResult.Serializer> SPECIAL_REBOUND = RESULTS.register("internal_rebound", () -> new ReboundEntityUpgradeResult.Serializer());
	public static final RegistryObject<AbsorbItemsUpgradeResult.Serializer> SPECIAL_ABSORB_ITEMS = RESULTS.register("internal_absorb_items", () -> new AbsorbItemsUpgradeResult.Serializer());
	public static final RegistryObject<SoulboundChargesUpgradeResult.Serializer> SPECIAL_SOULBOUND_CHARGES = RESULTS.register("internal_soulbound_charges", () -> new SoulboundChargesUpgradeResult.Serializer());
	
	
}