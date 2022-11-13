package io._3650.itemupgrader.upgrades.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.registry.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MagnetUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Vec3> posEntry;
	private final boolean attractItems;
	private final boolean attractXp;
	private final double range;
	private final double speed;
	private final boolean isSphere;
	private final double cubeRange;
	private final double rangeSquared;
	
	public MagnetUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Vec3> posEntry, boolean attractItems, boolean attractXp, double range, double speed, boolean isSphere) {
		super(internals, UpgradeEntrySet.LEVEL_POSITION.fillCategories(mapper -> {
			mapper.set(EntryCategory.POSITION, posEntry);
		}));
		this.posEntry = posEntry;
		this.attractItems = attractItems;
		this.attractXp = attractXp;
		this.range = range;
		this.speed = speed;
		this.isSphere = isSphere;
		this.cubeRange = isSphere ? Math.sqrt((range * range) * 2) : range;
		this.rangeSquared = range * range;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) { 
		Vec3 pos = data.getEntry(this.posEntry);
		Level level = data.getEntry(UpgradeEntry.LEVEL);
		
		AABB aabb = new AABB(pos.x() + this.cubeRange, pos.y() + this.cubeRange, pos.z() + this.cubeRange, pos.x() - this.cubeRange, pos.y() - this.cubeRange, pos.z() - this.cubeRange);
		
		List<Entity> entities = level.getEntities((Entity) null, aabb, entity -> {
			boolean validType = ((entity instanceof ItemEntity) && this.attractItems) || ((entity instanceof ExperienceOrb) && this.attractXp);
			if (this.isSphere && validType) return entity.position().distanceToSqr(pos) < this.rangeSquared;
			else return validType;
		});
		
		for (Entity target : entities) {
			if (!target.isAlive()) continue;
			Vec3 velocity = pos.subtract(target.position()).normalize();
			if (!target.isNoGravity()) velocity = velocity.add(0, -0.15, 0);
			target.setDeltaMovement(velocity.scale(this.speed));
		}
		
		return entities.size() > 0;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		ArrayList<MutableComponent> types = new ArrayList<>(2);
		if(this.attractItems) types.add(new TranslatableComponent("upgradeResult.itemupgrader.magnet.items"));
		if(this.attractXp) types.add(new TranslatableComponent("upgradeResult.itemupgrader.magnet.xp"));
		return new MutableComponent[] {ComponentHelper.andList(types), new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.range))};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<MagnetUpgradeResult> {
		
		@Override
		public MagnetUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json);
			boolean attractItems = GsonHelper.getAsBoolean(json, "items", true);
			boolean attractXp = GsonHelper.getAsBoolean(json, "xp", true);
			double range = GsonHelper.getAsDouble(json, "range", 5.0D);
			double speed = GsonHelper.getAsDouble(json, "speed", 1.0D) / 2.0D;
			boolean isSphere = GsonHelper.getAsString(json, "shape", "cube").equals("sphere") && Config.COMMON.allowMagneticSphere.get();
			return new MagnetUpgradeResult(internals, posEntry, attractItems, attractXp, range, speed, isSphere);
		}
		
		@Override
		public void toNetwork(MagnetUpgradeResult result, FriendlyByteBuf buf) {
			result.posEntry.toNetwork(buf);
			buf.writeBoolean(result.attractItems);
			buf.writeBoolean(result.attractXp);
			buf.writeDouble(result.range);
			buf.writeDouble(result.speed);
			buf.writeBoolean(result.isSphere);
		}
		
		@Override
		public MagnetUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			boolean attractItems = buf.readBoolean();
			boolean attractXp = buf.readBoolean();
			double range = buf.readDouble();
			double speed = buf.readDouble();
			boolean isSphere = buf.readBoolean();
			return new MagnetUpgradeResult(internals, posEntry, attractItems, attractXp, range, speed, isSphere);
		}
		
	}
	
}