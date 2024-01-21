package ch.bbw.m319.battleship;

import ch.bbw.m319.battleship.api.BattleshipArena;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

public class MyPlayer1 implements BattleshipPlayer {

	public static void main(String[] args) {
		// let it play against itself
		//BattleshipArena.playOnce(new MyPlayer1(), new HumanPlayer("Cyril"));
		BattleshipArena.playMultiple(new MyPlayer1(), new MyPlayer1(), 10000);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ShipPosition placeYourShip() {
		int posX = (int) Math.floor(Math.random() * 3);
		int posY = (int) Math.floor(Math.random() * 2);
		boolean turnShip = (int) Math.floor(Math.random() * 2) == 1;

		int shipPosition = posX + posY * 3;

		if(turnShip){
			shipPosition = posY + posX * 3;
			return new ShipPosition(getSelectedField(shipPosition), getSelectedField(shipPosition + 1));
		}
		return new ShipPosition(getSelectedField(shipPosition), getSelectedField(shipPosition + 3));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BattleshipField takeAim() {
		//select random number
		int randomPos = (int) Math.floor(Math.random() * 9);

		System.out.println("AI: What's your target? " + randomPos);
		return getSelectedField(randomPos);
	}

	@Override
	public void outcomeOfYourTurn(BattleshipField targetedField, boolean isHit) {
		System.out.println("Your shot at " + targetedField + " did " + (isHit ? "HIT!" : "miss..."));
	}

	public BattleshipField getSelectedField(int selectedFieldAsNamber){
		BattleshipField[] allPossiblePosition = BattleshipField.values();

		return allPossiblePosition[selectedFieldAsNamber];
	}
}
