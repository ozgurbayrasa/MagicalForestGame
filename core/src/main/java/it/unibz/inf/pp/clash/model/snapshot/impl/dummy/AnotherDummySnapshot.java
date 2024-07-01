//package it.unibz.inf.pp.clash.model.snapshot.impl.dummy;
//
//import it.unibz.inf.pp.clash.model.snapshot.Board;
//import it.unibz.inf.pp.clash.model.snapshot.Hero;
//import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
//import it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;
//import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
//import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
//import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
//import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
//import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
//import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
//import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
//import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
//import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wall;
//
//
//import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.*;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.util.List;
//import java.util.Optional;
//import java.util.Random;
//
///**
// * This class is a dummy implementation, for demonstration purposes.
// * It should not appear in the final project.
// */
//public class AnotherDummySnapshot implements Snapshot {
//
//
//    /**
//	 *
//	 */
//	private static final long serialVersionUID = 1L;
//
//	protected final Board board;
//    private final Hero firstHero;
//    private final Hero secondHero;
//    private Player activeplayer;
//    private int actionsRemaining;
//    protected TileCoordinates ongoingMove;
//
//	public AnotherDummySnapshot(String firstHeroName, String secondHeroName) {
//        firstHero = new HeroImpl(firstHeroName, 20);
//        secondHero = new HeroImpl(secondHeroName, 10);
//        board = BoardImpl.createEmptyBoard(11, 7);
//        activeplayer = Player.FIRST;
//        actionsRemaining = 2;
//        ongoingMove = null;
////        this.ongoingMove = new TileCoordinates(6, 1);
//        populateTiles();
//    }
//
//    public Board getBoard() {
//        return board;
//    }
//
//    @Override
//    public void setOngoingMove(TileCoordinates ongoingMove) {
//        this.ongoingMove = ongoingMove;
//    }
//
//    @Override
//    public Hero getHero(Player player) {
//        return switch (player) {
//            case FIRST -> firstHero;
//            case SECOND -> secondHero;
//        };
//    }
//
//    @Override
//    public Player getActivePlayer() {
//        return activeplayer;
//    }
//
//    @Override
//    public void setActivePlayer(Player nextPlayer) {
//        this.activeplayer = nextPlayer;
//    }
//
//    @Override
//    public Optional<TileCoordinates> getOngoingMove() {
//        return Optional.ofNullable(ongoingMove);
//    }
//
//    @Override
//    public int getNumberOfRemainingActions() {
//        return actionsRemaining;
//    }
//
//    @Override
//    public int getDefaultActionsRemaining() {
//        return 0;
//    }
//
//    @Override
//    public void setNumberOfRemainingActions(int actionsRemaining) {
//
//    }
//
//    public void populateTiles() {
//        Unicorn bigUnicorn = new Unicorn(THREE);
//        bigUnicorn.setAttackCountdown(2);
//        bigUnicorn.setHealth(10);
//        Fairy bigFairy = new Fairy(TWO);
//        bigFairy.setAttackCountdown(3);
//        bigFairy.setHealth(15);
//        //Player 2
//        board.addUnit(4, 0, new Fairy(THREE));
//        board.addUnit(5, 0, new Wall());
//        board.addUnit(4, 2, new Unicorn(ONE));
//        board.addUnit(5, 2, new Unicorn(THREE));
//        board.addUnit(1, 3, new Butterfly(THREE));
//        board.addUnit(2, 3, bigFairy);
//        board.addUnit(3, 3, bigFairy);
//        board.addUnit(4, 3, bigFairy);
//        board.addUnit(5, 3, new Wall());
//        board.addUnit(5, 4, new Wall());
//        board.addUnit(5, 7, new Butterfly(THREE));
//        //Player 1
//        board.addUnit(6, 1, new Butterfly(THREE));
//        board.addUnit(6, 2, new Butterfly(ONE));
//        board.addUnit(7, 2, new Butterfly(TWO));
//        board.addUnit(8, 2, new Fairy(ONE));
//        board.addUnit(6, 4, new Wall());
//        board.addUnit(6, 5, bigUnicorn);
//        board.addUnit(7, 5, bigUnicorn);
//        board.addUnit(8, 5, bigUnicorn);
//        board.addUnit(9, 5, new Fairy(THREE));
//        board.addUnit(6, 7, new Butterfly(ONE));
//        board.addUnit(7, 7, new Butterfly(ONE));
//    }
//
//	@Override
//	public void serializeSnapshot(String path) throws IOException {
//
//	}
//
//	@Override
//	public int getSizeOfReinforcement(Player player) {
//		return 0;
//	}
//
//    @Override
//    public List<Unit> getReinforcementList(Player player) {
//        return List.of();
//    }
//
//    @Override
//    public void addReinforcementToList(Player player, Unit unit) {
//
//    }
//
//    @Override
//    public void removeReinforcementFromList(Player player, int unitIndex) {
//
//    }
//}
