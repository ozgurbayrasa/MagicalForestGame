package it.unibz.inf.pp.clash.model.snapshot;

import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.AbstractMobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wall;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Board for an ongoing game.
 * This is a two-dimensional grid.
 * <p>
 * Tile coordinates are natural numbers and start at 0.
 * The top left tile has coordinates (0,0).
 * <p>
 * So for instance, the tile with row index 1 and column index 3 is on the second row (from top to bottom) and
 * fourth column (from left to right).
 * <p>
 * A tile may have a unit standing on it.
 * A same unit may stand on multiple (adjacent) tiles.
 */
public interface Board extends Serializable{

    Map<AbstractMobileUnit, Set<AbstractMobileUnit>> getFormationToSmallUnitsMap(Player player);

    void removeFormationFromMap(Player player, AbstractMobileUnit formation);

    Map<Wall, AbstractMobileUnit> getWallToUnitMap(Player player);

    void removeWallFromMap(Player player, Wall wall);

    /**
     * A pair of coordinates for a tile.
     */
    record TileCoordinates(int rowIndex, int columnIndex){}

    /**
     * @return the maximum possible index for a column (a.k.a. size of a row -1)
     */
    int getMaxColumnIndex();

    /**
     * @return the maximum possible index for a row (a.k.a. size of a column -1)
     */
    int getMaxRowIndex();

    /**
     * @return true if the input coordinates are within the board's boundaries.
     */
    boolean areValidCoordinates(int rowIndex, int columnIndex);

    /**
     * @return the unit standing on the tile at the input coordinates, if any.
     * @throws CoordinatesOutOfBoardException if the coordinates are out of this board's boundaries.
     */
    Optional<Unit> getUnit(int rowIndex, int columnIndex);

    /**
     * Adds the input unit to the tile at the input coordinates.
     *
     * @throws OccupiedTileException if there is already a unit on this tile.
     * @throws CoordinatesOutOfBoardException if the coordinates are out of this board's boundaries.
     */
    void addUnit(int rowIndex, int columnIndex, Unit unit);

    /**
     * Removes the unit currently standing on the tile at the input coordinates.
     * If there was no unit, then this method has no effect.
     *
     * @throws CoordinatesOutOfBoardException if the coordinates are out of this board's boundaries.
     */
    void removeUnit(int rowIndex, int columnIndex);

    /**
     * Moves the unit from the input to the output coordinates.
     * 
     * @param inputRowIndex
     * @param inputColumnIndex
     * @param outputRowIndex
     * @param outputColumnIndex
     */
    void moveUnit(int inputRowIndex, int inputColumnIndex, int outputRowIndex, int outputColumnIndex);

    /**
     * Moves in all units as close to the middle border as possible.
     * 
     * @param player
     */
    void moveUnitsIn(Player player);

    /**
     * Creates a big vertical unit out of the small units.
     * @param centerRowIndex
     * @param columnIndex
     * @return
     */
    AbstractMobileUnit create3x1Formation(int centerRowIndex, int columnIndex);

    /**
     * Moves the specified big vertical unit next to the middle border.
     * @param formation
     * @param centerRowIndex
     * @param columnIndex
     */
    void move3x1In(AbstractMobileUnit formation, int centerRowIndex, int columnIndex);

    /**
     * Moves the specified big horizontal unit next to the middle border.
     * @param wall
     * @param centerRowIndex
     * @param columnIndex
     */
    void moveWallUnitsIn(Wall wall, int centerRowIndex, int columnIndex);

    void removeUnit(Unit unit);

    /**
     *
     *
     *
     * @return allowed unit number for each player.
     */
    int getAllowedUnits();

    /**
     *
     *
     *
     * @return number of units of player.
     */
    int countUnits(Player player);
    
}
