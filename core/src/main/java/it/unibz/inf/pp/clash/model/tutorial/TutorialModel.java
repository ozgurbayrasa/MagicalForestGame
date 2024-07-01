package it.unibz.inf.pp.clash.model.tutorial;

public class TutorialModel {
    private final String[] tutorialTexts;
    private final String[] tutorialImages;
    private int currentIndex;

    public TutorialModel(String[] tutorialTexts, String[] tutorialImages) {
        this.tutorialTexts = tutorialTexts;
        this.tutorialImages = tutorialImages;
        this.currentIndex = 0;
    }

    public String getCurrentText() {
        return tutorialTexts[currentIndex];
    }

    public String getCurrentImage() {
        return tutorialImages[currentIndex];
    }

    public void nextStep() {
        if (currentIndex < tutorialTexts.length - 1) {
            currentIndex++;
        }
    }

    public boolean isFinished() {
        return currentIndex >= tutorialTexts.length - 1;
    }
}
