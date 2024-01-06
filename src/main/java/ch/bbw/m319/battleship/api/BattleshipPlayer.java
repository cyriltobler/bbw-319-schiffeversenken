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
	 *
	 * @return Koordinaten wo das eigene Schiffchen für das folgende Spiel versteckt liegt.
	 */
	ShipPosition placeYourShip();

	/**
	 * Du bist an der Reihe mit Deinem Zug. Wo vermutest Du das gegnerische Schiff?
	 * Wird in einem Spiel pro Zug 1x aufgerufen.
	 *
	 * @return Die Koordinate wo das Schiff des Gegners vermutet wird.
	 */
	BattleshipField takeAim();

	/**
	 * Wird 1x <b>nach dem eigenen Zug</b> {@link #takeAim()} aufgerufen und informiert ob der Gegner getroffen wurde.
	 * Muss nicht zwingend implementiert werden, aber ist auf jeden Fall sinnvoll.
	 *
	 * @param targetedField das Feld welches zuvor mit {@code takeAim()} angezielt wurde.
	 * @param isHit {@code true}, falls das gegnerische Schiff getroffen wurde.
	 */
	default void outcomeOfYourTurn(BattleshipField targetedField, boolean isHit) {

	}

	/**
	 * Wird 1x <b>nach dem gegnerischen Zug</b> aufgerufen und informiert, wo der Gegner hinzielte.
	 * Muss nicht implementiert werden, ist aber eine Möglichkeit die Strategie des Gegners zu lernen.
	 *
	 * @param targetedField wohin der Gegner gezielt hat.
	 * @param isHit {@code true}, falls das eigene Schiff dadurch getroffen wurde.
	 */
	default void outcomeOfOpponentsTurn(BattleshipField targetedField, boolean isHit) {

	}

	/**
	 * Das Endresultat dieser Spielrunde. Wird 1x am Ende einer Partie aufgerufen.
	 * Muss nicht implementiert werden, ist aber eine Möglichkeit die Strategie des Gegners zu lernen.
	 *
	 * @param opponentShip wo das gegnerische Schiff versteckt war.
	 * @param youHaveWon true, falls dieser Spieler als erster das gegnerische Schiff komplett getroffen hat.
	 */
	default void gameFinished(ShipPosition opponentShip, boolean youHaveWon) {

	}

}
