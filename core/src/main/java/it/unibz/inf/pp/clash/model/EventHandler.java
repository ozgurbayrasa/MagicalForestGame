package it.unibz.inf.pp.clash.model;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.AbstractMobileUnit;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

public interface EventHandler {

    /**
     * This method is called when the user starts a new game.
     *
     * @param firstHero name of the hero for the first player
     * @param secondHero name of the hero for the second player
     */
    void newGame(String firstHero, String secondHero);

    /**
     * This method is called if there is an ongoing game and the user interrupts it.
     */
    void exitGame();

    /**
     * @return the current Snapshot
     */
    Snapshot getSnapshot();

    /**
     * This method is called if the active player decides to skip his/her turn.
     */
    void skipTurn();

    /**
     * This method is called when the user calls reinforcements.
     * <p>
     * Note that this may be an "invalid" action.
     * For instance (depending on the rules of the game):
     * - if there is no reinforcement unit,
     * - if there is an ongoing move,
     * - etc.
     */
    void callReinforcement();

    /**
     * This method is called if the user selects the tile with coordinates ({@code rowIndex}, {@code columnIndex}).
     * This tile may for instance be the arrival tile of the ongoing move (if any), or the initial tile of a new move.
     * <p>
     * Note that the coordinates may be "incorrect".
     * For instance (depending on the rules of the game):
     * - if the tile is not on the board of the active player (but instead on the board of the other player),
     * - if there is no ongoing move and the tile is empty,
     * - etc.
     */
    void selectTile(int rowIndex, int columnIndex) throws NoGameOnScreenException;

    /**
     * Helper method simply returns if the tile is on the player's board.
     *
     * @param player
     * @param board
     * @param rowIndex
     * @return
     */
    boolean tileIsOnPlayerBoard(Player player, Board board, int rowIndex);

    /**
     * This method is called if the user tries to delete the unit standing on the tile with coordinates
     * ({@code rowIndex}, {@code columnIndex}).
     * <p>
     * Note that the coordinates may be "incorrect".
     * For instance (depending on the rules of the game):
     * - if there is no unit on this tile,
     * - if the unit on this tile cannot be deleted,
     * - if there is an ongoing move,
     * - etc.
     */
    void deleteUnit(int rowIndex, int columnIndex);

    /**
     * Deletes the unit from the board without adding it to reinforcements in exchange for a modifier or buff.
     * @param rowIndex
     * @param columnIndex
     */
    void sacrificeUnit(int rowIndex, int columnIndex);

    /**
     * Continues the game when the user clicks the appropriate button, given that there is a serialized snapshot at the path specified.
     * If there is no snapshot file, a new game is started.
     *
     * @param firstHero
     * @param secondHero
     */
	void continueGame(String firstHero, String secondHero);

    /**
     * This method is called to handle the encounter between two units.
     * It checks the status of the units, applies the attack value of the attacking unit to the defending unit,
     * and updates the health of both units accordingly.
     *
     *
     */
    void encounter(Board board, AbstractMobileUnit attackingUnit, Player opponent, int col);

    /**
     * This method adds the selected modifier to the modifier list and removes the unit or formation from the board.
     *
     * @param modifier
     */
    void awardModifier(String modifier);

    /**
     * This method handles the placement of modifiers on the opponent player's board.
     *
     * @param rowIndex
     * @param columnIndex
     */
    void placeModifier(int rowIndex, int columnIndex);

    /**
     * This method switches the modifierTime boolean value between true and false.
     */
    void switchModifierMode();

    /**
     * @return the boolean value of modifierMode.
     */
    boolean modifierModeIsOn();
}
