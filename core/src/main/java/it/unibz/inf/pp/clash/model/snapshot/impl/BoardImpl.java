package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wall;

import java.io.Serial;
import java.util.Optional;

public class BoardImpl implements Board {

    /**
	 * 
	 */
	@Serial
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
        return 20;
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
                // Repeat until all units are moved in.
				for(int n = 0; n < ((getMaxRowIndex() / 2) + 1); n ++) {
                    // Repeat for every row.
					for(int i = ((getMaxRowIndex() / 2) + 2); i <= getMaxRowIndex(); i++) {
                        // Repeat for every column.
			            for(int j = 0; j < getMaxColumnIndex() + 1; j++) {
			            	if(areValidCoordinates(i - 1, j) && getUnit(i - 1, j).isEmpty()) {
								moveUnit(i, j, i - 1, j);
							}
			            }
			        }
				}
			}
			case SECOND -> {
                // Repeat until all units are moved in.
				for(int n = 0; n < ((getMaxRowIndex() / 2) + 1); n ++) {
                    // Repeat for every row.
					for(int i = 0; i < ((getMaxRowIndex() / 2)); i++) {
                        // Repeat for column.
			            for(int j = 0; j < getMaxColumnIndex() + 1; j++) {
			            	if(areValidCoordinates(i + 1, j) && getUnit(i + 1, j).isEmpty()) {
								moveUnit(i, j, i + 1, j);
							}
			            }
			        }
				}
			}
		}
	}

    @Override
    public Unit createBigVerticalUnit(int centerRowIndex, int columnIndex) {
        // Create a "big unit" out of one of the three small units.
        Unit bigUnit = getUnit(centerRowIndex, columnIndex).get();
        // Remove the three small units from the board.
        removeUnit(centerRowIndex - 1, columnIndex);
        removeUnit(centerRowIndex, columnIndex);
        removeUnit(centerRowIndex + 1, columnIndex);
        return bigUnit;
    }

    @Override
    public Unit createWallUnit(int rowIndex, int columnIndex) {
        // Create a wall out of one of the small unit.
        Unit wall = new Wall();
        // Remove the small unit from the board.
        removeUnit(rowIndex, columnIndex);
        return wall;
    }

    // This method takes care of replacing the three small units with the same instance of a "big unit" and moving it as close to the border as possible.
    @Override
    public void moveBigVerticalUnitIn(Unit bigUnit, int centerRowIndex, int columnIndex) {
        int halfBoard = (getMaxRowIndex() / 2) + 1;
        // Check which half the big unit is in.
        if(centerRowIndex > halfBoard) {
            // Move all other units in the column as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.FIRST);
            // Add the "big unit" to the board, as close to the border as possible, depending on the walls.
            int rowIndex = halfBoard;
            while(getUnit(rowIndex, columnIndex).isPresent() && getUnit(rowIndex, columnIndex).get() instanceof Wall) {
                rowIndex++;
            }
            addUnit(rowIndex, columnIndex, bigUnit);
            addUnit(rowIndex + 1, columnIndex, bigUnit);
            addUnit(rowIndex + 2, columnIndex, bigUnit);
            // Move the units back in.
            moveUnitsIn(Player.FIRST);
        } else {
            // Move all other units in the column as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.SECOND);
            // Add the "big unit" to the board, as close to the border as possible, depending on the walls.
            int rowIndex = halfBoard - 1;
            while(getUnit(rowIndex, columnIndex).isPresent() && getUnit(rowIndex, columnIndex).get() instanceof Wall) {
                rowIndex--;
            }
            addUnit(rowIndex, columnIndex, bigUnit);
            addUnit(rowIndex - 1, columnIndex, bigUnit);
            addUnit(rowIndex - 2, columnIndex, bigUnit);
            // Move the units back in.
            moveUnitsIn(Player.SECOND);
        }
    }

    // This method takes care of replacing the three small units with the same instance of a "big unit" and moving it as close to the border as possible.
    @Override
    public void moveWallUnitIn(Unit wall, int rowIndex, int columnIndex) {
        int halfBoard = (getMaxRowIndex() / 2) + 1;
        // Check which half the big unit is in.
        if(rowIndex >= halfBoard) {
            // Move all other units in the columns as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.FIRST);
            // Add the "big unit" to the board.
            addUnit((getMaxRowIndex() / 2) + 1, columnIndex, wall);
            // Move the units back in.
            moveUnitsIn(Player.FIRST);
        } else {
            // Move all other units in the columns as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.SECOND);
            // Add the "big unit" to the board.
            addUnit(getMaxRowIndex() / 2, columnIndex, wall);
            // Move the units back in.
            moveUnitsIn(Player.SECOND);
        }
    }

    // Helper method which moves all units as far away from the middle border as possible.
    private void moveColumnOut(int columnIndex, Player player) {
        int halfBoard = (getMaxColumnIndex() / 2) + 1;
        switch(player) {
            case FIRST -> {
                // Repeat until all units are moved out.
                for(int n = 0; n < halfBoard; n ++) {
                    // Repeat for every row.
                    for(int i = getMaxRowIndex(); i >= (getMaxRowIndex() / 2) + 1; i--) {
                        // Don't move out walls.
                        if(getUnit(i, columnIndex).isPresent() && getUnit(i, columnIndex).get() instanceof Wall) {
                            continue;
                        }
                        // Move one cell closer if the next cell is empty.
                        if(areValidCoordinates(i + 1, columnIndex) && getUnit(i + 1, columnIndex).isEmpty()) {
                            moveUnit(i, columnIndex, i + 1, columnIndex);
                        }
                    }
                }
            }
            case SECOND -> {
                // Repeat until all units are moved out.
                for(int n = 0; n < ((getMaxRowIndex() / 2) + 1); n ++) {
                    // Repeat for every row.
                    for(int i = 0; i < ((getMaxRowIndex() / 2) + 1); i++) {
                        // Don't move out walls.
                        if(getUnit(i, columnIndex).isPresent() && getUnit(i, columnIndex).get() instanceof Wall) {
                            continue;
                        }
                        // Move one cell closer if the next cell is empty.
                        if(areValidCoordinates(i - 1, columnIndex) && getUnit(i - 1, columnIndex).isEmpty()) {
                            moveUnit(i, columnIndex, i - 1, columnIndex);
                        }
                    }
                }
            }
        }
    }
}

