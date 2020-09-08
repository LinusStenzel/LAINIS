package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.Action.ActionType;
import game.Action.Who;
import game.BettingRound.Turn;

public class Game {

	// AI is Me, Human is Op

	String name;
	int bigBlind;

	int handNumber = 0;

	Player playerOne;
	int p1ChipsInFront;
	Player playerTwo;
	int p2ChipsInFront;

	Deck deck;
	List<Card> board;
	int pot;

	BettingRound preflop;
	BettingRound flop;
	BettingRound turn;
	BettingRound river;

	int allMoney = 3000;
	int[] blindStructure = new int[] { 20, 30, 40, 50, 60, 80, 100, 120 };
	int blindRaiseEveryXHands = 21;

	public Game(String name) {
		this.name = name;
		System.out.println("Spiel " + name + " startet");

		playerOne = new Player("human");
		playerTwo = new Player("ai");

		playerOne.setHuman(true);
		playerTwo.setHuman(false);

		playerOne.setChips(allMoney / 2);
		playerTwo.setChips(allMoney / 2);

		playerOne.setButton(true);
		playerTwo.setButton(false);

		do {
			handNumber++;
			bigBlind = blindStructure[handNumber / blindRaiseEveryXHands];
			pot = 0;
			board = new ArrayList<Card>();
			preflop = new BettingRound(Turn.PREFLOP);
			flop = new BettingRound(Turn.FLOP);
			turn = new BettingRound(Turn.TURN);
			river = new BettingRound(Turn.RIVER);

			playHand();
		} while (playerOne.getChips() != 0 && playerTwo.getChips() != 0);
		Player.closeScanner();
	}

	private void playHand() {
		boolean goOn;

		System.out.println(playerOne.getName() + " Chips: " + playerOne.getChips());
		System.out.println(playerTwo.getName() + " Chips: " + playerTwo.getChips());

		Player toAct = preflopDealer();
		goOn = play(Turn.PREFLOP, toAct);

		if (!goOn) {
			if (playerOne.isOutOfHand()) {
				playerTwo.addChips(pot);
				System.out.println(playerOne.getName() + " hat gefoldet");
			} else if (playerTwo.isOutOfHand()) {
				playerOne.addChips(pot);
				System.out.println(playerTwo.getName() + " hat gefoldet");
			}
		} else {

			toAct = flopDealer();
			goOn = play(Turn.FLOP, toAct);

			if (!goOn) {
				if (playerOne.isOutOfHand()) {
					playerTwo.addChips(pot);
					System.out.println(playerOne.getName() + " hat gefoldet");
				} else if (playerTwo.isOutOfHand()) {
					playerOne.addChips(pot);
					System.out.println(playerTwo.getName() + " hat gefoldet");
				}
			} else {

				toAct = turnDealer();
				goOn = play(Turn.TURN, toAct);

				if (!goOn) {
					if (playerOne.isOutOfHand()) {
						playerTwo.addChips(pot);
						System.out.println(playerOne.getName() + " hat gefoldet");
					} else if (playerTwo.isOutOfHand()) {
						playerOne.addChips(pot);
						System.out.println(playerTwo.getName() + " hat gefoldet");
					}
				} else {

					toAct = riverDealer();
					play(Turn.RIVER, toAct);

					if (!goOn) {
						if (playerOne.isOutOfHand()) {
							playerTwo.addChips(pot);
							System.out.println(playerOne.getName() + " hat gefoldet");
						} else if (playerTwo.isOutOfHand()) {
							playerOne.addChips(pot);
							System.out.println(playerTwo.getName() + " hat gefoldet");
						}
					} else {
						HandOutcome ho1 = new HandEvaluator(board, playerOne.getHoleCards(),
								Turn.RIVER).evaluate();
						HandOutcome ho2 = new HandEvaluator(board, playerTwo.getHoleCards(),
								Turn.RIVER).evaluate();
						int compared = ho1.compareTo(ho2);

						if (compared == 0) {
							playerOne.addChips(pot / 2);
							playerTwo.addChips(pot / 2);
						} else if (compared == 1) {
							playerOne.addChips(pot);
						} else if (compared == -1) {
							playerTwo.addChips(pot);
						}
					}
				}
			}
		}
	}

	private boolean play(Turn turnType, Player toAct) {
		boolean p1Acted = false;
		boolean p2Acted = false;

		Action a = null;
		do {
			if (toAct == playerOne) {

				ActionType[] poss;
				int minBet;
				int maxBet = playerOne.getChips();
				int callAmount = 0;
				if (p1ChipsInFront == p2ChipsInFront) {
					poss = new ActionType[] { ActionType.FOLD, ActionType.CHECK, ActionType.BET };
					minBet = bigBlind;
				} else {
					poss = new ActionType[] { ActionType.FOLD, ActionType.CALL, ActionType.RAISE };
					if (turnType == Turn.PREFLOP) {
						minBet = p2ChipsInFront + bigBlind;
					} else {
						minBet = p2ChipsInFront;
					}
					callAmount = p2ChipsInFront - p1ChipsInFront;
				}
				a = toAct.act(poss, minBet, maxBet, callAmount, Who.OP);

				p1ChipsInFront += playerOne.removeChips(a.getAmount());
				pot += a.getAmount();

				p1Acted = true;
				toAct = playerTwo;
			} else if (toAct == playerTwo) {

				ActionType[] poss;
				int minBet;
				int maxBet = playerTwo.getChips();
				int callAmount = 0;
				if (p2ChipsInFront == p1ChipsInFront) {
					poss = new ActionType[] { ActionType.FOLD, ActionType.CHECK, ActionType.BET };
					minBet = bigBlind;
				} else {
					poss = new ActionType[] { ActionType.FOLD, ActionType.CALL, ActionType.RAISE };
					if (turnType == Turn.PREFLOP) {
						minBet = p2ChipsInFront + bigBlind;
					} else {
						minBet = p2ChipsInFront;
					}
					callAmount = p1ChipsInFront - p2ChipsInFront;
				}
				a = toAct.act(poss, minBet, maxBet, callAmount, Who.ME);

				p2ChipsInFront += playerOne.removeChips(a.getAmount());
				pot += a.getAmount();

				p2Acted = true;
				toAct = playerOne;
			}

			switch (turnType) {
			case PREFLOP:
				preflop.addAction(a);
				break;
			case FLOP:
				flop.addAction(a);
				break;
			case TURN:
				turn.addAction(a);
				break;
			case RIVER:
				river.addAction(a);
				break;
			default:
				break;
			}
		} while (!(p1Acted && p2Acted && p1ChipsInFront == p2ChipsInFront)
				&& a.getActionType() != ActionType.FOLD);
		return !playerOne.isOutOfHand() && !playerTwo.isOutOfHand();
	}

	private Player preflopDealer() {
		deck = new Deck();
		deck.fill();
		deck.shuffle();
		System.out.println("Deck mischen...");

		Player toAct = null;

		System.out.println("Blinds werden gesetzt");
		System.out.println("Big Blind: " + bigBlind);
		if (playerOne.isButton()) {
			System.out.println(playerOne.getName() + " ist Button");

			pot += p1ChipsInFront = playerOne.removeChips(bigBlind / 2);
			Action sb = new Action();
			sb.setActionType(ActionType.SB);
			sb.setWho(Who.OP);
			preflop.addAction(sb);

			pot += p2ChipsInFront = playerTwo.removeChips(bigBlind);
			Action bb = new Action();
			bb.setActionType(ActionType.BB);
			bb.setWho(Who.ME);
			preflop.addAction(bb);

			toAct = playerOne;
		} else if (playerTwo.isButton()) {
			System.out.println(playerTwo.getName() + " ist Button");

			pot += p2ChipsInFront = playerTwo.removeChips(bigBlind / 2);
			Action sb = new Action();
			sb.setActionType(ActionType.SB);
			sb.setWho(Who.ME);
			preflop.addAction(sb);

			pot += p1ChipsInFront = playerOne.removeChips(bigBlind);
			Action bb = new Action();
			bb.setActionType(ActionType.BB);
			bb.setWho(Who.OP);
			preflop.addAction(bb);

			toAct = playerTwo;
		}

		playerOne.setHoleCards(new Card[] { deck.pop(), deck.pop() });
		System.out.println(playerOne.getName() + " erhält Karten: "
				+ Arrays.toString(playerOne.getHoleCards()));
		playerTwo.setHoleCards(new Card[] { deck.pop(), deck.pop() });
		System.out.println(playerTwo.getName() + " erhält Karten: "
				+ Arrays.toString(playerTwo.getHoleCards()));
		return toAct;
	}

	private Player flopDealer() {
		p1ChipsInFront = 0;
		p2ChipsInFront = 0;

		board.add(deck.pop());
		board.add(deck.pop());
		board.add(deck.pop());

		System.out.println("Board: " + board.toString());

		Player toAct = null;
		if (playerOne.isButton()) {
			toAct = playerTwo;
		} else if (playerTwo.isButton()) {
			toAct = playerOne;
		}
		return toAct;
	}

	private Player turnDealer() {
		p1ChipsInFront = 0;
		p2ChipsInFront = 0;

		board.add(deck.pop());

		System.out.println("Board: " + board.toString());

		Player toAct = null;
		if (playerOne.isButton()) {
			toAct = playerTwo;
		} else if (playerTwo.isButton()) {
			toAct = playerOne;
		}
		return toAct;
	}

	private Player riverDealer() {
		p1ChipsInFront = 0;
		p2ChipsInFront = 0;

		board.add(deck.pop());

		System.out.println("Board: " + board.toString());

		Player toAct = null;
		if (playerOne.isButton()) {
			toAct = playerTwo;
		} else if (playerTwo.isButton()) {
			toAct = playerOne;
		}
		return toAct;
	}

}
