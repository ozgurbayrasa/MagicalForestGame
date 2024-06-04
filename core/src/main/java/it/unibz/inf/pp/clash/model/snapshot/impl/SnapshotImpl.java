package it.unibz.inf.pp.clash.model.snapshot.impl;

import java.io.*;
import java.util.*;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.*;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

import static java.util.concurrent.ThreadLocalRandom.current;

public class SnapshotImpl implements Snapshot {

	@Serial
	private static final long serialVersionUID = 1L;
	protected final Board board;
	private final Hero firstHero;
	private final Hero secondHero;

	private Player activeplayer;
	private int actionsRemaining;
	// example value
	public final int defaultActionsRemaining = 3;
	protected TileCoordinates ongoingMove;

	private final Set<AbstractMobileUnit> reinforcementsFIRST = new HashSet<>(),
										  reinforcementsSECOND = new HashSet<>();

	public SnapshotImpl(String firstHeroName, String secondHeroName) {
		firstHero = new HeroImpl(firstHeroName, 20);
		secondHero = new HeroImpl(secondHeroName, 20);
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
		// Move the units in.
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
		// Move the units in.
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
			throw new IOException(e);
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
		return getReinforcementSet(player).size();
	}

	@Override
	public Set<AbstractMobileUnit> getReinforcementSet(Player player) {
		return switch(player) {
			case FIRST -> reinforcementsFIRST;
			case SECOND -> reinforcementsSECOND;
		};
	}

	@Override
	public void addReinforcementToSet(Player player, Unit unit) {
		switch (player) {
			case FIRST -> {
				// If the unit is big, add all the corresponding distinct references to the list and remove the entry (AbstractMobileUnit, Set<AbstractMobileUnit>) from the map.
				if(unit instanceof AbstractMobileUnit && board.getBigUnitToSmallUnitsMap(player).containsKey(unit)) {
					reinforcementsFIRST.addAll(board.getBigUnitToSmallUnitsMap(player).get(unit));
					board.removeBigUnitFromMap(player, (AbstractMobileUnit) unit);
					reinforcementsFIRST.add((AbstractMobileUnit) unit);
				// If the unit is a wall, add the corresponding reference to the list and remove the entry (Wall, AbstractMobileUnit) from the map.
				} else if(unit instanceof Wall && board.getWallToUnitMap(player).containsKey(unit)) {
					reinforcementsFIRST.add(board.getWallToUnitMap(player).get(unit));
					board.removeWallFromMap(player, (Wall) unit);
				// If the unit is a small unit, just add it to the list.
				} else if(unit instanceof AbstractMobileUnit) {
					reinforcementsFIRST.add((AbstractMobileUnit) unit);
				}
			}
			case SECOND -> {
				// If the unit is big, add all the corresponding distinct references to the list and remove the entry (AbstractMobileUnit, Set<AbstractMobileUnit>) from the map.
				if(unit instanceof AbstractMobileUnit && board.getBigUnitToSmallUnitsMap(player).containsKey(unit)) {
					reinforcementsSECOND.addAll(board.getBigUnitToSmallUnitsMap(player).get(unit));
					board.removeBigUnitFromMap(player, (AbstractMobileUnit) unit);
					reinforcementsSECOND.add((AbstractMobileUnit) unit);
				// If the unit is a wall, add the corresponding reference to the list and remove the entry (Wall, AbstractMobileUnit) from the map.
				} else if(unit instanceof Wall && board.getWallToUnitMap(player).containsKey(unit)) {
					reinforcementsSECOND.add(board.getWallToUnitMap(player).get(unit));
					board.removeWallFromMap(player, (Wall) unit);
				// If the unit is a small unit, just add it to the list.
				} else if(unit instanceof AbstractMobileUnit) {
					reinforcementsSECOND.add((AbstractMobileUnit) unit);
				}
			}
		}
	}

	@Override
	public void removeReinforcementFromSet(Player player, AbstractMobileUnit unit) {
		switch (player) {
			case FIRST -> reinforcementsFIRST.remove(unit);
			case SECOND -> reinforcementsSECOND.remove(unit);
		}
	}
}
