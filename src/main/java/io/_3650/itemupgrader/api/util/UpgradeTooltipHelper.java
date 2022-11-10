package io._3650.itemupgrader.api.util;

import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public class UpgradeTooltipHelper {
	
	/**
	 * Gets the tooltip for the given action
	 * @param action The {@linkplain UpgradeAction} to get the tooltip for
	 * @param stack The {@linkplain ItemStack} used for context
	 * @return The generated {@linkplain MutableComponent}
	 */
	public static MutableComponent action(UpgradeAction action, ItemStack stack) {
		if (action.hasTooltipOverride()) return new TranslatableComponent(action.getTooltipOverride(), (Object[]) action.getTooltip(stack)).withStyle(ChatFormatting.BLUE);
		else return action.applyTooltip(new TranslatableComponent(action.getDescriptionId(), (Object[]) action.getTooltip(stack)).withStyle(ChatFormatting.BLUE), stack);
	}
	
	/**
	 * Gets the tooltip for the given condition
	 * @param condition The {@linkplain UpgradeCondition} to get the tooltip for
	 * @param stack The {@linkplain ItemStack} used for context
	 * @return The generated {@linkplain MutableComponent}
	 */
	public static MutableComponent condition(UpgradeCondition condition, ItemStack stack) {
		if (condition.hasTooltipOverride()) return new TranslatableComponent(condition.getTooltipOverride(), (Object[]) condition.getTooltip(stack)).withStyle(ChatFormatting.BLUE);
		else return new TranslatableComponent(condition.getDescriptionId(), (Object[]) condition.getTooltip(stack)).withStyle(ChatFormatting.BLUE);
	}
	
	/**
	 * Gets the tooltip for the given result
	 * @param condition The {@linkplain UpgrdeResult} to get the tooltip for
	 * @param stack The {@linkplain ItemStack} used for context
	 * @return The generated {@linkplain MutableComponent}
	 */
	public static MutableComponent result(UpgradeResult result, ItemStack stack) {
		if (result.hasTooltipOverride()) return new TranslatableComponent(result.getTooltipOverride(), (Object[]) result.getTooltip(stack)).withStyle(result.getColor());
		else return new TranslatableComponent(result.getDescriptionId(), (Object[]) result.getTooltip(stack)).withStyle(result.getColor());
	}
	
}