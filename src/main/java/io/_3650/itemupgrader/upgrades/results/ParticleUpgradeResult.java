package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ParticleUpgradeResult extends UpgradeResult {
	
	private final ResourceLocation particleId;
	private final Vec3 offset;
	public ParticleUpgradeResult(IUpgradeInternals internals, ResourceLocation particleId, Vec3 offset) {
		super(internals);
		this.particleId = particleId;
		this.offset = offset;
	}
	
	@Override
	public UpgradeEntrySet getRequiredData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public UpgradeResultSerializer<?> getSerializer() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		// TODO Auto-generated method stub
		
	}
	
	public static class Serializer extends UpgradeResultSerializer<ParticleUpgradeResult> {
		
		@Override
		public ParticleUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void toNetwork(ParticleUpgradeResult result, FriendlyByteBuf buf) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public ParticleUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}