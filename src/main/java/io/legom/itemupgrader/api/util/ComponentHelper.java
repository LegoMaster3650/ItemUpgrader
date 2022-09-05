package io.legom.itemupgrader.api.util;

import java.util.List;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class ComponentHelper {
	
	public static String keyFormat(ResourceLocation id) {
		return id.getNamespace() + "." + id.getPath();
	}
	
	public static MutableComponent applyColor(TextColor color, MutableComponent component) {
		return component.setStyle(component.getStyle().withColor(color));
	}
	
	public static MutableComponent[] arrayify(MutableComponent component) {
		return new MutableComponent[] {component};
	}
	
	public static MutableComponent orList(List<MutableComponent> components) {
		if (components.size() <= 0) return new TranslatableComponent("tooltip.itemupgrader.error");
		if (components.size() == 1) return components.get(0);
		if (components.size() == 2) return components.get(0).append(new TranslatableComponent("itemupgrader.text.or")).append(components.get(1));
		MutableComponent main = components.get(0);
		for (var i = 1; i < components.size() - 1; i++) {
			main.append(new TranslatableComponent("itemupgrader.text.list")).append(components.get(i));
		}
		return main.append(new TranslatableComponent("itemupgrader.text.or.list")).append(components.get(components.size() - 1));
	}
	
	public static MutableComponent andList(List<MutableComponent> components) {
		if (components.size() <= 0) return new TranslatableComponent("tooltip.itemupgrader.error");
		if (components.size() == 1) return components.get(0);
		if (components.size() == 2) return components.get(0).append(new TranslatableComponent("itemupgrader.text.and")).append(components.get(1));
		MutableComponent main = components.get(0);
		for (var i = 1; i < components.size() - 1; i++) {
			main.append(new TranslatableComponent("itemupgrader.text.list", components.get(i)));
		}
		return main.append(new TranslatableComponent("itemupgrader.text.and.list")).append(components.get(components.size() - 1));
	}
	
	public static MutableComponent list(List<MutableComponent> components) {
		if (components.size() <= 0) return new TranslatableComponent("tooltip.itemupgrader.error");
		if (components.size() == 1) return components.get(0);
		MutableComponent main = components.get(0);
		for (var i = 1; i < components.size() - 1; i++) {
			main.append(new TranslatableComponent("itemupgrader.text.list")).append(components.get(i));
		}
		return main;
	}
	
	public static TranslatableComponent componentFromSlot(EquipmentSlot slot) {
		return new TranslatableComponent("equipmentSlot." + slot.getName());
	}
	
	public static TranslatableComponent slotInOn(EquipmentSlot slot) {
		return new TranslatableComponent("tooltip.itemupgrader.slots." + (slot.getType() == EquipmentSlot.Type.ARMOR ? "on" : "in"), ComponentHelper.componentFromSlot(slot));
	}
	
}