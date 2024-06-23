package it.unibz.inf.pp.clash.controller.listeners;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import it.unibz.inf.pp.clash.controller.tutorial.TutorialController;
import it.unibz.inf.pp.clash.model.tutorial.TutorialModel;
import it.unibz.inf.pp.clash.view.screen.tutorial.TutorialView;

public class TutorialListener extends ClickListener {

    private final Stage stage;
    private final Skin skin;

    public TutorialListener(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        String[] tutorialTexts = {
                "Welcome to tutorial\nWelcome",
                "In game you can...",
                "A player is forbidden...",
                "Have fun!"
        };

        TutorialModel model = new TutorialModel(tutorialTexts);
        TutorialView view = new TutorialView(stage, skin);
        new TutorialController(model, view);
    }
}

