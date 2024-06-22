package it.unibz.inf.pp.clash.controller.listeners;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

public class UnitMiddleClickListener extends ClickListener {

    private final int rowIndex;
    private final int columnIndex;

    private final EventHandler eventHandler;

    public UnitMiddleClickListener(int rowIndex, int columnIndex, EventHandler eventHandler) {
        super(Input.Buttons.MIDDLE);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.eventHandler = eventHandler;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        System.out.printf(
                "Middle click on the unit at Tile (%s, %s)%n",
                rowIndex,
                columnIndex
        );
        eventHandler.sacrificeUnit(rowIndex, columnIndex);
    }

}
