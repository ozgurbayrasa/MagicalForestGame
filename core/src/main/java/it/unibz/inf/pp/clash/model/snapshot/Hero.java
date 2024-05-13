package it.unibz.inf.pp.clash.model.snapshot;

import java.io.Serializable;

public interface Hero extends Serializable {

    int getHealth();

    void setHealth(int health);

    String getName();

}
