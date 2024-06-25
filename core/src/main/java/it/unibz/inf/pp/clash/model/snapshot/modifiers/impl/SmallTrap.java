package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

public class SmallTrap extends AbstractTrap {

    Rarity rarity;;
    int health;

    public SmallTrap(Rarity rarity) {
        this.rarity = rarity;

        switch (rarity) {
            case COMMON -> {
                health = -2;
            }
            case RARE -> {
                health = -3;
            }
            case EPIC -> {
                health = -5;
            }
        }
    }
}
