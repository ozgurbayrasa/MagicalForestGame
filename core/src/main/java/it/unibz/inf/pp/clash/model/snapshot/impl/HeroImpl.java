package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Hero;

import java.io.Serial;

public class HeroImpl implements Hero {

    /**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;

	private int health;

    private final String name;

    public HeroImpl(String name, int health) {
        this.name = name;
        this.health = health;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public String getName() {
        return name;
    }
}
