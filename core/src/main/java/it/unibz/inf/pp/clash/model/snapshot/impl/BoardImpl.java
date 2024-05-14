package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class BoardImpl implements Board {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Unit[][] grid;

    public static Board createEmptyBoard(int maxRowIndex, int maxColumnIndex) {
        Unit[][] grid = new Unit[maxRowIndex + 1][maxColumnIndex + 1];
        return new BoardImpl(grid);
    }

    public BoardImpl(Unit[][] grid) {
        this.grid = grid;
    }

    @Override
    public int getMaxRowIndex() {
        return grid.length - 1;
    }

    @Override
    public int getMaxColumnIndex() {
        return grid[0].length - 1;
    }

    @Override
    public boolean areValidCoordinates(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex <= getMaxRowIndex() && columnIndex >= 0 && columnIndex <= getMaxColumnIndex();
    }

    @Override
    public Optional<Unit> getUnit(int rowIndex, int columnIndex) throws CoordinatesOutOfBoardException {
        checkBoundaries(rowIndex, columnIndex);
        return Optional.ofNullable(grid[rowIndex][columnIndex]);
    }

    @Override
    public void addUnit(int rowIndex, int columnIndex, Unit unit) throws OccupiedTileException, CoordinatesOutOfBoardException {
        checkBoundaries(rowIndex, columnIndex);
        if (grid[rowIndex][columnIndex] != null) {
            throw new OccupiedTileException(grid[rowIndex][columnIndex]);
        }
        grid[rowIndex][columnIndex] = unit;
    }

    @Override
    public void removeUnit(int rowIndex, int columnIndex) throws CoordinatesOutOfBoardException {
        checkBoundaries(rowIndex, columnIndex);
        grid[rowIndex][columnIndex] = null;
    }

    // It simply returns 12, we may change this method
    // so player can decide it before the game, or
    // it can stay like fixed number such as 12.
    @Override
    public int getAllowedUnits() {
        return 12;
    }

    // A set is used to count different units on the board.
    // For instance, a big unit which takes 3 squares on the
    // board will be counted as 1 since it is the same unit.

    // If it is the first player, we take the lowest part of the
    // board. Otherwise, we take the upper part of the board. And
    // we look every square in that sub-board.
    @Override
    public int countUnits(Player player) {
    	int unitCount = 0;
        switch (player){
            case FIRST -> {
                for (int rowInd = ((getMaxRowIndex() / 2) + 1); rowInd <= getMaxRowIndex(); rowInd++) {
                    Unit[] row = grid[rowInd];
                    for (Unit unit : row) {
                        if (unit != null) {
                        	unitCount++;
                        }
                    }
                }
            }

            case SECOND -> {
                for (int rowInd = 0; rowInd < ((getMaxRowIndex() / 2) + 1); rowInd++) {
                    Unit[] row = grid[rowInd];
                    for (Unit unit : row) {
                        if (unit != null) {
                        	unitCount++;
                        }
                    }
                }
            }
        }

        return unitCount;
    }

    private void checkBoundaries(int rowIndex, int columnIndex) throws CoordinatesOutOfBoardException {
        if (!areValidCoordinates(rowIndex, columnIndex)) {
            throw new CoordinatesOutOfBoardException(rowIndex, columnIndex, getMaxRowIndex(), getMaxColumnIndex());
        }
	}

	@Override
	public void moveUnit(int inputRowIndex, int inputColumnIndex, int outputRowIndex, int outputColumnIndex) {
		addUnit(outputRowIndex, outputColumnIndex, getUnit(inputRowIndex, inputColumnIndex).orElse(null));
		removeUnit(inputRowIndex, inputColumnIndex);
	}

	@Override
	public void moveUnitsIn(Player player) {
		switch(player) {
			case FIRST -> {
				for(int n = 0; n < ((getMaxRowIndex() + 1) / 2); n ++) {
					for(int i = ((getMaxRowIndex() / 2) + 2); i <= getMaxRowIndex(); i++) {
			            for(int j = 0; j < getMaxColumnIndex() + 1; j++) {
			            	if(getUnit(i - 1, j).isEmpty() && areValidCoordinates(i - 1, j)) {
								moveUnit(i, j, i - 1, j);
							}
			            }
			        }
				}
			}
			case SECOND -> {
				for(int n = 0; n < ((getMaxRowIndex() + 1) / 2); n ++) {
					for(int i = 0; i < ((getMaxRowIndex() / 2)); i++) {
			            for(int j = 0; j < getMaxColumnIndex() + 1; j++) {
			            	if(getUnit(i + 1, j).isEmpty() && areValidCoordinates(i + 1, j)) {
								moveUnit(i, j, i + 1, j);
							}
			            }
			        }
				}
			}
		}
	}
	
}

