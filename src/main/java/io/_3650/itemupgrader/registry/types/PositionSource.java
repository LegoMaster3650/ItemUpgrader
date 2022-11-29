package io._3650.itemupgrader.registry.types;

public enum PositionSource {
	
	FOOT("feet"),
	EYE("eyes");
	
	private final String name;
	
	private PositionSource(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static PositionSource byName(String name) {
		for (var value : values()) {
			if (value.name.equals(name)) return value;
		}
		return FOOT;
	}
	
}