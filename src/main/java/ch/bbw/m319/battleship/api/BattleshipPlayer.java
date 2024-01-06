package ch.bbw.m319.battleship.api;

/**
 * Einer von zwei Spielern des Spiels <a href="https://de.wikipedia.org/wiki/Schiffe_versenken">Schiffeversenken</a>.
 * Gespielt wird eine Variante auf einem 3x3 Spielbrett und es wird nur ein einziges 1x2 Schiff platziert.
 * Danach wird abwechselnd gezielt, bis ein Spieler beide Schiffsteile getroffen hat.
 */
public interface BattleshipPlayer {

	/**
	 * Wird am Anfang eines Spiels 1x aufgerufen und legt fest, wo der Gegner das eigene Schiff finden soll.
	 * Beachte, dass diese Methode bei mehreren Runden (z.B. Hin- und Rückspiel) mehrfach aufgerufen wird.
	 * @return Koordinaten wo das eigene Schiffchen für das folgende Spiel versteckt liegt.
	 */
	ShipPosition placeYourShip();

	/**
	 * Du bist an der Reihe mit Deinem Zug. Wo vermutest Du das gegnerische Schiff?
	 * Wird in einem Spiel pro Zug 1x aufgerufen.
	 * @param lastShotWasHit {@code true}, falls das gegnerische Schiff im <b>letzten</b> Zug getroffen wurde.
	 * @return Die Koordinate wo das Schiff des Gegners vermutet wird.
	 */
	BattleshipField aimAt(boolean lastShotWasHit);

	/**
	 * Die Zielposition des Gegners. Wird 1x nach dem gegnerischen Zug aufgerufen.
	 * Muss nicht implementiert werden, ist aber eine Möglichkeit die Strategie des Gegners zu lernen.
	 * @param position wohin der Gegner gezielt hat.
	 */
	default void opponentAimsAt(BattleshipField position) {

	}

	/**
	 * Das Endresultat dieser Spielrunde. Wird 1x am Ende einer Partie aufgerufen.
	 * Muss nicht implementiert werden, ist aber eine Möglichkeit die Strategie des Gegners zu lernen.
	 * @param youHaveWon true, falls dieser Spieler als erster das gegnerische Schiff komplett getroffen hat.
	 */
	default void gameFinished(boolean youHaveWon) {

	}

}
