package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;

public abstract class AbstractBuff implements Modifier {

    Rarity rarity;
    int health;
    int countdown;

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
