package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class BigTrap implements Trap {

    Rarity rarity;;
    int health;
    int countdown;

    public BigTrap(Rarity rarity) {
        this.rarity = rarity;
        switch (rarity) {
            case COMMON -> {
                countdown = 2;
                health = -2;
            }
            case RARE -> {
                countdown = 3;
                health = -3;
            }
            case EPIC -> {
                countdown = 5;
                health = -5;
            }
        }
    }

    @Override
    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public int getHealth() {
        return health;
    }

    public int getCountdown() {
        return countdown;
    }
}
