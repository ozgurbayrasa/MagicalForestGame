package it.unibz.inf.pp.clash.model.snapshot;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.AbstractMobileUnit;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;


import static it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;

public interface Snapshot extends Serializable{

    enum Player {FIRST, SECOND}

    /**
     * @return the current state of the board
     */
    Board getBoard();

    /**
     * @param player first or second player
     * @return the hero of the input {@code player}
     */
    Hero getHero(Player player);

    /**
     * @return first of second player, depending on whose turn it is
     */
    Player getActivePlayer();

    /**
     * Switches the active player.
     */
    void setActivePlayer(Player nextPlayer);

    /**
     * @return the number of remaining actions for the active player
     */
    int getNumberOfRemainingActions();

    /**
     *
     * @return the default number of remaining actions
     */
    int getDefaultActionsRemaining();

    /**
     * Sets the number of actions remaining.
     * @param actionsRemaining
     */
    void setNumberOfRemainingActions(int actionsRemaining);

    /**
     * @return if the active player has selected the initial tile of his/her next move, then returns the coordinates of
     * this tile.
     * Otherwise, returns Optional.empty().
     */
    Optional<TileCoordinates> getOngoingMove();

    /**
     * Sets the ongoing move.
     */
    void setOngoingMove(TileCoordinates ongoingMove);

    /**
     * @return the number of units that will enter the board if reinforcement is called for the input {@code player}
     */
    int getSizeOfReinforcement(Player player);

    /**
     * Resets the health of reinforcement units to their original health when they are returned to the board.
     *
     * @param player
     */
    void resetHealthofReinforcements(Player player);

    /**
     *
     * @param player
     * @return set of reinforcements for the specified player
     */
    Set<AbstractMobileUnit> getReinforcementSet(Player player);

    /**
     *
     * @param player
     * @param unit deleted from the board
     */
    void addReinforcementToSet(Player player, Unit unit);

    /**
     *
     * @param player
     * @param unit added to the board
     */
    void removeReinforcementFromSet(Player player, AbstractMobileUnit unit);

    /**
     * Serializes the snapshot when exiting a game
     * @param path to Snapshot
     * @throws IOException
     */
    void serializeSnapshot(String path) throws IOException;

    /**
     * Randomly populates the board with units.
     */
    void populateTiles();

    /**
     *
     * @param activePlayer
     * @return the list of modifiers of the given player.
     */
    List<Modifier> getModifierList(Player activePlayer);

    /**
     * Adds a modifier to the list of modifiers of the given player.
     *
     * @param activePlayer
     * @param modifier
     */
    void addModifierToList(Player activePlayer, Modifier modifier);

    /**
     * Removes a modifier from the list of modifiers of the given player.
     *
     * @param player
     * @param modifierIndex
     */
    void removeModifierFromList(Player player, int modifierIndex);

    /**
     *
     * @param activePlayer
     * @return the size of the modifier list of the given player.
     */
    int getSizeOfModifierList(Player activePlayer);
    
}
