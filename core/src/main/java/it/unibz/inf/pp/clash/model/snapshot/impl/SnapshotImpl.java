package it.unibz.inf.pp.clash.model.snapshot.impl;

import java.io.*;
import java.util.*;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;
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

	private Player activePlayer;
	private int actionsRemaining;
	// example value
	public final int defaultActionsRemaining = 3;
	protected TileCoordinates ongoingMove;

	private final Set<AbstractMobileUnit> reinforcementsFIRST = new HashSet<>(),
										  reinforcementsSECOND = new HashSet<>();

	private final List<Modifier> modifierListFIRST = new ArrayList<>(),
								 modifierListSECOND = new ArrayList<>();

	public SnapshotImpl(String firstHeroName, String secondHeroName) {
		firstHero = new HeroImpl(firstHeroName, 20);
		secondHero = new HeroImpl(secondHeroName, 20);
		board = BoardImpl.createEmptyBoard(11, 7);
		activePlayer = Player.values()[current().nextInt(Player.values().length)];
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
		return activePlayer;
	}

	@Override
	public void setActivePlayer(Player nextPlayer) {
		this.activePlayer = nextPlayer;
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
		int halfBoard = (board.getMaxRowIndex() / 2) + 1;
		Random random = new Random();

		// Lists of units to be placed on the board.
		List<Unit> unitsFIRST = createListOfUnits(Player.FIRST);
		List<Unit> unitsSECOND = createListOfUnits(Player.SECOND);

		// Repeat while there are units in the list.
		while(!unitsFIRST.isEmpty()) {
			// Repeat for every row.
			for(int i = halfBoard; i < board.getMaxRowIndex() + 1; i++) {
				// Repeat for every column.
				for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
					// Check if there aren't any more units in the list and if the tile is empty.
					if (!unitsFIRST.isEmpty() && board.getUnit(i, j).isEmpty() && random.nextBoolean()) {
						// Select random (possibly null) unit, add it to the board and remove it from the list.
						int unitIndex = random.nextInt(unitsFIRST.size());
						Unit unit = unitsFIRST.get(unitIndex);
						board.addUnit(i, j, unit);
						unitsFIRST.remove(unit);
					}
				}
			}
		}
		// Move the mandatoryUnits in.
		board.moveUnitsIn(Player.FIRST);

		// Repeat while there are units in the list.
		while(!unitsSECOND.isEmpty()) {
			// Repeat for every row.
			for(int i = (board.getMaxRowIndex() / 2); i >= 0; i--) {
				// Repeat for every column.
				for (int j = 0; j < board.getMaxColumnIndex() + 1; j++) {
					// Check if there aren't any more units in the list and if the tile is empty.
					if (!unitsSECOND.isEmpty() && board.getUnit(i, j).isEmpty() && random.nextBoolean()) {
						// Select random (possibly null) unit, add it to the board and remove it from the list.
						int unitIndex = random.nextInt(unitsSECOND.size());
						Unit unit = unitsSECOND.get(unitIndex);
						board.addUnit(i, j, unit);
						unitsSECOND.remove(unit);
					}
				}
			}
		}
		// Move the mandatoryUnits in.
		board.moveUnitsIn(Player.SECOND);
	}

	// Helper method to create a list of units.
	private List<Unit> createListOfUnits(Player player) {
		Random random = new Random();
		List<Unit> unitsToBePlaced = new ArrayList<>();

		// Get the number of allowed units per player, depending on the hero style.
		int allowedNumberOfUnits = getHero(player).getHeroType() == HeroImpl.HeroType.OFFENSIVE ? board.getAllowedUnits() + 5 : board.getAllowedUnits();
		// Number of units to be placed.
		int numberOfUnits = 0;

		// Pick a number between 2 and 4, which decides how many determined combinations will be added.
		int numOfCombinations = 2 + random.nextInt(3);

		// Add the determined combinations.
		// Repeat until the determined number of combinations are added.
		for(int i = 0; i < numOfCombinations; i++) {
			// Pick a random unit (class and color).
			int randomUnitClass = random.nextInt(3);
			int randomUnitColor = random.nextInt(3);
			// Add three new instances of that unit to the list and update the number of units.
			unitsToBePlaced.add(createInstance(randomUnitClass, randomUnitColor));
			unitsToBePlaced.add(createInstance(randomUnitClass, randomUnitColor));
			unitsToBePlaced.add(createInstance(randomUnitClass, randomUnitColor));
			numberOfUnits = unitsToBePlaced.size();
		}

		// Add the remaining random units.
		// Repeat until the number reaches the max allowed number of units.
		while(numberOfUnits < allowedNumberOfUnits) {
			// Pick a random unit (class and color).
			int randomUnitClass = random.nextInt(3);
			int randomUnitColor = random.nextInt(3);
			// Add it to the list and update the number of units.
			unitsToBePlaced.add(createInstance(randomUnitClass, randomUnitColor));
			numberOfUnits = unitsToBePlaced.size();
		}

		return unitsToBePlaced;
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
	public void resetHealthOfReinforcements(Player player) {
		for(Unit unit : getReinforcementSet(player)) {
			if(unit instanceof Fairy) {
				unit.setHealth(2);
			} else if(unit instanceof Unicorn) {
				unit.setHealth(3);
			} else if(unit instanceof Butterfly) {
				unit.setHealth(5);
			}
		}
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
				if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(player).containsKey(unit)) {
					reinforcementsFIRST.addAll(board.getFormationToSmallUnitsMap(player).get(unit));
					board.removeFormationFromMap(player, (AbstractMobileUnit) unit);
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
				if(unit instanceof AbstractMobileUnit && board.getFormationToSmallUnitsMap(player).containsKey(unit)) {
					reinforcementsSECOND.addAll(board.getFormationToSmallUnitsMap(player).get(unit));
					board.removeFormationFromMap(player, (AbstractMobileUnit) unit);
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
		getReinforcementSet(player).remove(unit);
	}

	@Override
	public List<Modifier> getModifierList(Player activePlayer) {
		return switch (activePlayer) {
			case FIRST -> modifierListFIRST;
			case SECOND -> modifierListSECOND;
		};
	}

	@Override
	public void addModifierToList(Player activePlayer, Modifier modifier) {
		getModifierList(activePlayer).add(modifier);
	}

	@Override
	public void removeModifierFromList(Player activePlayer, int modifierIndex) {
		getModifierList(activePlayer).remove(modifierIndex);

	}

	@Override
	public int getSizeOfModifierList(Player activePlayer) {
		return getModifierList(activePlayer).size();
	}
}
