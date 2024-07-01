package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

public class BigTrap extends AbstractTrap {

    Rarity rarity;

    public BigTrap(Rarity rarity) {
        this.rarity = rarity;
        switch (rarity) {
            case COMMON -> {
                this.countdown = 1;
                this.health = -2;
            }
            case RARE -> {
                this.countdown = 2;
                this.health = -3;
            }
            case EPIC -> {
                this.countdown = 3;
                this.health = -5;
            }
        }
    }
}
