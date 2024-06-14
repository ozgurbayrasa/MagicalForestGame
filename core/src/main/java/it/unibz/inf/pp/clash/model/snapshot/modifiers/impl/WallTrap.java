package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class WallTrap implements Trap {

    public WallTrap() {

    }

    @Override
    public TrapRarity getTrapRarity() {
        return null;
    }

    @Override
    public int getDamage() {
        return 0;
    }
}
