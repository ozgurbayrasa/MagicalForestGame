package it.unibz.inf.pp.clash.model.impl;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.impl.AbstractSnapshot;
import it.unibz.inf.pp.clash.view.DisplayManager;

public class EventHandlerImpl implements EventHandler {

    private final DisplayManager displayManager;
    
    public EventHandlerImpl(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

	@Override
	public void newGame(String firstHero, String secondHero) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitGame() {
		// TODO save snapshot
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
		// TODO Auto-generated method stub
		
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
