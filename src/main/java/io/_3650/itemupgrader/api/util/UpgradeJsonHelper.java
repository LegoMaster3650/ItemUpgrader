package io._3650.itemupgrader.api.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Vec3i;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class UpgradeJsonHelper {
	
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
	
	public static boolean isJsonNumber(JsonElement json) {
		return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber();
	}
	
	public static int getAsInt(JsonElement json, int defaultValue) {
		return isJsonNumber(json) ? json.getAsInt() : defaultValue;
	}
	
	public static float getAsFloat(JsonElement json, float defaultValue) {
		return isJsonNumber(json) ? json.getAsFloat() : defaultValue;
	}
	
	public static double getAsDouble(JsonElement json, double defaultValue) {
		return isJsonNumber(json) ? json.getAsDouble() : defaultValue;
	}
	
	public static Vec3 getVec3(JsonObject json, String key) {
		return Objects.requireNonNull(getVec3(json, key, null));
	}
	
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
	
	public static Vec3i getVec3i(JsonObject json, String key) {
		return Objects.requireNonNull(getVec3i(json, key, null));
	}
	
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
	
	public static Vec2 getVec2(JsonObject json, String key) {
		return Objects.requireNonNull(getVec2(json, key, null));
	}
	
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