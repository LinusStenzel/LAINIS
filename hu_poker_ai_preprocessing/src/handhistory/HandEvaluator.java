package handhistory;

import java.util.ArrayList;

import handhistory.BettingRound.Turn;
import handhistory.Card.Suit;
import handhistory.Card.Value;

/**
 * can calculate the hand outcome of any given 5, 6 or 7 cards and save top 5
 * cards according to the rules of th poker
 * 
 * @author stenz
 *
 */
public class HandEvaluator {

	/*
	 * given values + combined
	 */
	private Card[] holeCards;
	private ArrayList<Card> board;
	private ArrayList<Card> melted;

	/**
	 * all possible hand outcome names of th poker
	 *
	 */
	public enum HandName {
		HIGH_CARD(0), PAIR(1), TWO_PAIR(2), SET(3), STRAIGHT(4), FLUSH(5), FULL_HOUSE(6),
		QUADS(7), STRAIGHT_FLUSH(8); // combined straight flush with royal flush

		/**
		 * for vectorizing
		 */
		private int value;

		public int getValue() {
			return value;
		}

		private HandName(int value) {
			this.value = value;
		}
	}

	private HandName handName;

	/**
	 * top five cards not simply by card values but by searching for cards
	 * associated to hand outcome plus maybe kickers(left over cards with highest
	 * card values)
	 */
	private ArrayList<Card> topFive;

	/*
	 * aid for analyzing possible hand outcomes
	 */
	private int[] numOfSameValue = new int[13];
	private int[] numOfSameSuit = new int[4];

	/**
	 * for adding right amount of left over kicker cards
	 */
	private int amountOverFive;

	/*
	 * getters
	 */

	public HandName getHandName() {
		return handName;
	}

	public ArrayList<Card> getTopFive() {
		return topFive;
	}

	public HandEvaluator(ArrayList<Card> board, Card[] holeCards, Turn turn) {
		this.board = board;
		this.holeCards = holeCards;
		melt();
		howMany(turn);
	}

	/**
	 * melts hole cards and board to new array list
	 */
	private void melt() {
		ArrayList<Card> melted = new ArrayList<Card>(board);
		melted.add(holeCards[0]);
		melted.add(holeCards[1]);
		this.melted = melted;
	}

	/**
	 * calcs amount of same card values of melted
	 */
	private void numOfSameValue() {
		for (int i = 0; i < melted.size(); i++) {
			numOfSameValue[melted.get(i).getValue().getValue()]++;
		}
	}

	/**
	 * calcs amount of same card suits of melted
	 */
	private void numOfSameSuit() {
		for (int i = 0; i < melted.size(); i++) {
			numOfSameSuit[melted.get(i).getSuit().getValue()]++;
		}
	}

	/**
	 * "main" function of class evaluates hand by analyzing given data; saves top 5
	 * cards and hand name in a hand outcome
	 * 
	 * @return new filled hand outcome instance
	 */
	public HandOutcome evaluate() {

		// data is needed for nearly every check method
		numOfSameValue();
		numOfSameSuit();

		// checks top to bottom if melted cards are that hand outcome
		// top to bottom grants easier handling in check functions because many
		// situation are then impossible to reach and have no need get checked
		if ((topFive = checkStraightFlush()) != null) {
			handName = HandName.STRAIGHT_FLUSH;
		} else if ((topFive = checkQuads()) != null) {
			handName = HandName.QUADS;
		} else if ((topFive = checkFullHouse()) != null) {
			handName = HandName.FULL_HOUSE;
		} else if ((topFive = checkFlush()) != null) {
			handName = HandName.FLUSH;
		} else if ((topFive = checkStraight()) != null) {
			handName = HandName.STRAIGHT;
		} else if ((topFive = checkSet()) != null) {
			handName = HandName.SET;
		} else if ((topFive = checkTwoPair()) != null) {
			handName = HandName.TWO_PAIR;
		} else if ((topFive = checkPair()) != null) {
			handName = HandName.PAIR;
		} else {
			// if nothing above is true, high card is the only possibility
			handName = HandName.HIGH_CARD;
			topFive = new ArrayList<Card>();
			// just adding top five cards by values
			topFive = addTopRest(topFive);
		}

		return new HandOutcome(handName, topFive, holeCards);
	}

	/**
	 * checks for pair
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkPair() {
		Value cv = null;

		// check for any two same card values, save value
		for (int i = 0; i < numOfSameValue.length && cv == null; i++) {
			if (numOfSameValue[i] == 2) {
				cv = Value.fromInt(i);
			}
		}

		if (cv != null) { // found pair
			ArrayList<Card> topFive = new ArrayList<Card>();

			// add pair to top 5
			for (int i = 0; i < melted.size(); i++) {
				if (melted.get(i).getValue().getValue() == cv.getValue()) {
					topFive.add(melted.get(i));
				}
			}
			removeAddedCards(topFive);
			return addTopRest(topFive);
		} else {
			return null;
		}
	}

	/**
	 * checks for two pair
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkTwoPair() {
		Value cv1 = null;
		Value cv2 = null;

		// check for any two two same card values, save values
		for (int i = numOfSameValue.length - 1; i >= 0
				&& (cv1 == null || cv2 == null); i--) {
			if (numOfSameValue[i] == 2) {
				if (cv1 == null) {
					cv1 = Value.fromInt(i);
				} else {
					cv2 = Value.fromInt(i);
				}
			}
		}

		if (cv1 != null && cv2 != null) { // found two pairs
			ArrayList<Card> topFive = new ArrayList<Card>();

			// add both pairs to top 5
			for (int i = 0; i < melted.size(); i++) {
				if (melted.get(i).getValue().equals(cv1)
						|| melted.get(i).getValue().equals(cv2)) {
					topFive.add(melted.get(i));
				}
			}
			removeAddedCards(topFive);
			return addTopRest(topFive);
		} else {
			return null;
		}
	}

	/**
	 * checks for set
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkSet() {
		Value cv = null;

		// check for any three same card values, save value
		for (int i = 0; i < numOfSameValue.length && cv == null; i++) {
			if (numOfSameValue[i] == 3) {
				cv = Value.fromInt(i);
			}
		}

		if (cv != null) { // found set
			ArrayList<Card> topFive = new ArrayList<Card>();

			// add set to top 5
			for (int i = 0; i < melted.size(); i++) {
				if (melted.get(i).getValue().equals(cv)) {
					topFive.add(melted.get(i));
				}
			}
			removeAddedCards(topFive);
			return addTopRest(topFive);
		} else {
			return null;
		}
	}

	/**
	 * checks for straight
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkStraight() {
		boolean isStraight = false;
		Value cv = null;

		// check for five successive card values, save lowest value
		for (int i = numOfSameValue.length - 5; i >= 0 && !isStraight; i--) {
			isStraight = true;
			for (int j = 0; j < 5 && isStraight; j++) {
				isStraight = numOfSameValue[i + j] >= 1;
			}
			if (isStraight) {
				cv = Value.fromInt(i);
			}
		}

		if (cv != null) { // found straight
			ArrayList<Card> topFive = new ArrayList<Card>();
			Value tempCv;

			// add straight to top 5
			for (int i = 0; i < 5; i++) {
				// card values to add by lowest value
				tempCv = Value.fromInt(cv.getValue() + i);
				addStraightCard(topFive, tempCv);
			}
			return topFive;
		} else { // straight may go from ace to five
			isStraight = true;

			// check for sub straight(two to five)
			for (int i = 0; i < 4 && isStraight; i++) {
				isStraight = numOfSameValue[i] >= 1;
			}

			if (isStraight && numOfSameValue[12] >= 1) { // found lowest possible straight
				ArrayList<Card> topFive = new ArrayList<Card>();

				// manually saving all needed card values
				Value[] cvs = new Value[5];
				for (int i = 0; i < 4; i++) {
					cvs[i] = Value.fromInt(i);
				}
				cvs[4] = Value.ACE;

				for (int i = 0; i < cvs.length; i++) {
					addStraightCard(topFive, cvs[i]);
				}

				return topFive;
			} else {
				return null;
			}
		}
	}

	/**
	 * adds first occurrence of card with given value
	 * 
	 * @param topFive
	 * @param tempCv
	 */
	private void addStraightCard(ArrayList<Card> topFive, Value tempCv) {
		boolean added = false;
		for (int j = 0; j < melted.size() && !added; j++) {
			added = melted.get(j).getValue().equals(tempCv);
			// only add one card of each value
			if (added) {
				topFive.add(melted.get(j));
			}
		}
	}

	/**
	 * checks for flush
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkFlush() {
		Suit cs = null;

		// check for any five same card suits, save suit
		for (int i = 0; i < numOfSameSuit.length && cs == null; i++) {
			if (numOfSameSuit[i] >= 5) {
				cs = Suit.fromInt(i);
			}
		}

		if (cs != null) { // found flush
			ArrayList<Card> topFive = new ArrayList<Card>();

			// add highest cards of flush suit
			for (int i = melted.size() - 1; i >= 0 && topFive.size() < 5; i--) {
				if (melted.get(i).getSuit().equals(cs)) {
					topFive.add(melted.get(i));
				}
			}

			// sorting because of adding top to bottom before
			topFive.sort(Card.getCompByValue());
			return topFive;
		} else {
			return null;
		}

	}

	/**
	 * checks for full house
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkFullHouse() {
		Value cv1 = null;
		Value cv2 = null;

		// check for three and two same card values, save values
		for (int i = 0; i < numOfSameValue.length && (cv1 == null || cv2 == null); i++) {
			if (numOfSameValue[i] == 3) {
				cv1 = Value.fromInt(i);
			} else if (numOfSameValue[i] == 2) {
				cv2 = Value.fromInt(i);
			}
		}

		// both card values(of set and pair) may be three times in melted
		if (cv2 == null) {
			for (int i = 0; i < numOfSameValue.length && cv2 == null; i++) {
				// saving second card value if found and not same value as first card
				if (numOfSameValue[i] == 3 && !Value.fromInt(i).equals(cv1)) {
					cv2 = Value.fromInt(i);
				}
			}
		}

		if (cv1 != null && cv2 != null) { // full house found
			ArrayList<Card> topFive = new ArrayList<Card>();

			// add full house(pair and set) to top 5
			for (int i = melted.size() - 1; i >= 0 && topFive.size() < 5; i--) {
				if (melted.get(i).getValue().equals(cv1)
						|| melted.get(i).getValue().equals(cv2)) {
					topFive.add(melted.get(i));
				}
			}
			// sorting because of adding top to bottom before
			topFive.sort(Card.getCompByValue());
			return topFive;
		} else {
			return null;
		}

	}

	/**
	 * checks for quads
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkQuads() {
		Value cv = null;

		// check for four same card values, save value
		for (int i = 0; i < numOfSameValue.length && cv == null; i++) {
			if (numOfSameValue[i] == 4) {
				cv = Value.fromInt(i);
			}
		}

		if (cv != null) { // quads found
			ArrayList<Card> topFive = new ArrayList<Card>();

			// add quads to top 5
			for (int i = 0; i < melted.size(); i++) {
				if (melted.get(i).getValue().equals(cv)) {
					topFive.add(melted.get(i));
				}
			}
			removeAddedCards(topFive);
			return addTopRest(topFive);
		} else {
			return null;
		}
	}

	/**
	 * checks for straight flush; different approach than other check functions
	 * sorts melted cards by suit and checks every sub melted - cards 1 to 5, 2 to 6
	 * and/or 3 to 7 with - for the strict requirements of straight flush
	 * 
	 * @return top five cards if found - null if not
	 */
	private ArrayList<Card> checkStraightFlush() {
		// sorting by suit because cards must be successive and in same suit
		melted.sort(Card.getCompBySuit());
		ArrayList<Card> subMelted;

		for (int i = 0; i < amountOverFive + 1; i++) {
			subMelted = new ArrayList<Card>(melted.subList(i, i + 5));
			boolean subIsFlush = true;

			// check for flush - cards 2 to 5 have to be same suit as first card
			Suit cs = subMelted.get(0).getSuit();
			for (int j = 1; j < subMelted.size() && subIsFlush; j++) {
				subIsFlush = subMelted.get(j).getSuit().equals(cs);
			}

			if (subIsFlush) { // found flush
				boolean subIsStraight = true;

				// check for straight
				for (int j = 0; j < subMelted.size() - 1 && subIsStraight; j++) {
					subIsStraight = cardsAreSuccessive(subMelted, j);
				}

				if (subIsStraight) {
					// sort because of sorting by suit before
					melted.sort(Card.getCompByValue());
					return subMelted;
				} else { // straight may go from ace to five
					subIsStraight = true;

					for (int j = 0; j < subMelted.size() - 2 && subIsStraight; j++) {
						subIsStraight = cardsAreSuccessive(subMelted, j);
					}

					if (subIsStraight && subMelted.get(4).getValue().getValue() == 12
							&& subMelted.get(4).getValue().getValue() == 2) {
						melted.sort(Card.getCompByValue());
						return subMelted;
					}
				}
			}
		}
		// sorting here because always checking for straight flush first
		melted.sort(Card.getCompByValue());
		return null;
	}

	/*
	 * help function
	 */
	private boolean cardsAreSuccessive(ArrayList<Card> subMelted, int j) {
		return subMelted.get(j + 1).getValue().getValue()
				- subMelted.get(j).getValue().getValue() == 1;

	}

	/**
	 * adds left over card with highest card values
	 * 
	 * @param topFive
	 * @param amountOverFive
	 * @return
	 */
	private ArrayList<Card> addTopRest(ArrayList<Card> topFive) {
		// removing not required(worst) cards, may be all needed
		for (int i = 0; i < amountOverFive; i++) {
			melted.remove(0);
		}
		// add whole rest, may be nothing left
		for (int i = 0; i < melted.size(); i++) {
			topFive.add(melted.get(i));
		}
		return topFive;
	}

	/**
	 * just calcs how many, more than five, cards are given
	 * 
	 * @param turn the more progressed the more cards
	 */
	private void howMany(Turn turn) {
		switch (turn) {
		case FLOP:
			amountOverFive = 0;
			break;
		case TURN:
			amountOverFive = 1;
			break;
		case RIVER:
			amountOverFive = 2;
			break;
		default:
			break;
		}
	}

	/**
	 * removes already added(to top 5) cards from melted
	 * 
	 * @param topFive holding cards corresponding to hand outcome
	 */
	private void removeAddedCards(ArrayList<Card> topFive) {
		for (int i = 0; i < topFive.size(); i++) {
			melted.remove(melted.indexOf(topFive.get(i)));
		}
	}

}
