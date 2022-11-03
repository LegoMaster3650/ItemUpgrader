package io._3650.itemupgrader.api.util;

/**
 * Utility class for holding a boolean value for accessing inside of lambdas<br>
 * Mostly just a hacky fix not even bothering with getters and setters
 * @author LegoMaster3650
 */
public class BoolHolder {
	
	/**
	 * The value of the {@linkplain BoolHolder}
	 */
	public boolean value;
	
	/**
	 * Constructs a new BoolHolder
	 * @param value The initial value of the holder
	 */
	public BoolHolder(boolean value) {
		this.value = value;
	}
	
}