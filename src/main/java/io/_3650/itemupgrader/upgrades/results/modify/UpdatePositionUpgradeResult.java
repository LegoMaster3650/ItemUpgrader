package io._3650.itemupgrader.upgrades.results.modify;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.upgrades.data.PositionSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class UpdatePositionUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Vec3> posEntry;
	private final UpgradeEntry<Entity> entityEntry;
	private final PositionSource source;
	
	public UpdatePositionUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Vec3> posEntry, UpgradeEntry<Entity> entityEntry, PositionSource source) {
		super(internals, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.POSITION, posEntry).set(EntryCategory.ENTITY, entityEntry);
		}));
		this.posEntry = posEntry;
		this.entityEntry = entityEntry;
		this.source = source;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Entity entity = data.getEntry(this.entityEntry);
		Vec3 pos;
		switch (this.source) {
		default:
		case FOOT:
			pos = entity.position();
		case EYE:
			pos = entity.getEyePosition();
		}
		data.forceModifyEntry(this.posEntry, pos);
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TranslatableComponent(ComponentHelper.entryFormat(this.posEntry))};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<UpdatePositionUpgradeResult> {
		
		@Override
		public UpdatePositionUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json, "entry");
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			PositionSource source = PositionSource.byName(GsonHelper.getAsString(json, "source", PositionSource.FOOT.getName()));
			return new UpdatePositionUpgradeResult(internals, posEntry, entityEntry, source);
		}
		
		@Override
		public void toNetwork(UpdatePositionUpgradeResult result, FriendlyByteBuf buf) {
			result.posEntry.toNetwork(buf);
			result.entityEntry.toNetwork(buf);
			buf.writeEnum(result.source);
		}
		
		@Override
		public UpdatePositionUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			PositionSource source = buf.readEnum(PositionSource.class);
			return new UpdatePositionUpgradeResult(internals, posEntry, entityEntry, source);
		}
		
	}
	
}