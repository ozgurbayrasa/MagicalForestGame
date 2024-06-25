package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public abstract class AbstractMobileUnit extends AbstractUnit implements MobileUnit {

    UnitColor color;
    int attackCountDown = -1;

    protected AbstractMobileUnit(int health, UnitColor color) {
        super(health);
        this.color = color;
    }

    @Override
    public UnitColor getColor() {
        return color;
    }

    @Override
    public void setColor(UnitColor color) {
        this.color = color;
    }

    @Override
    public int getAttackCountdown() {
        return attackCountDown;
    }

    @Override
    public void setAttackCountdown(int attackCountDown) {
        this.attackCountDown = attackCountDown;
    }
}
