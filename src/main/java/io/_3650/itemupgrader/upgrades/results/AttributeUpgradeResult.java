package io._3650.itemupgrader.upgrades.results;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.registry.types.AttributeReplacement;
import io._3650.itemupgrader.registry.types.ModUpgradeEntry;
import io._3650.itemupgrader.registry.types.ModUpgradeEntrySet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeUpgradeResult extends UpgradeResult {
	
	private final ResourceLocation attributeId;
	private final Attribute attribute;
	private final Operation operation;
	private final double amount;
	private final String name;
	private final Map<EquipmentSlot, UUID> uuids;
	
	public AttributeUpgradeResult(IUpgradeInternals internals, ResourceLocation attributeId, Operation operation, double amount, @Nullable String name, Map<EquipmentSlot, UUID> uuids) {
		super(internals, ModUpgradeEntrySet.ATTRIBUTES);
		this.attributeId = attributeId;
		this.attribute = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(attributeId));
		this.operation = operation;
		this.amount = amount;
		this.name = name;
		this.uuids = uuids;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Multimap<Attribute, AttributeModifier> modifiers = data.getEntry(ModUpgradeEntry.ATTRIBUTES);
		if (modifiers == null) return false;
		boolean applied = false;
		if (modifiers.containsKey(this.attribute)) {
			for (AttributeModifier modifier : modifiers.get(this.attribute)) {
				if (modifier.getOperation() == this.operation && (this.name == null || modifier.getName() == this.name)) {
					AttributeModifier newModifier = new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getAmount() + this.amount, modifier.getOperation());
					AttributeReplacement replacement = new AttributeReplacement(this.attribute, modifier, newModifier);
					Set<AttributeReplacement> replacements = data.getEntry(ModUpgradeEntry.ATTRIBUTE_REPLACEMENTS);
					applied = replacements != null && replacements.add(replacement);
					if (applied) {
//						modifiers.remove(this.attribute, modifier);
						break;
					}
				}
			}
		}
		
		if (!applied) {
			SetMultimap<Attribute, AttributeModifier> additions = data.getEntry(ModUpgradeEntry.ATTRIBUTE_ADDITIONS);
			applied = additions != null && additions.put(this.attribute, new AttributeModifier(this.uuids.get(data.getEntry(UpgradeEntry.SLOT)), this.name == null ? "Upgrader Attribute" : this.name, this.amount, this.operation));
		}
		
		return applied; //how convenient
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		//Set amount
		double displayAmount = this.amount;
		if (this.operation == AttributeModifier.Operation.MULTIPLY_BASE || this.operation == AttributeModifier.Operation.MULTIPLY_TOTAL) displayAmount = this.amount * 100.0D;
		else if (this.attribute == Attributes.KNOCKBACK_RESISTANCE) displayAmount = this.amount * 10.0D;
		
		//Make component
		if (this.amount >= 0.0D) {
			return ComponentHelper.arrayify(new TranslatableComponent("attribute.modifier.plus."+ this.operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount), new TranslatableComponent(this.attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
		} else {
			displayAmount *= -1.0D;
			return ComponentHelper.arrayify(new TranslatableComponent("attribute.modifier.take."+ this.operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount), new TranslatableComponent(this.attribute.getDescriptionId())).withStyle(ChatFormatting.RED));
		}
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<AttributeUpgradeResult> {
		
		@Override
		public AttributeUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			ResourceLocation attributeId = new ResourceLocation(GsonHelper.getAsString(json, "attribute"));
			Operation operation = getAttributeOperationByName(GsonHelper.getAsString(json, "operation", "add"));
			double amount = GsonHelper.getAsDouble(json, "amount");
			String name = GsonHelper.getAsString(json, "name", null);
			Map<EquipmentSlot, UUID> uuids = Maps.newHashMap();
			if (GsonHelper.isObjectNode(json, "uuids")) {
				JsonObject uuidJson = GsonHelper.getAsJsonObject(json, "uuids");
				for (var slot : EquipmentSlot.values()) {
					if (GsonHelper.isStringValue(uuidJson, slot.getName())) {
						try {
							UUID uuid = UUID.fromString(GsonHelper.getAsString(uuidJson, slot.getName()));
							uuids.put(slot, uuid);
						} catch (IllegalArgumentException ignored) {
							uuids.put(slot, UUID.randomUUID());
						}
					} else {
						uuids.put(slot, UUID.randomUUID());
					}
				}
			} else {
				for (var slot : EquipmentSlot.values()) {
					uuids.put(slot, UUID.randomUUID());
				}
			}
			return new AttributeUpgradeResult(internals, attributeId, operation, amount, name, uuids);
		}
		
		@Override
		public void toNetwork(AttributeUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeResourceLocation(result.attributeId);
			buf.writeEnum(result.operation);
			buf.writeDouble(result.amount);
			//name
			buf.writeBoolean(result.name != null);
			if (result.name != null) buf.writeUtf(result.name);
			buf.writeMap(result.uuids, (buffer, slot) -> buffer.writeEnum(slot), (buffer, uuid) -> buffer.writeUUID(uuid));
		}
		
		@Override
		public AttributeUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			ResourceLocation attributeId = buf.readResourceLocation();
			Operation operation = buf.readEnum(Operation.class);
			double amount = buf.readDouble();
			String name = null;
			if (buf.readBoolean()) name = buf.readUtf();
			Map<EquipmentSlot, UUID> uuids = buf.readMap(buffer -> buffer.readEnum(EquipmentSlot.class), buffer -> buffer.readUUID());
			return new AttributeUpgradeResult(internals, attributeId, operation, amount, name, uuids);
		}
		
	}
	
	private static AttributeModifier.Operation getAttributeOperationByName(String name) {
		AttributeModifier.Operation modifierOperation = null;
		switch (name) {
			case "add":
				modifierOperation = AttributeModifier.Operation.ADDITION;
				break;
			case "multiply_base":
				modifierOperation = AttributeModifier.Operation.MULTIPLY_BASE;
				break;
			case "multiply":
				modifierOperation = AttributeModifier.Operation.MULTIPLY_TOTAL;
				break;
		}
		return modifierOperation;
	}
	
}
