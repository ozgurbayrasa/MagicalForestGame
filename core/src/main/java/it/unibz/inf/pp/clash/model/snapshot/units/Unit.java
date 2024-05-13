package it.unibz.inf.pp.clash.model.snapshot.units;

import java.io.Serializable;

/**
 * A unit is anything that may be standing on a tile (including walls for instance).
 */
public interface Unit extends Serializable {

    /**
     * @return remaining health points
     */
    int getHealth();

    void setHealth(int health);
}
