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

        // Add click listener to the "Next" button
        view.addNextButtonListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleNextButtonClicked();
            }
        });

        // Add click listener to the "Exit" button
        view.addExitButtonListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                view.removeWindow();
            }
        });

        // Update the view with initial data
        updateView();
    }

    // Handle the "Next" button click
    private void handleNextButtonClicked() {
        if (model.isFinished()) {
            view.removeWindow();
        } else {
            model.nextStep();
            updateView();
        }
    }

    // Update the tutorial view
    private void updateView() {
        view.setTutorialText(model.getCurrentText());
        view.setTutorialImage(model.getCurrentImage());
        if (model.isFinished()) {
            view.setNextButtonText("End");
            view.showExitButton(false); // Hide the "Exit" button
        } else {
            view.setNextButtonText("Next");
            view.showExitButton(true); // Show the "Exit" button
        }
    }
}
