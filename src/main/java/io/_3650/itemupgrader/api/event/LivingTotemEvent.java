package io._3650.itemupgrader.api.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Why does this not already exist<br>
 * Events for the player using a totem of undying
 * @author LegoMaster3650
 */
public abstract class LivingTotemEvent extends Event {
	
	/**The {@linkplain LivingEntity} that triggered the totem*/
	public final LivingEntity living;
	/**The {@linkplain ItemStack} of the totem triggered*/
	public final ItemStack totem;
	/**The {@linkplain DamageSource} that triggered the totem*/
	public final DamageSource damageSource;
	
	public LivingTotemEvent(LivingEntity living, ItemStack totem, DamageSource damageSource) {
		this.living = living;
		this.totem = totem;
		this.damageSource = damageSource;
	}
	
	/**
	 * Fired before a totem of undying (no support for modded totems I do that myself anyways) is consumed<br>
	 * This event IS cancellable, which will cause the totem to not be consumed and the player death logic to proceed
	 * @author LegoMaster3650
	 */
	@Cancelable
	public static class Pre extends LivingTotemEvent {

		/**The {@linkplain InteractionHand} the totem was triggered in*/
		public final InteractionHand hand;
		
		public Pre(LivingEntity living, ItemStack totem, DamageSource damageSource, InteractionHand hand) {
			super(living, totem, damageSource);
			this.hand = hand;
		}
		
		@Override
		public boolean isCancelable() {
			return true;
		}
		
	}
	
	/**
	 * Fired after a totem of undying (no support for modded totems I do that myself anyways) is consumed<br>
	 * This event IS NOT cancellable
	 * @author LegoMaster3650
	 */
	public static class Post extends LivingTotemEvent {
		
		public Post(LivingEntity living, ItemStack totem, DamageSource damageSource) {
			super(living, totem, damageSource);
		}
		
	}
	
}