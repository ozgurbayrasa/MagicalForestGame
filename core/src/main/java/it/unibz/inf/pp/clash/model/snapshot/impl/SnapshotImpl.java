package it.unibz.inf.pp.clash.model.snapshot.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

public class SnapshotImpl implements Snapshot {

	 /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected final Board board;
	    private final Hero firstHero;
	    private final Hero secondHero;

	    private Player activeplayer;
	    private int actionsRemaining;
	    protected TileCoordinates ongoingMove;

		private final List<Unit> reinforcementsFIRST = new ArrayList<>(),
								 reinforcementsSECOND = new ArrayList<>();

	    public SnapshotImpl(Hero firstHero, Hero secondHero, Board board, Player activeplayer, int actionsRemaining,
				TileCoordinates ongoingMove) {
	    	this.board = board;
	        this.firstHero = firstHero;
	        this.secondHero = secondHero;
	        this.activeplayer = activeplayer;
	        this.actionsRemaining = actionsRemaining;
	        this.ongoingMove = ongoingMove;
			populateTiles();
		}

		public Board getBoard() {
	        return board;
	    }

	    @Override
	    public Hero getHero(Player player) {
	        return switch (player) {
	            case FIRST -> firstHero;
	            case SECOND -> secondHero;
	        };
	    }

	    @Override
	    public Player getActivePlayer() {
	        return activeplayer;
	    }

		@Override
		public void setActivePlayer(Player nextPlayer) {
			this.activeplayer = nextPlayer;
		}

	    @Override
	    public Optional<TileCoordinates> getOngoingMove() {
	        return Optional.ofNullable(ongoingMove);
	    }

		@Override
		public void setOngoingMove(TileCoordinates ongoingMove) {
			this.ongoingMove = ongoingMove;
		}

		@Override
	    public int getNumberOfRemainingActions() {
	        return actionsRemaining;
	    }

		@Override
		public void setNumberOfRemainingActions(int defaultActionsRemaining) {
			actionsRemaining = defaultActionsRemaining;
		}
	    
	    @Override
	    public void populateTiles() {
			// Array of all possible units.
	    	Unit[] units = {new Butterfly(UnitColor.ONE), new Butterfly(UnitColor.TWO), new Butterfly(UnitColor.THREE), 
	    					new Fairy(UnitColor.ONE), new Fairy(UnitColor.TWO), new Fairy(UnitColor.THREE), 
	    					new Unicorn(UnitColor.ONE), new Unicorn(UnitColor.TWO), new Unicorn(UnitColor.THREE)};
			Random random = new Random();
			int numberOfPlacedUnits = 0;
			// Repeat for every row.
			for(int i = (board.getMaxRowIndex() / 2) + 1; i <= board.getMaxRowIndex(); i++) {
				// Repeat for every column.
				for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
					if(numberOfPlacedUnits < board.getAllowedUnits()) {
						if(random.nextBoolean()) {
							// Select random (possibly null) unit and add it to the board.
							int unitIndex = random.nextInt(units.length);
		                    Unit unit = units[unitIndex];
		                    board.addUnit(i, j, unit);
							numberOfPlacedUnits++;
						}
					}
				}
			}
			board.moveUnitsIn(Player.FIRST);

			numberOfPlacedUnits = 0;
			// Repeat for every row.
			for(int i = (board.getMaxRowIndex() / 2); i >= 0; i--) {
				// Repeat for every column.
				for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
					if(numberOfPlacedUnits < board.getAllowedUnits()) {
						if(random.nextBoolean()) {
							// Select random (possibly null) unit and add it to the board.
							int unitIndex = random.nextInt(units.length);
		                    Unit unit = units[unitIndex];
		                    board.addUnit(i, j, unit);
							numberOfPlacedUnits++;
						}
					}
				}
			}
			board.moveUnitsIn(Player.SECOND);
	    }			

	    @Override
	    public void serializeSnapshot(String path) throws IOException {
			try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
			    out.writeObject(this);
			} catch (IOException e) {
			    throw new RuntimeException(e);
			}
		}
	    
		public static Snapshot deserializeSnapshot(String path) throws IOException, ClassNotFoundException {
			Snapshot deserializedSnapshot;
			try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
				deserializedSnapshot = (Snapshot) in.readObject();
			} catch (IOException | ClassNotFoundException e ) {
				  throw new RuntimeException(e);
			}
			return deserializedSnapshot;
		}
		
		@Override
		public int getSizeOfReinforcement(Player player) {
			return getReinforcementList(player).size();
		}

		@Override
		public List<Unit> getReinforcementList(Player player) {
			switch(player) {
				case FIRST -> {
					return reinforcementsFIRST;
				}
				case SECOND -> {
					return reinforcementsSECOND;
				}
			}
			return null;
		}

		@Override
		public void addReinforcementToList(Player player, Unit unit) {
			switch (player) {
				case FIRST -> reinforcementsFIRST.add(unit);
				case SECOND -> reinforcementsSECOND.add(unit);
			}
		}

		@Override
		public void removeReinforcementFromList(Player player, int unitIndex) {
			switch (player) {
				case FIRST -> reinforcementsFIRST.remove(unitIndex);
				case SECOND -> reinforcementsSECOND.remove(unitIndex);
			}
		}
}
