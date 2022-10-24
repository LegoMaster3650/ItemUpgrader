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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class ExplosionUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Vec3> posEntry;
	private final UpgradeEntry<Entity> entityEntry;
	private final float radius;
	private final boolean allowGriefing;
	
	public ExplosionUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Vec3> posEntry, UpgradeEntry<Entity> entityEntry, float radius, boolean allowGriefing) {
		super(internals, UpgradeEntrySet.ENTITY.fillCategories(mapper -> {
			mapper.set(EntryCategory.POSITION, posEntry).set(EntryCategory.ENTITY, entityEntry);
		}));
		this.posEntry = posEntry;
		this.entityEntry = entityEntry;
		this.radius = radius;
		this.allowGriefing = allowGriefing;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		Entity entity = data.getEntry(this.entityEntry);
		Level level = entity.level;
		if (level.isClientSide) return;
		boolean destructive = ForgeEventFactory.getMobGriefingEvent(level, entity);
		Vec3 pos = data.getEntry(this.posEntry);
		level.explode(entity, pos.x(), pos.y(), pos.z(), this.radius, destructive ? BlockInteraction.BREAK : BlockInteraction.NONE);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.radius)));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<ExplosionUpgradeResult> {
		
		@Override
		public ExplosionUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json);
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			float radius = GsonHelper.getAsFloat(json, "power", 3.0F);
			boolean allowGriefing = GsonHelper.getAsBoolean(json, "allow_destruction", true);
			return new ExplosionUpgradeResult(internals, posEntry, entityEntry, radius, allowGriefing);
		}
		
		@Override
		public void toNetwork(ExplosionUpgradeResult result, FriendlyByteBuf buf) {
			result.posEntry.toNetwork(buf);
			result.entityEntry.toNetwork(buf);
			buf.writeFloat(result.radius);
			buf.writeBoolean(result.allowGriefing);
		}
		
		@Override
		public ExplosionUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			float radius = buf.readFloat();
			boolean allowGriefing = buf.readBoolean();
			return new ExplosionUpgradeResult(internals, posEntry, entityEntry, radius, allowGriefing);
		}
		
	}
	
}