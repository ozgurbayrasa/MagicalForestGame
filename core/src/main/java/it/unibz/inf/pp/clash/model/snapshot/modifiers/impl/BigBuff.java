package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Buff;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;

public class BigBuff implements Buff {

    Rarity rarity;
    int health;
    int countdown;

    public BigBuff(Rarity rarity) {
        this.rarity = rarity;
        switch (rarity) {
            case COMMON -> {
                countdown = -1;
                health = 2;
            }
            case RARE -> {
                countdown = -2;
                health = 3;
            }
            case EPIC -> {
                countdown = -3;
                health = 5;
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
