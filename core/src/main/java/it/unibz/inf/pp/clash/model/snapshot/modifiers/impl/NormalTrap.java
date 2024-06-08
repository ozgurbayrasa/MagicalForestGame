package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

import it.unibz.inf.pp.clash.model.snapshot.modifiers.Trap;

public class NormalTrap implements Trap {

    TrapRarity rarity;
    int activationCountdown;
    int damage;

    public NormalTrap(TrapRarity rarity) {
        switch (rarity) {
            case COMMON -> {
                this.rarity = rarity;
                activationCountdown = 3;
                damage = 2;
            }
            case UNCOMMON -> {
                this.rarity = rarity;
                activationCountdown = 2;
                damage = 4;
            }
            case RARE -> {
                this.rarity = rarity;
                activationCountdown = 1;
                damage = 6;
            }
            case EPIC -> {
                this.rarity = rarity;
                activationCountdown = 1;
                damage = 10;
            }
            case LEGENDARY -> {
                this.rarity = rarity;
                activationCountdown = 1;
                damage = 15;
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

    @Override
    public int getActivationCountdown() {
        return activationCountdown;
    }
}
