package it.unibz.inf.pp.clash.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.impl.*;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.*;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;
import it.unibz.inf.pp.clash.view.screen.game.GameCompositor;
import static it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl.HeroType.DEFENSIVE;

public class EventHandlerImpl implements EventHandler {

	private final DisplayManager displayManager;
	private final String path = "../core/src/test/java/serialized/snapshot.ser";
	private Snapshot s;
	private Unit unitToSacrifice;
	private int numberOfSacrificesFIRST, numberOfSacrificesSECOND;
	public static boolean modifierMode = false;

	public EventHandlerImpl(DisplayManager displayManager) {
		this.displayManager = displayManager;
	}

	@Override
	public void newGame(String firstHero, String secondHero) {
		// Reset the number of sacrifices per player.
		numberOfSacrificesFIRST = 0;
		numberOfSacrificesSECOND = 0;
		// Initialize a new snapshot.
		s = new SnapshotImpl(firstHero, secondHero);
		// Make sure the modifier select box is hidden.
		GameCompositor.showModifierSelectBox(false);
		// Detect any randomly generated formations.
		detectFormations();
		// Draw the snapshot on screen.
		displayManager.drawSnapshot(s, "A new game has been started!");
	}

	@Override
	public void continueGame(String firstHero, String secondHero) {
		// If there is a snapshot file in the provided directory, initialize it.
		if (new File(path).exists()) {
			try {
				s = SnapshotImpl.deserializeSnapshot(path);
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Snapshot cannot be deserialized.");
			}
			// Draw the snapshot on screen.
			displayManager.drawSnapshot(s, "The game has been continued!");
			// If not, start a new game.
		} else {
			newGame(firstHero, secondHero);
		}
	}

	@Override
	public void exitGame() {
		// Serialize the last snapshot.
		try {
			s.serializeSnapshot(path);
		} catch (IOException e) {
			System.out.println("Snapshot cannot be serialized.");
		}
		displayManager.drawHomeScreen();
	}

	@Override
	public Snapshot getSnapshot() {
		return s;
	}

	// Skip the current turn.
	@Override
	public void skipTurn() {
		Player activePlayer = s.getActivePlayer();
		// End the ongoing move.
		s.setOngoingMove(null);
		// Check if modifier mode is on and switch it off if so.
		if (modifierMode) {
			switchModifierMode();
		}
		// Switch the active player.
		s.setActivePlayer(activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST);
		// Reset number of remaining actions.
		s.setNumberOfRemainingActions(s.getDefaultActionsRemaining());
		// Draw ths snapshot on screen.
		displayManager.drawSnapshot(s, "Player " + activePlayer + " skipped his turn!");
		// Handle any upcoming encounters.
		handleEncounters();
	}

	// End the current turn when the actions are depleted.
	private void endTurnIfNoActionsRemaining() {
		if (s.getNumberOfRemainingActions() == 0) {
			skipTurn();
		}
	}

	@Override
	public void callReinforcement() {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		Random random = new Random();

		// Check if the active player has deleted units.
		if (s.getSizeOfReinforcement(activePlayer) <= 0) {
			updateMessage("No reinforcements available!");
			return;
		}

		// Reset the health of reinforcements.
		s.resetHealthOfReinforcements(activePlayer);

		// Populate the board of the player with their corresponding reinforcement units.
		if (activePlayer == Player.FIRST) {
			placeReinforcements(board, activePlayer, halfBoard, board.getMaxRowIndex(), random);
		} else {
			placeReinforcements(board, activePlayer, 0, halfBoard - 1, random);
		}

		// Move the units next to the border.
		board.moveUnitsIn(activePlayer);

		// Check if a formation is detected, and if so, do not decrement the number of remaining actions.
		if (detectFormations() == 0) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		endTurnIfNoActionsRemaining();
		// Draw the snapshot on screen.
		displayManager.drawSnapshot(s, "Player " + activePlayer + " called reinforcements!");
	}

	// Helper method which populates the board with all reinforcements in the player's set.
	private void placeReinforcements(Board board, Player activePlayer, int startRow, int endRow, Random random) {
		// Repeat until reinforcements are depleted.
		while (s.getSizeOfReinforcement(activePlayer) > 0) {
			// Iterate through each row.
			for (int i = startRow; i <= endRow; i++) {
				// Iterate through each column.
				for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
					if (board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
						if (random.nextBoolean()) {
							// Select random (possibly null) reinforcement unit and add it to the board.
							AbstractMobileUnit unit = getRandomReinforcement(activePlayer, random);
							// Add unit to board and remove from list.
							board.addUnit(i, j, unit);
							s.removeReinforcementFromSet(activePlayer, unit);
							return;
						}
					}
				}
			}
		}
	}

	// Helper method which selects a random reinforcement unit to place.
	private AbstractMobileUnit getRandomReinforcement(Player activePlayer, Random random) {
		int unitIndex = random.nextInt(s.getSizeOfReinforcement(activePlayer));
		for (AbstractMobileUnit u : s.getReinforcementSet(activePlayer)) {
			if (unitIndex-- == 0) {
				return u;
			}
		}
		return null;
	}

	@Override
	public void selectTile(int rowIndex, int columnIndex) throws CoordinatesOutOfBoardException {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is on the active player's board
		if (tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			updateMessage("Error: Selected tile is not on the active player's board.");
			return;
		}

		// Check if a unit is being sacrificed.
		if (GameCompositor.modifierSelectBoxIsShown()) {
			updateMessage("Error: Units cannot be moved while sacrificing a unit.");
			return;
		}

		// Check if the unit is the last in the column.
		if (!unitIsInLastRow(rowIndex, columnIndex, board, activePlayer)) {
			updateMessage("Error: Only units in the last non-empty row of each column can be selected.");
			return;
		}

		// Check if the destination cell is the same or an empty cell in the same column, and if so, cancel the move.
		if (ongoingMove.isPresent() && unitStaysStationary(ongoingMove.get(), rowIndex, columnIndex, board, activePlayer)) {
			s.setOngoingMove(null);
			displayManager.drawSnapshot(s, "Move canceled!");
			return;
		}

		// Check if the selected tile is empty when there isn't any ongoing move
		if (board.getUnit(rowIndex, columnIndex).isEmpty() && ongoingMove.isEmpty()) {
			updateMessage("Error: Selected tile can't be empty when there isn't any ongoing move.");
			return;
		}

		// Check if the selected tile is present when there is an ongoing move.
		if (board.getUnit(rowIndex, columnIndex).isPresent() && ongoingMove.isPresent()) {
			updateMessage("Error: Selected tile must be empty when there is ongoing move.");
			return;
		}

		// Select tile and set according to ongoing move.
		if (ongoingMove.isEmpty()) {
			startNewMove(rowIndex, columnIndex);
		} else if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			completeMove(ongoingMove.get(), rowIndex, columnIndex, board, activePlayer);
		}
	}

	// Helper method which simply returns if the tile is on the input player's board.
	@Override
	public boolean tileIsOnPlayerBoard(Player player, Board board, int rowIndex) {
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		return switch (player) {
			case FIRST -> rowIndex >= halfBoard && rowIndex <= board.getMaxRowIndex();
			case SECOND -> rowIndex >= 0 && rowIndex < halfBoard;
		};
	}

	// Helper method which simplifies the call for updating the message.
	private void updateMessage(String message) {
		try {
			displayManager.updateMessage(message);
		} catch (NoGameOnScreenException e) {
			throw new RuntimeException(e);
		}
	}

	// Helper method for starting a new move.
	private void startNewMove(int rowIndex, int columnIndex) {
		s.setOngoingMove(new Board.TileCoordinates(rowIndex, columnIndex));
		displayManager.drawSnapshot(s, String.format(
				"Tile (%s,%s) has just been selected.",
				rowIndex,
				columnIndex
		));
	}

	// Helper method which checks if the unit is in the max row or if there is a non-empty tile above (resp. below) the selected tile, which means it is in the last present row.
	private boolean unitIsInLastRow(int rowIndex, int columnIndex, Board board, Player activeplayer) {
		return (activeplayer == Player.FIRST && (rowIndex == board.getMaxRowIndex() || board.getUnit(rowIndex + 1, columnIndex).isEmpty())
				|| (activeplayer == Player.SECOND && (rowIndex == 0 || board.getUnit(rowIndex - 1, columnIndex).isEmpty())));
	}

	// Helper method for completing a move.
	private void completeMove(Board.TileCoordinates ongoingMove, int rowIndex, int columnIndex, Board board, Player activePlayer) {
		s.setOngoingMove(null);
		board.moveUnit(ongoingMove.rowIndex(), ongoingMove.columnIndex(), rowIndex, columnIndex);
		board.moveUnitsIn(activePlayer);
		if (detectFormations() == 0) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		endTurnIfNoActionsRemaining();
		displayManager.drawSnapshot(s, "Successful move!");
	}

	// Helper method which checks if the destination column stays the same and if the unit is in the max row or there is an empty tile either above (resp. below) the selected tile, which means that it shouldn't move.
	private boolean unitStaysStationary(Board.TileCoordinates ongoingMove, int rowIndex, int columnIndex, Board board, Player activeplayer) {
		return ongoingMove.rowIndex() == rowIndex && ongoingMove.columnIndex() == columnIndex || (
				activeplayer == Player.FIRST && (ongoingMove.rowIndex() == board.getMaxRowIndex() || board.getUnit(ongoingMove.rowIndex() + 1, ongoingMove.columnIndex()).isEmpty())
						|| (activeplayer == Player.SECOND && (ongoingMove.rowIndex() == 0 || board.getUnit(ongoingMove.rowIndex() - 1, ongoingMove.columnIndex()).isEmpty())))
				&& ongoingMove.columnIndex() == columnIndex;
	}

	@Override
	public void deleteUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is on the active player's board.
		if (tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			updateMessage("Error: Selected tile is not on the active player's board.");
			return;
		}

		// Check if a unit is being sacrificed.
		if (GameCompositor.modifierSelectBoxIsShown()) {
			updateMessage("Error: Units cannot be deleted while sacrificing a unit.");
			return;
		}

		// Check if the tile is empty.
		if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			updateMessage("Error: Selected tile is empty.");
			return;
		}

		// Check if modifier mode is on and switch it off is so.
		if (modifierMode) {
			switchModifierMode();
		}

		// Get the unit.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();

		// Check if there is an ongoing move.
		if (ongoingMove.isPresent()) {
			updateMessage("Error: Cannot delete unit during an ongoing move.");
			// Check if the unit is a formation (since formations cannot be removed).
		} else if (!(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit))) {
			// Add the unit to the reinforcement list and remove it from the board.
			s.addReinforcementToSet(activePlayer, board.getUnit(rowIndex, columnIndex).orElse(null));
			board.removeUnit(rowIndex, columnIndex);
			board.moveUnitsIn(activePlayer);
			// Check if a formation is detected, and if so, do not decrement the number of remaining actions.
			if (detectFormations() == 0) {
				s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
			}
			endTurnIfNoActionsRemaining();
			// Draw the snapshot.
			displayManager.drawSnapshot(s, "Player " + activePlayer + " deleted unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
		}
	}

	// This method handles the detection and moving of wall units (1x3).
	// It returns true if a new wall unit has been detected.
	private boolean detect1x3Formation() {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();

		// Iterate through each cell in the board.
		for (int i = 0; i <= board.getMaxRowIndex(); i++) {
			for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
				// Check if the coordinates to the left and right of the cell are valid.
				if (unitsArePresent(board, i, j - 1, i, j, i, j + 1) && unitsMatch(board, i, j - 1, i, j, i, j + 1, activePlayer)) {
					createWallUnits(board, i, j - 1, j, j + 1);
					return true;
				}
			}
		}
		return false;
	}

	// Helper method, which checks if the coordinates of the adjacent tiles are valid and if the units make a formation on one player's side.
	private boolean unitsArePresent(Board board, int row1, int col1, int row2, int col2, int row3, int col3) {
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		return board.areValidCoordinates(row1, col1) && board.areValidCoordinates(row2, col2) && board.areValidCoordinates(row3, col3)
				&& board.getUnit(row1, col1).isPresent() && board.getUnit(row2, col2).isPresent() && board.getUnit(row3, col3).isPresent()
				&& ((row1 >= halfBoard && row2 >= halfBoard && row3 >= halfBoard) || (row1 <= halfBoard && row2 <= halfBoard && row3 <= halfBoard));
	}

	// Helper method which checks if the classes and colors of the units match and if they are a part of a formation.
	private boolean unitsMatch(Board board, int row1, int col1, int row2, int col2, int row3, int col3, Player activePlayer) {
		// Already checked.
		assert board.getUnit(row1, col1).isPresent() && board.getUnit(row2, col2).isPresent() && board.getUnit(row3, col3).isPresent();

		Unit first = board.getUnit(row1, col1).get();
		Unit second = board.getUnit(row2, col2).get();
		Unit third = board.getUnit(row3, col3).get();

		return first.getClass().equals(second.getClass()) && second.getClass().equals(third.getClass())
				&& first instanceof AbstractMobileUnit && second instanceof AbstractMobileUnit && third instanceof AbstractMobileUnit
				&& !board.getFormationToSmallUnitsMap(activePlayer).containsKey(first) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(second) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(third)
				&& ((AbstractMobileUnit) first).getColor().equals(((AbstractMobileUnit) second).getColor())
				&& ((AbstractMobileUnit) second).getColor().equals(((AbstractMobileUnit) third).getColor());
	}

	// Helper method for creating and moving in wall units.
	private void createWallUnits(Board board, int row, int leftColumn, int centerColumn, int rightColumn) {
		board.moveWallUnitsIn(new Wall(), row, leftColumn);
		board.moveWallUnitsIn(new Wall(), row, centerColumn);
		board.moveWallUnitsIn(new Wall(), row, rightColumn);
	}

	// This method handles the detection and moving of vertical formations (3x1).
	// It returns true if a new formation has been detected.
	private boolean detect3x1Formation() {
		Board board = s.getBoard();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;

		// Check the first player's board, starting from the middle.
		for (int i = halfBoard; i <= board.getMaxRowIndex(); i++) {
			for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
				if (i >= (halfBoard + 1) && unitsArePresent(board, i - 1, j, i, j, i + 1, j) && unitsMatch(board, i - 1, j, i, j, i + 1, j, Player.FIRST)) {
					createVerticalFormation(board, i - 1, i, i + 1, j);
					return true;
				}
			}
		}
		// Check the second player's board, starting from the middle.
		for (int i = halfBoard - 1; i > 0; i--) {
			for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
				if (i < (halfBoard - 1) && unitsArePresent(board, i - 1, j, i, j, i + 1, j) && unitsMatch(board, i - 1, j, i, j, i + 1, j, Player.SECOND)) {
					createVerticalFormation(board, i - 1, i, i + 1, j);
					return true;
				}
			}
		}
		return false;
	}


	// Helper method for creating vertical formations.
	private void createVerticalFormation(Board board, int leftRow, int centerRow, int rightRow, int column) {
		// Already checked.
		assert board.getUnit(leftRow, column).isPresent() && board.getUnit(centerRow, column).isPresent() && board.getUnit(rightRow, column).isPresent();

		// Get the units.
		Unit above = board.getUnit(leftRow, column).get();
		Unit center = board.getUnit(centerRow, column).get();
		Unit below = board.getUnit(rightRow, column).get();

		// Create a formation.
		AbstractMobileUnit formation = board.create3x1Formation(centerRow, column);
		// Set the formation health to the average health of the three units.
		formation.setHealth(Math.round((float) (above.getHealth() + center.getHealth() + below.getHealth()) / 3));
		// Move it next to the border.
		board.move3x1In(formation, centerRow, column);

		((AbstractMobileUnit) above).setAttackCountdown(3);
		((AbstractMobileUnit) center).setAttackCountdown(3);
		((AbstractMobileUnit) below).setAttackCountdown(3);
	}

	// This method handles the detection of formations based on the hero type.
	// It prioritizes wall detection for defensive heroes and formation detection for offensive heroes.
	// It returns the number of formations detected.
	private int detectFormations() {
		int numOfFormations = 0;
		if (s.getHero(s.getActivePlayer()).getHeroType() == DEFENSIVE) {
			while (detect1x3Formation()) {
				numOfFormations++;
			}
			while (detect3x1Formation()) {
				numOfFormations++;
			}
		} else {
			while (detect3x1Formation()) {
				numOfFormations++;
			}
			while (detect1x3Formation()) {
				numOfFormations++;
			}
		}
		return numOfFormations;
	}

	// This method handles encounters. It is called when a player's turn ends.
	// For the new player it checks if there are units with attackCountdown > 1 or == 1.
	public void handleEncounters() {
		// Assigning necessary values (board, active player and opponent player.)
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;

		// Set to track units that have been processed (for decreasing attackCountdown).
		Set<AbstractMobileUnit> unitsWithAttackingCountdown = new HashSet<>();
		Set<AbstractMobileUnit> unitsAttackedAndRemoved = new HashSet<>();

		// Get the range of rows for the active player's board section.
		int[] range = getPlayerBoardRange(activePlayer, board);
		for (int row = range[0]; row <= range[1]; row++) {
			for (int col = 0; col <= board.getMaxColumnIndex(); col++) {
				// Get active player's unit if so.
				Optional<Unit> unitOpt = board.getUnit(row, col);
				// Check if it is mobileUnit. Mobile Units have attackCountdown.
				if (unitOpt.isPresent() && unitOpt.get() instanceof AbstractMobileUnit mobileUnit) {
					// If it is mobile unit and attackCountdown > 1
					if (mobileUnit.getAttackCountdown() > 1) {
						// Add it to the set, then we can simply decrement its attackCountdown
						// !! We can't do it here since in formations, when we change a value of
						// a unit in that formation, all the units in that are effected.
						// If I set attackCountdown to 1, then all  will be 1.
						// So here we don't want to make it 3 times (when we see a mobile unit
						// with attackCountdown more than 1)
						unitsWithAttackingCountdown.add(mobileUnit);

						// If it has attackCountdown 1, so it's time for encounter.
					} else if (mobileUnit.getAttackCountdown() == 1) {
						// Call encounter method for that mobile unit.
						// It will damage all units from opponentPlayer board
						// at that column.
						encounter(board, mobileUnit, opponentPlayer, col);

						// Remove the attacking unit from the board and add it to reinforcements.
						board.removeUnit(row, col);
						// Add this unit to be set to -1 for attackCountdown.
						if (board.getFormationToSmallUnitsMap(activePlayer).containsKey(mobileUnit)) {
							unitsAttackedAndRemoved.addAll(board.getFormationToSmallUnitsMap(activePlayer).get(mobileUnit));
						}
						unitsAttackedAndRemoved.add(mobileUnit);
						s.addReinforcementToSet(activePlayer, mobileUnit);

					}
				}
			}
		}

		// Decrement attackCountdown by 1 for all units with attackCountDown more than 1.
		// For formation there is just one unit from that formation.
		for (AbstractMobileUnit unit : unitsWithAttackingCountdown) {
			unit.setAttackCountdown(unit.getAttackCountdown() - 1);
		}

		// Set attack countdown to -1 for all units attacked and removed.
		// For formation there is just one unit from that formation.
		for (AbstractMobileUnit unit : unitsAttackedAndRemoved) {
			unit.setAttackCountdown(-1);
		}

		// Move units in both player's sections.
		board.moveUnitsIn(Player.FIRST);
		board.moveUnitsIn(Player.SECOND);

		// Update the display after the encounter.
		displayManager.drawSnapshot(s, "Encounter completed!");

		// Check if the opponent player has been defeated.
		if (s.getHero(opponentPlayer).getHealth() <= 0) {
			// If it's the end of the game, clear the board. Display the end of game message.
			clearBoard(board);
			try {
				displayManager.updateMessage(s.getHero(opponentPlayer).getName() + " has been defeated!");
			} catch (NoGameOnScreenException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// Encounter method simply takes attacking unit, board, opponent and
	// attackingColumnIndex.
	@Override
	public void encounter(Board board, AbstractMobileUnit attackingUnit, Player opponent, int attackingColumnIndex) {

		// Attack value is set to the health of the attacking unit.
		int attackValue = attackingUnit.getHealth();

		// Get the range of rows for the opponent's board section.
		int[] opponentRange = getPlayerBoardRange(opponent, board);

		// Determine the direction of iteration based on the opponent player.
		// Here if the opponent player is first, that is okay since its are is from row 6 to 11
		// And we should look from the row which is closest to the middle, and it is 6 to 11
		// for first player.

		// However for second player, it is 0 and 5. But we shouldn't begin from 0 to 5 since
		// if the first row to be damaged will be 5. row. So for second player, we should
		// start from 5 and iterate to 0.

		// Here we get range from getPlayerBoardRange() in ascending order (like (0,5) and (6,11)).
		// So, here according the opponent we decide which for loop to begin. Since the order
		// must be different because of players.
		int beginningStep = (opponent == Player.FIRST) ? opponentRange[0] : opponentRange[1];
		int endStep = (opponent == Player.FIRST) ? opponentRange[1] : opponentRange[0];
		int step = (beginningStep > endStep) ? -1 : 1;

		Set<AbstractMobileUnit> processedUnits = new HashSet<>();

		// Loop over the opponent's area for attackingColumn.
		for (int r = beginningStep; (step == 1) ? (r < endStep) : (r > endStep); r += step) {
			// Get unit on attackingColumn from opponent area.
			Optional<Unit> unitOpt = board.getUnit(r, attackingColumnIndex);
			// If there is a unit on the opponent's row. ATTACK that unit.
			if (unitOpt.isPresent()) {

				Unit opponentUnit = unitOpt.get();
				int unitHealth = opponentUnit.getHealth();
				// If attacking is more than opponent's unit. It must be destroyed.
				if (attackValue >= unitHealth) {
					// Unit is destroyed
					board.removeUnit(r, attackingColumnIndex);
					// Don't forget to add it to the reinforcements list.
					// And set it's attackCountDown to -1 if it's a mobile unit.

					if (opponentUnit instanceof AbstractMobileUnit mobileUnit) {
						processedUnits.add(mobileUnit);
					}
					s.addReinforcementToSet(opponent, opponentUnit);
					// Decrement the attackValue by opponent's unit health.
					attackValue -= unitHealth;
				} else {
					// Opponent's Unit is damaged. Not destroyed.
					opponentUnit.setHealth(unitHealth - attackValue);
					// Attack value is over.
					attackValue = 0;
					break; // Attack value depleted
				}
			}

		}
		for (AbstractMobileUnit mobileUnit : processedUnits) {
			mobileUnit.setAttackCountdown(-1);
		}

		// If after looking for unit's on opponent area, we still have attack value.
		// It means that we reached the player, then we can hit them.
		if (attackValue > 0) {
			applyRemainingDamageToPlayer(opponent, attackValue);
		}
	}

	// Applies remaining damage to the opponent player's health.
	private void applyRemainingDamageToPlayer(Player opponent, int remainingDamage) {
		int playerHealth = s.getHero(opponent).getHealth();
		s.getHero(opponent).setHealth(playerHealth - remainingDamage);
		if (s.getHero(opponent).getHealth() < 0) {
			s.getHero(opponent).setHealth(0);
		}
	}

	// Helper method that returns the board section of the player.
	// This method is similar to tileIsOnPlayerBoard method.
	// We may make it more modular for readability later.
	private int[] getPlayerBoardRange(Player activePlayer, Board board) {
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		if (activePlayer == Player.SECOND) {
			return new int[]{0, halfBoard - 1};
		} else {
			return new int[]{halfBoard, board.getMaxRowIndex()};
		}
	}

	@Override
	public void sacrificeUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();

		// Check if the player has sacrifices left.
		if (rowIndex >= halfBoard) {
			if (numberOfSacrificesFIRST == 3) {
				updateMessage("No sacrifices left.");
				return;
			}
		} else {
			if (numberOfSacrificesSECOND == 3) {
				updateMessage("No sacrifices left.");
				return;
			}
		}

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is on the active player's board.
		if (tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			updateMessage("Error: Selected tile is not on the active player's board.");
			return;
		}

		// Check if the tile is empty.
		if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			updateMessage("Error: Selected tile is empty.");
			return;
		}

		// Check if there is an ongoing move.
		if (ongoingMove.isPresent()) {
			// Check if a unit is being sacrificed.
			if (GameCompositor.modifierSelectBoxIsShown()) {
				// If the same tile is selected cancel the sacrifice.
				if (ongoingMove.get().rowIndex() == rowIndex && ongoingMove.get().columnIndex() == columnIndex) {
					s.setOngoingMove(null);
					GameCompositor.showModifierSelectBox(false);
					displayManager.drawSnapshot(s, "Sacrifice canceled!");
					return;
				}
			}
			updateMessage("Error: Cannot sacrifice unit during an ongoing move.");
			return;
		}

		// Start an ongoing move.
		s.setOngoingMove(new Board.TileCoordinates(rowIndex, columnIndex));

		// Check if modifier mode is on and switch it off is so.
		if (modifierMode) {
			switchModifierMode();
		}

		// Get unit.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();

		// List of strings with modifiers for the select box.
		List<String> modifiers = new ArrayList<>();

		// Unit distribution for Alice:
		// fairy -> rare buff, common trap,
		// unicorn -> epic buff, uncommon trap,
		// butterfly -> legendary buff, rare trap,
		// wall -> wall buff.
		if (s.getHero(activePlayer).getName().equals("Alice(SD)")) {
			// formations
			if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Fairy) {
				modifiers.add("Rare buff(-2 CD, +3 HP)");
				modifiers.add("Common trap(+1 CD, -2 HP");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Unicorn) {
				modifiers.add("Epic buff(-2 CD, +4 HP)");
				modifiers.add("Uncommon trap(+2 CD, -2 HP)");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Butterfly) {
				modifiers.add("Legendary buff(-3 CD, +5 HP)");
				modifiers.add("Rare trap(+2 CD, -3 HP)");
				// small units
			} else if (unit instanceof Fairy) {
				modifiers.add("Rare buff(+3 HP)");
				modifiers.add("Common trap(-1 HP");
				// If the unit to be sacrificed is a small unicorn...
			} else if (unit instanceof Unicorn) {
				modifiers.add("Epic buff(+4 HP)");
				modifiers.add("Uncommon trap(-2 HP)");
				// If the unit to be sacrificed is a small butterfly...
			} else if (unit instanceof Butterfly) {
				modifiers.add("Legendary buff(+5 HP)");
				modifiers.add("Rare trap(-3 HP)");
			} else if (unit.getClass().equals(Wall.class)) {
				modifiers.add("Wall buff");
			}
			// Unit distribution for Carol:
			// fairy -> uncommon buff, common trap,
			// unicorn -> rare buff, uncommon trap,
			// butterfly -> epic buff, rare trap,
			// wall -> wall buff.
		} else if (s.getHero(activePlayer).getName().equals("Carol(MD)")) {
			// formations
			if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Fairy) {
				modifiers.add("Uncommon buff(-2 CD, +2 HP)");
				modifiers.add("Common trap(+1 CD, -2 HP)");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Unicorn) {
				modifiers.add("Rare buff(-2 CD, +3 HP)");
				modifiers.add("Uncommon trap(+2 CD, -2 HP)");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Butterfly) {
				modifiers.add("Epic buff(-2 CD, +4 HP)");
				modifiers.add("Rare trap(+2 CD, -3 HP)");
				// small units
			} else if (unit instanceof Fairy) {
				modifiers.add("Uncommon buff(+2 HP)");
				modifiers.add("Common trap(-1 HP)");
			} else if (unit instanceof Unicorn) {
				modifiers.add("Rare buff(+3 HP)");
				modifiers.add("Uncommon trap(-2 HP)");
			} else if (unit instanceof Butterfly) {
				modifiers.add("Epic buff(+4 HP)");
				modifiers.add("Rare trap(-3 HP)");
			} else if (unit.getClass().equals(Wall.class)) {
				modifiers.add("Wall buff");
			}
			// Unit distribution for Bob:
			// fairy -> common buff, uncommon trap,
			// unicorn -> uncommon buff, rare trap,
			// butterfly -> rare buff, epic trap,
			// wall -> wall trap.
		} else if (s.getHero(activePlayer).getName().equals("Bob(MO)")) {
			// formations
			if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Fairy) {
				modifiers.add("Common buff(-1 CD, +2 HP)");
				modifiers.add("Uncommon trap(+2 CD, -2 HP)");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Unicorn) {
				modifiers.add("Uncommon buff(-2 CD, +2 HP)");
				modifiers.add("Rare trap(+2 CD, -3 HP)");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Butterfly) {
				modifiers.add("Rare buff(-2 CD, +3 HP)");
				modifiers.add("Epic trap(+2 CD, -4 HP)");
				// small units
			} else if (unit instanceof Fairy) {
				modifiers.add("Common buff(+1 HP)");
				modifiers.add("Uncommon trap(-2 HP)");
			} else if (unit instanceof Unicorn) {
				modifiers.add("Uncommon buff(+2 HP)");
				modifiers.add("Rare trap(-3 HP)");
			} else if (unit instanceof Butterfly) {
				modifiers.add("Rare buff(+3 HP)");
				modifiers.add("Epic trap(-4 HP)");
			} else if (unit.getClass().equals(Wall.class)) {
				modifiers.add("Wall trap");
			}
			// Unit distribution for Dan:
			// fairy -> common buff, rare trap,
			// unicorn -> uncommon buff, epic trap,
			// butterfly -> rare buff, legendary trap,
			// wall -> wall trap.
		} else {
			// formations
			if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Fairy) {
				modifiers.add("Common buff(-1 CD, +2 HP)");
				modifiers.add("Rare trap(+2 CD, -3 HP");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Unicorn) {
				modifiers.add("Uncommon buff(-2 CD, +2 HP)");
				modifiers.add("Epic trap(+2 CD, -4 HP)");
			} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Butterfly) {
				modifiers.add("Rare buff(-2 CD, +3 HP)");
				modifiers.add("Legendary trap(+3 CD, -5 HP)");
				// small units
			} else if (unit instanceof Fairy) {
				modifiers.add("Common buff(+1 HP)");
				modifiers.add("Rare trap(-3 HP");
				// If the unit to be sacrificed is a small unicorn...
			} else if (unit instanceof Unicorn) {
				modifiers.add("Uncommon buff(+2 HP)");
				modifiers.add("Epic trap(-4 HP)");
				// If the unit to be sacrificed is a small butterfly...
			} else if (unit instanceof Butterfly) {
				modifiers.add("Rare buff(+3 HP)");
				modifiers.add("Legendary trap(-5 HP)");
			} else if (unit.getClass().equals(Wall.class)) {
				modifiers.add("Wall trap");
			}
		}

		// Save the unit to be sacrificed.
		unitToSacrifice = unit;

		// Make an array out of the list.
		String[] modifiersArray = modifiers.toArray(new String[0]);

		// Set the select box array.
		GameCompositor.setListOfModifiers(modifiersArray);

		// Show the modifier select box.
		GameCompositor.showModifierSelectBox(true);

		// Draw the snapshot.
		displayManager.drawSnapshot(s, "Player " + activePlayer + " sacrificed a " + unit.getClass().getSimpleName() + " at tile (" + rowIndex + ", " + columnIndex + ")!");
	}

	@Override
	public void awardModifier(String modifier) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();

		// End the ongoing move.
		s.setOngoingMove(null);

		// Add the corresponding modifier to the list.
		switch (modifier) {
			case "Common buff(-1 CD, +2 HP)" -> s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.COMMON));
			case "Common trap(+1 CD, -2 HP)" -> s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.COMMON));
			case "Uncommon buff(-2 CD, +2 HP)" ->
					s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.UNCOMMON));
			case "Uncommon trap(+2 CD, -2 HP)" ->
					s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.UNCOMMON));
			case "Rare buff(-2 CD, +3 HP)" -> s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.RARE));
			case "Rare trap(+2 CD, -3 HP)" -> s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.RARE));
			case "Epic buff(-2 CD, +4 HP)" -> s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.EPIC));
			case "Epic trap(+2 CD, -4 HP)" -> s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.EPIC));
			case "Legendary buff(-3 CD, +5 HP)" ->
					s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.LEGENDARY));
			case "Legendary trap(+3 CD, -5 HP)" ->
					s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.LEGENDARY));
			case "Common buff(+1 HP)" -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.COMMON));
			case "Common trap(-1 HP)" -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.COMMON));
			case "Uncommon buff(+2 HP)" -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.UNCOMMON));
			case "Uncommon trap(-2 HP)" -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.UNCOMMON));
			case "Rare buff(+3 HP)" -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.RARE));
			case "Rare trap(-3 HP)" -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.RARE));
			case "Epic buff(+4 HP)" -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.EPIC));
			case "Epic trap(-4 HP)" -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.EPIC));
			case "Legendary buff(+5 HP)" -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.LEGENDARY));
			case "Legendary trap(-5 HP)" -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.LEGENDARY));
			case "Wall buff" -> s.addModifierToList(activePlayer, new WallBuff());
			case "Wall trap" -> s.addModifierToList(activePlayer, new WallTrap());
		}
		// Handle the unit to be sacrificed.
		removeSacrificedUnit(unitToSacrifice, board, activePlayer);
		// Hide the modifier select box.
		GameCompositor.showModifierSelectBox(false);
		// Move the units in.
		board.moveUnitsIn(activePlayer);
		// Decrement the number of remaining actions if no formation is created.
		if (detectFormations() == 0) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		// Increment the number of sacrifices and disable them for the player if they are over 3.
		incrementNumberOfSacrifices(activePlayer);
		// End the turn if the actions are depleted.
		endTurnIfNoActionsRemaining();
		// Draw the snapshot.
		displayManager.drawSnapshot(s, "Player " + activePlayer + " received a modifier!");
	}

	// Helper method for incrementing the number of sacrifices of a player.
	private void incrementNumberOfSacrifices(Player activePlayer) {
		switch (activePlayer) {
			case FIRST -> numberOfSacrificesFIRST++;
			case SECOND -> numberOfSacrificesSECOND++;
		}
	}

	// Helper method which removes the sacrificed unit from the board (and corresponding map if needed).
	private void removeSacrificedUnit(Unit unit, Board board, Player activePlayer) {
		// Check if the unit is a part of a formation and remove the formation from the board and map if so.
		if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit)) {
			removeFormation(unit, activePlayer);
			board.removeFormationFromMap(activePlayer, (AbstractMobileUnit) unit);
			// Check if the unit is a wall and remove it from the board and map if so.
		} else if (unit instanceof Wall) {
			board.removeWallFromMap(activePlayer, (Wall) unit);
			board.removeUnit(unit);
			// Otherwise just remove the unit.
		} else {
			board.removeUnit(unit);
		}
	}

	// Helper method which removes a single whole formation from the board.
	private void removeFormation(Unit unit, Player activePlayer) {
		Board board = s.getBoard();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		switch (activePlayer) {
			case FIRST -> {
				for (int i = halfBoard; i <= board.getMaxRowIndex(); i++) {
					for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
						if (board.getUnit(i, j).isPresent() && board.getUnit(i, j).get().equals(unit)) {
							board.removeUnit(i, j);
						}
					}
				}
			}
			case SECOND -> {
				for (int i = 0; i < halfBoard; i++) {
					for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
						if (board.getUnit(i, j).isPresent() && board.getUnit(i, j).get().equals(unit)) {
							board.removeUnit(i, j);
						}
					}
				}
			}
		}
	}

	@Override
	public void placeModifier(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;

		// Check if modifier mode is on.
		if (!modifierMode) {
			updateMessage("Error: Modifier mode must be on!");
			return;
		}

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is empty.
		if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			updateMessage("Error: Selected tile cannot be empty when placing a modifier.");
			return;
		}

		// Get the unit and the modifier.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();
		Modifier modifier = s.getModifierList(activePlayer).get(0);

		// Check if the tile is on the enemy player's board
		if (modifier instanceof AbstractTrap && tileIsOnPlayerBoard(activePlayer, board, rowIndex)) {
			updateMessage("Error: Selected tile must be on the enemy player's board.");
			return;
		} else if (modifier instanceof AbstractBuff && tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			updateMessage("Error: Selected tile must be on the active player's board.");
			return;
		}

		// Check if a small modifier is placed on a formation.
		if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(unit) && modifier instanceof SmallTrap) {
			updateMessage("Formations can only be damaged by big traps.");
			return;
		} else if (unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && modifier instanceof SmallBuff) {
			updateMessage("Formations can only be affected by big buffs.");
		}

		// Check if a big modifier is placed on a small unit.
		if (unit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(unit) && modifier instanceof BigTrap) {
			updateMessage("Small units can only be damaged by small traps.");
			return;
		} else if (unit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && modifier instanceof BigBuff) {
			updateMessage("Small units can only be affected by small buffs.");
			return;
		}

		// Check if a wall modifier is placed on wall.
		if (unit instanceof Wall && (modifier instanceof WallTrap || modifier instanceof WallBuff || modifier instanceof BigTrap)) {
			updateMessage("Walls can only be affected by small traps.");
			return;
		}

		// Activate the modifier.
		if(!activateModifier(modifier, rowIndex, columnIndex)) {
			updateMessage("Unable to place modifier.");
			return;
		}
		// Switch off modifier mode.
		switchModifierMode();
		// Remove the modifier from the list.
		s.removeModifierFromList(activePlayer, 0);
		// Decrement the number of remaining actions if no formation is created.
		if (detectFormations() == 0) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		// End the turn if the actions are depleted.
		endTurnIfNoActionsRemaining();
		// Draw the snapshot.
		displayManager.drawSnapshot(s, "Modifier placed on tile (" + rowIndex + ", " + columnIndex + ")");
	}

	// Helper method that activates the modifier upon placement.
	// Returns true if a modifier is successfully activated.
	private boolean activateModifier(Modifier modifier, int rowIndex, int columnIndex) {
		Board board = s.getBoard();

		// Assert that the unit is present, since the placeModifier() method has already checked it.
		assert board.getUnit(rowIndex, columnIndex).isPresent();
		// Get the unit.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();

		// Activate a small trap.
		if (modifier instanceof SmallTrap) {
			// Decrease the health of the unit.
			unit.setHealth(unit.getHealth() + modifier.getHealth());
			// Check if the unit's health is depleted and remove it from the board if so.
			if (unit.getHealth() <= 0) {
				board.removeUnit(rowIndex, columnIndex);
			}
			return true;
		}
		// Activate a big trap.
		if (modifier instanceof BigTrap) {
			// Decrease the health of the side units.
			modifySideUnitHealth(unit, modifier);
			// Increase the countdown of the unit.
			((AbstractMobileUnit) unit).setAttackCountdown(((AbstractMobileUnit) unit).getAttackCountdown() + modifier.getCountdown());
			return true;
		}
		// Activate a wall trap.
		if (modifier instanceof WallTrap) {
			// Turn the unit into a wall with corresponding health.
			wallTrapActivation(rowIndex, columnIndex, unit);
			return true;
		}
		// Activate a small buff.
		if (modifier instanceof SmallBuff) {
			// Increase the health of the unit.
			unit.setHealth(unit.getHealth() + modifier.getHealth());
			return true;
		}
		// Activate a big buff.
		if (modifier instanceof BigBuff) {
			// Increase the health of the side units.
			modifySideUnitHealth(unit, modifier);
			// Decrease the countdown of the unit and deploy it if needed.
			if (((AbstractMobileUnit) unit).getAttackCountdown() + modifier.getCountdown() <= 0) {
				((AbstractMobileUnit) unit).setAttackCountdown(1);
				handleEncounters();
			} else {
				((AbstractMobileUnit) unit).setAttackCountdown(((AbstractMobileUnit) unit).getAttackCountdown() + modifier.getCountdown());
			}
			return true;
		}
		// Activate a wall buff.
		if (modifier instanceof WallBuff) {
			// Switch the color of a single unit to create a formation.
			return wallBuffActivation(rowIndex, columnIndex);
		}
		return false;
	}

	// Helper method for big modifiers.
	private void modifySideUnitHealth(Unit unit, Modifier modifier) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;

		// The player who the modifier is applied on.
		Player modifiedPlayer = activePlayer;
		if (modifier instanceof AbstractTrap) {
			modifiedPlayer = opponentPlayer;
		}

		// Iterate over rows.
		for (int i = 0; i <= board.getMaxRowIndex(); i++) {
			// Iterate over columns.
			for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
				// Check if the unit is present and that it doesn't equal the target unit.
				if (board.getUnit(i, j).isPresent() && !board.getUnit(i, j).get().equals(unit)) {
					// Check if the unit above is present, is on the same player's board and matches the target unit.
					if (board.areValidCoordinates(i - 1, j) && board.getUnit(i - 1, j).isPresent()
							&& tileIsOnPlayerBoard(modifiedPlayer, board, i) && board.getUnit(i - 1, j).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, modifiedPlayer, i, j);
						// Check if the unit below is present, is on the same player's board and matches the target unit.
					} else if (board.areValidCoordinates(i + 1, j) && board.getUnit(i + 1, j).isPresent()
							&& tileIsOnPlayerBoard(modifiedPlayer, board, i) && board.getUnit(i + 1, j).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, modifiedPlayer, i, j);
						// Check if the unit to the left is present, is on the same player's board and matches the target unit.
					} else if (board.areValidCoordinates(i, j - 1) && board.getUnit(i, j - 1).isPresent()
							&& board.getUnit(i, j - 1).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, modifiedPlayer, i, j);
						// Check if the unit to the right is present, is on the same player's board and matches the target unit.
					} else if (board.areValidCoordinates(i, j + 1) && board.getUnit(i, j + 1).isPresent()
							&& board.getUnit(i, j + 1).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, modifiedPlayer, i, j);
					}
				}
			}
		}
		// Move the units in.
		board.moveUnitsIn(opponentPlayer);
	}

	// Helper method which modifies the health of side unit.
	private void handleSideUnit(Board board, Modifier modifier, Player modifiedPlayer, int rowIndex, int columnIndex) {
		assert board.getUnit(rowIndex, columnIndex).isPresent();
		// Get the unit.
		Unit sideUnit = board.getUnit(rowIndex, columnIndex).get();
		// Check if the unit is small...
		if ((sideUnit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(modifiedPlayer).containsKey(sideUnit) || sideUnit instanceof Wall)) {
			// Modify the health.
			sideUnit.setHealth(sideUnit.getHealth() + modifier.getHealth());
			// Remove the unit from the board if it's health is depleted.
			if (sideUnit.getHealth() <= 0) {
				board.removeUnit(rowIndex, columnIndex);
			}
			// ...or a part of a formation.
		} else if (sideUnit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(modifiedPlayer).containsKey(sideUnit)) {
			// Modify the health.
			sideUnit.setHealth(sideUnit.getHealth() + Math.round((float) modifier.getHealth() / 3));
			// Remove the formation from the board if it's health is depleted.
			if (sideUnit.getHealth() <= 0) {
				removeFormation(sideUnit, modifiedPlayer);
				board.removeFormationFromMap(modifiedPlayer, (AbstractMobileUnit) sideUnit);
			}
		}
	}

	// Helper method for wall traps.
	private void wallTrapActivation(int rowIndex, int columnIndex, Unit unit) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = activePlayer == Player.FIRST ? Player.SECOND : Player.FIRST;

		// Replace the unit with a wall (with the same health) and move the wall up to the border.
		Wall wall = new Wall();
		wall.setHealth(unit.getHealth());
		board.moveWallUnitsIn(wall, rowIndex, columnIndex);

		// If the small unit replaced by a wall is a part of a formation, only that small unit is replaced, and the remaining formation is split into small units.
		// Index of unit in formation list.
		int index = 0;
		// Iterate over rows.
		for (int i = 0; i <= board.getMaxRowIndex(); i++) {
			// Iterate over columns.
			for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
				// Check if the unit is present and equals the target unit (which means the target unit was a part of a formation).
				if (board.getUnit(i, j).isPresent() && board.getUnit(i, j).get().equals(unit)) {
					// Make a list from formationToSmallUnitsMap.
					List<AbstractMobileUnit> smallUnits = new ArrayList<>(board.getFormationToSmallUnitsMap(opponentPlayer).get((AbstractMobileUnit) unit));
					// Remove the unit.
					board.removeUnit(i, j);
					// Replace it with the next small unit from the list.
					board.addUnit(i, j, smallUnits.get(index));
					// Reset the attack countdown.
					smallUnits.get(index).setAttackCountdown(-1);
					// Increment the index.
					index++;
				}
			}
		}
		// Remove the formation from the map.
		board.removeFormationFromMap(opponentPlayer, (AbstractMobileUnit) unit);
	}

	// Helper method for activating wall buffs.
	private boolean wallBuffActivation(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();

		// Check for 1x3 formations.
		// Check the two cells to the right.
		if(wallBuffActivationOn1x3Auxiliary(board, activePlayer, rowIndex, columnIndex, columnIndex + 1, columnIndex + 2)) {
			return true;
		}
		// Check the cells to the left and right.
		if(wallBuffActivationOn1x3Auxiliary(board, activePlayer, rowIndex, columnIndex, columnIndex - 1, columnIndex + 1)) {
			return true;
		}
		// Check the two cells to the left.
		if(wallBuffActivationOn1x3Auxiliary(board, activePlayer, rowIndex, columnIndex, columnIndex - 1, columnIndex - 2)) {
			return true;
		}

		// Check for 3x1 formations.
		// Check the two cells above.
		if(wallBuffActivationOn3x1Auxiliary(board, activePlayer, rowIndex, rowIndex + 1, rowIndex + 2, columnIndex)) {
			return true;
		}
		// Check the cells below and above.
		if(wallBuffActivationOn3x1Auxiliary(board, activePlayer, rowIndex, rowIndex - 1, rowIndex + 1, columnIndex)) {
			return true;
		}
		// Check the two cells below.
        return wallBuffActivationOn3x1Auxiliary(board, activePlayer, rowIndex, rowIndex - 1, rowIndex + 2, columnIndex);
    }

	// Helper method for wall buff activation on 1x3 formations.
	// Returns true if a 1x3 formation has been created.
	private boolean wallBuffActivationOn1x3Auxiliary(Board board, Player activePlayer, int row, int col1, int col2, int col3) {
		// Check if the cells have valid coordinates.
		if (unitsArePresent(board, row, col1, row, col2, row, col3)) {
			// Already checked.
			assert board.getUnit(row, col1).isPresent() && board.getUnit(row, col2).isPresent() && board.getUnit(row, col3).isPresent();
			// Check if the three units are instances of AbstractMobileUnit.
			if(board.getUnit(row, col1).get() instanceof AbstractMobileUnit first && board.getUnit(row, col2).get() instanceof AbstractMobileUnit second && board.getUnit(row, col3).get() instanceof AbstractMobileUnit third) {
				// Check if the three units are of the same type but the first one has different color.
				if (first.getClass().equals(second.getClass()) && second.getClass().equals(third.getClass())
						&& second.getColor().equals(third.getColor()) && !first.getColor().equals(second.getColor())
						// Check if the units are a part of a formation.
						&& !board.getFormationToSmallUnitsMap(activePlayer).containsKey(first) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(second) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(third)) {
					// Match the color of the first unit to the others.
					first.setColor(second.getColor());
					return true;
				}
			}
		}
		updateMessage("Unable to apply wall buff.");
		return false;
	}

	// Helper method for wall buff activation on 3x1 formations.
	// Returns true if a 3x1 formation has been created.
	private boolean wallBuffActivationOn3x1Auxiliary(Board board, Player activePlayer, int row1, int row2, int row3, int col) {
		// Check if the cells have valid coordinates.
		if (unitsArePresent(board, row1, col, row2, col, row3, col)) {
			// Already checked.
			assert board.getUnit(row1, col).isPresent() && board.getUnit(row2, col).isPresent() && board.getUnit(row3, col).isPresent();
			// Check if the three units are instances of AbstractMobileUnit.
			if (board.getUnit(row1, col).get() instanceof AbstractMobileUnit first && board.getUnit(row2, col).get() instanceof AbstractMobileUnit second && board.getUnit(row3, col).get() instanceof AbstractMobileUnit third) {
				// Check if the three units are of the same type but the first one has different color.
				if (first.getClass().equals(second.getClass()) && second.getClass().equals(third.getClass())
						&& second.getColor().equals(third.getColor()) && !first.getColor().equals(second.getColor())
						// Check if the units are a part of a formation.
						&& !board.getFormationToSmallUnitsMap(activePlayer).containsKey(first) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(second) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(third)) {
					// Match the color of the first unit to the others.
					first.setColor(second.getColor());
					return true;
				}
			}
		}
		updateMessage("Unable to apply wall buff.");
		return false;
	}

	// FOLLOWING METHOD CAN BE IMPLEMENTED IN BOARD

	/**
	 * This method clears the entire board by setting all units to null.
	 * It iterates through all rows and columns of the board and removes each unit.
	 *
	 * @param board The board to be cleared.
	 */
	public void clearBoard(Board board) {
		// Iterate through all rows
		for (int row = 0; row <= board.getMaxRowIndex(); row++) {
			// Iterate through all columns
			for (int col = 0; col <= board.getMaxColumnIndex(); col++) {
				// Remove the unit at the specified row and column
				board.removeUnit(row, col);
			}
		}
	}

	@Override
	public boolean modifierModeIsOn() {
		return modifierMode;
	}

	// Helper method to get the stats of a modifier for the text box.
	private String getModifierStats(Modifier modifier) {
		String plusSignHP = modifier.getHealth() > 0 ? "+" : "";
		String plusSignCD = modifier.getCountdown() > 0 ? "+" : "";
		if (modifier instanceof BigBuff || modifier instanceof BigTrap) {
			return "(" + plusSignCD + modifier.getCountdown() + "CD," + plusSignHP + modifier.getHealth() + "HP)";
		} else if (modifier instanceof SmallBuff || modifier instanceof SmallTrap) {
			return "(" + plusSignHP + modifier.getHealth() + "HP)";
		} else {
			return null;
		}
	}

	@Override
	public void switchModifierMode() {
		Player activePlayer = s.getActivePlayer();
		// Check if there are any modifiers available.
		if (s.getSizeOfModifierList(activePlayer) <= 0) {
			updateMessage("Error: No modifiers available!");
			return;
		}
		// Switch the boolean value.
		modifierMode = !modifierMode;
		// Update the message depending on the new value.
		displayManager.drawSnapshot(s, modifierMode ? (s.getModifierList(activePlayer).get(0) instanceof AbstractTrap ? "Trap" + getModifierStats(s.getModifierList(activePlayer).get(0)) + " picked" : "Buff" + getModifierStats(s.getModifierList(activePlayer).get(0)) + " picked") : "Modifier mode switched off.");
	}
}
