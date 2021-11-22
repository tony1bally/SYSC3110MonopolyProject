package Listener;
import Model.GamePlayer.Player;

/**
 * Interface for buyable locations
 * @author Tony Massaad
 */
public interface BuyableLocation {

    /**
     * Method for handling the functionality if the location is owned by another player
     * @param p Player, the player
     * @param totalDiceRoll Integer, the total dice roll
     * @param currentTurn Integer, the current turn
     */
    void handleLocationOwnedFunctionality(Player p, int totalDiceRoll, int currentTurn);

    /**
     * Method for handling the functionality for the User if the location is not owned
     * @param p Player, the player
     * @param totalDiceRoll Integer, the total dice roll
     * @param currentTurn Integer, the current turn
     */
    void handleLocationNotOwnedFunctionalityUser(Player p, int totalDiceRoll, int currentTurn);

    /**
     /**
     * Method for handling the functionality if the location is owned by the current player landing on it
     * @param p Player, the player
     * @param totalDiceRoll Integer, the total dice roll
     * @param currentTurn Integer, the current turn
     */
    void handleLocationOwnedByPlayerFunctionality(Player p, int totalDiceRoll, int currentTurn);

}