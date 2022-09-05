package io.legom.itemupgrader.api.type;

import io.legom.itemupgrader.api.data.UpgradeEventData;
import io.legom.itemupgrader.api.serializer.UpgradeActionSerializer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public abstract class UpgradeAction extends IUpgradeType {
	
	public UpgradeAction(IUpgradeInternals internals) {
		super(internals);
	}
	
	/**
	 * Mostly for internal use, determines whether to automatically use a default translated component as the base for the tooltip or if this action type uses its' own.<br>
	 * (Note: It still gets turned blue either way to encourage sticking to the standard style)
	 * @return <b>false</b> to use the automatic tooltip base, <b>true</b> to just use the value of {@linkplain #getActionTooltip(ItemStack)}
	 */
	public boolean customTooltipBase() {
		return false;
	}
	
	@Override
	public final MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {this.getActionTooltip(stack)};
	}
	
	public abstract MutableComponent getActionTooltip(ItemStack stack);
	
	public final MutableComponent getActionTooltipWithOverride(ItemStack stack) {
		return this.hasTooltipOverride() ? new TranslatableComponent(this.getTooltipOverride()) : this.getActionTooltip(stack);
	}
	
	/**
	 * Defines the behavior for an UpgradeAction after running
	 * @param event The event data parameters passed in
	 * @see UpgradeEventData
	 */
	public abstract void run(UpgradeEventData event);
	
	/**
	 * Use this to return your class's serializer instance.<br>
	 * Ensure the return type is an UpgradeActionSerializer<<b>This Class</b>> in some form, whether just that or a subclass of that, just please make sure it's not the default Wildcard ? type
	 * @return Your own serializer instance
	 */
	public abstract UpgradeActionSerializer<?> getSerializer();
	
}