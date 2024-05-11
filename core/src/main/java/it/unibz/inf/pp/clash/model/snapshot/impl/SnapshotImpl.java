package it.unibz.inf.pp.clash.model.snapshot.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

public class SnapshotImpl extends AbstractSnapshot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SnapshotImpl(Hero firstHero, Hero secondHero, Board board, Player activeplayer, int actionsRemaining,
			TileCoordinates ongoingMove) {
		super(firstHero, secondHero, board, activeplayer, actionsRemaining, ongoingMove);
		populateTiles();
	}

	@Override
	public int getSizeOfReinforcement(Player player) {
        return (board.getAllowedUnits() - board.countUnits(player));
	}

}
