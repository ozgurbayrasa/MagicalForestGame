package it.unibz.inf.pp.clash.controller.tutorial;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import it.unibz.inf.pp.clash.model.tutorial.TutorialModel;
import it.unibz.inf.pp.clash.view.screen.tutorial.TutorialView;

public class TutorialController {
    private final TutorialModel model;
    private final TutorialView view;

    public TutorialController(TutorialModel model, TutorialView view) {
        this.model = model;
        this.view = view;

        view.addNextButtonListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleNextButtonClicked();
            }
        });

        updateView();
    }

    private void handleNextButtonClicked() {
        if (model.isFinished()) {
            view.removeWindow();
        } else {
            model.nextStep();
            updateView();
        }
    }

    private void updateView() {
        view.setTutorialText(model.getCurrentText());
        view.setTutorialImage(model.getCurrentImage());
        if (model.isFinished()) {
            view.setNextButtonText("End");
        } else {
            view.setNextButtonText("Next");
        }
    }
}
