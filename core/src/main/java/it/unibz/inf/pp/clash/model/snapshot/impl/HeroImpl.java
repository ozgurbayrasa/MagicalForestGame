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
        DEFFENSIVE,
        OFFENSIVE
    }
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

    @Override
    public HeroType getHeroType() {
        if(name.equalsIgnoreCase("Alice") || name.equalsIgnoreCase("Carol")){
            return HeroType.DEFFENSIVE;
        }
        else{
            return HeroType.OFFENSIVE;
        }
    }
}
