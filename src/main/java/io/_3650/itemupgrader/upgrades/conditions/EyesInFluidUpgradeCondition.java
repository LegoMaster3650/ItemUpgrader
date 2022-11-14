package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class EyesInFluidUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<Entity> entityEntry;
	private final ResourceLocation fluidId;
	private final TagKey<Fluid> fluid;
	private final String fluidKey;
	
	public EyesInFluidUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<Entity> entityEntry, ResourceLocation fluidId, String fluidKey) {
		super(internals, inverted, UpgradeEntrySet.create(builder -> {
			builder.require(entityEntry);
		}));
		this.entityEntry = entityEntry;
		this.fluidId = fluidId;
		this.fluid = //net.minecraft.tags.FluidTags.create(fluidId); //Using registry for now
				ForgeRegistries.FLUIDS.tags().createTagKey(fluidId);
		this.fluidKey = fluidKey;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		return data.getEntry(this.entityEntry).isEyeInFluid(this.fluid);
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TranslatableComponent(this.fluidKey));
	}
	
	@Override
	public Serializer getSerializer() {
		return new Serializer();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<EyesInFluidUpgradeCondition> {
		
		@Override
		public EyesInFluidUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
			String fluidKey = "fluid." + GsonHelper.getAsString(json, "fluidKey", ComponentHelper.keyFormat(fluidId));
			return new EyesInFluidUpgradeCondition(internals, inverted, entityEntry, fluidId, fluidKey);
		}
		
		@Override
		public void toNetwork(EyesInFluidUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.entityEntry.toNetwork(buf);
			buf.writeResourceLocation(condition.fluidId);
			buf.writeUtf(condition.fluidKey);
		}
		
		@Override
		public EyesInFluidUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			ResourceLocation fluidId = buf.readResourceLocation();
			String fluidKey = buf.readUtf();
			return new EyesInFluidUpgradeCondition(internals, inverted, entityEntry, fluidId, fluidKey);
		}
		
	}
	
}