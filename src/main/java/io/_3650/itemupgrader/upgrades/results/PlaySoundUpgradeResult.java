package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.client.ClientSoundHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Vec3> posEntry;
	private final ResourceLocation soundId;
	private final SoundEvent sound;
	private final SoundSource source;
	private final float volume;
	private final float pitch;
	
	public PlaySoundUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Vec3> posEntry, ResourceLocation soundId, SoundSource source, float volume, float pitch) {
		super(internals, UpgradeEntrySet.LEVEL);
		this.posEntry = posEntry;
		this.soundId = soundId;
		this.sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
		this.source = source;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return;
		Vec3 pos = data.getEntry(this.posEntry);
		level.playSound(null, pos.x(), pos.y(), pos.z(), this.sound, this.source, this.volume, this.pitch);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	private MutableComponent subtitleCache = null;
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		if (this.subtitleCache == null) {
			Component subtitleComponent = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ClientSoundHelper.getSubtitle(this.soundId));
			if (/* subtitleComponent != null && */subtitleComponent instanceof MutableComponent component) this.subtitleCache = component;
			else this.subtitleCache = new TranslatableComponent("subtitles." + this.soundId.getPath());
		}
		return ComponentHelper.arrayify(this.subtitleCache);
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<PlaySoundUpgradeResult> {
		
		@Override
		public PlaySoundUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json);
			ResourceLocation soundId = new ResourceLocation(GsonHelper.getAsString(json, "sound"));
			if (!ForgeRegistries.SOUND_EVENTS.containsKey(soundId)) throw new IllegalArgumentException("Sound does not exist: " + soundId);
			SoundSource source = sourceByName(GsonHelper.getAsString(json, "source", "master"));
			float volume = GsonHelper.getAsFloat(json, "volume", 1.0F);
			float pitch = GsonHelper.getAsFloat(json, "pitch", 1.0F);
			return new PlaySoundUpgradeResult(internals, posEntry, soundId, source, volume, pitch);
		}
		
		@Override
		public void toNetwork(PlaySoundUpgradeResult result, FriendlyByteBuf buf) {
			result.posEntry.toNetwork(buf);
			buf.writeResourceLocation(result.soundId);
			buf.writeEnum(result.source);
			buf.writeFloat(result.volume);
			buf.writeFloat(result.pitch);
		}
		
		@Override
		public PlaySoundUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			ResourceLocation soundId = buf.readResourceLocation();
			SoundSource source = buf.readEnum(SoundSource.class);
			float volume = buf.readFloat();
			float pitch = buf.readFloat();
			return new PlaySoundUpgradeResult(internals, posEntry, soundId, source, volume, pitch);
		}
		
	}
	
	private static SoundSource sourceByName(String name) {
		for (var source : SoundSource.values()) {
			if (source.getName().equals(name)) return source;
		}
		return SoundSource.MASTER;
	}
	
}
