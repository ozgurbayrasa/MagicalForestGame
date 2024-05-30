package it.unibz.inf.pp.clash.model.snapshot;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;


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
     * Serializes the snapshot when exiting a game
     * @param snapshot
     * @throws IOException
     */
    void serializeSnapshot(String path) throws IOException;
    
//    /**
//     * Deserializes the snapshot when starting a game
//     * @return Snapshot
//     * @throws IOException
//     * @throws ClassNotFoundException
//     */
//    Snapshot readSnapshot(String path) throws IOException, ClassNotFoundException;
    
    void populateTiles();
    
    
}
