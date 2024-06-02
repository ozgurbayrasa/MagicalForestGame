package it.unibz.inf.pp.clash.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;
//import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.*;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.AbstractMobileUnit;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

public class EventHandlerImpl implements EventHandler {

    private final DisplayManager displayManager;
    private final String path = "../core/src/test/java/serialized/snapshot.ser";
    private Snapshot s;

    public EventHandlerImpl(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

	@Override
	public void newGame(String firstHero, String secondHero) {
		s = new SnapshotImpl(firstHero, secondHero);
//		Snapshot dummy1 = new DummySnapshot(firstHero, secondHero);
//		Snapshot dummy2 = new AnotherDummySnapshot(firstHero, secondHero);
		detectBigUnits();
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

	// Skip the current turn.
	@Override
	public void skipTurn() {
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
		displayManager.drawSnapshot(s, "Player " + activePlayer + " skipped his turn!");
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
									int unitIndex = random.nextInt(s.getSizeOfReinforcement(activePlayer));
				                    Unit unit = s.getReinforcementList(activePlayer).get(unitIndex);
									// Add unit to board and remove from list.
				                    board.addUnit(i, j, unit);
									s.removeReinforcementFromList(activePlayer, unitIndex);
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
									int unitIndex = random.nextInt(s.getSizeOfReinforcement(activePlayer));
									Unit unit = s.getReinforcementList(activePlayer).get(unitIndex);
									// Add unit to board and remove from list.
									board.addUnit(i, j, unit);
									s.removeReinforcementFromList(activePlayer, unitIndex);
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
		if(!detectBigUnits()) {
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
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();

		// Check if the selected tile is within the board limits.
		if (!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}

		// Check if the tile is on the active player's board
		Player activePlayer = s.getActivePlayer();
		if (tileIsOnActivePlayerBoard(activePlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile is not on the active player's board.");
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
			startNewMove(rowIndex, columnIndex, board, activePlayer);
		} else if (board.getUnit(rowIndex, columnIndex).isEmpty()) {
			completeMove(ongoingMove.get(), rowIndex, columnIndex, board, activePlayer);
		}
	}

	// Helper method simply returns if tile on active player's board.
	private boolean tileIsOnActivePlayerBoard(Player activePlayer, Board board, int rowIndex) {
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		if (activePlayer == Player.FIRST) {
            return rowIndex < halfBoard || rowIndex > board.getMaxRowIndex();
		} else if (activePlayer == Player.SECOND) {
            return rowIndex < 0 || rowIndex >= halfBoard;
		}
		return true;
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
	private void startNewMove(int rowIndex, int columnIndex, Board board, Player activePlayer) {
		// Check if the unit is the last in the column.
		if(unitIsInLastRow(rowIndex, columnIndex, board, activePlayer)) {
			s.setOngoingMove(new Board.TileCoordinates(rowIndex, columnIndex));
			displayManager.drawSnapshot(s, String.format(
					"Tile (%s,%s) has just been selected.",
					rowIndex,
					columnIndex
			));
		} else {
			displayErrorMessage("Error: Only units in the last non-empty row of each column can be selected.");
		}
	}

	// Helper method which checks if the unit is in the max row or if there is a non-empty tile above (resp. below) the selected tile.
	private boolean unitIsInLastRow(int rowIndex, int columnIndex, Board board, Player activeplayer) {
		return (activeplayer == Player.FIRST && (rowIndex == board.getMaxRowIndex() || board.getUnit(rowIndex + 1, columnIndex).isEmpty())
				|| (activeplayer == Player.SECOND && (rowIndex == 0 || board.getUnit(rowIndex - 1, columnIndex).isEmpty())));
	}

	// Helper method for completing a move which has an ongoing move.
	private void completeMove(Board.TileCoordinates ongoingMove, int rowIndex, int columnIndex, Board board, Player activePlayer) {
		s.setOngoingMove(null);
		// Check if the unit doesn't change position and if so, do not decrement the number of remaining actions.
		if(unitStaysStationary(ongoingMove, columnIndex, board, activePlayer)) {
			displayManager.drawSnapshot(s, "No move made!");
		} else {
			board.moveUnit(ongoingMove.rowIndex(), ongoingMove.columnIndex(), rowIndex, columnIndex);
			board.moveUnitsIn(activePlayer);
			if (!detectBigUnits()) {
				s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
			}
			endTurnIfNoActionsRemaining();
			displayManager.drawSnapshot(s, "Successful move!");
		}
	}

	// Helper method which checks if the destination column stays the same and if the unit is in the max row or there is an empty tile either above (resp. below) the selected tile, which means that the unit doesn't move.
	private boolean unitStaysStationary(Board.TileCoordinates ongoingMove, int columnIndex, Board board, Player activeplayer) {
		return ((activeplayer == Player.FIRST && (ongoingMove.rowIndex() == board.getMaxRowIndex() || board.getUnit(ongoingMove.rowIndex() + 1, ongoingMove.columnIndex()).isEmpty())
				|| (activeplayer == Player. SECOND && (ongoingMove.rowIndex() == 0 || board.getUnit(ongoingMove.rowIndex() - 1, ongoingMove.columnIndex()).isEmpty()))))
				&& ongoingMove.columnIndex() == columnIndex;
	}

	@Override
	public void deleteUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		Optional<Board.TileCoordinates> ongoingMove = s.getOngoingMove();
		// Check if the selected tile is within the board limits.
		if(!board.areValidCoordinates(rowIndex, columnIndex)) {
			return;
		}
		// Check if the tile is on the active player's board.
		if(tileIsOnActivePlayerBoard(activePlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile is not on the active player's board.");
			return;
		}
		// Check if the tile is empty.
		if(board.getUnit(rowIndex, columnIndex).isEmpty()) {
			displayErrorMessage("Error: Selected tile is empty.");
			return;
		}
		// Check if there is an ongoing move.
		if(ongoingMove.isPresent()) {
			displayErrorMessage("Error: Cannot delete unit during an ongoing move.");
		} else {
			// Add the unit to the reinforcement list and remove it from the board.
			s.addReinforcementToList(activePlayer, board.getUnit(rowIndex, columnIndex).orElse(null));
			board.removeUnit(rowIndex, columnIndex);
			board.moveUnitsIn(activePlayer);
			// Check if a big unit is detected, and if so, do not decrement the number of remaining actions.
			if(!detectBigUnits()) {
				s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
			}
			endTurnIfNoActionsRemaining();
			displayManager.drawSnapshot(s, "Player " + activePlayer + " deleted unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
		}
	}

	// This method handles the detection and moving of big units (only vertical for now).
	// It returns true if a new big unit has been detected.
	public boolean detectBigUnits() {
		Board board = s.getBoard();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		// Repeat for every row.
		for(int i = 0; i < board.getMaxRowIndex() + 1; i++) {
			// Repeat for every column.
			for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
				// Check if the coordinates above and below the the cell are valid.
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
						// Check if this class is a subclass of AbstractMobileUnit)
						if(center instanceof AbstractMobileUnit) {
							// Check if the unit has an attack countdown (which means that it is already a part of a big unit) and if the colors of the units match.
							if(((AbstractMobileUnit) center).getAttackCountdown() == -1 && ((AbstractMobileUnit) above).getColor().equals(((AbstractMobileUnit) center).getColor()) && ((AbstractMobileUnit) center).getColor().equals(((AbstractMobileUnit) below).getColor())) {
								// Create a big unit and move it next to the border.
								// If the method returns false,
								board.moveBigVerticalUnitIn(i, j);
								((AbstractMobileUnit) above).setAttackCountdown(3);
								((AbstractMobileUnit) center).setAttackCountdown(3);
								((AbstractMobileUnit) below).setAttackCountdown(3);
								displayManager.drawSnapshot(s, "");
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
