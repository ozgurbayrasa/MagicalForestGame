package it.unibz.inf.pp.clash.model.snapshot.modifiers;

public interface Trap {

    enum TrapRarity {COMMON, UNCOMMON, RARE, EPIC, LEGENDARY}

    TrapRarity getTrapRarity();

    int getDamage();

    int getActivationCountdown();
}
