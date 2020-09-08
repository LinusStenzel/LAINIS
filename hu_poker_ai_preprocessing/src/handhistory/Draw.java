package handhistory;

import java.util.ArrayList;

import handhistory.BettingRound.Turn;
import handhistory.HandEvaluator.HandName;

/**
 * for calculating outs of a poker hand on flop and turn
 * 
 * @author stenz
 *
 */
public class Draw {

	/*
	 * all found hand outcomes to properly chance of hitting it
	 */
	private ArrayList<HandOutcome> handOutcomes = new ArrayList<HandOutcome>();
	private ArrayList<Float> chances = new ArrayList<Float>();

	/**
	 * to evaluate current hands
	 */
	private HandEvaluator handEvaluator;

	public Draw() {
	}

	/**
	 * recursive method that runs through all possible board combination from
	 * current status to river and then evaluates board and hole cards to a hand
	 * outcome
	 * 
	 * @param deck      all current cards
	 * @param board
	 * @param holeCards
	 * @param chance    to hit the status
	 */
	public void calculateDraws(Deck deck, ArrayList<Card> board, Card[] holeCards, float chance) {
		if (board.size() == 5) { // on river
			// evaluating current status of board and hole cards
			handEvaluator = new HandEvaluator(board, holeCards, Turn.RIVER);
			HandOutcome handOutcome = handEvaluator.evaluate();

			// adding results
			handOutcomes.add(handOutcome);
			chances.add(chance);
		} else {
			// because every card is equally likely to be hit
			chance = chance / deck.size();

			// recursive calling every new possible game state with changed vars
			for (int i = 0; i < deck.size(); i++) {
				ArrayList<Card> futureBoard = new ArrayList<Card>(board);
				Deck futureDeck = deck.copy();

				Card card = deck.getCardAt(i);

				futureBoard.add(card);
				futureDeck.removeCard(card);
				calculateDraws(futureDeck, futureBoard, holeCards, chance);
			}
		}
	}

	/**
	 * combining all same game states according to the rules of th poker
	 * 
	 * @return a new filled outs instance
	 */
	public Outs combineOutcomes() {
		Outs outs = new Outs();

		for (int i = 0; i < handOutcomes.size(); i++) {
			// getting data pairs(hand outcome, chance)
			HandOutcome handOutcome = handOutcomes.get(i);
			HandName handName = handOutcome.getHandName();
			float chance = chances.get(i);

			// changing chance by considering hole cards to top cards
			chance = holeInTopToChance(handOutcome, chance);

			// add chance at according vector spot
			switch (handName) {
			case HIGH_CARD:
				outs.addChance(handName, handOutcome.getKicker().getValue(), chance);
				break;
			case PAIR:
				outs.addChance(handName, handOutcome.getPair().getValue(), chance);
				break;
			case TWO_PAIR:
				outs.addChance(handName, handOutcome.getTwoPair()[1].getValue(), chance);
				break;
			case SET:
				outs.addChance(handName, handOutcome.getSet().getValue(), chance);
				break;
			case STRAIGHT:
				outs.addChance(handName, handOutcome.getTopOfStraight().getValue(), chance);
				break;
			case FLUSH:
				outs.addChance(handName, handOutcome.getTopOfFlush().getValue(), chance);
				break;
			case FULL_HOUSE:
				outs.addChance(handName, handOutcome.getFullHouse()[0].getValue(), chance);
				break;
			case QUADS:
				outs.addChance(handName, handOutcome.getQuads().getValue(), chance);
				break;
			case STRAIGHT_FLUSH:
				outs.addChance(handName, handOutcome.getTopOfStraightFlush().getValue(), chance);
				break;
			default:
				break;
			}

		}

		// trying to remove always zero values
		try {
			outs.removeUselessSpots();
		} catch (Exception e) {
			System.err.print("removed non zero");
			e.printStackTrace();
		}

		return outs;

	}

	/**
	 * changing chance because the top five cards of a hand outcome could contain 0,
	 * 1 or 2 of my hole cards; holding 0 of top 5 cards is horrible e.g.
	 * 
	 * @param handOutcome to get hand name and hole in top 5 cards value
	 * @param chance      to change
	 * @return changed chance
	 */
	private float holeInTopToChance(HandOutcome handOutcome, float chance) {
		// TODO erst ab evo nutzen und werte vererben

		switch (handOutcome.getHoleCardsInTopCards()) {
		case 0:
			chance *= 1;
			break;
		case 1:
			if (handOutcome.getHandName() == HandName.TWO_PAIR) {
				chance *= 1;
			} else {
				chance *= 1;
			}
			break;
		case 2:
			chance *= 1;
			break;
		default:
			break;
		}
		return chance;
	}

}
