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
    private final TextButton previousButton;
    private final TextButton exitButton;
    private final Table contentTable;

    public TutorialView(Stage stage, Skin skin) {
        // Create a window for the tutorial
        window = new Window("", skin);
        window.setSize(1920, 1080);

        // Create a label for tutorial text
        tutorialLabel = new Label("", skin);
        tutorialLabel.setWrap(true); // Enable text wrapping
        tutorialLabel.setAlignment(Align.center); // Center align the text

        // Create an image for the tutorial
        tutorialImage = new Image();
        tutorialImage.setScaling(Scaling.fit); // Scale the image to fit
        tutorialImage.setSize(1200, 800); // Set a fixed size for the image

        // Create buttons for "Next", "Previous", and "Exit"
        nextButton = new TextButton("Next", skin);
        previousButton = new TextButton("Previous", skin);
        exitButton = new TextButton("Exit", skin);

        // Create a table to organize the content
        contentTable = new Table();
        contentTable.setFillParent(true); // Make the table fill the parent window
        contentTable.pad(20); // Add padding around the table

        // Add components to the table
        contentTable.add(tutorialLabel).width(1800).padBottom(20).row();
        contentTable.add(tutorialImage).size(1200, 800).padBottom(20).row();

        // Create a sub-table for buttons
        Table buttonTable = new Table();
        buttonTable.add(previousButton).padRight(10); // Add padding between buttons
        buttonTable.add(nextButton).padRight(10);
        buttonTable.add(exitButton);

        // Add the button table to the main content table
        contentTable.add(buttonTable).padTop(20).center();

        // Add the content table to the window and set its properties
        window.add(contentTable).expand().fill();
        window.pack();
        window.setPosition(stage.getWidth() / 2 - window.getWidth() / 2, stage.getHeight() / 2 - window.getHeight() / 2);
        stage.addActor(window);
    }

    // Method to add a click listener to the "Next" button
    public void addNextButtonListener(ClickListener listener) {
        nextButton.addListener(listener);
    }

    // Method to add a click listener to the "Previous" button
    public void addPreviousButtonListener(ClickListener listener) {
        previousButton.addListener(listener);
    }

    // Method to add a click listener to the "Exit" button
    public void addExitButtonListener(ClickListener listener) {
        exitButton.addListener(listener);
    }

    // Method to set the tutorial text
    public void setTutorialText(String text) {
        tutorialLabel.setText(text);
    }

    // Method to set the tutorial image
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

        // Re-add buttons to the table
        Table buttonTable = new Table();
        buttonTable.add(previousButton).padRight(10); // Add padding between buttons
        buttonTable.add(nextButton).padRight(10);
        buttonTable.add(exitButton);
        contentTable.add(buttonTable).padTop(20).center();
    }

    // Method to set the text of the "Next" button
    public void setNextButtonText(String text) {
        nextButton.setText(text);
    }

    // Method to show or hide the "Previous" button
    public void showPreviousButton(boolean show) {
        previousButton.setVisible(show);
    }

    // Method to show or hide the "Exit" button
    public void showExitButton(boolean show) {
        exitButton.setVisible(show);
    }

    // Method to remove the tutorial window
    public void removeWindow() {
        window.remove();
    }
}
