package it.unibz.inf.pp.clash.model.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.DummySnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

public class EventHandlerImpl implements EventHandler {

    private final DisplayManager displayManager;
    private final String path = "../core/src/test/java/serialized/snapshot.ser";
    
    public EventHandlerImpl(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

    Snapshot s;
    
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
}
