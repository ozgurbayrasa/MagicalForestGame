package it.unibz.inf.pp.clash.view.screen.tutorial;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public class TutorialView {
    private final Window window;
    private final Label tutorialLabel;
    private final Image tutorialImage;
    private final TextButton nextButton;
    private final Table contentTable;

    public TutorialView(Stage stage, Skin skin) {
        window = new Window("", skin);
        window.setSize(1920, 1080);

        tutorialLabel = new Label("", skin);
        tutorialLabel.setWrap(true);
        tutorialLabel.setAlignment(Align.center);

        tutorialImage = new Image();
        tutorialImage.setScaling(Scaling.fit);
        tutorialImage.setSize(1200, 800);

        nextButton = new TextButton("Next", skin);

        contentTable = new Table();
        contentTable.setFillParent(true);
        contentTable.pad(20);

        contentTable.add(tutorialLabel).width(1800).padBottom(20).row();
        contentTable.add(tutorialImage).size(1200, 800).padBottom(20).row();
        contentTable.add(nextButton).padTop(20).center();

        window.add(contentTable).expand().fill();
        window.pack();
        window.setPosition(stage.getWidth() / 2 - window.getWidth() / 2, stage.getHeight() / 2 - window.getHeight() / 2);
        stage.addActor(window);
    }

    public void addNextButtonListener(ClickListener listener) {
        nextButton.addListener(listener);
    }

    public void setTutorialText(String text) {
        tutorialLabel.setText(text);
    }

    public void setTutorialImage(String imagePath) {
        contentTable.clear();
        contentTable.add(tutorialLabel).width(1800).padBottom(20).row();

        if (imagePath == null || imagePath.isEmpty()) {
            tutorialImage.setVisible(false);
        } else {
            try {
                Texture texture = new Texture(imagePath);
                tutorialImage.setDrawable(new TextureRegionDrawable(texture));
                tutorialImage.setVisible(true);
                contentTable.add(tutorialImage).size(1200, 600).padBottom(20).row();
            } catch (Exception e) {
                tutorialImage.setVisible(false);
            }
        }

        contentTable.add(nextButton).padTop(20).center();
    }

    public void setNextButtonText(String text) {
        nextButton.setText(text);
    }

    public void removeWindow() {
        window.remove();
    }
}
