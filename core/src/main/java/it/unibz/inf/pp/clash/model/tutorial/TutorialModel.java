package it.unibz.inf.pp.clash.model.tutorial;

public class TutorialModel {
    private String[] tutorialTexts;
    private int currentStep;

    public TutorialModel(String[] tutorialTexts) {
        this.tutorialTexts = tutorialTexts;
        this.currentStep = 0;
    }

    public String getCurrentText() {
        if (currentStep < tutorialTexts.length) {
            return tutorialTexts[currentStep];
        }
        return null;
    }

    public void nextStep() {
        if (currentStep < tutorialTexts.length - 1) {
            currentStep++;
        } else {
            currentStep++;
        }
    }
    public boolean isFinished() {
        return currentStep == getTotalSteps() - 1;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return tutorialTexts.length;
    }
}

