package io._3650.itemupgrader.api.util;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Vec3i;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Utility class for getting certain things from json
 * @author LegoMaster3650
 */
public class UpgradeJsonHelper {
	
	/**
	 * Applies given function to the given json element and collects the results into an ArrayList.<br>
	 * If the element is a json object, it runs for the element.<br>
	 * If the element is a json array, it runs for every json object contained in the array.<br>
	 * @param <T> The type to collect into the array.
	 * @param jsonUnknown The unknown {@linkplain JsonElement} to collect
	 * @param jsonFunc The {@linkplain Function} to convert a JsonObject into the desired type. This can return null to skip the object.
	 * @return An {@linkplain ArrayList} containing every resulting non-null object
	 */
	public static <T> ArrayList<T> collectObjects(JsonElement jsonUnknown, Function<JsonObject, T> jsonFunc) {
		ArrayList<T> res = new ArrayList<>();
		if (jsonUnknown.isJsonArray()) {
			jsonUnknown.getAsJsonArray().forEach(json -> {
				if (json.isJsonObject()) {
					@Nullable T t = jsonFunc.apply(json.getAsJsonObject());
					if (t != null) res.add(t);
				}
			});
		} else if (jsonUnknown.isJsonObject()) {
			T t = jsonFunc.apply(jsonUnknown.getAsJsonObject());
			if (t != null) res.add(t);
		}
		return res;
	}
	
	/**
	 * Gets a position at the given key, guaranteed not to be null
	 * @param json The {@linkplain JsonObject} to get the position from
	 * @param key The json key to check for the position
	 * @return The {@linkplain Vec3} described in the json, defaulting to zero if not present
	 */
	@Nonnull
	public static Vec3 getPosition(JsonObject json, String key) {
		if (GsonHelper.isObjectNode(json, key)) {
			JsonObject values = GsonHelper.getAsJsonObject(json, key);
			double x = GsonHelper.getAsDouble(values, "x", 0);
			double y = GsonHelper.getAsDouble(values, "y", 0);
			double z = GsonHelper.getAsDouble(values, "z", 0);
			return new Vec3(x, y, z);
		} else return Vec3.ZERO;
	}

	/**
	 * Gets an integer position at the given key, guaranteed not to be null
	 * @param json The {@linkplain JsonObject} to get the position from
	 * @param key The json key to check for the position
	 * @return The {@linkplain Vec3i} described in the json, defaulting to zero if not present
	 */
	@Nonnull
	public static Vec3i getIntPosition(JsonObject json, String key) {
		if (GsonHelper.isObjectNode(json, key)) {
			JsonObject values = GsonHelper.getAsJsonObject(json, key);
			int x = GsonHelper.getAsInt(values, "x", 0);
			int y = GsonHelper.getAsInt(values, "y", 0);
			int z = GsonHelper.getAsInt(values, "z", 0);
			return new Vec3i(x, y, z);
		} else return Vec3i.ZERO;
	}
	
	/**
	 * Checks if the given json element is a number
	 * @param json The {@linkplain JsonElement} to check
	 * @return Whether the given element is a number
	 */
	public static boolean isJsonNumber(JsonElement json) {
		return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber();
	}
	
	/**
	 * Gets the given json element as an integer with a default value if it fails
	 * @param json The {@linkplain JsonElement} to get as an integer
	 * @param defaultValue The default value if the conversion fails
	 * @return The given element as an integer, or the default value if it failed
	 */
	public static int getAsInt(JsonElement json, int defaultValue) {
		return isJsonNumber(json) ? json.getAsInt() : defaultValue;
	}
	
	/**
	 * Gets the given json element as a long with a default value if it fails
	 * @param json The {@linkplain JsonElement} to get as a long
	 * @param defaultValue The default value if the conversion fails
	 * @return The given element as a long, or the default value if it failed
	 */
	public static long getAsLong(JsonElement json, long defaultValue) {
		return isJsonNumber(json) ? json.getAsLong() : defaultValue;
	}
	
	/**
	 * Gets the given json element as a float with a default value if it fails
	 * @param json The {@linkplain JsonElement} to get as a float
	 * @param defaultValue The default value if the conversion fails
	 * @return The given element as a float, or the default value if it failed
	 */
	public static float getAsFloat(JsonElement json, float defaultValue) {
		return isJsonNumber(json) ? json.getAsFloat() : defaultValue;
	}

	/**
	 * Gets the given json element as a double with a default value if it fails
	 * @param json The {@linkplain JsonElement} to get as a double
	 * @param defaultValue The default value if the conversion fails
	 * @return The given element as a double, or the default value if it failed
	 */
	public static double getAsDouble(JsonElement json, double defaultValue) {
		return isJsonNumber(json) ? json.getAsDouble() : defaultValue;
	}
	
	/**
	 * Gets a {@linkplain Vec3} from a json array at the given key<br>
	 * <b>REQUIRES IT TO BE NON-NULL</b>
	 * @param json The {@linkplain JsonObject} to get the Vec3 from
	 * @param key The key in the json to get the Vec3 from
	 * @return The {@linkplain Vec3} from the json
	 * @throws NullPointerException if the json doesn't have a valid Vec3 array present
	 */
	@Nonnull
	public static Vec3 getVec3(JsonObject json, String key) throws NullPointerException {
		return Objects.requireNonNull(getVec3(json, key, null));
	}
	
	/**
	 * Gets a {@linkplain Vec3} from a json array at the given key
	 * @param json The {@linkplain JsonObject} to get the Vec3 from
	 * @param key The key in the json to get the Vec3 from
	 * @param defaultValue The default value to return if the value is missing
	 * @return The {@linkplain Vec3} from the json, or the default value if there is no valid Vec3 array present
	 */
	public static Vec3 getVec3(JsonObject json, String key, Vec3 defaultValue) {
		if (GsonHelper.isArrayNode(json, key)) {
			JsonArray values = GsonHelper.getAsJsonArray(json, key);
			if (values.size() < 3) return defaultValue;
			else {
				double x = getAsDouble(values.get(0), 0);
				double y = getAsDouble(values.get(1), 0);
				double z = getAsDouble(values.get(2), 0);
				return new Vec3(x, y, z);
			}
		} else return defaultValue;
	}

	/**
	 * Gets a {@linkplain Vec3i} from a json array at the given key<br>
	 * <b>REQUIRES IT TO BE NON-NULL</b>
	 * @param json The {@linkplain JsonObject} to get the Vec3i from
	 * @param key The key in the json to get the Vec3i from
	 * @return The {@linkplain Vec3i} from the json
	 * @throws NullPointerException if the json doesn't have a valid Vec3i array present
	 */
	@Nonnull
	public static Vec3i getVec3i(JsonObject json, String key) throws NullPointerException {
		return Objects.requireNonNull(getVec3i(json, key, null));
	}

	/**
	 * Gets a {@linkplain Vec3i} from a json array at the given key
	 * @param json The {@linkplain JsonObject} to get the Vec3i from
	 * @param key The key in the json to get the Vec3i from
	 * @param defaultValue The default value to return if the value is missing
	 * @return The {@linkplain Vec3i} from the json, or the default value if there is no valid Vec3i array present
	 */
	public static Vec3i getVec3i(JsonObject json, String key, Vec3i defaultValue) {
		if (GsonHelper.isArrayNode(json, key)) {
			JsonArray values = GsonHelper.getAsJsonArray(json, key);
			if (values.size() < 3) return defaultValue;
			else {
				int x = getAsInt(values.get(0), 0);
				int y = getAsInt(values.get(1), 0);
				int z = getAsInt(values.get(2), 0);
				return new Vec3i(x, y, z);
			}
		} else return defaultValue;
	}
	
	/**
	 * Gets a {@linkplain Vec2} from a json array at the given key<br>
	 * <b>REQUIRES IT TO BE NON-NULL</b>
	 * @param json The {@linkplain JsonObject} to get the Vec2 from
	 * @param key The key in the json to get the Vec2 from
	 * @return The {@linkplain Vec2} from the json
	 * @throws NullPointerException if the json doesn't have a valid Vec2 array present
	 */
	public static Vec2 getVec2(JsonObject json, String key) throws NullPointerException {
		return Objects.requireNonNull(getVec2(json, key, null));
	}
	
	/**
	 * Gets a {@linkplain Vec2} from a json array at the given key
	 * @param json The {@linkplain JsonObject} to get the Vec2 from
	 * @param key The key in the json to get the Vec2 from
	 * @param defaultValue The default value to return if the value is missing
	 * @return The {@linkplain Vec2} from the json, or the default value if there is no valid Vec2 array present
	 */
	public static Vec2 getVec2(JsonObject json, String key, Vec2 defaultValue) {
		if (GsonHelper.isArrayNode(json, key)) {
			JsonArray values = GsonHelper.getAsJsonArray(json, key);
			if (values.size() < 3) return defaultValue;
			else {
				float x = getAsFloat(values.get(0), 0);
				float y = getAsFloat(values.get(1), 0);
				return new Vec2(x, y);
			}
		} else return defaultValue;
	}
	
}