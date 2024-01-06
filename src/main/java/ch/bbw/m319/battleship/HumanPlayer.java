package ch.bbw.m319.battleship;

import java.util.Locale;
import java.util.Scanner;

import ch.bbw.m319.battleship.api.BattleshipArena;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

/**
 * Ein Beispiel-Spieler welcher allen Input von der Konsole liest.
 */
public record HumanPlayer(String name) implements BattleshipPlayer {

	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println("Shrink your terminal height, such that there are only 2 lines visible...");
		System.out.println("Valid coordinates are A1 up to C3.");
		BattleshipArena.playOnce(new HumanPlayer("Player1"), new HumanPlayer("Player2"));
	}

	private void print(String msg) {
		System.out.print(name + ": " + msg);
	}

	@Override
	public ShipPosition placeYourShip() {
		print("1st position of your ship? ");
		var position1 = readField();
		print("2nd position of your ship? ");
		var position2 = readField();
		System.out.println("---");
		return new ShipPosition(position1, position2);
	}

	@Override
	public BattleshipField takeAim() {
		print("What's your target? ");
		return readField();
	}

	@Override
	public void outcomeOfYourTurn(BattleshipField targetedField, boolean isHit) {
		print("Your shot at " + targetedField + " did " + (isHit ? "HIT!" : "miss..."));
		System.out.println();
	}

	@Override
	public void gameFinished(ShipPosition ship, boolean youHaveWon) {
		if (youHaveWon) {
			print("WINNER! ");
		}
	}

	private BattleshipField readField() {
		while (true) {
			var line = scanner.nextLine();
			var str = line.trim().toUpperCase(Locale.ROOT);
			try {
				return BattleshipField.valueOf(str);
			} catch (IllegalArgumentException e) {
				print("invalid input, try again with A1-C3... ");
			}
		}
	}
}
