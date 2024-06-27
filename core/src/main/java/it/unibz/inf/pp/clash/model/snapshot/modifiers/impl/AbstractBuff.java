package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Modifier;

public abstract class AbstractBuff implements Modifier {

    int health;
    int countdown;

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getCountdown() {
        return countdown;
    }
}
