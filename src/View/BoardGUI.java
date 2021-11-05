package View;

import Events.*;
import Listener.BoardView;
import Model.BoardModel;
import Model.Location;
import Model.Player;
import View.Components.GameDisplayPanel;
import View.Components.PlayerDisplayPanel;
import View.Controllers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BoardGUI extends JFrame implements BoardView{
    private final static int GAME_WIDTH = 985;
    private final static int GAME_HEIGHT = 807;
    private final static int[] DICE_DIM = new int[]{96, 96};


    private GameDisplayPanel gamePanel;
    private PlayerDisplayPanel sidePanel;
    private JPanel gameControlPanel;

    protected BoardModel model;
    private ArrayList<Player> gamePlayers;
    private int currentTurn;
    private final JButton turnPass, quit, roll, payOutOfJail, rollDouble, purchaseEstateHouses, sellHouses;
    private final ArrayList<Image> diceImages;
    private JLabel dice1, dice2;

    public BoardGUI(){
        super("Monopoly");
        this.gamePanel = new GameDisplayPanel();
        this.sidePanel = new PlayerDisplayPanel();
        this.currentTurn = 0;
        this.gamePlayers = new ArrayList<>();
        this.model = new BoardModel();
        this.turnPass = new JButton("Pass");
        this.quit = new JButton("Quit");
        this.roll = new JButton("Roll");
        this.payOutOfJail = new JButton("Pay out of jail");
        this.rollDouble = new JButton("Roll Double");
        this.purchaseEstateHouses = new JButton("Purchase Houses");
        this.sellHouses = new JButton("Sell Houses");

        BoardController controller = new BoardController(this.model);
        this.roll.setActionCommand(1 + " ");
        this.roll.addActionListener(controller);
        this.quit.setActionCommand(2 + " ");
        this.quit.addActionListener(controller);
        this.turnPass.setActionCommand(3 + " ");
        this.turnPass.addActionListener(controller);
        this.payOutOfJail.setActionCommand(4 + " ");
        this.payOutOfJail.addActionListener(controller);
        this.rollDouble.setActionCommand(5 + " ");
        this.rollDouble.addActionListener(controller);
        this.purchaseEstateHouses.setActionCommand(6 + " ");
        this.purchaseEstateHouses.addActionListener(controller);
        this.sellHouses.setActionCommand(7 + " ");
        this.sellHouses.addActionListener(controller);

        this.gameControlPanel = new JPanel();
        this.gameControlPanel.setLayout(new BoxLayout(this.gameControlPanel, BoxLayout.Y_AXIS));
        this.gameControlPanel.setBounds(520,315, 150,200);
        this.gameControlPanel.setBackground(new Color(255,255,255));
        this.setLayout(null);
        this.sidePanel.setBounds(0,0,200, GAME_HEIGHT);
        this.gamePanel.setBounds(200,0,GAME_WIDTH,GAME_HEIGHT);
        this.diceImages = new ArrayList<>(){{
            add(new ImageIcon(this.getClass().getResource("/DiceImages/dice1.png")).getImage().getScaledInstance(DICE_DIM[0], DICE_DIM[1], Image.SCALE_SMOOTH));
            add(new ImageIcon(this.getClass().getResource("/DiceImages/dice2.png")).getImage().getScaledInstance(DICE_DIM[0], DICE_DIM[1], Image.SCALE_SMOOTH));
            add(new ImageIcon(this.getClass().getResource("/DiceImages/dice3.png")).getImage().getScaledInstance(DICE_DIM[0], DICE_DIM[1], Image.SCALE_SMOOTH));
            add(new ImageIcon(this.getClass().getResource("/DiceImages/dice4.png")).getImage().getScaledInstance(DICE_DIM[0], DICE_DIM[1], Image.SCALE_SMOOTH));
            add(new ImageIcon(this.getClass().getResource("/DiceImages/dice5.png")).getImage().getScaledInstance(DICE_DIM[0], DICE_DIM[1], Image.SCALE_SMOOTH));
            add(new ImageIcon(this.getClass().getResource("/DiceImages/dice6.png")).getImage().getScaledInstance(DICE_DIM[0], DICE_DIM[1], Image.SCALE_SMOOTH));
        }};
        this.dice1 = new JLabel(new ImageIcon(this.diceImages.get(0)));
        this.dice2 = new JLabel(new ImageIcon(this.diceImages.get(0)));
        this.dice1.setBounds(450,150,96,96);
        this.dice2.setBounds(650,150,96,96);
        this.add(this.dice1);
        this.add(this.dice2);
        this.add(this.gameControlPanel);
        this.add(this.gamePanel);
        this.add(this.sidePanel);
        this.model.addView(this);
        this.model.addViewToListener(this);
        this.setSize(GAME_WIDTH,GAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);

        StartGameController start = new StartGameController();
        int num = start.getNumOfPlayers(this);
        ArrayList<String> names = start.getNameOfPlayers(num, this);
        for (int i = 0; i < num; i++){
            this.gamePlayers.add(new Player(names.get(i)));
            this.sidePanel.addNewPlayerViewButton(this.gamePlayers.get(i), i);
        }
        this.updateChoicePanel();
    }

    //****BEGINNING OF PROPERTY FUNCTIONS**//

    /**
     * Overridden method to handle a property with no owner. A player can either pass on or buy a property.
     * If bought, give player choices to purchase houses.
     * @param e A Events.PropertyEvent e.
     */
    @Override
    public void propertyNoOwner(PropertyEvent e) {
        LocationController control = new LocationController();
        ConfirmMessageController messageController = new ConfirmMessageController();
        int result = control.LocationNoOwnerController(this, e.getProperty().getName(), e.getProperty().getCost());
        if (result == JOptionPane.YES_OPTION) {
            if (e.getProperty().buy(e.getPlayer())) {
                messageController.sendMessage(this, "Not enough Money, moving to the next player");
                return;
            }
            this.model.announcePurchasingProperty(e.getProperty());
        }
    }


    /**
     * Overridden method to handle a property already owned by a player.
     * Can either buy houses/hotels or pass.
     * @param e A Events.PropertyEvent e.
     */
    @Override
    public void propertyOwned(PropertyEvent e) {
        ConfirmMessageController messageController = new ConfirmMessageController();
        messageController.sendMessage(this, "You landed on " + e.getProperty().getName() + ", Property you own");
    }


    /**
     * Overridden method to handle a property owned by a player not currently on their turn.
     * If the player does not have enough money to pay rent, they are bankrupted.
     * @param e A Events.PropertyEvent e.
     */
    @Override
    public void propertyRent(PropertyEvent e) {
        Player owner = e.getProperty().getOwner();
        Player landedPlayer = e.getPlayer();
        this.model.announcePlayerMessage("You landed on " + e.getProperty().getName() + " Owned by " + owner.getPlayerName() + " and rent is $" + e.getProperty().getRent());
        int doubleAmount = 1;
        if (owner.numberOfColoredPropertiesOwned(e.getProperty().getColor(), e.getProperty().getNumberOfColor()))
            doubleAmount = 2;
        int landedPlayerMoney = landedPlayer.getMoneyAmount();
        int rentCost = e.getProperty().getRentCost(e.getProperty().getNumOfHouses());
        if (landedPlayerMoney <= rentCost){
            owner.setMoneyAmount(owner.getMoneyAmount() + landedPlayerMoney);
            this.model.announceBankruptedPlayer(landedPlayer);
            return;
        }
        landedPlayer.setMoneyAmount(landedPlayer.getMoneyAmount() - rentCost);
        owner.setMoneyAmount(landedPlayer.getMoneyAmount() + (rentCost * doubleAmount));
    }


    // ** ENDING OF PROPERTY IMPLEMENTATION


    //** BEGINNING OF RAIL ROAD IMPLEMENTATION **//

    /**
     * Overridden method to handle a railroad with no owner. A player can either pass on or buy a property.
     * @param e A Events.RailRoadEvent e.
     */
    @Override
    public void railRoadNoOwner(RailRoadEvent e) {
        ConfirmMessageController messageController = new ConfirmMessageController();
        LocationController control = new LocationController();
        int result = control.LocationNoOwnerController(this, e.getRailRoad().getName(), e.getRailRoad().getCost());
        if (result == JOptionPane.YES_OPTION) {
            if (e.getRailRoad().buy(e.getPlayer())) {
                messageController.sendMessage(this, "Not enough Money, moving to the next player");
                return;
            }
            this.model.announcePurchasingProperty(e.getRailRoad());
        }
    }



    /**
     * Method to handle a railroad that a player who owns it lands on.
     * @param e A Events.RailRoadEvent e.
     */
    @Override
    public void railRoadOwned(RailRoadEvent e) {
        ConfirmMessageController messageController = new ConfirmMessageController();
        messageController.sendMessage(this, "You landed on " + e.getRailRoad().getName() + ", Property you own");
    }


    /**
     * Overridden method to handle a railroad owned by a player not currently on their turn.
     * If the player does not have enough money to pay rent, they are bankrupted.
     * @param e A Events.RailRoadEvent e.
     */
    @Override
    public void railRoadRent(RailRoadEvent e) {
        Player owner = e.getRailRoad().getOwner();
        this.model.announcePlayerMessage("You landed on " + e.getRailRoad().getName() + " Owned by " + owner.getPlayerName() + " and payment is $" + e.getRailRoad().getPayment());
        int landedPlayerMoney = e.getPlayer().getMoneyAmount();
        int payment = e.getRailRoad().getPayment(owner.getNumOfRailroads());
        if (landedPlayerMoney <= payment){
            owner.setMoneyAmount(owner.getMoneyAmount() + landedPlayerMoney);
            this.model.announceBankruptedPlayer((e.getPlayer()));
            return;
        }
        e.getPlayer().setMoneyAmount(e.getPlayer().getMoneyAmount() - payment);
        owner.setMoneyAmount(e.getPlayer().getMoneyAmount() + payment);
    }

    // **END OF RAIL ROAD IMPLEMENTATION** //

    //** BEGINNING OF UTILITY IMPLEMENTATION **//

    /**
     * Overridden method to handle a utility with no owner. A player can either pass on or buy a property.
     * @param e A Events.UtilityEvent e.
     */
    @Override
    public void UtilityNoOwner(UtilityEvent e) {
        ConfirmMessageController messageController = new ConfirmMessageController();
        LocationController control = new LocationController();
        int result = control.LocationNoOwnerController(this, e.getUtility().getName(), e.getUtility().getCost());
        if (result == JOptionPane.YES_OPTION) {
            if (e.getUtility().buy(e.getPlayer())) {
                messageController.sendMessage(this, "Not enough Money, moving to the next player");
                return;
            }
            this.model.announcePurchasingProperty(e.getUtility());
        }
    }


    /**
     * Method to handle a utility that a player who owns it lands on.
     * @param e A Events.UtilityEvent e.
     */
    @Override
    public void UtilityOwned(UtilityEvent e) {
        ConfirmMessageController messageController = new ConfirmMessageController();
        messageController.sendMessage(this, "You landed on " + e.getUtility().getName() + ", Property you own");
    }


    /**
     * Overridden method to handle a utility owned by a player not currently on their turn.
     * If the player does not have enough money to pay rent, they are bankrupted.
     * @param e A Events.UtilityEvent e.
     */
    @Override
    public void UtilityPay(UtilityEvent e) {
        Player owner = e.getUtility().getOwner();
        this.model.announcePlayerMessage("You landed on " + e.getUtility().getName() + " Owned by " + owner.getPlayerName() + ".\n" +
                "Number of utilities owned is " + owner.getNumOfUtilities() + "Payment (dice roll * (10 if 2 utilities else 4)) is $" + e.getUtility().payment(e.getTotalDiceRoll()));
        int landedPlayerMoney = e.getPlayer().getMoneyAmount();
        int payment = e.getUtility().payment(e.getTotalDiceRoll());
        if (landedPlayerMoney <= payment){
            owner.setMoneyAmount(owner.getMoneyAmount() + landedPlayerMoney);
            this.model.announceBankruptedPlayer(e.getPlayer());
            return;
        }
        e.getPlayer().setMoneyAmount(e.getPlayer().getMoneyAmount() - payment);
        owner.setMoneyAmount(e.getPlayer().getMoneyAmount() + payment);
    }

    // ** END OF UTILITY IMPLEMENTATION **

    /**
     * Overridden method for handling free parking.
     * If a player lands here they get the money in the "center" of the board.
     * @param e A Events.Tax_FreeParkingEvent event.
     */
    @Override
    public void FreeParking(Tax_FreeParkingEvent e) {
        if (e.getLocation().getCenterMoney() == 0)
            e.getLocation().setCenterMoney(100);
        this.model.announcePlayerMessage(e.getPlayer().getPlayerName() + " landed on free parking, they receive $" + e.getLocation().getCenterMoney());
        e.getPlayer().setMoneyAmount(e.getPlayer().getMoneyAmount() + e.getLocation().getCenterMoney());
        e.getLocation().setCenterMoney(0);
    }

    /**
     * Overridden method for handling the payment of tax.
     * If player runs out of money, they will go bankrupt.
     * @param e A Events.Tax_FreeParkingEvent event.
     */
    @Override
    public void payTax(Tax_FreeParkingEvent e) {
        this.model.announcePlayerMessage(e.getPlayer().getPlayerName() + " landed on " + e.getLocation().getName() + ", they lose $" + e.getLocation().getCost());
        if (e.getPlayer().getMoneyAmount() <= e.getLocation().getCost()){
            e.getLocation().addToCenterMoney(e.getPlayer().getMoneyAmount());
            this.model.announceBankruptedPlayer(e.getPlayer());
            return;
        }
        e.getLocation().addToCenterMoney(e.getLocation().getCost());
        e.getPlayer().setMoneyAmount(e.getPlayer().getMoneyAmount() - e.getLocation().getCost());
    }


    /**
     * Overridden method for handling "Just Visiting".
     * @param e A Events.LandOnJailEvent event.
     */
    @Override
    public void visiting(LandOnJailEvent e) {
        Player p = this.gamePlayers.get(this.currentTurn);
        this.model.announcePlayerMessage(p.getPlayerName() + " landed " + e.getLandOnJail().getName() + " - Just Visiting");
        p.setCurrLocation(e.getLandOnJail().getName() + "- Just Visiting");
    }


    /**
     * Overridden method for sending a player to jail.
     * @param e Events.GoToJailEvent e.
     */
    @Override
    public void SendPlayerToJail(GoToJailEvent e) {
        Player p = this.gamePlayers.get(this.currentTurn);
        this.model.announcePlayerMessage(p.getPlayerName() + " landed on " + e.getGoToJail().getName() + ", they go to jail.");
        e.getPlayer().setPosition(BoardModel.JAIL_POSITION);
        p.setCurrLocation(e.getGoToJail().getName());
        e.getPlayer().setInJail(true);
    }


    /**
     * Overridden method for handling a free pass. These replace the Chance + Community Chest as placeholders on the board.
     * @param e Events.FreePassEvent event.
     */
    @Override
    public void FreePass(FreePassEvent e) {
        // Do nothing
    }


    /* HANDLES */


    @Override
    public void handleAnnounceBankruptedPlayer(Player p){
        ConfirmMessageController messageController = new ConfirmMessageController();
        messageController.sendMessage(this, "Player: " + p.getPlayerName() + " has no more money. Removing player from game.");
        messageController.sendMessage(this, "Player properties are now back in estate!");
        this.removePlayer();
    }



    /**
     * Announce property purchasing to every player
     * @param place
     */
    @Override
    public void handleAnnounceLocationPurchasing(Location place){
        Player player = this.gamePlayers.get(this.currentTurn);
        ConfirmMessageController messageController = new ConfirmMessageController();
        messageController.sendMessage(this, "Player: " + player.getPlayerName() + " has purchased " + place.getName());
    }

    /**
     * Overridden boolean method for updating the game players if one loses the game or quits.
     * @param e A Events.BoardEvent e.
     * @return True if the game players is updated, false otherwise.
     */
    @Override
    public boolean updateGamePlayers(BoardEvent e) {
        Player p = this.gamePlayers.get(this.currentTurn);
        if (p.getMoneyAmount() == 0){
            this.model.announcePlayerMessage("Player: " + p.getPlayerName() + " is out of money and is now removed from the game!");
            this.removePlayer();
            return true;
        }
        return false;
    }

    private void removePlayer(){
        this.gamePlayers.get(this.currentTurn).bankrupted();
        this.gamePanel.removePieceFromBoard(this.currentTurn);
        this.sidePanel.removePlayerView(this.currentTurn, this.gamePlayers.get(this.currentTurn));
        this.gamePlayers.remove(this.currentTurn);
    }


    /**
     * Overridden method for handling the player quit.
     * @param e A Events.BoardEvent e.
     */
    @Override
    public void handlePlayerQuit(BoardEvent e) {
        Player quittingPlayer = this.gamePlayers.get(currentTurn);
        this.model.announcePlayerMessage("Player: " + quittingPlayer.getPlayerName() + " has quit the game");
        this.removePlayer();
    }

    /**
     * Overridden method for handling the announcement of a player passing the turn.
     * @param e A Events.BoardEvent e.
     */
    @Override
    public void announcePlayerPass(BoardEvent e) {
        Player p = this.gamePlayers.get(currentTurn);
        this.model.announcePlayerMessage("Player " + p.getPlayerName() + " passed the turn. Moving to the next player.");
    }

    /**
     * Overridden method for handling the next turn of the player.
     * @param e A Events.BoardEvent e.
     */
    @Override
    public void handleNextTurn(BoardEvent e) {
        this.currentTurn++;
        if (this.currentTurn == this.gamePlayers.size())
            this.currentTurn = 0;
    }


    /**
     * Overridden boolean method for handling the payment in jail.
     * @param e A Events.BoardEvent e.
     * @return True if player has paid the jail fee, false otherwise.
     */
    @Override
    public boolean payJail(BoardEvent e){
        Location place = e.boardElement(this.gamePlayers.get(this.currentTurn).getPosition());
        Player p = this.gamePlayers.get(this.currentTurn);
        ConfirmMessageController messageController = new ConfirmMessageController();
        if (this.gamePlayers.get(this.currentTurn).payJail()){
            this.model.announcePlayerMessage(p.getPlayerName() + " payed are out of jail and now just visiting");
            p.setCurrLocation(place.getName() + " - Just Visiting");
            p.setInJail(false);
            return true;
        }
        messageController.sendMessage(this, "Not enough money to get out of jail");
        return false;
    }

    /**
     * Button handle for rolling doubles to get out of jail
     * @param e BoardEvent, the BoardEvent
     */
    @Override
    public void handleRollingDoubles(BoardEvent e){
        Player p = this.gamePlayers.get(this.currentTurn);
        Location place = e.boardElement(this.gamePlayers.get(this.currentTurn).getPosition());
        this.updateRoll(e.getRoll1(), e.getRoll2());
        if (e.getDoubles()){
            this.model.announcePlayerMessage(p.getPlayerName() + " rolled a double! they are now Just Visiting Jail");
            p.setInJail(false);
            p.setCurrLocation(place.getName() + "- Just visiting");
            return;
        }
        p.setTurnsInJail(p.getTurnsInJail() - 1);
        if (p.getTurnsInJail() != 0){
            this.model.announcePlayerMessage(p.getPlayerName() + " attempted to roll out of jail. They failed and now have " + p.getTurnsInJail() + " left");
        }
        else{
            this.model.announcePlayerMessage(p.getPlayerName() + " are now out of turns in jail, they have to pay the $50");
            p.setMoneyAmount(p.getMoneyAmount() - 50);
            if (p.getMoneyAmount() == 0){
                this.model.announceBankruptedPlayer(p);
            }
        }
    }

    /**
     * Handle event for when the player presses roll
     * @param e BoardEvent, the BoardEvent
     */
    @Override
    public void handleGameplayRoll(BoardEvent e){
        Location place = e.boardElement(this.gamePlayers.get(this.currentTurn).getPosition());
        Player p = this.gamePlayers.get(this.currentTurn);
        this.updateRoll(e.getRoll1(), e.getRoll2());
        this.gamePanel.movePlayerPiece(this.currentTurn, e.diceSum());
        if (this.gamePlayers.get(this.currentTurn).movePlayer(e.diceSum())){
            e.getModel().announceReachingGo();
        }
        if (place.getName().equals("In Jail") && !p.getInJail()){
            p.setCurrLocation(place.getName() + ", Just visiting");
            e.boardElement(this.gamePlayers.get(this.currentTurn).getPosition()).locationElementFunctionality(this.gamePlayers.get(this.currentTurn), e.diceSum());
            return;
        }
        this.gamePlayers.get(this.currentTurn).setCurrLocation(e.boardElement(p.getPosition()).getName());
        e.boardElement(p.getPosition()).locationElementFunctionality(p, e.diceSum());
    }


    /**
     * Overridden method that announces when a player has reached GO!.
     */
    @Override
    public void announceReachingGo() {
        Player p = this.gamePlayers.get(this.currentTurn);
        this.model.announcePlayerMessage(p.getPlayerName() + " received $" + BoardModel.GO_MONEY + " for reaching GO");
    }

    /**
     * method to handle message announcement for each player
     * @param s String method
     */
    @Override
    public void handleMessageAnnouncement(String s){
        ConfirmMessageController controller = new ConfirmMessageController();
        controller.sendMessage(this, s);
    }


    @Override
    public void handleNextTurnDisplay(BoardEvent e){
        for (int i = 0; i<this.gamePlayers.size(); i++)
            this.sidePanel.updateCurrentTurn(this.currentTurn, i, this.gamePlayers);
    }

    @Override
    public void handleUpdateSidePanelDisplay(BoardEvent e){
        for (int i = 0; i<this.gamePlayers.size(); i++)
            this.sidePanel.updatePlayerDisplay(i, this.gamePlayers.get(i));
    }

    private void updateRoll(int roll1, int roll2) {
        this.dice1.setIcon(new ImageIcon(this.diceImages.get(roll1-1)));
        this.dice2.setIcon(new ImageIcon(this.diceImages.get(roll2-1)));
    }


    /**
     * Update panel according to the views
     */
    @Override
    public void updateChoicePanel() {
        for (Player p : this.gamePlayers) {
            this.gameControlPanel.removeAll();
            boolean inJail = p.getInJail();
            boolean hasProperty = p.numberOfEstateProperties() != 0;
            if (!inJail) {
                if (hasProperty) {
                    this.gameControlPanel.add(this.roll);
                    this.gameControlPanel.add(this.purchaseEstateHouses);
                    this.gameControlPanel.add(this.turnPass);
                    this.gameControlPanel.add(this.quit);
                } else {
                    this.gameControlPanel.add(this.roll);
                    this.gameControlPanel.add(this.turnPass);
                    this.gameControlPanel.add(this.quit);
                }
            } else {
                this.gameControlPanel.add(this.payOutOfJail);
                this.gameControlPanel.add(this.rollDouble);
                this.gameControlPanel.add(this.quit);
            }
            this.gameControlPanel.revalidate();
        }
    }

    public static void main(String[] args) {
        new BoardGUI();
    }

}
