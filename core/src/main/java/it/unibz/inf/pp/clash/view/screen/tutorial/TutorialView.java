package it.unibz.inf.pp.clash.view.screen.tutorial;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class TutorialView {
    private Stage stage;
    private Skin skin;
    private Label tutorialLabel;
    private TextButton nextButton;
    private Window tutorialWindow;

    public TutorialView(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        createUI();
    }

    private void createUI() {
        tutorialLabel = new Label("", skin);
        nextButton = new TextButton("Next", skin);

        Table table = new Table();
        table.add(tutorialLabel).pad(10);
        table.row();
        table.add(nextButton).pad(10);

        tutorialWindow = new Window("", skin);
        tutorialWindow.add(table);
        tutorialWindow.pack();
        tutorialWindow.setMovable(true);
        tutorialWindow.setResizable(true);
        tutorialWindow.setModal(true);
        tutorialWindow.setSize(800, 600);
        tutorialWindow.setPosition(
                (stage.getWidth() - tutorialWindow.getWidth()) / 2,
                (stage.getHeight() - tutorialWindow.getHeight()) / 2
        );

        stage.addActor(tutorialWindow);
    }

    public void setTutorialText(String text) {
        tutorialLabel.setText(text);
    }

    public void setNextButtonText(String text) {
        nextButton.setText(text);
    }

    public void addNextButtonListener(EventListener listener) {
        nextButton.addListener(listener);
    }

    public void removeWindow() {
        tutorialWindow.remove();
    }
}


