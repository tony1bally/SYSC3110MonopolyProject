package Model;

import Events.GoToJailEvent;
import Listener.BoardView;
import Listener.GoToJailListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tony Massaad
 * Class MVC.GoToJail that defines a MVC.GoToJail element. Extends MVC.Location
 */
public class GoToJail extends Location{

    private List<GoToJailListener> goToJailListener;

    /**
     * Constructor for GoToJail for the board model
     * @param element enum of elements
     */
    public GoToJail(BoardModel.BoardElements element) {
        super(element.getCost(), element.getName());
        this.goToJailListener= new ArrayList<>();
    }

    /**
     * Describes functionality of the MVC.GoToJail element
     * @param p MVC.Player object p
     * @param totalDiceRoll Integer sum of dice roll
     * @param currentTurn
     * @return A boolean, always returns false
     */
    @Override
    public boolean locationElementFunctionality(Player p, int totalDiceRoll, int currentTurn) {
        int oldPos = p.getPosition();
        p.setPosition(BoardModel.JAIL_POSITION);
        p.setCurrLocation("In Jail");
        p.setInJail(true);
        for (GoToJailListener listener : this.goToJailListener){
            listener.SendPlayerToJail(new GoToJailEvent(this, p, currentTurn, oldPos, p.getPosition()));
        }
        return false;
    }

    /**
     * Java toString method
     * @param p A player p
     * @return A string sentence of someone landing on MVC.GoToJail
     */
    @Override
    public String toString(Player p) {
        return p.getPlayerName() + " landed on " + this.getName() + ". Being Sent to Jail.";
    }

    /**
     * Does nothing for this class
     */
    @Override
    public boolean buy(Player p) {
        return false;
    }

    /**
     * Does nothing for this class
     */
    @Override
    public void resetOwner() {

    }

    /**
     * Adds the view to the ArrayList of goToJailListeners
     * @param view A Listener.BoardView view
     */
    @Override
    public void addListener(BoardView view) {
        this.goToJailListener.add(view);
    }

}
