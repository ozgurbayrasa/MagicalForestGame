package it.unibz.inf.pp.clash.model;

public class EventHandler {
    private int currentPlayerIndex;
    private boolean ongoingMove;
    private boolean hasReinforcementUnit;
    private static final int FIRST_PLAYER = 0;
    private static final int SECOND_PLAYER = 1;

    public EventHandler() {
        this.currentPlayerIndex = FIRST_PLAYER;
        this.ongoingMove = false;  // This should be set according to game logic
        this.hasReinforcementUnit = true;  // This should be set according to game logic
    }

    /**
     * This method is called when the user calls reinforcements.
     * <p>
     * Note that this may be an "invalid" action.
     * For instance (depending on the rules of the game):
     * - if there is no reinforcement unit,
     * - if there is an ongoing move,
     * - etc.
     */
    public void skipTurn() {
        // Check if the action is invalid
        if (!hasReinforcementUnit || ongoingMove) {
            System.out.println("Cannot skip turn: invalid action.");
            return;
        }

        // Switch to the next player
        if (currentPlayerIndex == FIRST_PLAYER) {
            currentPlayerIndex = SECOND_PLAYER;
        } else {
            currentPlayerIndex = FIRST_PLAYER;
        }

        System.out.println("Turn skipped. Current player: " + getCurrentPlayer());
    }

    // Getter for the current player
    public int getCurrentPlayer() {
        return currentPlayerIndex;
    }

    // Setter for ongoingMove
    public void setOngoingMove(boolean ongoingMove) {
        this.ongoingMove = ongoingMove;
    }

    // Setter for hasReinforcementUnit
    public void setHasReinforcementUnit(boolean hasReinforcementUnit) {
        this.hasReinforcementUnit = hasReinforcementUnit;
    }
}
