package it.unibz.inf.pp.clash.model.snapshot.modifiers;

public interface Trap {

    enum TrapType {COMMON, RARE, LEGENDARY}

    TrapType getTrapType();

    int getActivationCountdown();
}
