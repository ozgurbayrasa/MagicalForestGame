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

    // Constructor: Initializes the stage and skin for the tutorial view
    public TutorialListener(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        // Array of tutorial text for each step in the tutorial
        String[] tutorialTexts = {
                "Welcome to the tutorial! This tutorial will help you learn the basic mechanics to survive the Magical Forest.",
                "In this game, two players face off, each controlling their own hero and units.",
                "Each player selects a hero with unique abilities. There are two types of heroes: defensive (Alice and Carol) and offensive (Bob and Dan).\n- Alice specializes in strong defense (SD) - more health points, less units, stronger buffs.\n- Dan specializes in strong offense (SO) - less health points, more units, stronger traps.\n - Carole and Bob respectively have moderate defense (MD) and offense (MO).",
                "The first player is chosen randomly and unit sets are unique every game. There are three types of units - fairies (2 HP), unicorns (3 HP) and butterflies (5 HP).\n\nEach player has 3 actions per turn. These actions include:\n- Moving a unit (left click)\n- Deleting a unit (right click)\n- Sacrificing a unit (middle click)\n- Applying a modifier\n- Calling reinforcements\n- Skipping the current turn.",
                // Moving units, formations.
                /*tutorial1*/"Units are the building blocks of the game and can be moved to any empty tile on the player's side of the board. By strategically placing units of the same type and color next to each other, players can create formations.\n\nFormation types:\n- Horizontal(1x3) formation: three wall units in a horizontal line, providing defense.\n- Vertical(1x3) formation: an attacking formation, delivering powerful strikes after its countdown finishes.",
                /*tutorial2*/"Attacking formations: These formations normally attack the opponent after 3 turns by the attacking player.\nThe total health (HP) of the attacking formation is equal to the average health of the units forming it, times 3. Its attack damage is also equal to that health.\nWall formations: Each wall unit is created with 10 HP, blocking incoming attacks.\n\n Creating a formation of any type will grant the player with one extra move.",
                "When deployed, attacking formations damage the opponent by first applying damage to the units in their way and then to the hero. However damage goes both ways, so your formation may run out of health if your opponent has strong defense.",
                /*tutorial3*/"If two wall formations are to be created simultaneously, the left one takes priority.\nIf two attacking formations are to be created simultaneously, the one closer to the dividing line takes priority.\n\nThis is easily identifiable by the two yellow lines, on the leftmost part of the screen and between the player halves of the board.\nIn other words, the formations closer to these lines will be created in cases of ambiguity.",
                /*tutorial4*/"If a wall formation and an attacking formation are to be created simultaneously, the wall takes priority if the playing hero is defensive, while the formation takes priority if the hero is offensive.",
                // Sacrificing units, modifiers.
                "When a player sacrifices a unit (only 3 sacrifices per player allowed), they gain the choice between two different modifiers, which can be applied to units on the board.\nBuffs are helpful to your units, while traps are harmful to your opponent's units.\n",
                "Depending on your hero style and the type of unit you sacrificed, there are a variety of possible options for modifiers:\n- Sacrificing a small unit awards the player with a small buff or a small trap, which are only applicable to small units and affect their HP (which can in turn affect the HP of a formation).\n- Sacrificing an attacking formation awards the player with a big buff or a big trap. Those are only applicable to other attacking formations and affect their countdown (CD), as well as the HP of their surrounding units.\n- Sacrificing a wall unit awards the player with a wall buff or a wall trap, more information about those is in the next slide.",
                /*tutorial5*/"Wall buff: If a player has three units of the same type in a formation structure, but one of the three units has a different color, a wall buff can be applied to that unit, changing its color and thus completing the formation.\n\nWall trap: This trap turns any small opponent unit to a wall unit with the health of the original unit. In this way it is possible to destroy attacking formations.",
                /*tutorial6*/"Applying a modifier is achieved by clicking the modifier button and selecting a unit.\n\nAttention: Modifiers can only be applied in the order they were awarded.",
                // Calling reinforcements.
                /*tutorial7*/"A player's reinforcements consist of those units deleted by the player or destroyed in combat. Calling reinforcement brings these units back to the battlefield and is achieved by clicking the reinforcement button.\n\nAttention: Sacrificed units and units destroyed by traps cannot be brought back.",
                // Skipping a turn.
                /*tutorial8*/"Skipping the current turn is achieved by clicking the skip button.",
                "Attention: Only units in the last occupied cells of each column can be moved.\n\nSelected units can only be placed in unoccupied tiles.\n\nAfter having selected a unit, either to move or sacrifice, a (respectively left or middle) click on the same unit will cancel the operation.",
                "A player wins the game by exhausting their opponent's health points. Use your units strategically and take advantage of your hero's abilities to defeat your opponents and claim victory in the Magical Forest!"
        };

        // Array of image paths corresponding to each tutorial step
        String[] tutorialImages = {
                null,
                null,
                null,
                null,
                "images/png/tutorial_images/tutorial1.png",
                "images/png/tutorial_images/tutorial2.png",
                null,
                "images/png/tutorial_images/tutorial3.png",
                "images/png/tutorial_images/tutorial4.png",
                null,
                null,
                "images/png/tutorial_images/tutorial5.png",
                "images/png/1920x1080/icons/large/modifier.png",
                "images/png/1920x1080/icons/large/backup.png",
                "images/png/1920x1080/icons/large/next-button.png",
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

        // Create a TutorialModel with the provided texts and images
        TutorialModel model = new TutorialModel(tutorialTexts, tutorialImages);

        // Create a TutorialView and pass the stage and skin to it
        TutorialView view = new TutorialView(stage, skin);

        // Create a TutorialController with the model and view
        new TutorialController(model, view);
    }
}
