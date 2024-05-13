package it.unibz.inf.pp.clash.controller.listeners;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import it.unibz.inf.pp.clash.model.EventHandler;

public class ContinueGameListener extends ClickListener {
	
    private final EventHandler eventHandler;

	public ContinueGameListener(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		eventHandler.continueGame();
	}
}
