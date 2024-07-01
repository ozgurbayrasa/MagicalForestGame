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
import it.unibz.inf.pp.clash.model.snapshot.modifiers.impl.AbstractBuff;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.impl.AbstractTrap;
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
	public Snapshot s;
	public List<Optional<Unit>> reinforcementsFIRST = new ArrayList<>();
	private List<Optional<Unit>> reinforcementsSECOND = new ArrayList<>();

	public EventHandlerImpl(DisplayManager displayManager) {
		this.displayManager = displayManager;
	}

	@Override
	public void newGame(String firstHero, String secondHero) {
		s = new SnapshotImpl(
				new HeroImpl(firstHero, 20),
				new HeroImpl(secondHero, 20),
				BoardImpl.createEmptyBoard(11, 7),
				Player.FIRST,
				0,
				null);
		displayManager.drawSnapshot(s, "A new game has been started!");
	}

	@Override
	public void continueGame() {
		if (new File(path).exists()) {
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
		// Get the current active player
		Player activePlayer = s.getActivePlayer();

		// Determine the next player using an if-else statement
		Player nextPlayer;
		if (activePlayer == Player.FIRST) {
			nextPlayer = Player.SECOND;
		} else {
			nextPlayer = Player.FIRST;
		}

		// Use reflection to update the active player field
		try {
			java.lang.reflect.Field field = SnapshotImpl.class.getDeclaredField("activeplayer");
			field.setAccessible(true);
			field.set(s, nextPlayer);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}

		// Update the display with the new snapshot
		displayManager.drawSnapshot(s, "Player " + activePlayer + " skipped his turn!");
	}

	@Override
	public void callReinforcement() {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		Random random = new Random();
		if (reinforcementSize <= 0) {
			try {
				displayManager.updateMessage("No reinforcements available!");
				return;
			} catch (NoGameOnScreenException e) {
				throw new RuntimeException(e);
			}
		}
		switch (activePlayer) {
			case FIRST -> {
				loop: while (reinforcementSize > 0) {
					for (int i = ((board.getMaxRowIndex() / 2) + 2); i <= board.getMaxRowIndex(); i++) {
						for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
							if (board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
								if (random.nextBoolean()) {
									int unitIndex = random.nextInt(reinforcementsFIRST.size());
									Unit unit = reinforcementsFIRST.get(unitIndex).orElse(null);
									board.addUnit(i, j, unit);
									reinforcementsFIRST.remove(unitIndex);
									reinforcementSize--;
									continue loop;
								}
							}
						}
					}
				}
			}
			case SECOND -> {
				loop: while (reinforcementSize > 0) {
					for (int i = (board.getMaxRowIndex() / 2); i >= 0; i--) {
						for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
							if (board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
								if (random.nextBoolean()) {
									int unitIndex = random.nextInt(reinforcementsSECOND.size());
									Unit unit = reinforcementsSECOND.get(unitIndex).orElse(null);
									board.addUnit(i, j, unit);
									reinforcementsSECOND.remove(unitIndex);
									reinforcementSize--;
									continue loop;
								}
							}
						}
					}
				}
			}
		}
		board.moveUnitsIn(activePlayer);
		// Check if a formation is detected, and if so, do not decrement the number of remaining actions.
		if(detectFormations() == 0) {
			s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
		}
		endTurnIfNoActionsRemaining();
		displayManager.drawSnapshot(s, "Player " + activePlayer + " called reinforcements!");
	}

	@Override
	public void requestInformation(int rowIndex, int columnIndex) {
		// TODO info about unit
		try {
			displayManager.updateMessage(
					String.format(
							"Tile (%s,%s), ",
							rowIndex,
							columnIndex));
		} catch (NoGameOnScreenException e) {
			throw new RuntimeException(e);
		}
	}

	// This method simply allows user to select a unit from their board and put it somewhere in their board.
	@Override
	public void selectTile(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		if (!board.getUnit(rowIndex, columnIndex).isEmpty() && board.areValidCoordinates(rowIndex, columnIndex)) {
			switch (activePlayer) {
				case FIRST -> {
					if (rowIndex > board.getMaxRowIndex() / 2) {
						reinforcementsFIRST.add(board.getUnit(rowIndex, columnIndex));
						board.removeUnit(rowIndex, columnIndex);
						s.getSizeOfReinforcement(activePlayer);
					} else {
						try {
							displayManager.updateMessage("Cannot remove unit!");
							return;
						} catch (NoGameOnScreenException e) {
							e.printStackTrace();
						}
					}
				}
				case SECOND -> {
					if (rowIndex < (board.getMaxRowIndex() + 1) / 2) {
						reinforcementsSECOND.add(board.getUnit(rowIndex, columnIndex));
						board.removeUnit(rowIndex, columnIndex);
						s.getSizeOfReinforcement(activePlayer);
					} else {
						try {
							displayManager.updateMessage("Cannot remove unit!");
							return;
						} catch (NoGameOnScreenException e) {
							e.printStackTrace();
						}
					}
				}
			}
			board.moveUnitsIn(activePlayer);
			// Check if a formation is detected, and if so, do not decrement the number of remaining actions.
			if(detectFormations() == 0) {
				s.setNumberOfRemainingActions(s.getNumberOfRemainingActions() - 1);
			}
			endTurnIfNoActionsRemaining();
			displayManager.drawSnapshot(s, "Player " + activePlayer + " deleted unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
		}
	}

	// This method handles the detection and moving of wall units (1x3).
	// It returns true if a new wall unit has been detected.
	private boolean detect1x3Formation() {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();

		// Iterate through each cell in the board.
		for (int i = 0; i < board.getMaxRowIndex() + 1; i++) {
			for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
				// Check if the coordinates to the left and right of the cell are valid.
				if (board.areValidCoordinates(i, j - 1) && board.areValidCoordinates(i, j + 1)
						&& board.getUnit(i, j - 1).isPresent() && board.getUnit(i, j).isPresent() && board.getUnit(i, j + 1).isPresent()) {
					// Get the three units.
					Unit left = board.getUnit(i, j - 1).get();
					Unit center = board.getUnit(i, j).get();
					Unit right = board.getUnit(i, j + 1).get();
					// Check if the three units have the same class.
					if (left.getClass().equals(center.getClass()) && center.getClass().equals(right.getClass())) {
						// Check if this class is a subclass of AbstractMobileUnit.
						if (left instanceof AbstractMobileUnit && center instanceof AbstractMobileUnit && right instanceof AbstractMobileUnit) {
							// Check if the units are already a part of a formation and if their colors match.
							if (!board.getFormationToSmallUnitsMap(activePlayer).containsKey(left) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(center) && !board.getFormationToSmallUnitsMap(activePlayer).containsKey(right) &&
									((AbstractMobileUnit) left).getColor().equals(((AbstractMobileUnit) center).getColor()) && ((AbstractMobileUnit) center).getColor().equals(((AbstractMobileUnit) right).getColor())) {
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
		displayManager.drawSnapshot(s,
				"Player " + activePlayer + " deleted unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
	}
}
