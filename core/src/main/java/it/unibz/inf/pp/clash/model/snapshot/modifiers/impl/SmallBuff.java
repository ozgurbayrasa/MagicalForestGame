package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

public class SmallBuff extends AbstractBuff {

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
}
