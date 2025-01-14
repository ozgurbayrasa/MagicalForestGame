package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Hero;

import java.io.Serial;


public class HeroImpl implements Hero {

    /**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;

    public enum HeroType{
        DEFENSIVE,
        OFFENSIVE
    }
	private int health;

    private final String name;

    public HeroImpl(String name, int health) {
        this.name = name;
        if(name.equalsIgnoreCase("Alice(SD)")){
            this.health = health + 5;
        } else if(name.equalsIgnoreCase("Carol(MD)") || name.equalsIgnoreCase("Bob(MO)")) {
            this.health = health;
        } else { // Dan
            this.health = health - 5;
        }
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

    @Override
    public HeroType getHeroType() {
        if(name.equalsIgnoreCase("Alice(SD)") || name.equalsIgnoreCase("Carol(MD)")){
            return HeroType.DEFENSIVE;
        }
        else{
            return HeroType.OFFENSIVE;
        }
    }
}
