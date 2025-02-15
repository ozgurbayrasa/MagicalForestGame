package it.unibz.inf.pp.clash.controller.listeners;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

public class EmptyTileHoverAndLeftClickListener extends TileHoverListener {

    public EmptyTileHoverAndLeftClickListener(int rowIndex, int columnIndex, EventHandler eventHandler) {
        super(rowIndex, columnIndex, Input.Buttons.LEFT, eventHandler);
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if(super.isHoveredOver()) {
                System.out.printf(
                        "Hovering over Tile (%s, %s)%n",
                        rowIndex,
                        columnIndex
                );
            }
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        System.out.printf(
                "Left click on Tile (%s, %s)%n",
                rowIndex,
                columnIndex
        );
        try {
            if(eventHandler.modifierModeIsOn()) {
                eventHandler.placeModifier(rowIndex, columnIndex);
            } else {
                eventHandler.selectTile(rowIndex, columnIndex);
            }
        } catch (NoGameOnScreenException e) {
            throw new RuntimeException(e);
        }
    }
}
