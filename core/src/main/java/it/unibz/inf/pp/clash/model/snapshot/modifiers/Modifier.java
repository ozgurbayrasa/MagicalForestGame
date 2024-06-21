package it.unibz.inf.pp.clash.model.snapshot.modifiers;

public interface Modifier {

    enum Rarity {COMMON, RARE, EPIC};

    Rarity getRarity();

    int getHealth();

    int getCountdown();
}
