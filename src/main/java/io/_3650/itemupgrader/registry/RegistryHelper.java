package io._3650.itemupgrader.registry;

public class RegistryHelper {
	
	/**
	 * Fixes the stupid class to fix
	 * @param <T> The stupid class type that should be there in the first place
	 * @param theStupidClassToFix The stupid class to fix (shocking I know)
	 * @return The stupid class to fix but fixed
	 */
	@SuppressWarnings("unchecked") //I hate this is the solution
	public static <T> Class<T> fixStupidClass(Class<?> theStupidClassToFix) {
		return (Class<T>) theStupidClassToFix;
	}
	
}