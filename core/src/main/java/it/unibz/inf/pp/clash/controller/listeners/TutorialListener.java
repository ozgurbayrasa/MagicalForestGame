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
                "Welcome to the tutorial! This tutorial will help you learn the basic mechanics and controls of the game.",
                "In this game, two players face each other, each controlling a hero and units. The objective of the game is to defeat the opponent's hero and win the game.",
                "Each player selects a hero with unique abilities. There are two types of heroes: defensive and offensive.\n- Defensive Heroes: Alice and Carol. \n- Offensive Heroes: Bob and Adam.",
                "Units are the building blocks of the game and move in a specific order on the battlefield. By strategically placing your units, you can form formations.\nFormation types:\n- 1x3 Wall Formation: Consists of three units of the same type and color in a horizontal line, providing defense.\n- 3x1 Vertical Formation: Consists of three units of the same type and color in a vertical line, delivering powerful attacks.",
                "Each player has 3 actions per turn. These actions can be used to:\n- Move a unit\n- Delete a unit\n- Sacrifice a unit\n- Call reinforcements\nIf desired, players can also skip their turn.",
                "For defensive heroes, if both 3x1 and 1x3 formations are formed simultaneously, the wall formation takes priority. For offensive heroes, the big unit formation is prioritized.",
                "Big Units: These formations attack the opponent after 3 rounds. The attack damage is equal to the total health of the units in the formation.\nWall: Walls are formed with 10 health and provide defensive capabilities, blocking incoming attacks.",
                "When selecting a unit to move, you must choose the unit at the end of the row. This rule ensures strategic depth and planning.",
                "When you sacrifice a unit, you gain the option to choose a buff or trap based on the unit sacrificed.\n- Buffs grant extra health to your units.\n- Traps reduce the health of an opponent's unit or potentially destroy it.\nYou can select and apply these buffs or traps during your turn.",
                "Players can call reinforcements if their units are depleted or need additional support. Reinforcements bring new units to the battlefield and can be a game-changer.",
                "Defensive heroes start the game with an extra +5 health to their units. Offensive heroes begin with an additional +5 units, giving them an early advantage in numbers.",
                "Units that are deleted by the player or destroyed in combat can be recalled. However, units that are destroyed by traps or sacrificed cannot be brought back.",
                "The objective of the game is to defeat your opponent's hero. Use your units strategically and take advantage of your hero's abilities to defeat your opponent's hero and claim victory!",
                "Now that you have learned the basic rules and mechanics of the game, start the game and use your strategies to defeat your opponent. Good luck!"
        };

        String[] tutorialImages = {
                "images/png/tutorial_images/tutorial1.png",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        };

        TutorialModel model = new TutorialModel(tutorialTexts, tutorialImages);
        TutorialView view = new TutorialView(stage, skin);
        new TutorialController(model, view);
    }
}
