package io._3650.itemupgrader.registry;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.upgrades.conditions.BlockIDUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.BlockTagUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.CompareNumbersUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.DamageSourceTypeUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.DamageSourceUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.EdibleUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.EyesInFluidUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.HasUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.ItemCooldownUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.PredicateUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.ResultSuccessUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.SneakingUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.TagVarBoolUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.TagVarFloatUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.TagVarIntUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.VerifyTimestampUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.compound.AndUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.compound.OrUpgradeCondition;
import io._3650.itemupgrader.upgrades.conditions.compound.XorUpgradeCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModUpgradeConditions {
	
	public static final DeferredRegister<UpgradeConditionSerializer<?>> CONDITIONS = DeferredRegister.create(ItemUpgraderRegistry.CONDITIONS, ItemUpgrader.MOD_ID);
	
	public static final RegistryObject<AndUpgradeCondition.Serializer> AND = CONDITIONS.register("and", () -> new AndUpgradeCondition.Serializer());
	public static final RegistryObject<OrUpgradeCondition.Serializer> OR = CONDITIONS.register("or", () -> new OrUpgradeCondition.Serializer());
	public static final RegistryObject<XorUpgradeCondition.Serializer> XOR = CONDITIONS.register("xor", () -> new XorUpgradeCondition.Serializer());
	
	public static final RegistryObject<PredicateUpgradeCondition.Serializer> PREDICATE = CONDITIONS.register("predicate", () -> new PredicateUpgradeCondition.Serializer());
	public static final RegistryObject<EyesInFluidUpgradeCondition.Serializer> EYES_IN_FLUID = CONDITIONS.register("eyes_in_fluid", () -> new EyesInFluidUpgradeCondition.Serializer());
	public static final RegistryObject<HasUpgradeCondition.Serializer> HAS_UPGRADE = CONDITIONS.register("has_upgrade", () -> new HasUpgradeCondition.Serializer());
	public static final RegistryObject<CompareNumbersUpgradeCondition.Serializer> COMPARE_NUMBERS = CONDITIONS.register("compare", () -> new CompareNumbersUpgradeCondition.Serializer());
	public static final RegistryObject<ItemCooldownUpgradeCondition.Serializer> COOLDOWN = CONDITIONS.register("cooldown", () -> new ItemCooldownUpgradeCondition.Serializer());
	public static final RegistryObject<TagVarBoolUpgradeCondition.Serializer> TAGVAR_BOOLEAN = CONDITIONS.register("tag_boolean", () -> new TagVarBoolUpgradeCondition.Serializer());
	public static final RegistryObject<TagVarIntUpgradeCondition.Serializer> TAGVAR_INT = CONDITIONS.register("tag_int", () -> new TagVarIntUpgradeCondition.Serializer());
	public static final RegistryObject<TagVarFloatUpgradeCondition.Serializer> TAGVAR_FLOAT = CONDITIONS.register("tag_float", () -> new TagVarFloatUpgradeCondition.Serializer());
	public static final RegistryObject<DamageSourceUpgradeCondition.Serializer> DAMAGE_SOURCE = CONDITIONS.register("damage_source", () -> new DamageSourceUpgradeCondition.Serializer());
	public static final RegistryObject<DamageSourceTypeUpgradeCondition.Serializer> DAMAGE_SOURCE_TYPE = CONDITIONS.register("damage_source_type", () -> new DamageSourceTypeUpgradeCondition.Serializer());
	public static final RegistryObject<BlockIDUpgradeCondition.Serializer> BLOCK_ID = CONDITIONS.register("block", () -> new BlockIDUpgradeCondition.Serializer());
	public static final RegistryObject<BlockTagUpgradeCondition.Serializer> BLOCK_TAG = CONDITIONS.register("block_tag", () -> new BlockTagUpgradeCondition.Serializer());
	public static final RegistryObject<VerifyTimestampUpgradeCondition.Serializer> VERIFY_TIMESTAMP = CONDITIONS.register("verify_timestamp", () -> new VerifyTimestampUpgradeCondition.Serializer());
	public static final RegistryObject<ResultSuccessUpgradeCondition.Serializer> RESULT_SUCCESS = CONDITIONS.register("success", () -> new ResultSuccessUpgradeCondition.Serializer());
	public static final RegistryObject<EdibleUpgradeCondition.Serializer> EDIBLE = CONDITIONS.register("edible", () -> new EdibleUpgradeCondition.Serializer());
	public static final RegistryObject<SneakingUpgradeCondition.Serializer> SNEAKING = CONDITIONS.register("sneaking", () -> new SneakingUpgradeCondition.Serializer());
	
}