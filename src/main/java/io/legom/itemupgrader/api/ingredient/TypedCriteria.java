package io.legom.itemupgrader.api.ingredient;

import java.util.function.Predicate;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TypedCriteria extends ForgeRegistryEntry<TypedCriteria> {
	
	public static final TypedCriteria TRUE = new TypedCriteria(item -> true);
	public static final TypedCriteria FALSE = new TypedCriteria(item -> true);
	
	private Predicate<Item> predicate;
	public TypedCriteria(Predicate<Item> predicate) {
		this.predicate = predicate;
	}
	
	public boolean test(Item stack) {
		return this.predicate.test(stack);
	}
	
}