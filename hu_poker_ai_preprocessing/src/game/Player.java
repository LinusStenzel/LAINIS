package game;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import game.Action.ActionType;
import game.Action.Who;

public class Player {

	private String name;

	private int chips;
	private Card[] holeCards;
	private boolean button;

	private boolean isOutOfHand;

	private boolean isHuman;

	private static Scanner scanner = new Scanner(System.in);

	public static void closeScanner() {
		scanner.close();
	}

	public Player(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getChips() {
		return chips;
	}

	public void setChips(int chips) {
		this.chips = chips;
	}

	public Card[] getHoleCards() {
		return holeCards;
	}

	public void setHoleCards(Card[] holeCards) {
		this.holeCards = holeCards;
	}

	public boolean isButton() {
		return button;
	}

	public void setButton(boolean button) {
		this.button = button;
	}

	public boolean isOutOfHand() {
		return isOutOfHand;
	}

	public void setOutOfHand(boolean isOutOfHand) {
		this.isOutOfHand = isOutOfHand;
	}

	public boolean isHuman() {
		return isHuman;
	}

	public void setHuman(boolean isHuman) {
		this.isHuman = isHuman;
	}

	public int removeChips(int amount) {
		chips -= amount;
		return amount;
	}

	public int addChips(int amount) {
		return chips += amount;
	}

	public Action act(ActionType[] poss, int minBet, int maxBet, int callAmount, Who who) {
		System.out.println(name + " ist an der Reihe");
		System.out.println("Mögliche Aktionen: " + Arrays.toString(poss));
		System.out.println("Min Bet: " + minBet + " Max Bet: " + maxBet);
		System.out.println("Call Amount: " + callAmount);

		ActionType aType;
		int amount = 0;
		List<ActionType> possAsList = Arrays.asList(poss);

		do {
			System.out.println("Aktion eingeben!");

			String s = scanner.next();
			switch (s) {
			case "Fold":
				aType = ActionType.FOLD;
				isOutOfHand = true;
				break;
			case "Check":
				aType = ActionType.CHECK;
				break;
			case "Call":
				aType = ActionType.CALL;
				break;
			case "Bet":
				aType = ActionType.BET;
				break;
			case "Raise":
				aType = ActionType.RAISE;
				break;
			default:
				aType = null;
				break;
			}

			if (aType == ActionType.CALL) {
				amount = callAmount;
			} else if (aType == ActionType.BET || aType == ActionType.RAISE) {
				do {
					System.out.print("Amount eingeben!");
					amount = scanner.nextInt();
				} while (amount < minBet || amount > maxBet);
			}
		} while (!possAsList.contains(aType));

		Action a = new Action();
		a.setWho(who);
		a.setActionType(aType);
		a.setAmount(amount);
		a.setAllIn(amount == maxBet);
		return a;
	}

}
