package it.unibz.inf.pp.clash.model.snapshot.impl.dummy;

import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.AbstractSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wall;

import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.*;

import java.io.IOException;

/**
 * This class is a dummy implementation, for demonstration purposes.
 * It should not appear in the final project.
 */
public class DummySnapshot extends AbstractSnapshot implements Snapshot {


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DummySnapshot(String firstHeroName, String secondHeroName) {
        super(
                new HeroImpl(firstHeroName, 20),
                new HeroImpl(secondHeroName, 15),
                BoardImpl.createEmptyBoard(11, 7),
                Player.FIRST,
                3,
                null
        );
//        this.ongoingMove = new TileCoordinates(6, 1);
        populateTiles();
    }


    public void populateTiles() {

        Butterfly bigButterfly = new Butterfly(ONE);
        bigButterfly.setAttackCountdown(1);
        bigButterfly.setHealth(10);
        Unicorn bigUnicorn = new Unicorn(THREE);
        bigUnicorn.setAttackCountdown(2);
        bigUnicorn.setHealth(15);
        Fairy bigFairy = new Fairy(TWO);
        bigFairy.setAttackCountdown(2);
        bigFairy.setHealth(15);

        //Player 2
        board.addUnit(4, 0, new Butterfly(TWO));
        board.addUnit(5, 0, new Wall());
        board.addUnit(1, 2, new Unicorn(ONE));
        board.addUnit(2, 2, new Unicorn(THREE));
        board.addUnit(3, 2, bigButterfly);
        board.addUnit(4, 2, bigButterfly);
        board.addUnit(5, 2, bigButterfly);
        board.addUnit(1, 3, new Butterfly(THREE));
        board.addUnit(2, 3, bigFairy);
        board.addUnit(3, 3, bigFairy);
        board.addUnit(4, 3, bigFairy);
        board.addUnit(5, 3, new Wall());
        board.addUnit(5, 4, new Wall());
        board.addUnit(5, 7, new Butterfly(THREE));
        //Player 1
        board.addUnit(6, 1, new Butterfly(THREE));
        board.addUnit(6, 2, new Butterfly(ONE));
        board.addUnit(7, 2, new Butterfly(TWO));
        board.addUnit(6, 4, new Wall());
        board.addUnit(6, 5, bigUnicorn);
        board.addUnit(7, 5, bigUnicorn);
        board.addUnit(8, 5, bigUnicorn);
        board.addUnit(9, 5, new Fairy(THREE));
        board.addUnit(6, 7, new Butterfly(ONE));
    }

    @Override
    public int getSizeOfReinforcement(Player player) {
        return (board.getAllowedUnits() - board.countUnits(player));
    }


	@Override
	public void serializeSnapshot(String path) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
