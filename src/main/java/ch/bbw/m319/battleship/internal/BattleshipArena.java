package ch.bbw.m319.battleship.internal;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import ch.bbw.m319.battleship.HumanPlayer;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

/**
 * Do NOT look at this implementation. Only use its public methods.
 */
public record BattleshipArena(BattleshipPlayer player1, BattleshipPlayer player2) {

	private static final int MAX_ROUNDS_PER_GAME = 100;

	private static final long NO_TIMEOUT = 0;

	static long TIMEOUT_MS = NO_TIMEOUT;

	static boolean LOG_ENABLED = true;

	public static void log(String msg) {
		if (LOG_ENABLED) {
			System.err.println(msg);
		}
	}

	/**
	 * A simple, single round of Battleship, where player 1 will go first.
	 */
	public static GameResult play(BattleshipPlayer player1, BattleshipPlayer player2) {
		var result = new BattleshipArena(player1, player2).playInternal(true);
		log("outcome (from player 1 pov): " + result);
		return result;
	}

	public GameResult playMultiple(int rounds) {
		var player1WinCounter = 0;
		for (int i = 0; i < rounds; i++) {
			player1WinCounter += switch (playTwo()) {
				case WIN -> 1;
				case DRAW -> 0;
				case LOSS -> -1;
			};
		}
		var result = player1WinCounter == 0 ? GameResult.DRAW : (player1WinCounter > 0 ? GameResult.WIN : GameResult.LOSS);
		log("outcome (from player 1 pov) after " + rounds + " rounds: " + result);
		return result;
	}

	/**
	 * Turnament mode, with both players going first once.
	 */
	public GameResult playTwo() {
		var game1 = playIgnoreCrash(true);
		var game2 = playIgnoreCrash(false);
		return game1 == game2 ? game1 : GameResult.DRAW;
	}

	/**
	 * Turnament mode, wher a crash is rated as a loss for the affected player.
	 */
	public GameResult playIgnoreCrash(boolean startWithPlayer1) {
		try {
			return playInternal(startWithPlayer1);
		} catch (GameRulesViolatedException e) {
			if (LOG_ENABLED) {
				e.printStackTrace();
			}
			log("player " + e.offender.getClass().getSimpleName() + " was misbehaving: " + e.getMessage());
			return e.offender == player1 ? GameResult.LOSS : GameResult.WIN;
		}
	}

	private GameResult playInternal(boolean startWithPlayer1) {
		var p1 = new PlayerState(player1);
		var p2 = new PlayerState(player2);
		var active = startWithPlayer1 ? p1 : p2;
		var opponent = startWithPlayer1 ? p2 : p1;
		for (int i = 0; i < MAX_ROUNDS_PER_GAME; i++) {
			var target = active.aimAt(active.lastWasHit);
			opponent.opponentAimsAt(target);
			active.lastWasHit = opponent.ship.contains(target);
			if (active.lastWasHit) {
				if (active.lastHit != null && active.lastHit != target) { // we have a winner!
					active.gameFinished(true);
					opponent.gameFinished(false);
					return active == p1 ? GameResult.WIN : GameResult.LOSS;
				}
				active.lastHit = target;
			}
			// swap players
			var tmp = active;
			active = opponent;
			opponent = tmp;
		}
		// that took too long
		return GameResult.DRAW;
	}

	public enum GameResult {
		WIN, DRAW, LOSS
	}

	public static class GameRulesViolatedException extends RuntimeException {

		public final transient BattleshipPlayer offender;

		public GameRulesViolatedException(BattleshipPlayer offender, String message, Throwable cause) {
			super(message, cause);
			this.offender = offender;
		}
	}

	private static class PlayerState implements BattleshipPlayer {

		private static final ExecutorService executor = Executors.newSingleThreadExecutor(x -> {
			var t = new Thread(x);
			t.setDaemon(true);
			return t;
		});

		private final BattleshipPlayer player;

		private final ShipPosition ship;

		private BattleshipField lastHit;

		private boolean lastWasHit;

		private PlayerState(BattleshipPlayer player) {
			this.player = player;
			this.ship = placeYourShip();
		}

		/**
		 * prevent missbehaving players from ruining a turnament.
		 */
		private <T> T timed(Supplier<T> exec) {
			var timeout = player.getClass().equals(HumanPlayer.class) ? NO_TIMEOUT : TIMEOUT_MS; // humans are slow...
			var future = executor.submit(exec::get);
			try {
				if (timeout == NO_TIMEOUT) {
					return future.get();
				}
				return future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				future.cancel(true);
				throw new GameRulesViolatedException(player, "operation took too long", e);
			} catch (ExecutionException e) {
				throw new GameRulesViolatedException(player, "operation crashed: " + e.getMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("unexpected interrupt", e);
			}
		}

		@Override
		public ShipPosition placeYourShip() {
			return timed(() -> Objects.requireNonNull(player.placeYourShip()));
		}

		@Override
		public BattleshipField aimAt(boolean lastShotWasHit) {
			return timed(() -> Objects.requireNonNull(player.aimAt(lastShotWasHit)));
		}

		@Override
		public void opponentAimsAt(BattleshipField position) {
			timed(() -> {
				player.opponentAimsAt(position);
				return null;
			});
		}

		@Override
		public void gameFinished(boolean youHaveWon) {
			timed(() -> {
				player.gameFinished(youHaveWon);
				return null;
			});
		}
	}
}
