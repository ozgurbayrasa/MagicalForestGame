package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class SmallTrap implements Trap {

    TrapRarity rarity;;
    int damage;

    public SmallTrap(TrapRarity rarity) {
        this.rarity = rarity;

        switch (rarity) {
            case COMMON -> {
                damage = 2;
            }
            case RARE -> {
                damage = 3;
            }
            case EPIC -> {
                damage = 5;
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
}
