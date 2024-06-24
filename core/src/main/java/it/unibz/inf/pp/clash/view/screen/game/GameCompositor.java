package it.unibz.inf.pp.clash.view.screen.game;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import it.unibz.inf.pp.clash.controller.listeners.ContinueGameListener;
import it.unibz.inf.pp.clash.controller.listeners.ExitButtonListener;
import it.unibz.inf.pp.clash.controller.listeners.ReceiveModifierListener;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.view.screen.Compositor;
import it.unibz.inf.pp.clash.view.screen.sync.AnimationCounter;
import it.unibz.inf.pp.clash.view.singletons.Dimensions;
import it.unibz.inf.pp.clash.view.singletons.FontManager;
import it.unibz.inf.pp.clash.view.singletons.ImageManager;
import it.unibz.inf.pp.clash.view.singletons.SkinManager;

import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.FIRST;
import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.SECOND;
import static it.unibz.inf.pp.clash.view.singletons.ColorManager.GuiColor.BOARD_SEPARATOR;
import static it.unibz.inf.pp.clash.view.singletons.FontManager.FontType.INFORMATION;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.Icon.EXIT;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.IconSize.LARGE;

public class GameCompositor extends Compositor {

    private final PlayerCompositor playerCompositor;
    private final BoardCompositor boardCompositor;
    private static boolean showModifierSelectBox = false; // Flag to control modifierSelectBox visibility
    private static String[] listOfModifiers;

    public GameCompositor(EventHandler eventHandler, AnimationCounter animationCounter, boolean debug) {
        super(eventHandler, animationCounter, debug);
        playerCompositor = new PlayerCompositor(eventHandler, animationCounter, debug);
        boardCompositor = new BoardCompositor(eventHandler, debug, animationCounter);
    }

    /**
     * Returns a pointer to the Libgdx "actor" that contains the message displayed on screen.
     * For this reason, the main table (i.e. the "actor" that contains the whole game screen) is not returned by the method,
     * but passed as a parameter, and modified inside the method.
     */
    public Label drawGame(Snapshot previousSnapshot, Snapshot newSnapshot, String message, Table mainTable){

        Dimensions dimensions = Dimensions.instance();

        Board previousBoard = previousSnapshot == null?
                null:
                previousSnapshot.getBoard();
        Board newBoard = newSnapshot.getBoard();

        mainTable.add(
                playerCompositor.drawPlayer(
                        previousSnapshot,
                        newSnapshot,
                        SECOND
                )).top();

        mainTable.add(
                boardCompositor.drawPlayerBoard(
                        previousBoard,
                        newBoard,
                        newSnapshot.getOngoingMove().orElse(null),
                        SECOND
                )).expandY().bottom();

        Label messageLabel = drawMessage(
                message
        );
        mainTable.add(messageLabel)
                .width(dimensions.getInfoboxWidth())
                .pad(15);

        mainTable.row();

        mainTable.add();

        mainTable.add(drawBoardSeparation())
                .height(dimensions.getPlayerSeparatorHeight())
                .expand()
                .fill();

        if (showModifierSelectBox) { // Only draw modifierSelectBox if flag is true
            mainTable.add(drawModifierSelector(listOfModifiers)).height(dimensions.getPlayerSeparatorHeight());
        }

        mainTable.add();

        mainTable.row();

        mainTable.add(
                playerCompositor.drawPlayer(
                        previousSnapshot,
                        newSnapshot, FIRST
                )).bottom();

        mainTable.add(
                boardCompositor.drawPlayerBoard(
                        previousBoard,
                        newBoard,
                        newSnapshot.getOngoingMove().orElse(null),
                        FIRST
                )).expandY().top();

        mainTable.add(drawExitButton())
                .expandY().bottom();

        return messageLabel;
    }

    private Label drawMessage(String text) {
        Label label = FontManager.instance().getLabel(
                text,
                INFORMATION
        );
        label.setWrap(true);
        return label;
    }

    private Table drawBoardSeparation() {
        return ImageManager.instance().getColoredTable(BOARD_SEPARATOR);
    }

    private Table drawExitButton() {
        Table table = new Table();
        table.add(getExitButton());
        table.row();
        addMediumVerticalSpace(table);
        return table;
    }

    private ImageButton getExitButton() {
        ImageButton button = ImageManager.instance().getIconButton(EXIT, LARGE);
        button.addListener(
                new ExitButtonListener(eventHandler)
        );
        return button;
    }

    private Table drawModifierSelector(String[] listOfModifiers) {
        Table table = new Table();
        Skin skin = SkinManager.instance().getDefaultSkin();
        SelectBox<String> modifierSelectBox = new SelectBox<>(skin);
        modifierSelectBox.setItems(listOfModifiers);
        TextButton receiveButton = new TextButton("Receive", skin);
        receiveButton.addListener(new ReceiveModifierListener(modifierSelectBox, eventHandler));
        table.add(modifierSelectBox);
        addMediumVerticalSpace(table);
        table.add(receiveButton).height(50);
        return table;
    }

    // Method to toggle the modifierSelectBox visibility and set the list of modifiers
    public static void showModifierSelectBox(boolean show) {
        showModifierSelectBox = show;
    }

    public static void setListOfModifiers(String[] modifiers) {
        listOfModifiers = modifiers;
    }
}
