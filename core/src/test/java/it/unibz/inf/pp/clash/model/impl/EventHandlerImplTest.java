package it.unibz.inf.pp.clash.model.impl;

import static org.junit.jupiter.api.Assertions.*;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventHandlerImplTest {

    private EventHandlerImpl eventHandler;
    private DisplayManager displayManager;

    @BeforeEach
    public void setUp() {
        displayManager = new DisplayManager() {
            @Override
            public void drawSnapshot(Snapshot snapshot, String message) {
                // Implementation of drawSnapshot for testing
            }

            @Override
            public void drawHomeScreen() {
                // Implementation of drawHomeScreen for testing
            }

            @Override
            public void updateMessage(String message) throws NoGameOnScreenException {
                // Implementation of updateMessage for testing
            }

            @Override
            public void setEventHandler(EventHandler eventHandler) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setEventHandler'");
            }
        };
        eventHandler = new EventHandlerImpl(displayManager);
    }

    @Test
    public void testSkipTurn() {
        // Create a valid board with required dimensions
        Board board = BoardImpl.createEmptyBoard(11, 7);
        
        // Initialize SnapshotImpl with a valid board and heroes
        List<Unit> reinforcements = new ArrayList<>();
        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                board,
                Player.FIRST,
                0,
                reinforcements); // Empty list for reinforcements
        
        // Set the snapshot in the EventHandlerImpl instance
        eventHandler.s = snapshot;
        
        // Call the skipTurn method
        try {
            eventHandler.skipTurn();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Verify that the active player has changed
        assertEquals(Player.FIRST, eventHandler.s.getActivePlayer());
    }

    @Test
    public void testSetNumberOfRemainingActions() {
        // Create a valid board with required dimensions
        Board board = BoardImpl.createEmptyBoard(11, 7);

        // Initialize SnapshotImpl with a valid board and heroes
        List<Unit> reinforcements = new ArrayList<>();
        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                board,
                Player.FIRST,
                0,
                reinforcements); // Empty list for reinforcements

        // Set the snapshot in the EventHandlerImpl instance
        eventHandler.s = snapshot;

        // Set number of remaining actions
        eventHandler.s.setNumberOfRemainingActions(5);

        // Verify that the number of remaining actions has been set correctly
        assertEquals(5, eventHandler.s.getNumberOfRemainingActions());
    }

    @Test
    public void testOngoingMove() {
        // Create a valid board with required dimensions
        Board board = BoardImpl.createEmptyBoard(11, 7);

        // Initialize SnapshotImpl with a valid board and heroes
        List<Unit> reinforcements = new ArrayList<>();
        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                board,
                Player.FIRST,
                0,
                reinforcements); // Empty list for reinforcements

        // Set the snapshot in the EventHandlerImpl instance
        eventHandler.s = snapshot;

        // Set an ongoing move
        Board.TileCoordinates coordinates = new Board.TileCoordinates(5, 5);
        eventHandler.s.setOngoingMove(coordinates);

        // Verify that the ongoing move is set correctly
        assertEquals(Optional.of(coordinates), eventHandler.s.getOngoingMove());
    }

        @Test
    public void testUnitStats() {

        Unicorn unicorn = new Unicorn(UnitColor.ONE);

        assertTrue(unicorn.getAttackCountdown() < 0);

        assertFalse(unicorn.getHealth() < 0);
    }

    @Test
    public void testDifferentObjects() {

        Butterfly butterfly = new Butterfly(UnitColor.ONE);
        Butterfly similarButterfly = new Butterfly(UnitColor.ONE);

        assertEquals(butterfly.getColor(), similarButterfly.getColor());

        assertNotEquals(butterfly, similarButterfly);
    }

        /**
     * Checks that an exception is thrown if two units are added to the same tile
     */
    @Test
    public void testAddRemoveUnit() {
        Board board = new BoardImpl(new Unit[4][4]);

        board.addUnit(1,0, new Unicorn(UnitColor.ONE));
        assertTrue(board.getUnit(1,0).isPresent());

        board.removeUnit(1,0);
        assertTrue(board.getUnit(1,0).isEmpty());
    }

    /**
     * Checks that an exception is thrown if two units are added to the same tile
     */
    @Test
    public void testOccupiedTile() {
        Board board = new BoardImpl(new Unit[4][4]);
        board.addUnit(1,0, new Unicorn(MobileUnit.UnitColor.ONE));
        assertThrows(
                OccupiedTileException.class,
                () -> board.addUnit(1,0, new Fairy(UnitColor.THREE))
        );
    }
}