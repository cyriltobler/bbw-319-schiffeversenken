package ch.bbw.m319.battleship.internal;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import ch.bbw.m319.battleship.api.BattleshipArena.GameResult;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

/**
 * Do NOT look at this implementation. Use {@link ch.bbw.m319.battleship.api.BattleshipArena} instead.
 */
public record TournamentGrounds(BattleshipPlayer player1, BattleshipPlayer player2) {

	public static final int MAX_ROUNDS_PER_GAME = 100;

	private static final Duration MAX_TURN_DURATION = Duration.ofMillis(100);

	/**
	 * Turnament mode, with both players going first once.
	 */
	public GameResult playTurnamentMode() {
		var game1 = playIgnoreCrash(true);
		var game2 = playIgnoreCrash(false);
		return game1 == game2 ? game1 : GameResult.DRAW;
	}

	/**
	 * Turnament mode, wher a crash is rated as a loss for the affected player.
	 */
	private GameResult playIgnoreCrash(boolean startWithPlayer1) {
		try {
			return playInternal(startWithPlayer1, MAX_TURN_DURATION);
		} catch (GameRulesViolatedException e) {
			System.err.println("player " + e.offender.getClass().getSimpleName() + " was misbehaving: " + e.getMessage());
			return e.offender == player1 ? GameResult.LOSS : GameResult.WIN;
		}
	}

	public GameResult playDebugMode() {
		return playInternal(true, null);
	}

	private GameResult playInternal(boolean startWithPlayer1, Duration timeout) {
		var p1 = new PlayerState(player1, timeout);
		var p2 = new PlayerState(player2, timeout);
		var active = startWithPlayer1 ? p1 : p2;
		var opponent = startWithPlayer1 ? p2 : p1;
		for (int i = 0; i < MAX_ROUNDS_PER_GAME; i++) {
			var target = active.takeAim();
			active.lastWasHit = opponent.ship.contains(target);
			active.outcomeOfYourTurn(target, active.lastWasHit);
			opponent.outcomeOfOpponentsTurn(target, active.lastWasHit);
			if (active.lastWasHit) {
				if (active.lastHit != null && active.lastHit != target) { // we have a winner!
					active.gameFinished(opponent.ship, true);
					opponent.gameFinished(active.ship, false);
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

		private final Duration timeout;

		private final ShipPosition ship;

		private BattleshipField lastHit;

		private boolean lastWasHit;

		private PlayerState(BattleshipPlayer player, Duration timeout) {
			this.player = player;
			this.timeout = timeout;
			this.ship = placeYourShip();
		}

		/**
		 * prevent missbehaving players from ruining a turnament.
		 */
		private <T> T timed(Supplier<T> exec) {
			if (timeout == null) {
				return exec.get(); // no wrapping requested
			}
			var future = executor.submit(exec::get);
			try {
				return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
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
		public BattleshipField takeAim() {
			return timed(() -> Objects.requireNonNull(player.takeAim()));
		}

		@Override
		public void outcomeOfYourTurn(BattleshipField targetedField, boolean isHit) {
			timed(() -> {
				player.outcomeOfYourTurn(targetedField, isHit);
				return null;
			});
		}

		@Override
		public void outcomeOfOpponentsTurn(BattleshipField targetedField, boolean isHit) {
			timed(() -> {
				player.outcomeOfOpponentsTurn(targetedField, isHit);
				return null;
			});
		}

		@Override
		public void gameFinished(ShipPosition ship, boolean youHaveWon) {
			timed(() -> {
				player.gameFinished(ship, youHaveWon);
				return null;
			});
		}
	}
}
