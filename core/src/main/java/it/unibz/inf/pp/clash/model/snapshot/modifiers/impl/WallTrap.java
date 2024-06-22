package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class WallTrap implements Trap {

    public WallTrap(int rowIndex, int columnIndex) {

    }

    @Override
    public Rarity getRarity() {
        return null;
    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public int getCountdown() {
        return -1;
    }
}
