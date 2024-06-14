package it.unibz.inf.pp.clash.model.snapshot.modifiers;

public interface Trap {

    enum TrapRarity {COMMON, RARE, EPIC}

    TrapRarity getTrapRarity();

    int getDamage();
}
