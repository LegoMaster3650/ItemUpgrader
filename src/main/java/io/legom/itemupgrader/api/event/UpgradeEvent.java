package io.legom.itemupgrader.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class UpgradeEvent extends Event {
	
	public ItemStack stack;
	public ResourceLocation upgradeId;
	
	public UpgradeEvent(ItemStack stack, ResourceLocation upgradeId) {
		this.stack = stack;
		this.upgradeId = upgradeId;
	}
	
	public static class Apply extends UpgradeEvent {
		
		private Apply(ItemStack stack, ResourceLocation upgradeId) {
			super(stack, upgradeId);
		}
		
		@Cancelable
		public static class Pre extends Apply {
			public Pre(ItemStack stack, ResourceLocation upgradeId) {
				super(stack, upgradeId);
			}
			
			@Override
			public boolean isCancelable() {
				return true;
			}
		}
		
		public static class Post extends Apply {
			public Post(ItemStack stack, ResourceLocation upgradeId) {
				super(stack, upgradeId);
			}
		}
		
	}
	
	public static class Remove extends UpgradeEvent {
		
		public Remove(ItemStack stack, ResourceLocation upgradeId) {
			super(stack, upgradeId);
		}
		
	}
	
	@Cancelable
	public static class Replace extends UpgradeEvent {
		
		public ResourceLocation previousUpgradeId;
		
		public Replace(ItemStack stack, ResourceLocation upgradeId, ResourceLocation previousUpgradeId) {
			super(stack, upgradeId);
			this.previousUpgradeId = previousUpgradeId;
		}
		
		@Override
		public boolean isCancelable() {
			return true;
		}
		
	}
	
}