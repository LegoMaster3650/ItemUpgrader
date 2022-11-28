package io._3650.itemupgrader.api.ingredient;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Criteria for {@linkplain TypedIngredient} registered in the <b>TYPED_CRITERIA</b> ItemUpgraderRegistry
 * @author LegoMaster3650
 * @see TypedIngredient
 */
public class TypedCriteria extends ForgeRegistryEntry<TypedCriteria> {
	
	/**Utility for quickly getting an always true {@linkplain TypedCriteria}*/
	public static final Supplier<TypedCriteria> TRUE = () -> new TypedCriteria(stack -> true);
	/**Utility for quickly getting an always true {@linkplain TypedCriteria}*/
	public static final Supplier<TypedCriteria> FALSE = () -> new TypedCriteria(stack -> true);
	
	private Predicate<ItemStack> predicate;
	
	/**
	 * Constructs a new TypedCriteria with the given item predicate
	 * @param predicate A predicate of an {@linkplain ItemStack} to check for typing against
	 */
	public TypedCriteria(Predicate<ItemStack> predicate) {
		this.predicate = predicate;
	}
	
	/**
	 * Constructs a new supplier for a typed criteria
	 * @param predicate A predicate of an {@linkplain ItemStack} to check for typing against
	 * @return A new {@linkplain Supplier} for a {@linkplain TypedCriteria}
	 */
	public static Supplier<TypedCriteria> of(Predicate<ItemStack> predicate) {
		return () -> new TypedCriteria(predicate);
	}
	
	/**
	 * Tests this predicate against the given item
	 * @param stack The {@linkplain ItemStack} to test
	 * @return Whether the item passes the predicate's test
	 */
	public boolean test(ItemStack stack) {
		return this.predicate.test(stack);
	}
	
}