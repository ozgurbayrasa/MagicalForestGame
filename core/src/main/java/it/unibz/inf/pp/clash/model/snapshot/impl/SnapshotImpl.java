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
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.AbstractMobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

import static java.util.concurrent.ThreadLocalRandom.current;

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
		// example value
		public final int defaultActionsRemaining = 3;
	    protected TileCoordinates ongoingMove;

		private final List<Unit> reinforcementsFIRST = new ArrayList<>(),
								 reinforcementsSECOND = new ArrayList<>();

	    public SnapshotImpl(String firstHeroName, String secondHeroName) {
			firstHero = new HeroImpl(firstHeroName, 20);
			secondHero = new HeroImpl(secondHeroName, 15);
			board = BoardImpl.createEmptyBoard(11, 7);
			activeplayer = Player.values()[current().nextInt(Player.values().length)];
	        actionsRemaining = defaultActionsRemaining;
	        ongoingMove = null;
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
		public int getDefaultActionsRemaining() {
			return defaultActionsRemaining;
		}

		@Override
		public void setNumberOfRemainingActions(int defaultActionsRemaining) {
			actionsRemaining = defaultActionsRemaining;
		}
	    
	    @Override
	    public void populateTiles() {
			Random random = new Random();
			int numberOfPlacedUnits = 0;
			// Repeat for every row.
			for(int i = (board.getMaxRowIndex() / 2) + 1; i <= board.getMaxRowIndex(); i++) {
				// Repeat for every column.
				for(int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
					if(numberOfPlacedUnits < board.getAllowedUnits()) {
						if(random.nextBoolean()) {
							int randomUnitClass = random.nextInt(3);
							int randomUnitColor = random.nextInt(3);
							// Select random (possibly null) unit and add it to the board.
		                    Unit unit = createInstance(randomUnitClass, randomUnitColor);
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
							int randomUnitClass = random.nextInt(3);
							int randomUnitColor = random.nextInt(3);
							// Select random (possibly null) unit and add it to the board.
							Unit unit = createInstance(randomUnitClass, randomUnitColor);
							board.addUnit(i, j, unit);
							numberOfPlacedUnits++;
						}
					}
				}
			}
			board.moveUnitsIn(Player.SECOND);
	    }

		// Helper method to create new random instances.
		private AbstractMobileUnit createInstance(int unitClass, int unitColor) {
			switch (unitClass) {
				case 0:
					switch (unitColor) {
						case 0:
							return new Butterfly(UnitColor.ONE);
						case 1:
							return new Butterfly(UnitColor.TWO);
						case 2:
							return new Butterfly(UnitColor.THREE);
					}
				case 1:
					switch (unitColor) {
						case 0:
							return new Fairy(UnitColor.ONE);
						case 1:
							return new Fairy(UnitColor.TWO);
						case 2:
							return new Fairy(UnitColor.THREE);
					}
				case 2:
					switch (unitColor) {
						case 0:
							return new Unicorn(UnitColor.ONE);
						case 1:
							return new Unicorn(UnitColor.TWO);
						case 2:
							return new Unicorn(UnitColor.THREE);
					}
				default:
					return null;
			}
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
