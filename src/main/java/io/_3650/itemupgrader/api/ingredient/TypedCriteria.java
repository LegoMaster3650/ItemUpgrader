package io._3650.itemupgrader.api.ingredient;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Criteria for {@linkplain TypedIngredient} registered in the <b>TYPED_CRITERIA</b> ItemUpgraderRegistry
 * @author LegoMaster3650
 * @see TypedIngredient
 */
public class TypedCriteria extends ForgeRegistryEntry<TypedCriteria> {
	
	/**Utility for quickly getting an always true {@linkplain TypedCriteria}*/
	public static final Supplier<TypedCriteria> TRUE = () -> new TypedCriteria(item -> true);
	/**Utility for quickly getting an always true {@linkplain TypedCriteria}*/
	public static final Supplier<TypedCriteria> FALSE = () -> new TypedCriteria(item -> true);
	
	private Predicate<Item> predicate;
	
	/**
	 * Constructs a new TypedCriteria with the given item predicate
	 * @param predicate A predicate of an {@linkplain Item} (NOT ItemStack) to check for typing against
	 */
	public TypedCriteria(Predicate<Item> predicate) {
		this.predicate = predicate;
	}
	
	/**
	 * Tests this predicate against the given item
	 * @param item The {@linkplain Item} to test
	 * @return Whether the item passes the predicate's test
	 */
	public boolean test(Item item) {
		return this.predicate.test(item);
	}
	
}