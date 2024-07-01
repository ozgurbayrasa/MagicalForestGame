package it.unibz.inf.pp.clash.model.snapshot.modifiers.impl;

public class SmallBuff extends AbstractBuff {

    Rarity rarity;

    public SmallBuff(Rarity rarity) {
        this.rarity = rarity;

        switch (rarity) {
            case COMMON -> this.health = 1;
            case UNCOMMON -> this.health = 2;
            case RARE -> this.health = 3;
            case EPIC -> this.health = 4;
            case LEGENDARY -> this.health = 5;
        }
    }
}
