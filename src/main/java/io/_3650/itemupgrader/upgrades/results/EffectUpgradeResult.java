package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectUpgradeResult extends UpgradeResult {
	
	private final ResourceLocation effectId;
	private final UpgradeEntry<LivingEntity> livingEntry;
	private final MobEffect effect;
	private final int duration;
	private final int amplifier;
	private final boolean ambient;
	private final boolean showParticles;
	private final boolean showIcon;
	
	public EffectUpgradeResult(IUpgradeInternals internals, UpgradeEntry<LivingEntity> livingEntry, ResourceLocation effectId, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
		super(internals, UpgradeEntrySet.LIVING.fillCategories(mapper -> {
			mapper.set(EntryCategory.LIVING, livingEntry);
		}));
		this.livingEntry = livingEntry;
		this.effectId = effectId;
		this.effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
		this.duration = duration;
		this.amplifier = amplifier;
		this.ambient = ambient;
		this.showParticles = showParticles;
		this.showIcon = showIcon;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		LivingEntity entity = data.getEntry(this.livingEntry);
		return entity.addEffect(new MobEffectInstance(this.effect, this.duration, this.amplifier, this.ambient, this.showParticles, this.showIcon));
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		MutableComponent result = new TranslatableComponent(this.effect.getDescriptionId());
		if (this.amplifier > 0) result = new TranslatableComponent("potion.withAmplifier", result, new TranslatableComponent("potion.potency." + this.amplifier));
		if (this.duration > 20) result = new TranslatableComponent("potion.withDuration", result, MobEffectUtil.formatDuration(new MobEffectInstance(this.effect, this.duration, this.amplifier, this.ambient, this.showParticles, this.showIcon), 1.0F));
		return ComponentHelper.arrayify(result.withStyle(this.effect.getCategory().getTooltipFormatting()));
	}

	@Override
	public Serializer getSerializer() {
		return new Serializer();
	}

	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<EffectUpgradeResult> {
		
		@Override
		public EffectUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromJson(json, "entity");
			ResourceLocation effectId = new ResourceLocation(GsonHelper.getAsString(json, "effect"));
			if (!ForgeRegistries.MOB_EFFECTS.containsKey(effectId)) throw new IllegalArgumentException("Effect does not exist: " + effectId);
			int duration = GsonHelper.getAsInt(json, "duration");
			if (duration % 20 == 0 && GsonHelper.getAsBoolean(json, "anti_flicker", true)) duration += 2; //Helps stop the flickering of per-tick effects
			int amplifier = GsonHelper.getAsInt(json, "amplifier", 0);
			boolean ambient = GsonHelper.getAsBoolean(json, "ambient", false);
			boolean showParticles = GsonHelper.getAsBoolean(json, "show_particles", true);
			boolean showIcon = GsonHelper.getAsBoolean(json, "show_icon", true);
			return new EffectUpgradeResult(internals, livingEntry, effectId, duration, amplifier, ambient, showParticles, showIcon);
		}
		
		@Override
		public void toNetwork(EffectUpgradeResult result, FriendlyByteBuf buf) {
			result.livingEntry.toNetwork(buf);
			buf.writeResourceLocation(result.effectId);
			buf.writeInt(result.duration);
			buf.writeInt(result.amplifier);
			buf.writeBoolean(result.ambient);
			buf.writeBoolean(result.showParticles);
			buf.writeBoolean(result.showIcon);
		}
		
		@Override
		public EffectUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<LivingEntity> livingEntry = EntryCategory.LIVING.fromNetwork(buf);
			ResourceLocation effectId = buf.readResourceLocation();
			int duration = buf.readInt();
			int amplifier = buf.readInt();
			boolean ambient = buf.readBoolean();
			boolean showParticles = buf.readBoolean();
			boolean showIcon = buf.readBoolean();
			return new EffectUpgradeResult(internals, livingEntry, effectId, duration, amplifier, ambient, showParticles, showIcon);
		}
		
	}
	
}
