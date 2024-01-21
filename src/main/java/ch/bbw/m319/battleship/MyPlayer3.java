package ch.bbw.m319.battleship;

import ch.bbw.m319.battleship.api.BattleshipArena;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

public class MyPlayer3 implements BattleshipPlayer {

	public static void main(String[] args) {
		// let it play against itself
		//BattleshipArena.playOnce(new MyPlayer1(), new HumanPlayer("Cyril"));
		BattleshipArena.playMultiple(new MyPlayer3(), new MyPlayer2(), 10000);
	}

	int[] hit = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ShipPosition placeYourShip() {
		hit = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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

	@Override
	public BattleshipField takeAim() {
		int suggestion = ShotsWhenOneHitted();
		if(suggestion >= 0){
			System.out.println("AI: What's your target? " + suggestion);
			return getSelectedField(suggestion);
		}

		//select random number
		int randomPos = (int) Math.floor(Math.random() * 9);
		while(hit[randomPos] != 0){
			randomPos = (int) Math.floor(Math.random() * 9);
		}

		System.out.println("AI: What's your target? " + randomPos);
		return getSelectedField(randomPos);
	}
	private int ShotsWhenOneHitted(){
		for (int i = 0; i < hit.length; i++) {
			if(hit[i] == 2){
				int[] possibleCalculation = {-3, -1, 1, 3};
				for (int j = 0; j < 4; j++) {
					if(i + possibleCalculation[j] >= 0 && i + possibleCalculation[j] <= 8 && hit[i + possibleCalculation[j]] == 0){
						return i + possibleCalculation[j];
					}
				}
			}
		}
		return -1;
	}

	@Override
	public void outcomeOfYourTurn(BattleshipField targetedField, boolean isHit) {
		hit[targetedField.ordinal()] = (isHit ? 2 : 1);
		System.out.println("Your shot at " + targetedField + " did " + (isHit ? "HIT!" : "miss..."));
	}

	public BattleshipField getSelectedField(int selectedFieldAsNumber){
		BattleshipField[] allPossiblePosition = BattleshipField.values();

		return allPossiblePosition[selectedFieldAsNumber];
	}
}
