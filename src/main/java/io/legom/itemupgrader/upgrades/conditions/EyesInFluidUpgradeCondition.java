package io.legom.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io.legom.itemupgrader.api.data.UpgradeEntry;
import io.legom.itemupgrader.api.data.UpgradeEntrySet;
import io.legom.itemupgrader.api.data.UpgradeEventData;
import io.legom.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io.legom.itemupgrader.api.type.UpgradeCondition;
import io.legom.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class EyesInFluidUpgradeCondition extends UpgradeCondition {
	
	private final ResourceLocation fluidId;
	private final TagKey<Fluid> fluid;
	private final String fluidKey;
	
	public EyesInFluidUpgradeCondition(IUpgradeInternals internals, boolean inverted, ResourceLocation fluidId, String fluidKey) {
		super(internals, inverted);
		this.fluidId = fluidId;
		this.fluid = //net.minecraft.tags.FluidTags.create(fluidId); //Using registry for now
				ForgeRegistries.FLUIDS.tags().createTagKey(fluidId);
		this.fluidKey = fluidKey;
	}
	
	@Override
	public UpgradeEntrySet requiredData() {
		return UpgradeEntrySet.ENTITY;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		return data.getEntry(UpgradeEntry.ENTITY).isEyeInFluid(this.fluid);
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
			ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
			String fluidKey = "fluid." + GsonHelper.getAsString(json, "fluidKey", ComponentHelper.keyFormat(fluidId));
			return new EyesInFluidUpgradeCondition(internals, inverted, fluidId, fluidKey);
		}
		
		@Override
		public void toNetwork(EyesInFluidUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeResourceLocation(condition.fluidId);
			buf.writeUtf(condition.fluidKey);
		}
		
		@Override
		public EyesInFluidUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			ResourceLocation netFluidId = buf.readResourceLocation();
			String netFluidKey = buf.readUtf();
			return new EyesInFluidUpgradeCondition(internals, inverted, netFluidId, netFluidKey);
		}
		
	}
	
}