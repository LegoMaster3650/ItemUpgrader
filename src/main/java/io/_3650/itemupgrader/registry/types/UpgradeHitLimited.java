package io._3650.itemupgrader.registry.types;

public interface UpgradeHitLimited {
	
	public void itemupgrader_setHits(byte hits);
	
	public default boolean itemupgrader_doHit() {
		byte hits = this.itemupgrader_getHits();
		if (hits > 0) {
			hits -= 1;
			this.itemupgrader_setHits(hits);
			return true;
		} else return false;
	}
	
	public byte itemupgrader_getHits();
	
}