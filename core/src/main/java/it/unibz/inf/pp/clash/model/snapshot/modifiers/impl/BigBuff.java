package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

public class BigBuff extends AbstractBuff {

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
}
