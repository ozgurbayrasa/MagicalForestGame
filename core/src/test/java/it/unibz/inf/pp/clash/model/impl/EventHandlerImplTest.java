package it.unibz.inf.pp.clash.model.impl;

import static org.junit.jupiter.api.Assertions.*;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.impl.EventHandlerImpl;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class EventHandlerImplTest {

    private EventHandlerImpl eventHandler;
    private DisplayManager displayManager;

    @BeforeEach
    public void setUp() {
        displayManager = new DisplayManager() {
            @Override
            public void drawSnapshot(Snapshot snapshot, String message) {
                // Implementation of the drawSnapshot
            }

            @Override
            public void drawHomeScreen() {
                // Implementation of the drawHomeScreen
            }

            @Override
            public void updateMessage(String message) throws NoGameOnScreenException {
                // Implementation of the updateMessage
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
    public void testNewGame() {
        String firstHero = "Hero1";
        String secondHero = "Hero2";

        eventHandler.newGame(firstHero, secondHero);

        Snapshot s = eventHandler.s;

        assertNotNull(s);
        assertEquals("Hero1", s.getHero(Player.FIRST).getName());
        assertEquals("Hero2", s.getHero(Player.SECOND).getName());
        assertNotNull(s.getBoard());
        assertEquals(Player.FIRST, s.getActivePlayer());
    }

    @Test
    public void testContinueGame() throws IOException, ClassNotFoundException {
        String path = "../core/src/test/java/serialized/snapshot.ser";
        Files.createDirectories(Paths.get("../core/src/test/java/serialized/"));
        File file = new File(path);
        file.createNewFile();

        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                BoardImpl.createEmptyBoard(11, 7),
                Player.FIRST,
                0,
                null);
        snapshot.serializeSnapshot(path);

        eventHandler.continueGame();

        assertNotNull(eventHandler.s);
        file.delete();
    }

    @Test
    public void testExitGame() throws IOException {
        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                BoardImpl.createEmptyBoard(11, 7),
                Player.FIRST,
                0,
                null);
        eventHandler.s = snapshot;

        eventHandler.exitGame();

        File file = new File("../core/src/test/java/serialized/snapshot.ser");
        assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void testSkipTurn() {
        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                BoardImpl.createEmptyBoard(11, 7),
                Player.FIRST,
                0,
                null);
        eventHandler.s = snapshot;

        eventHandler.skipTurn();

        assertEquals(Player.SECOND, eventHandler.s.getActivePlayer());
    }

    @Test
    public void testCallReinforcement() throws NoGameOnScreenException {
        Board board = BoardImpl.createEmptyBoard(11, 7);
        SnapshotImpl snapshot = new SnapshotImpl(
                new HeroImpl("Hero1", 20),
                new HeroImpl("Hero2", 20),
                board,
                Player.FIRST,
                0,
                null);
        eventHandler.s = snapshot;
        eventHandler.reinforcementsFIRST.add(Optional.of(new Butterfly(UnitColor.ONE)));

        eventHandler.callReinforcement();

        assertEquals(12, board.countUnits(Player.FIRST));
    }
}