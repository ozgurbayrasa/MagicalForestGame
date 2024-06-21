package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Buff;
import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;

public class SmallBuff implements Buff {

    Rarity rarity;
    int health;

    public SmallBuff(Rarity rarity) {
        this.rarity = rarity;

        switch (rarity) {
            case COMMON -> {
                health = 2;
            }
            case RARE -> {
                health = 3;
            }
            case EPIC -> {
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

    @Override
    public int getCountdown() {
        return -1;
    }
}
