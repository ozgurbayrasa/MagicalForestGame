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
//import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.*;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Buff;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.impl.*;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.*;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;
import it.unibz.inf.pp.clash.view.screen.game.GameCompositor;

public class EventHandlerImpl implements EventHandler {

    private final DisplayManager displayManager;
    private final String path = "../core/src/test/java/serialized/snapshot.ser";
    private Snapshot s;

	// A boolean which is true while the modifier button is pressed and allows players to place modifiers.
	public static boolean modifierMode = false;

    public EventHandlerImpl(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

	@Override
	public void newGame(String firstHero, String secondHero) {
		s = new SnapshotImpl(firstHero, secondHero);
//		Snapshot dummy1 = new DummySnapshot(firstHero, secondHero);
//		Snapshot dummy2 = new AnotherDummySnapshot(firstHero, secondHero);
		detectFormations();
		displayManager.drawSnapshot(s, "A new game has been started!");
	}

	@Override
	public void continueGame() {
		if(new File(path).exists()) {
			try {
				s = SnapshotImpl.deserializeSnapshot(path);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
            displayManager.drawSnapshot(s, "The game has been continued!");
		}
	}

	@Override
	public void exitGame() {
		// Serialize the last snapshot
		try {
			s.serializeSnapshot(path);
		} catch (IOException e) {
			e.printStackTrace();
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
		s.setOngoingMove(null);
		if(modifierMode) {
			switchModifierMode();
		}
		Player activePlayer = s.getActivePlayer();
		Player nextPlayer;
		if (activePlayer == Player.FIRST) {
			nextPlayer = Player.SECOND;
		} else {
			nextPlayer = Player.FIRST;
		}
		s.setActivePlayer(nextPlayer);
		// Reset number of remaining actions.
		s.setNumberOfRemainingActions(s.getDefaultActionsRemaining());
		// Cancels the ongoing move.
		displayManager.drawSnapshot(s, "Player " + activePlayer + " skipped his turn!");
		handleEncounters();
	}

	// End the current turn when the actions are depleted.
	public void endTurnIfNoActionsRemaining() {
		if(s.getNumberOfRemainingActions() == 0) {
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
		if(s.getSizeOfReinforcement(activePlayer) <= 0) {
			try {
				displayManager.updateMessage("No reinforcements available!");
				return;
			} catch (NoGameOnScreenException e) {
				throw new RuntimeException(e);
			}
		}
		switch(activePlayer) {
			case FIRST -> {
				// Repeat until reinforcements are depleted.
				loop:
				while(s.getSizeOfReinforcement(activePlayer) > 0) {
					for(int i = halfBoard; i <= board.getMaxRowIndex(); i++) {
			            for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
			            	if(board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
			            		if(random.nextBoolean()) {
									// Select random (possibly null) reinforcement unit and add it to the board.
									AbstractMobileUnit unit = null;
									int unitIndex = random.nextInt(s.getSizeOfReinforcement(activePlayer));
									for(AbstractMobileUnit u : s.getReinforcementSet(activePlayer)) {
										if (unitIndex-- == 0) {
											unit = u;
											break;
										}
									}
									// Add unit to board and remove from list.
				                    board.addUnit(i, j, unit);
									s.removeReinforcementFromSet(activePlayer, unit);
				                    continue loop;
			            		}
							}
			            }
			        }
				}
			}
			case SECOND -> {
				// Repeat until reinforcements are depleted.
				loop:
				while(s.getSizeOfReinforcement(activePlayer) > 0) {
					for(int i = (board.getMaxRowIndex() / 2); i >= 0; i--) {
						for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
			            	if(board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
			            		if(random.nextBoolean()) {
									// Select random (possibly null) reinforcement unit and add it to the board.
									AbstractMobileUnit unit = null;
									int unitIndex = random.nextInt(s.getSizeOfReinforcement(activePlayer));
									for(AbstractMobileUnit u : s.getReinforcementSet(activePlayer)) {
										if (unitIndex-- == 0) {
											unit = u;
											break;
										}
									}
									// Add unit to board and remove from list.
									board.addUnit(i, j, unit);
									s.removeReinforcementFromSet(activePlayer, unit);
				                    continue loop;
			            		}
							}
			            }
			        }
				}
			}
		}
		board.moveUnitsIn(activePlayer);
		// Check if a big unit is detected, and if so, do not decrement the number of remaining actions.
		if(!detectFormations()) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		endTurnIfNoActionsRemaining();
		displayManager.drawSnapshot(s, "Player " + activePlayer + " called reinforcements!");
	}

	@Override
    public void requestInformation(int rowIndex, int columnIndex) {
		// TODO info about unit?
        try {
            displayManager.updateMessage(
                    String.format(
                            "Tile (%s,%s), ",
                            rowIndex,
                            columnIndex
                    ));
        } catch (NoGameOnScreenException e) {
            throw new RuntimeException(e);
        }
    }

	// This method simply allows user to select a unit from their board and put it somewhere in their board.
	@Override
	public void selectTile(int rowIndex, int columnIndex) throws CoordinatesOutOfBoardException {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is on the active player's board
		if (tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile is not on the active player's board.");
			return;
		}

		// Check if the unit is the last in the column.
		if(!unitIsInLastRow(rowIndex, columnIndex, board, activePlayer)) {
			displayErrorMessage("Error: Only units in the last non-empty row of each column can be selected.");
			return;
		}

		// Check if the destination cell is the same or an empty cell in the same column, and if so, cancel the move.
		if(ongoingMove.isPresent() && unitStaysStationary(ongoingMove.get(), rowIndex, columnIndex, board, activePlayer)) {
			s.setOngoingMove(null);
			displayManager.drawSnapshot(s, "Move canceled!");
			return;
		}

		// Check if the selected tile is empty when there isn't any ongoing move
		if (board.getUnit(rowIndex, columnIndex).isEmpty() && ongoingMove.isEmpty()) {
			displayErrorMessage("Error: Selected tile can't be empty when there isn't any ongoing move.");
			return;
		}

		if (board.getUnit(rowIndex, columnIndex).isPresent() && ongoingMove.isPresent()){
			displayErrorMessage("Error: Selected tile must be empty when there is ongoing move.");
			return;
		}

		// Select tile and set according to ongoing move.
		if (ongoingMove.isEmpty()) {
			startNewMove(rowIndex, columnIndex);
		} else if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			completeMove(ongoingMove.get(), rowIndex, columnIndex, board, activePlayer);
		}
	}

	// Helper method simply returns if tile on active player's board.
	@Override
	public boolean tileIsOnPlayerBoard(Player player, Board board, int rowIndex) {
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		return switch (player) {
			case FIRST -> rowIndex >= halfBoard && rowIndex < board.getMaxRowIndex() + 1;
			case SECOND -> rowIndex >= 0 && rowIndex < halfBoard;
		};
	}

	// Helper method for displaying error messages.
	private void displayErrorMessage(String message) {
		try {
			displayManager.updateMessage(message);
		} catch (NoGameOnScreenException e) {
			throw new RuntimeException(e);
		}
	}

	// Helper method for starting a new move (since there isn't any ongoing move).
	private void startNewMove(int rowIndex, int columnIndex) {
		s.setOngoingMove(new Board.TileCoordinates(rowIndex, columnIndex));
		displayManager.drawSnapshot(s, String.format(
				"Tile (%s,%s) has just been selected.",
				rowIndex,
				columnIndex
		));
	}

	// Helper method which checks if the unit is in the max row or if there is a non-empty tile above (resp. below) the selected tile.
	private boolean unitIsInLastRow(int rowIndex, int columnIndex, Board board, Player activeplayer) {
		return (activeplayer == Player.FIRST && (rowIndex == board.getMaxRowIndex() || board.getUnit(rowIndex + 1, columnIndex).isEmpty())
				|| (activeplayer == Player.SECOND && (rowIndex == 0 || board.getUnit(rowIndex - 1, columnIndex).isEmpty())));
	}

	// Helper method for completing a move which has an ongoing move.
	private void completeMove(Board.TileCoordinates ongoingMove, int rowIndex, int columnIndex, Board board, Player activePlayer) {
		s.setOngoingMove(null);
		board.moveUnit(ongoingMove.rowIndex(), ongoingMove.columnIndex(), rowIndex, columnIndex);
		board.moveUnitsIn(activePlayer);
		if (!detectFormations()) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		endTurnIfNoActionsRemaining();
		displayManager.drawSnapshot(s, "Successful move!");
	}

	// Helper method which checks if the destination column stays the same and if the unit is in the max row or there is an empty tile either above (resp. below) the selected tile, which means that the unit doesn't move.
	private boolean unitStaysStationary(Board.TileCoordinates ongoingMove, int rowIndex, int columnIndex, Board board, Player activeplayer) {
		return (ongoingMove.rowIndex() == rowIndex && ongoingMove.columnIndex() == columnIndex || (activeplayer == Player.FIRST && (ongoingMove.rowIndex() == board.getMaxRowIndex() || board.getUnit(ongoingMove.rowIndex() + 1, ongoingMove.columnIndex()).isEmpty())
				|| (activeplayer == Player. SECOND && (ongoingMove.rowIndex() == 0 || board.getUnit(ongoingMove.rowIndex() - 1, ongoingMove.columnIndex()).isEmpty()))))
				&& ongoingMove.columnIndex() == columnIndex;
	}

	@Override
	public void deleteUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();
		// Check if the selected tile is within the board limits.
		if(!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}
		// Check if the tile is on the active player's board.
		if(tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile is not on the active player's board.");
			return;
		}
		// Check if the tile is empty.
		if(board.getUnit(rowIndex, columnIndex).isEmpty()) {
			displayErrorMessage("Error: Selected tile is empty.");
			return;
		}
		// Get unit.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();
		// Check if there is an ongoing move.
		if(ongoingMove.isPresent()) {
			displayErrorMessage("Error: Cannot delete unit during an ongoing move.");
		// Check if the unit is not a big unit (because big units cannot be removed).
		} else if(!(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit))) {
			// Add the unit to the reinforcement list and remove it from the board.
			s.addReinforcementToSet(activePlayer, board.getUnit(rowIndex, columnIndex).orElse(null));
			board.removeUnit(rowIndex, columnIndex);
			board.moveUnitsIn(activePlayer);
			// Check if a big unit is detected, and if so, do not decrement the number of remaining actions.
			if(!detectFormations()) {
				s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
			}
			endTurnIfNoActionsRemaining();
			displayManager.drawSnapshot(s, "Player " + activePlayer + " deleted unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
		}
	}

	// This method handles the detection and moving of big units (only 3x1 and 1x3 for now).
	// It returns true if a new big unit has been detected.
	private boolean detectFormations() {
		Board board = s.getBoard();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		// Repeat for every row.
		for(int i = 0; i < board.getMaxRowIndex() + 1; i++) {
			// Repeat for every column.
			for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
				// Check if the coordinates above and below the cell are valid.
				if (board.areValidCoordinates(i - 1, j) && board.areValidCoordinates(i + 1, j)
						// Check if there are units in all three cells.
						&& board.getUnit(i - 1, j).isPresent() && board.getUnit(i, j).isPresent() && board.getUnit(i + 1, j).isPresent()
						// Make sure that the three cells are not on the border (the center unit is offset by 1).
						&& (i < halfBoard - 1 || i >= halfBoard + 1)) {
					// Get the three units.
					Unit above = board.getUnit(i - 1, j).get();
					Unit center = board.getUnit(i, j).get();
					Unit below = board.getUnit(i + 1, j).get();
					// Check if the three units have the same class.
					if(above.getClass().equals(center.getClass()) && center.getClass().equals(below.getClass())) {
						// Check if this class is a subclass of AbstractMobileUnit.
						if(center instanceof AbstractMobileUnit) {
							// Check if the unit has an attack countdown (which means that it is already a part of a big unit) and if the colors of the units match.
							if(((AbstractMobileUnit) center).getAttackCountdown() == -1 && ((AbstractMobileUnit) above).getColor().equals(((AbstractMobileUnit) center).getColor()) && ((AbstractMobileUnit) center).getColor().equals(((AbstractMobileUnit) below).getColor())) {
								// Create a big unit and move it next to the border.
								AbstractMobileUnit formation = board.create3x1Formation(i, j);
								board.move3x1In(formation, i, j);
								((AbstractMobileUnit) above).setAttackCountdown(3);
								((AbstractMobileUnit) center).setAttackCountdown(3);
								((AbstractMobileUnit) below).setAttackCountdown(3);
								return true;
							}
						}
					}
				}
				// Check if the coordinates to the left and right of the cell are valid.
				if(board.areValidCoordinates(i, j - 1) && board.areValidCoordinates(i, j + 1)
						// Check if there are units in all three cells.
						&& board.getUnit(i, j - 1).isPresent() && board.getUnit(i, j).isPresent() && board.getUnit(i, j + 1).isPresent()) {
					// Get the three units.
					Unit left = board.getUnit(i, j - 1).get();
					Unit center = board.getUnit(i, j).get();
					Unit right = board.getUnit(i, j + 1).get();
					// Check if the three units have the same class.
					if(left.getClass().equals(center.getClass()) && center.getClass().equals(right.getClass())) {
						// Check if this class is a subclass of AbstractMobileUnit.
						if (center instanceof AbstractMobileUnit) {
							// Check if the unit has an attack countdown (which means that it is already a part of a big unit) and if the colors of the units match.
							if (((AbstractMobileUnit) center).getAttackCountdown() == -1 && ((AbstractMobileUnit) left).getColor().equals(((AbstractMobileUnit) center).getColor()) && ((AbstractMobileUnit) center).getColor().equals(((AbstractMobileUnit) right).getColor())) {
								// Create wall units and move them next to the border.
								Wall leftWall = new Wall();
								Wall centerWall = new Wall();
								Wall rightWall = new Wall();
								board.moveWallUnitsIn(leftWall, i, j - 1);
								board.moveWallUnitsIn(centerWall, i, j);
								board.moveWallUnitsIn(rightWall, i, j + 1);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	// This method handles encounters. It is called when players turn ends. End the turn has
	// just changed to other player (At that moment active player has just gotten the turn.)
	// And for that player handleEncounters check if there are units with attackCountdown > 0
	// or == 0
	public void handleEncounters() {
		// Assigning necessary values (board, active player and opponent player.)
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;

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
						// !! We can't do it here since in big units, when we change a value of
						// a unit in that big unit, all the units in that are effected.
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
						if(board.getFormationToSmallUnitsMap(activePlayer).containsKey(mobileUnit)) {
							unitsAttackedAndRemoved.addAll(board.getFormationToSmallUnitsMap(activePlayer).get(mobileUnit));
						}
						unitsAttackedAndRemoved.add(mobileUnit);
						s.addReinforcementToSet(activePlayer, mobileUnit);

					}
				}
			}
		}

		// Decrement attackCountdown by 1 for all units with attackCountDown more than 1.
		// For big unit there is just one unit from that big unit.
		for (AbstractMobileUnit unit : unitsWithAttackingCountdown) {
			unit.setAttackCountdown(unit.getAttackCountdown() - 1);
		}

		// Set attack countdown to -1 for all units attacked and removed.
		// For big unit there is just one unit from that big unit.
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
		System.out.println("Opponent Range: " + opponentRange[0] + " " + opponentRange[1]);

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
					
					if(opponentUnit instanceof AbstractMobileUnit mobileUnit){
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
		for (AbstractMobileUnit mobileUnit: processedUnits) {
			mobileUnit.setAttackCountdown(-1);
		}

		// If after looking for unit's on opponent area, we still have attack value.
		// It means that we reached the player, then we can hit them.
		if (attackValue > 0) {
			applyRemainingDamageToPlayer(opponent, attackValue);
		}
	}

	private void applyRemainingDamageToPlayer(Player opponent, int remainingDamage) {
		// Apply remaining damage to the opponent player's health.
		int playerHealth = s.getHero(opponent).getHealth();
		s.getHero(opponent).setHealth(playerHealth - remainingDamage);
		if(s.getHero(opponent).getHealth() < 0){
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
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();
		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}
		// Check if the tile is on the active player's board.
		if (tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile is not on the active player's board.");
			return;
		}
		// Check if the tile is empty.
		if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			displayErrorMessage("Error: Selected tile is empty.");
			return;
		}
		// Check if modifier mode is on and switch it off is so.
		if(modifierMode) {
			switchModifierMode();
		}
		// Get unit.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();


		// Generate random number to choose between trap and buff.
		Random random = new Random();
		int randomNumber = random.nextInt(2);
		// Check if there is an ongoing move.
		if (ongoingMove.isPresent()) {
			displayErrorMessage("Error: Cannot sacrifice unit during an ongoing move.");
		} else {
			// Remove the unit or formation from the board and add a corresponding modifier (trap or buff) to the modifier list.
			if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Fairy) {
				switch(randomNumber) {
					case 0 -> s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.COMMON));
					case 1 -> s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.COMMON));
				}
				removeFormation(unit, activePlayer);
				board.removeFormationFromMap(activePlayer, (AbstractMobileUnit) unit);
			} else if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Unicorn) {
				switch(randomNumber) {
					case 0 -> s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.RARE));
					case 1 -> s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.RARE));
				}
				removeFormation(unit, activePlayer);
				board.removeFormationFromMap(activePlayer, (AbstractMobileUnit) unit);
			} else if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && unit instanceof Butterfly) {
				switch(randomNumber) {
					case 0 -> s.addModifierToList(activePlayer, new BigTrap(Modifier.Rarity.EPIC));
					case 1 -> s.addModifierToList(activePlayer, new BigBuff(Modifier.Rarity.EPIC));
				}
				removeFormation(unit, activePlayer);
				board.removeFormationFromMap(activePlayer, (AbstractMobileUnit) unit);
			} else if(unit instanceof Fairy) {
				switch(randomNumber) {
					case 0 -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.COMMON));
					case 1 -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.COMMON));
				}
				board.removeUnit(rowIndex, columnIndex);
			} else if (unit instanceof Unicorn) {
				switch(randomNumber) {
					case 0 -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.RARE));
					case 1 -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.RARE));
				}
				board.removeUnit(rowIndex, columnIndex);
			} else if(unit instanceof Butterfly) {
				switch(randomNumber) {
					case 0 -> s.addModifierToList(activePlayer, new SmallTrap(Modifier.Rarity.EPIC));
					case 1 -> s.addModifierToList(activePlayer, new SmallBuff(Modifier.Rarity.EPIC));
				}
				board.removeUnit(rowIndex, columnIndex);
			} else if(unit.getClass().equals(Wall.class)) {
				s.addModifierToList(activePlayer, new WallTrap(rowIndex, columnIndex));
				board.removeUnit(rowIndex, columnIndex);
			}
			// Move the units in.
			board.moveUnitsIn(activePlayer);
			// Decrement the number of remaining actions if no formation is created.
			if(!detectFormations()) {
				s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
			}
			// End the turn if the actions are depleted.
			endTurnIfNoActionsRemaining();
			// Draw the snapshot.
			displayManager.drawSnapshot(s, "Player " + activePlayer + " sacrificed unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
		}
	}

	// Helper method which removes a single whole formation from the board.
	private void removeFormation(Unit unit, Player activePlayer) {
		Board board = s.getBoard();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		switch (activePlayer) {
			case FIRST -> {
				for(int i = halfBoard; i <= board.getMaxRowIndex(); i++) {
					for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
						if(board.getUnit(i, j).isPresent() && board.getUnit(i, j).get().equals(unit)) {
							board.removeUnit(i, j);
						}
					}
				}
			}
			case SECOND -> {
				for(int i = 0; i < halfBoard; i++) {
					for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
						if(board.getUnit(i, j).isPresent() && board.getUnit(i, j).get().equals(unit)) {
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
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;

		// Check if modifier mode is on.
		if(!modifierMode) {
			displayErrorMessage("Error: Modifier mode must be on!");
			return;
		}

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is empty.
		if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			displayErrorMessage("Error: Selected tile cannot be empty when placing a modifier.");
			return;
		}

		// Get the unit and the modifier.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();
		Modifier modifier = s.getModifierList(activePlayer).get(0);

		// Check if the tile is on the enemy player's board
		if (modifier instanceof Trap && tileIsOnPlayerBoard(activePlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile must be on the enemy player's board.");
			return;
		} else if(modifier instanceof Buff && tileIsOnPlayerBoard(opponentPlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile must be on the active player's board.");
			return;
		}

		// Check if a small modifier is placed on a formation.
		if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(unit) && modifier instanceof SmallTrap) {
			displayErrorMessage("Formations can only be damaged by big traps.");
			return;
		} else if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && modifier instanceof SmallBuff) {
			displayErrorMessage("Formations can only be affected by big buffs.");
		}

		// Check if a big modifier is placed on a small unit.
		if(unit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(unit) && modifier instanceof BigTrap) {
			displayErrorMessage("Small units can only be damaged by small traps.");
			return;
		} else if(unit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(unit) && modifier instanceof BigBuff) {
			displayErrorMessage("Small units can only be affected by small buffs.");
			return;
		}

		// Check if a wall trap is placed on wall.
		if(unit instanceof Wall && (modifier instanceof WallTrap || modifier instanceof BigTrap)) {
			displayErrorMessage("Walls can only be damaged by small traps.");
			return;
		}

		// Activate the modifier.
		activateModifier(modifier, rowIndex, columnIndex);
		// Switch off modifier mode.
		switchModifierMode();
		// Remove the modifier from the list.
		s.removeModifierFromList(activePlayer, 0);
		// Decrement the number of remaining actions if no formation is created.
		if(!detectFormations()) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		// End the turn if the actions are depleted.
		endTurnIfNoActionsRemaining();
		// Draw the snapshot.
		displayManager.drawSnapshot(s,"Modifier placed on tile (" + rowIndex + ", " + columnIndex + ")");
    }

	// Helper method that activates the modifier upon placement.
	private void activateModifier(Modifier modifier, int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;

		// Assert that the unit is present, since the placeModifier() method has already checked it.
		assert board.getUnit(rowIndex, columnIndex).isPresent();
		// Get the unit.
		Unit unit = board.getUnit(rowIndex, columnIndex).get();

		// Activate a small trap.
		if(modifier instanceof SmallTrap) {
			unit.setHealth(unit.getHealth() + modifier.getHealth());
			if (unit.getHealth() <= 0) {
				board.removeUnit(rowIndex, columnIndex);
			}
		// Activate a big trap.
		} else if(modifier instanceof BigTrap) {
			modifySideHealth(unit, modifier);
			((AbstractMobileUnit) unit).setAttackCountdown(((AbstractMobileUnit) unit).getAttackCountdown() + modifier.getCountdown());
		// Activate a wall trap.
		} else if(modifier instanceof WallTrap) {
			if(unit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(unit)) {
				Wall wall = new Wall();
				wall.setHealth(unit.getHealth());
				board.moveWallUnitsIn(wall, rowIndex, columnIndex);
			} else if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(unit)) {
				replaceFormationWithWalls(unit);
			}
		// Activate a small buff.
		} else if(modifier instanceof SmallBuff) {
			unit.setHealth(unit.getHealth() + modifier.getHealth());
		// Activate a big buff.
		} else if(modifier instanceof BigBuff) {
			modifySideHealth(unit, modifier);
			((AbstractMobileUnit) unit).setAttackCountdown(((AbstractMobileUnit) unit).getAttackCountdown() - modifier.getCountdown());
		}
	}

	// Helper method for big modifiers.
	private void modifySideHealth(Unit unit, Modifier modifier) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;

		// Iterate over rows.
		for(int i = 0; i < board.getMaxRowIndex() + 1; i++) {
			// Iterate over columns.
			for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
				// Check if the unit is present and that it doesn't equal the target unit.
				if(board.getUnit(i, j).isPresent() && !board.getUnit(i, j).get().equals(unit)) {
					// Check if the unit above is present, is on the same player's board and matches the target unit.
					if(board.areValidCoordinates(i - 1, j) && board.getUnit(i - 1, j).isPresent()
							&& tileIsOnPlayerBoard(opponentPlayer, board, i) && board.getUnit(i - 1, j).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, opponentPlayer, i, j);
					// Check if the unit below is present, is on the same player's board and matches the target unit.
					} else if (board.areValidCoordinates(i + 1, j) && board.getUnit(i + 1, j).isPresent()
							&& tileIsOnPlayerBoard(opponentPlayer, board, i) && board.getUnit(i + 1, j).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, opponentPlayer, i, j);
					// Check if the unit to the left is present, is on the same player's board and matches the target unit.
					} else if (board.areValidCoordinates(i, j - 1) && board.getUnit(i, j - 1).isPresent()
							&& board.getUnit(i, j - 1).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, opponentPlayer, i, j);
					// Check if the unit to the right is present, is on the same player's board and matches the target unit.
					} else if (board.areValidCoordinates(i, j + 1) && board.getUnit(i, j + 1).isPresent()
							&& board.getUnit(i, j + 1).get().equals(unit)) {
						// Modify the health.
						handleSideUnit(board, modifier, opponentPlayer, i, j);
					}
				}
			}
		}
		// Move the units in.
		board.moveUnitsIn(opponentPlayer);
	}

	// Helper method which modifies health of side unit.
	private void handleSideUnit(Board board, Modifier modifier, Player opponentPlayer, int rowIndex, int columnIndex) {
		assert board.getUnit(rowIndex, columnIndex).isPresent();
		// Get the unit.
		Unit sideUnit = board.getUnit(rowIndex, columnIndex).get();
		// Check if the unit is small...
		if((sideUnit instanceof AbstractMobileUnit && !board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(sideUnit) || sideUnit instanceof Wall)) {
			// Modify the health.
			sideUnit.setHealth(sideUnit.getHealth() + modifier.getHealth());
			// Remove the unit from the board if it's health is depleted.
			if(sideUnit.getHealth() <= 0) {
				board.removeUnit(rowIndex, columnIndex);
			}
			// ...or a formation
		} else if(sideUnit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(opponentPlayer).containsKey(sideUnit)) {
			// Modify the health.
			sideUnit.setHealth(sideUnit.getHealth() + Math.round((float) modifier.getHealth() / 3));
			// Remove the formation from the board if it's health is depleted.
			if (sideUnit.getHealth() <= 0) {
				removeFormation(sideUnit, opponentPlayer);
				board.removeFormationFromMap(opponentPlayer, (AbstractMobileUnit) sideUnit);
			}
		}
	}

	// Helper method for wall traps.
	private void replaceFormationWithWalls(Unit unit) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Player opponentPlayer = (activePlayer == Player.FIRST) ? Player.SECOND : Player.FIRST;
		loop:
		// Iterate over rows.
		for(int i = 0; i < board.getMaxRowIndex() + 1; i++) {
			// Iterate over columns.
			for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
				// Check if the unit is present and equals the target unit.
				if (board.getUnit(i, j).isPresent() && board.getUnit(i, j).get().equals(unit)) {
					// Replace the unit with a wall (with the same health) and move the wall up to the border.
					Wall wall = new Wall();
					wall.setHealth(unit.getHealth());
					board.moveWallUnitsIn(wall, i, j);
					break loop;
				}
			}
		}
		// If the small unit replaced by a wall is a part of a formation, only that small unit is replaced, and the remaining formation is split into small units.
		// Index of unit in formation list.
		int index = 0;
		// Iterate over rows.
		for(int i = 0; i < board.getMaxRowIndex() + 1; i++) {
			// Iterate over columns.
			for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
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

	@Override
	public void switchModifierMode() {
		Player activePlayer = s.getActivePlayer();
		// Check if there are any modifiers available.
		if(s.getSizeOfModifierList(activePlayer) <= 0) {
			displayErrorMessage("Error: No modifiers available!");
			return;
		}
		// Switch the boolean value.
		modifierMode = !modifierMode;
		// Update the message depending on the new value.
		if(modifierMode) {
			if(s.getModifierList(activePlayer).get(0) instanceof Trap) {
				try {
					displayManager.updateMessage("Trap picked.");
				} catch (NoGameOnScreenException e) {
					throw new RuntimeException(e);
				}
			} else {
				try {
					displayManager.updateMessage("Buff picked.");
				} catch (NoGameOnScreenException e) {
					throw new RuntimeException(e);
				}
			}
        } else {
            try {
                displayManager.updateMessage("Modifier mode switched off.");
            } catch (NoGameOnScreenException e) {
                throw new RuntimeException(e);
            }
        }
	}
}
