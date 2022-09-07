package io._3650.itemupgrader.api.util;

import java.util.List;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Helpful class for dealing with some text component stuff
 * @author LegoMaster3650
 *
 */
public class ComponentHelper {
	
	/**
	 * Quickly splits and merges a ResourceLocation around a . instead of a : for text components
	 * @param id The {@linkplain ResourceLocation} to format
	 * @return The given ResourceLocation reformatted
	 */
	public static String keyFormat(ResourceLocation id) {
		return id.getNamespace() + "." + id.getPath();
	}
	
	/**
	 * Quickly applies a given color to a component
	 * @param color The {@linkplain TextColor} to apply
	 * @param component The {@linkplain MutableComponent} to color
	 * @return The colored component
	 */
	public static MutableComponent applyColor(TextColor color, MutableComponent component) {
		return component.setStyle(component.getStyle().withColor(color));
	}
	
	/**
	 * Utility for turning a single component into an array for the methods that demand an array
	 * @param component The {@linkplain MutableComponent} to turn into an array
	 * @return The component turned into an array
	 */
	public static MutableComponent[] arrayify(MutableComponent component) {
		return new MutableComponent[] {component};
	}
	
	/**
	 * Turns a list of components into a single component formatted into a comma seperated list terminating with or<br>
	 * Examples: "A, B, C, or D", "A or B", "A"
	 * @param components A {@linkplain List} of {@linkplain MutableComponent}s to format
	 * @return The components formatted as an or list
	 */
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
	
	/**
	 * Turns a list of components into a single component formatted into a comma seperated list terminating with and<br>
	 * Examples: "A, B, C, and D", "A and B", "A"
	 * @param components A {@linkplain List} of {@linkplain MutableComponent}s to format
	 * @return The components formatted as an and list
	 */
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
	
	/**
	 * Turns a list of components into a single component formatted into a comma seperated list<br>
	 * Examples: "A, B, C, D", "A, B", "A"
	 * @param components A {@linkplain List} of {@linkplain MutableComponent}s to format
	 * @return The components formatted as a list
	 */
	public static MutableComponent list(List<MutableComponent> components) {
		if (components.size() <= 0) return new TranslatableComponent("tooltip.itemupgrader.error");
		if (components.size() == 1) return components.get(0);
		MutableComponent main = components.get(0);
		for (var i = 1; i < components.size() - 1; i++) {
			main.append(new TranslatableComponent("itemupgrader.text.list")).append(components.get(i));
		}
		return main;
	}
	
	/**
	 * Quickly gets a translation key for the given slot
	 * @param slot The {@linkplain EquipmentSlot} to use
	 * @return A {@linkplain TranslatableComponent} with the item upgrader translation key for the given slot
	 */
	public static TranslatableComponent componentFromSlot(EquipmentSlot slot) {
		return new TranslatableComponent("equipmentSlot." + slot.getName());
	}
	
	/**
	 * Quickly gets a "When on X" or "When in X" depending on the slot type for the given slot
	 * @param slot The {@linkplain EquipmentSlot} to use
	 * @return A {@linkplain TranslatableComponent} for the given "When in/on slot"
	 */
	public static TranslatableComponent slotInOn(EquipmentSlot slot) {
		return new TranslatableComponent("tooltip.itemupgrader.slots." + (slot.getType() == EquipmentSlot.Type.ARMOR ? "on" : "in"), ComponentHelper.componentFromSlot(slot));
	}
	
}