package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.AbstractMobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wall;

import java.io.Serial;
import java.util.*;

public class BoardImpl implements Board {

    /**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;
	final Unit[][] grid;
    private final Map<AbstractMobileUnit, Set<AbstractMobileUnit>> bigUnitToSmallUnitsFIRST = new HashMap<>(),
                                                                   bigUnitToSmallUnitsSECOND = new HashMap<>();
    private final Map<Wall, AbstractMobileUnit> wallToUnitFIRST = new HashMap<>(),
                                                wallToUnitSECOND = new HashMap<>();

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
    public AbstractMobileUnit createBigVerticalUnit(int centerRowIndex, int columnIndex) {
        // Create a "big unit" out of one of the three small units.
        return (AbstractMobileUnit) getUnit(centerRowIndex, columnIndex).orElse(null);
    }

    @Override
    public Wall createWallUnit(int rowIndex, int columnIndex) {
        // Create a wall out of one of the small unit.
        return new Wall();
    }

    // This method takes care of replacing the three small units with the same instance of a "big unit" and moving it as close to the border as possible.
    @Override
    public void moveBigVerticalUnitIn(AbstractMobileUnit bigUnit, int centerRowIndex, int columnIndex) {
        int halfBoard = (getMaxRowIndex() / 2) + 1;
        // Create an array out of the three small units.
        Set<AbstractMobileUnit> smallUnits = Set.of((AbstractMobileUnit) getUnit(centerRowIndex - 1, columnIndex).get(), (AbstractMobileUnit) getUnit(centerRowIndex + 1, columnIndex).get());
        // Remove the three small units from the board.
        removeUnit(centerRowIndex - 1, columnIndex);
        removeUnit(centerRowIndex, columnIndex);
        removeUnit(centerRowIndex + 1, columnIndex);
        // Check which half the big unit is in.
        if(centerRowIndex > halfBoard) {
            // Put the entry into the corresponding map.
            bigUnitToSmallUnitsFIRST.put(bigUnit, smallUnits);
            // Move all other units in the column as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.FIRST);
            // Add the "big unit" to the board, as close to the border as possible, depending on the walls.
            int rowOffset = halfBoard;
            while(getUnit(rowOffset, columnIndex).isPresent() && getUnit(rowOffset, columnIndex).get() instanceof Wall) {
                rowOffset++;
            }
            addUnit(rowOffset, columnIndex, bigUnit);
            addUnit(rowOffset + 1, columnIndex, bigUnit);
            addUnit(rowOffset + 2, columnIndex, bigUnit);
            // Move the units back in.
            moveUnitsIn(Player.FIRST);
        } else {
            // Put the entry into the corresponding map.
            bigUnitToSmallUnitsSECOND.put(bigUnit, smallUnits);
            // Move all other units in the column as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.SECOND);
            // Add the "big unit" to the board, as close to the border as possible, depending on the walls.
            int rowOffset = halfBoard - 1;
            while(getUnit(rowOffset, columnIndex).isPresent() && getUnit(rowOffset, columnIndex).get() instanceof Wall) {
                rowOffset--;
            }
            addUnit(rowOffset, columnIndex, bigUnit);
            addUnit(rowOffset - 1, columnIndex, bigUnit);
            addUnit(rowOffset - 2, columnIndex, bigUnit);
            // Move the units back in.
            moveUnitsIn(Player.SECOND);
        }
    }

    // This method takes care of replacing the three small units with the same instance of a "big unit" and moving it as close to the border as possible.
    @Override
    public void moveWallUnitIn(Wall wall, int rowIndex, int columnIndex) {
        int halfBoard = (getMaxRowIndex() / 2) + 1;
        // Get the mobile unit.
        AbstractMobileUnit unit = (AbstractMobileUnit) getUnit(rowIndex, columnIndex).orElse(null);
        // Remove the small unit from the board.
        removeUnit(rowIndex, columnIndex);
        // Check which half the big unit is in.
        if(rowIndex >= halfBoard) {
            // Put the entry into the map.
            wallToUnitFIRST.put(wall, unit);
            // Move all other units in the columns as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.FIRST);
            // Add the wall to the board, as close to the border as possible, depending on the other walls.
            int rowOffset = halfBoard;
            while(getUnit(rowOffset, columnIndex).isPresent() && getUnit(rowOffset, columnIndex).get() instanceof Wall) {
                rowOffset++;
            }
            // Add the wall to the board.
            addUnit(rowOffset, columnIndex, wall);
            // Move the units back in.
            moveUnitsIn(Player.FIRST);
        } else {
            // Put the entry into the map.
            wallToUnitSECOND.put(wall, unit);
            // Move all other units in the columns as far away from the middle border as possible.
            moveColumnOut(columnIndex, Player.SECOND);
            // Add the wall to the board, as close to the border as possible, depending on the other walls.
            int rowOffset = halfBoard - 1;
            while(getUnit(rowOffset, columnIndex).isPresent() && getUnit(rowOffset, columnIndex).get() instanceof Wall) {
                rowOffset--;
            }
            // Add the "big unit" to the board.
            addUnit(rowOffset, columnIndex, wall);
            // Move the units back in.
            moveUnitsIn(Player.SECOND);
        }
    }

    // Helper method which moves all units of a column as far away from the middle border as possible.
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

    @Override
    public Map<AbstractMobileUnit, Set<AbstractMobileUnit>> getBigUnitToSmallUnitsMap(Player player) {
        return switch(player) {
            case FIRST -> bigUnitToSmallUnitsFIRST;
            case SECOND -> bigUnitToSmallUnitsSECOND;
        };
    }

    @Override
    public void removeBigUnitFromMap(Player player, AbstractMobileUnit bigUnit) {
        switch (player) {
            case FIRST -> bigUnitToSmallUnitsFIRST.remove(bigUnit);
            case SECOND -> bigUnitToSmallUnitsSECOND.remove(bigUnit);
        }
    }

    @Override
    public Map<Wall, AbstractMobileUnit> getWallToUnitMap(Player player) {
        return switch(player) {
            case FIRST -> wallToUnitFIRST;
            case SECOND -> wallToUnitSECOND;
        };
    }

    @Override
    public void removeWallFromMap(Player player, Wall wall) {
        switch(player) {
            case FIRST -> wallToUnitFIRST.remove(wall);
            case SECOND -> wallToUnitSECOND.remove(wall);
        }
    }
}

