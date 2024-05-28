package it.unibz.inf.pp.clash.model;

import java.util.List;

public class EventHandler {
    private int currentTurn;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean ongoingMove;
    private boolean hasReinforcementUnit;

    public EventHandler(List<Player> players) {
        this.currentTurn = 0;
        this.players = players;
        this.currentPlayerIndex = 0;
        this.ongoingMove = false;  // This should be set according to game logic
        this.hasReinforcementUnit = true;  // This should be set according to game logic
    }

    public void skipTurn() {
        // Check if the action is invalid
        if (!hasReinforcementUnit || ongoingMove) {
            System.out.println("Cannot skip turn: invalid action.");
            return;
        }

        currentTurn++;
        
        // Switch to the next player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        endOfTurn();
    }

    // Method to handle end-of-turn events
    private void endOfTurn() {
        // Placeholder for end-of-turn logic
        System.out.println("End of turn " + currentTurn + " for player " + getCurrentPlayer().getName());
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setOngoingMove(boolean ongoingMove) {
        this.ongoingMove = ongoingMove;
    }

    public void setHasReinforcementUnit(boolean hasReinforcementUnit) {
        this.hasReinforcementUnit = hasReinforcementUnit;
    }
}
