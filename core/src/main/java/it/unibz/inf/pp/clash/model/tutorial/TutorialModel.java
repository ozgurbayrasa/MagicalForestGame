package it.unibz.inf.pp.clash.model.tutorial;

public class TutorialModel {
    // Array of tutorial text for each step
    private final String[] tutorialTexts;

    // Array of image paths corresponding to each tutorial step
    private final String[] tutorialImages;

    // Index to keep track of the current tutorial step
    private int currentIndex;

    // Constructor: Initializes the tutorial texts, images, and sets the starting index
    public TutorialModel(String[] tutorialTexts, String[] tutorialImages) {
        this.tutorialTexts = tutorialTexts;
        this.tutorialImages = tutorialImages;
        this.currentIndex = 0; // Start at the first tutorial step
    }

    // Method to get the current tutorial text
    public String getCurrentText() {
        return tutorialTexts[currentIndex];
    }

    // Method to get the current tutorial image
    public String getCurrentImage() {
        return tutorialImages[currentIndex];
    }

    // Method to move to the next tutorial step
    public void nextStep() {
        if (currentIndex < tutorialTexts.length - 1) {
            currentIndex++; // Increment the index if not at the last step
        }
    }

    // Method to move to the previous tutorial step
    public void previousStep() {
        if (currentIndex > 0) {
            currentIndex--; // Decrement the index if not at the first step
        }
    }

    // Method to check if the tutorial is finished
    public boolean isFinished() {
        return currentIndex >= tutorialTexts.length - 1; // Return true if at the last step
    }

    // Method to check if the tutorial is at the beginning
    public boolean isAtBeginning() {
        return currentIndex == 0; // Return true if at the first step
    }
}
