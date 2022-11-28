package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.upgrades.results.AreaDamageUpgradeResult;
import io._3650.itemupgrader.upgrades.results.AttributeUpgradeResult;
import io._3650.itemupgrader.upgrades.results.AutoSmeltUpgradeResult;
import io._3650.itemupgrader.upgrades.results.BlockParticleUpgradeResult;
import io._3650.itemupgrader.upgrades.results.CancelUpgradeResult;
import io._3650.itemupgrader.upgrades.results.CompoundUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ConditionalUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ConsumeUpgradeResult;
import io._3650.itemupgrader.upgrades.results.DamageUpgradeResult;
import io._3650.itemupgrader.upgrades.results.DisplayItemUpgradeResult;
import io._3650.itemupgrader.upgrades.results.DurabilityDamageUpgradeResult;
import io._3650.itemupgrader.upgrades.results.EffectUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ExplosionUpgradeResult;
import io._3650.itemupgrader.upgrades.results.GiveItemUpgradeResult;
import io._3650.itemupgrader.upgrades.results.HealUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ItemCooldownUpgradeResult;
import io._3650.itemupgrader.upgrades.results.LoadPositionUpgradeResult;
import io._3650.itemupgrader.upgrades.results.MagnetUpgradeResult;
import io._3650.itemupgrader.upgrades.results.PlaySoundUpgradeResult;
import io._3650.itemupgrader.upgrades.results.ProjectilePierceUpgradeResult;
import io._3650.itemupgrader.upgrades.results.RandomTickUpgradeResult;
import io._3650.itemupgrader.upgrades.results.RemoveItemUpgradeResult;
import io._3650.itemupgrader.upgrades.results.CommandUpgradeResult;
import io._3650.itemupgrader.upgrades.results.SavePositionUpgradeResult;
import io._3650.itemupgrader.upgrades.results.SaveTimestampUpgradeResult;
import io._3650.itemupgrader.upgrades.results.SetHealthUpgradeResult;
import io._3650.itemupgrader.upgrades.results.TagVarBoolUpgradeResult;
import io._3650.itemupgrader.upgrades.results.TagVarFloatUpgradeResult;
import io._3650.itemupgrader.upgrades.results.TagVarIntUpgradeResult;
import io._3650.itemupgrader.upgrades.results.UpgradeRemoveUpgradeResult;
import io._3650.itemupgrader.upgrades.results.WithEntriesUpgradeResult;
import io._3650.itemupgrader.upgrades.results.modify.AddEntryUpgradeResult;
import io._3650.itemupgrader.upgrades.results.modify.MultiplyEntryUpgradeResult;
import io._3650.itemupgrader.upgrades.results.modify.ResetDefaultItemUpgradeResult;
import io._3650.itemupgrader.upgrades.results.modify.SetNumberEntryUpgradeResult;
import io._3650.itemupgrader.upgrades.results.modify.UpdatePositionUpgradeResult;
import io._3650.itemupgrader.upgrades.results.modify.UpdateSlotItemUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.FallToFoodUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.PlayerDeathpointUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.PlayerSpawnpointUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.ReboundEntityUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.ReflectProjectileUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.TellCoordsUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.TellTimeUpgradeResult;
import io._3650.itemupgrader.upgrades.results.special.TotemParticlesUpgradeResult;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeResults {
	
	public static final DeferredRegister<UpgradeResultSerializer<?>> RESULTS = DeferredRegister.create(ItemUpgraderRegistry.RESULTS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<CancelUpgradeResult.Serializer> CANCEL = RESULTS.register("cancel", () -> new CancelUpgradeResult.Serializer());
	public static final RegistryObject<ConsumeUpgradeResult.Serializer> CONSUME = RESULTS.register("consume", () -> new ConsumeUpgradeResult.Serializer());
	public static final RegistryObject<AttributeUpgradeResult.Serializer> ATTRIBUTE = RESULTS.register("attribute", () -> new AttributeUpgradeResult.Serializer());
	public static final RegistryObject<EffectUpgradeResult.Serializer> EFFECT = RESULTS.register("effect", () -> new EffectUpgradeResult.Serializer());
	public static final RegistryObject<BlockParticleUpgradeResult.Serializer> BLOCK_PARTICLE = RESULTS.register("block_particle", () -> new BlockParticleUpgradeResult.Serializer());
	public static final RegistryObject<CommandUpgradeResult.Serializer> RUN_COMMAND = RESULTS.register("command", () -> new CommandUpgradeResult.Serializer());
	public static final RegistryObject<ItemCooldownUpgradeResult.Serializer> COOLDOWN = RESULTS.register("cooldown", () -> new ItemCooldownUpgradeResult.Serializer());
	public static final RegistryObject<TagVarBoolUpgradeResult.Serializer> TAGVAR_BOOLEAN = RESULTS.register("tag_boolean", () -> new TagVarBoolUpgradeResult.Serializer());
	public static final RegistryObject<TagVarIntUpgradeResult.Serializer> TAGVAR_INT = RESULTS.register("tag_int", () -> new TagVarIntUpgradeResult.Serializer());
	public static final RegistryObject<TagVarFloatUpgradeResult.Serializer> TAGVAR_FLOAT = RESULTS.register("tag_float", () -> new TagVarFloatUpgradeResult.Serializer());
	public static final RegistryObject<SavePositionUpgradeResult.Serializer> SAVE_POSITION = RESULTS.register("save_position", () -> new SavePositionUpgradeResult.Serializer());
	public static final RegistryObject<LoadPositionUpgradeResult.Serializer> LOAD_POSITION = RESULTS.register("load_position", () -> new LoadPositionUpgradeResult.Serializer());
	public static final RegistryObject<DamageUpgradeResult.Serializer> DAMAGE = RESULTS.register("damage", () -> new DamageUpgradeResult.Serializer());
	public static final RegistryObject<HealUpgradeResult.Serializer> HEAL = RESULTS.register("heal", () -> new HealUpgradeResult.Serializer());
	public static final RegistryObject<SetHealthUpgradeResult.Serializer> SET_HEALTH = RESULTS.register("set_health", () -> new SetHealthUpgradeResult.Serializer());
	public static final RegistryObject<PlaySoundUpgradeResult.Serializer> PLAY_SOUND = RESULTS.register("sound", () -> new PlaySoundUpgradeResult.Serializer());
	public static final RegistryObject<ExplosionUpgradeResult.Serializer> EXPLOSION = RESULTS.register("explosion", () -> new ExplosionUpgradeResult.Serializer());
	public static final RegistryObject<DurabilityDamageUpgradeResult.Serializer> DURABILITY = RESULTS.register("durability", () -> new DurabilityDamageUpgradeResult.Serializer());
	public static final RegistryObject<GiveItemUpgradeResult.Serializer> GIVE_ITEM = RESULTS.register("give_item", () -> new GiveItemUpgradeResult.Serializer());
	public static final RegistryObject<RemoveItemUpgradeResult.Serializer> REMOVE_ITEM = RESULTS.register("remove_item", () -> new RemoveItemUpgradeResult.Serializer());
	public static final RegistryObject<UpgradeRemoveUpgradeResult.Serializer> REMOVE_UPGRADE = RESULTS.register("remove_upgrade", () -> new UpgradeRemoveUpgradeResult.Serializer());
	public static final RegistryObject<RandomTickUpgradeResult.Serializer> RANDOM_TICK = RESULTS.register("random_tick", () -> new RandomTickUpgradeResult.Serializer());
	public static final RegistryObject<SaveTimestampUpgradeResult.Serializer> SAVE_TIMESTAMP = RESULTS.register("save_timestamp", () -> new SaveTimestampUpgradeResult.Serializer());
	public static final RegistryObject<ConditionalUpgradeResult.Serializer> CONDITIONAL = RESULTS.register("conditional", () -> new ConditionalUpgradeResult.Serializer());
	public static final RegistryObject<CompoundUpgradeResult.Serializer> COMPOUND = RESULTS.register("compound", () -> new CompoundUpgradeResult.Serializer());
	public static final RegistryObject<WithEntriesUpgradeResult.Serializer> WITH_ENTRIES = RESULTS.register("with_entries", () -> new WithEntriesUpgradeResult.Serializer());
	public static final RegistryObject<MagnetUpgradeResult.Serializer> MAGNET = RESULTS.register("magnet", () -> new MagnetUpgradeResult.Serializer());
	public static final RegistryObject<AutoSmeltUpgradeResult.Serializer> AUTOSMELT = RESULTS.register("autosmelt", () -> new AutoSmeltUpgradeResult.Serializer());
	public static final RegistryObject<DisplayItemUpgradeResult.Serializer> DISPLAY_ITEM = RESULTS.register("display_item", () -> new DisplayItemUpgradeResult.Serializer());
	public static final RegistryObject<AreaDamageUpgradeResult.Serializer> AREA_DAMAGE = RESULTS.register("area_damage", () -> new AreaDamageUpgradeResult.Serializer());
	public static final RegistryObject<ProjectilePierceUpgradeResult.Serializer> PROJETILE_PIERCE = RESULTS.register("pierce", () -> new ProjectilePierceUpgradeResult.Serializer());
	
	public static final RegistryObject<AddEntryUpgradeResult.Serializer> ADD_ENTRY = RESULTS.register("add_entry", () -> new AddEntryUpgradeResult.Serializer());
	public static final RegistryObject<MultiplyEntryUpgradeResult.Serializer> MULTIPLY_ENTRY = RESULTS.register("multiply_entry", () -> new MultiplyEntryUpgradeResult.Serializer());
	public static final RegistryObject<SetNumberEntryUpgradeResult.Serializer> SET_NUMBER = RESULTS.register("set_number", () -> new SetNumberEntryUpgradeResult.Serializer());
	
	public static final RegistryObject<UpdatePositionUpgradeResult.Serializer> UPDATE_POSITION = RESULTS.register("update_position", () -> new UpdatePositionUpgradeResult.Serializer());
	public static final RegistryObject<UpdateSlotItemUpgradeResult.Serializer> UPDATE_ITEM = RESULTS.register("update_item", () -> new UpdateSlotItemUpgradeResult.Serializer());
	public static final RegistryObject<ResetDefaultItemUpgradeResult.Serializer> RESET_ITEM = RESULTS.register("reset_item", () -> new ResetDefaultItemUpgradeResult.Serializer());
	
	public static final RegistryObject<FallToFoodUpgradeResult.Serializer> SPECIAL_FALL_TO_FOOD = RESULTS.register("internal_fall_to_food", () -> new FallToFoodUpgradeResult.Serializer());
	public static final RegistryObject<PlayerSpawnpointUpgradeResult.Serializer> SPECIAL_SPAWNPOINT = RESULTS.register("internal_spawnpoint", () -> new PlayerSpawnpointUpgradeResult.Serializer());
	public static final RegistryObject<PlayerDeathpointUpgradeResult.Serializer> SPECIAL_DEATHPOINT = RESULTS.register("internal_deathpoint", () -> new PlayerDeathpointUpgradeResult.Serializer());
	public static final RegistryObject<TellCoordsUpgradeResult.Serializer> SPECIAL_TELL_COORDS = RESULTS.register("internal_tell_coords", () -> new TellCoordsUpgradeResult.Serializer());
	public static final RegistryObject<TellTimeUpgradeResult.Serializer> SPECIAL_TELL_TIME = RESULTS.register("internal_tell_time", () -> new TellTimeUpgradeResult.Serializer());
	public static final RegistryObject<TotemParticlesUpgradeResult.Serializer> SPECIAL_TOTEM_PARTICLES = RESULTS.register("internal_totem_particles", () -> new TotemParticlesUpgradeResult.Serializer());
	public static final RegistryObject<ReflectProjectileUpgradeResult.Serializer> SPECIAL_PARRY = RESULTS.register("internal_parry", () -> new ReflectProjectileUpgradeResult.Serializer());
	public static final RegistryObject<ReboundEntityUpgradeResult.Serializer> SPECIAL_REBOUND = RESULTS.register("internal_rebound", () -> new ReboundEntityUpgradeResult.Serializer());
	
	
}