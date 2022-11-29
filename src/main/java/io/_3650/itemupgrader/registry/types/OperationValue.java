package io._3650.itemupgrader.registry.types;

public enum OperationValue {
	
	EQ("=", false),
	NE("!=", false),
	GT(">", true),
	LT("<", true),
	GE(">=", true),
	LE("<=", true);
	
	private final String name;
	private final boolean comparison;
	
	private OperationValue(String name, boolean comparison) {
		this.name = name;
		this.comparison = comparison;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isComparison() {
		return this.comparison;
	}
	
	public static OperationValue byName(String name) {
		for (var value : values()) {
			if (value.name.equals(name)) return value;
		}
		return EQ;
	}
	
}