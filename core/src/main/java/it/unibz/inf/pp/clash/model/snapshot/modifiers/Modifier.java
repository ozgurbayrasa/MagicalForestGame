package it.unibz.inf.pp.clash.model.snapshot.modifiers;

public interface Modifier {

    int getHealth();

    int getCountdown();

    enum Rarity {COMMON, UNCOMMON, RARE, EPIC, LEGENDARY};

}
