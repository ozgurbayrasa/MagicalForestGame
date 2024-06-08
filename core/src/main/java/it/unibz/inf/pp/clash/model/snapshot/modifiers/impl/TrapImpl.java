package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

import java.lang.reflect.Type;

public class TrapImpl implements Trap {

    final TrapType type;
    int activationCountdown;
    int damage;

    public TrapImpl(TrapType type, int activationCountdown) {
        this.type = type;
        switch (type) {
            case COMMON -> damage = 2;
            case RARE -> damage = 5;
            case LEGENDARY -> damage = 9;
        }
        this.activationCountdown = activationCountdown;
    }

    @Override
    public TrapType getTrapType() {
        return type;
    }

    @Override
    public int getActivationCountdown() {
        return activationCountdown;
    }
}
