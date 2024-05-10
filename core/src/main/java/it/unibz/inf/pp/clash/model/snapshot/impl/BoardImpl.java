package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BoardImpl implements Board {

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
    public int countUnits(Snapshot.Player player) {
        Set<Unit> units = new HashSet<>();

        switch (player){
            case FIRST -> {
                for (int rowInd = ((getMaxRowIndex() / 2) + 1); rowInd <= getMaxRowIndex(); rowInd++) {
                    Unit[] row = grid[rowInd];
                    for (Unit unit : row) {
                        if (unit != null) {
                            units.add(unit);
                        }
                    }
                }

            }

            case SECOND -> {
                for (int rowInd = 0; rowInd < ((getMaxRowIndex() / 2) + 1); rowInd++) {
                    Unit[] row = grid[rowInd];
                    for (Unit unit : row) {
                        if (unit != null) {
                            units.add(unit);
                        }
                    }
                }

            }
        }

        return units.size();
    }

    private void checkBoundaries(int rowIndex, int columnIndex) throws CoordinatesOutOfBoardException {
        if (!areValidCoordinates(rowIndex, columnIndex)) {
            throw new CoordinatesOutOfBoardException(rowIndex, columnIndex, getMaxRowIndex(), getMaxColumnIndex());
        }
    }

	@Override
	public void populateBoard(Player p1, Player p2) {
		// TODO Auto-generated method stub
		
	}
}

