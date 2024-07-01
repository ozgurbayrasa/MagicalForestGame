package it.unibz.inf.pp.clash.controller.listeners;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.impl.SnapshotImpl;

public class ContinueGameListener extends ClickListener {

	private final SelectBox<String> firstHeroSelectBox;
	private final SelectBox<String> secondHeroSelectBox;
    private final EventHandler eventHandler;

	public ContinueGameListener(SelectBox<String> firstHeroSelectBox, SelectBox<String> secondHeroSelectBox,
						   EventHandler eventHandler) {
		this.firstHeroSelectBox = firstHeroSelectBox;
		this.secondHeroSelectBox = secondHeroSelectBox;
		this.eventHandler = eventHandler;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		eventHandler.continueGame(
				firstHeroSelectBox.getSelected(),
				secondHeroSelectBox.getSelected()
		);
	}
}
