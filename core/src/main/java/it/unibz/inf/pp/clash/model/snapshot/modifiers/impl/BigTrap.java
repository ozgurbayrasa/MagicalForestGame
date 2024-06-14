package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class BigTrap implements Trap {

    TrapRarity rarity;;
    int damage;
    int countdown;

    public BigTrap(TrapRarity rarity) {
        this.rarity = rarity;
        switch (rarity) {
            case COMMON -> {
                countdown = 2;
                damage = 4;
            }
            case RARE -> {
                countdown = 3;
                damage = 6;
            }
            case EPIC -> {
                countdown = 4;
                damage = 10;
            }
        }
    }

    @Override
    public TrapRarity getTrapRarity() {
        return rarity;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    public int getCountdown() {
        return countdown;
    }
}
