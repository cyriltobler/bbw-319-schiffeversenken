package ch.bbw.m319.battleship.api;

/**
 * Position eines Schiffes auf dem 3x3 Spielbrett bestehend aus zwei benachbarten Spielfeldern.
 * Erstelle eine neue Instanz dieser Klasse mit {@code new ShipPosition(A1, A2);}.
 */
public record ShipPosition(BattleshipField field1, BattleshipField field2) {

	public ShipPosition {
		if (field1 == null || field2 == null) {
			throw new IllegalArgumentException("never pass null coordinates!");
		}
		var i1 = field1.ordinal();
		var i2 = field2.ordinal();
		var areNeighbours = i1 / 3 == i2 / 3 && Math.abs(i1 - i2) == 1 // same row
				|| i1 % 3 == i2 % 3 && Math.abs(i1 - i2) == 3; // same col
		if (!areNeighbours) {
			throw new IllegalArgumentException("invalid ShipPosition: " + field1 + "/" + field2 + " must be adjacent");
		}
	}

	public boolean contains(BattleshipField target) {
		return target == field1 || target == field2;
	}
}
