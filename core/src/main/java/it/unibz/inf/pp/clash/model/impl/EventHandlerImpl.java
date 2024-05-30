package it.unibz.inf.pp.clash.model.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.DummySnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

public class EventHandlerImpl implements EventHandler {

    private final DisplayManager displayManager;
    private final String path = "../core/src/test/java/serialized/snapshot.ser";
    private Snapshot s;
    private final List<Optional<Unit>> reinforcementsFIRST = new ArrayList<>(),
									   reinforcementsSECOND = new ArrayList<>();

    
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
		Snapshot toSerialize = s;
		try {
			toSerialize.serializeSnapshot(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
        displayManager.drawHomeScreen();		
	}

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
		displayManager.drawSnapshot(s, "Player " + activePlayer + " skipped his turn!");
	}
	
	@Override
	public void callReinforcement() {
	    Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		int reinforcementSize = s.getSizeOfReinforcement(activePlayer);
		Random random = new Random();
		if(reinforcementSize <= 0) {
			try {
				displayManager.updateMessage("No available reinforcements!");
				return;
			} catch (NoGameOnScreenException e) {
				e.printStackTrace();
			}
		}
		switch(activePlayer) {
			case FIRST -> {
				loop:
				while(reinforcementSize > 0) {
					for(int i = ((board.getMaxRowIndex() / 2) + 2); i <= board.getMaxRowIndex(); i++) {
			            for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
			            	if(board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
			            		if(random.nextBoolean()) {
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
				board.moveUnitsIn(activePlayer);
			}
			case SECOND -> {
				loop:
				while(reinforcementSize > 0) {
					for(int i = (board.getMaxRowIndex() / 2); i >= 0; i--) {
						for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
			            	if(board.getUnit(i, j).isEmpty() && board.areValidCoordinates(i, j)) {
			            		if(random.nextBoolean()) {
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
				board.moveUnitsIn(activePlayer);
			}
		}
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
		if (!isTileOnActivePlayerBoard(activePlayer, board, rowIndex)) {
			displayErrorMessage("Error: Selected tile is not on the active player's board.");
			return;
		}

		// Check if the selected tile is empty when there isn't any ongoing move
		if (board.getUnit(rowIndex, columnIndex).isEmpty() && ongoingMove.isEmpty()) {
			displayErrorMessage("Error: Selected tile can't be empty when there isn't any ongoing move.");
			return;
		}

		if(board.getUnit(rowIndex, columnIndex).isPresent() && ongoingMove.isPresent()){
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
	private boolean isTileOnActivePlayerBoard(Player activePlayer, Board board, int rowIndex) {
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		if (activePlayer == Player.FIRST) {
			int startRow = halfBoard;
			return rowIndex >= startRow && rowIndex <= board.getMaxRowIndex();
		} else if (activePlayer == Player.SECOND) {
			int endRow = halfBoard;
			return rowIndex >= 0 && rowIndex < endRow;
		}
		return false;
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
		try {
			displayManager.updateMessage("Ongoing move: (" + rowIndex + "," + columnIndex + ")");
		} catch (NoGameOnScreenException e) {
			throw new RuntimeException(e);
		}
	}

	// Helper method for completing a move which has an ongoing move.
	private void completeMove(Board.TileCoordinates ongoingMove, int rowIndex, int columnIndex, Board board, Player activePlayer) {
		board.moveUnit(ongoingMove.rowIndex(), ongoingMove.columnIndex(), rowIndex, columnIndex);
		board.moveUnitsIn(activePlayer);
		s.setOngoingMove(null);
		displayManager.drawSnapshot(s, "Successful move");
	}

	@Override
	public void deleteUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		if(board.getUnit(rowIndex, columnIndex).isPresent() && board.areValidCoordinates(rowIndex, columnIndex)) {
			switch(activePlayer) {
				case FIRST -> {
					if(rowIndex > board.getMaxRowIndex() / 2) {
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
					if(rowIndex < (board.getMaxRowIndex() + 1) / 2) {
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
		} else {
			try {
				displayManager.updateMessage("Cannot remove unit!");
				return;
			} catch (NoGameOnScreenException e) {
				e.printStackTrace();
			}
		}
		displayManager.drawSnapshot(s, "Player " + activePlayer + " deleted unit at Tile (" + rowIndex + ", " + columnIndex + ")!");
	}
}
