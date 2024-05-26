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
    private List<Optional<Unit>> reinforcementsFIRST = new ArrayList<>(),
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
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callReinforcement() {
	    Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		int reinforcementSize = s.getSizeOfReinforcement(activePlayer);
//		Unit[] units = {new Butterfly(UnitColor.ONE), new Butterfly(UnitColor.TWO), new Butterfly(UnitColor.THREE), 
//				new Fairy(UnitColor.ONE), new Fairy(UnitColor.TWO), new Fairy(UnitColor.THREE), 
//				new Unicorn(UnitColor.ONE), new Unicorn(UnitColor.TWO), new Unicorn(UnitColor.THREE)};
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

	@Override
	public void selectTile(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUnit(int rowIndex, int columnIndex) {
		Board board = s.getBoard();
		Player activePlayer = s.getActivePlayer();
		if(!board.getUnit(rowIndex, columnIndex).isEmpty() && board.areValidCoordinates(rowIndex, columnIndex)) {
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
