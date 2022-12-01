package io._3650.itemupgrader.upgrades.results.special;

import java.util.List;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.mixin.AbstractArrowAccessor;
import io._3650.itemupgrader.network.NetworkHandler;
import io._3650.itemupgrader.network.PickupItemPacket;
import io._3650.itemupgrader.registry.types.UpgradeInventoryHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class AbsorbItemsUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Entity> entityEntry;
	private final double range;
	
	public AbsorbItemsUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Entity> entityEntry, double range) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(entityEntry);
		}));
		this.entityEntry = entityEntry;
		this.range = range;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Entity entity = data.getEntry(this.entityEntry);
		if (entity.level.isClientSide) return false;
		UpgradeInventoryHolder holder = (UpgradeInventoryHolder) entity;
		//Stop items that cannot be picked up at all from absorbing items to prevent items from being consumed forever
		if (entity instanceof AbstractArrow arrow && ((AbstractArrowAccessor)arrow).getPickup() == AbstractArrow.Pickup.DISALLOWED && arrow.getOwner() != null) return false;
		
		Vec3 pos = entity.position();
		Level level = entity.level;
		
		AABB aabb = new AABB(pos.x() + this.range, pos.y() + this.range, pos.z() + this.range, pos.x() - this.range, pos.y() - this.range, pos.z() - this.range);
		
		List<ItemEntity> itemEntities = level.getEntities(EntityTypeTest.forClass(ItemEntity.class), aabb, ignored -> true);
		
		var pusher = holder.itemupgrader_getInventoryPusher();
		
		for (ItemEntity item : itemEntities) {
			if (!item.isAlive()) continue;
			int amount = pusher.push(item.getItem());
			if (amount > 0) NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> item), new PickupItemPacket(item.getId(), entity.getId(), amount));
		}
		
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.range)));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<AbsorbItemsUpgradeResult> {
		
		@Override
		public AbsorbItemsUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			double range = GsonHelper.getAsDouble(json, "range", 3.0D);
			return new AbsorbItemsUpgradeResult(internals, entityEntry, range);
		}
		
		@Override
		public void toNetwork(AbsorbItemsUpgradeResult result, FriendlyByteBuf buf) {
			result.entityEntry.toNetwork(buf);
			buf.writeDouble(result.range);
		}
		
		@Override
		public AbsorbItemsUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			double range = buf.readDouble();
			return new AbsorbItemsUpgradeResult(internals, entityEntry, range);
		}
		
	}
	
}