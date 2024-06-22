package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class SmallTrap implements Trap {

    Rarity rarity;;
    int health;

    public SmallTrap(Rarity rarity) {
        this.rarity = rarity;

        switch (rarity) {
            case COMMON -> {
                health = -2;
            }
            case RARE -> {
                health = -3;
            }
            case EPIC -> {
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

    @Override
    public int getCountdown() {
        return -1;
    }
}
