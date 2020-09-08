package game;

import java.util.ArrayList;

import game.Card.Value;
import game.HandEvaluator.HandName;

/**
 * represents the outcome of any top five cards; stores most important data in
 * easy and compact way for feeding nn
 * 
 * @author stenz
 *
 */
public class HandOutcome {

	/*
	 * data given from hand evaluator
	 */
	private HandName handName;
	private ArrayList<Card> topCards;// sorted by value

	/*
	 * to calc hole cards in top cards count
	 */
	private Card[] holeCards;
	private int holeCardsInTopCards;

	/**
	 * needed for easier extracting important data
	 */
	private int[] numOfSameValue;

	/*
	 * for storing important data(vecs)
	 */
	private Card.Value kicker;
	private Card.Value pair;
	private Card.Value[] twoPair = new Card.Value[2]; // [0] value < [1] value
	private Card.Value set;
	private Card.Value topOfStraight;
	private Card.Value topOfFlush;
	private Card.Value[] fullHouse = new Card.Value[2]; // [0] 3 of xx [1] 2 of xx
	private Card.Value quads;
	private Card.Value topOfStraightFlush;

	/*
	 * getters
	 */

	public HandName getHandName() {
		return handName;
	}

	public Card.Value getKicker() {
		return kicker;
	}

	public Card.Value getPair() {
		return pair;
	}

	public Card.Value[] getTwoPair() {
		return twoPair;
	}

	public Card.Value getSet() {
		return set;
	}

	public Card.Value getTopOfStraight() {
		return topOfStraight;
	}

	public Card.Value getTopOfFlush() {
		return topOfFlush;
	}

	public Card.Value[] getFullHouse() {
		return fullHouse;
	}

	public Card.Value getQuads() {
		return quads;
	}

	public Card.Value getTopOfStraightFlush() {
		return topOfStraightFlush;
	}

	public int getHoleCardsInTopCards() {
		return holeCardsInTopCards;
	}

	/**
	 * refining respectively extract important data directly when creating instance
	 * 
	 * @param handName
	 * @param topCards
	 * @param holeCards
	 */
	public HandOutcome(HandName handName, ArrayList<Card> topCards, Card[] holeCards) {
		this.handName = handName;
		this.topCards = topCards;
		this.holeCards = holeCards;
		refine();
	}

	/**
	 * extracting important data by considering hand name because of granted hand
	 * name and top cards easy handling
	 */
	private void refine() {
		numOfSameValue();

		switch (handName) {
		case HIGH_CARD:
			// just best card
			kicker = topCards.get(4).getValue();
			break;
		case PAIR:
			// pair and kicker
			for (int i = 0; i < numOfSameValue.length; i++) {
				if (numOfSameValue[i] == 2) {
					pair = Value.fromInt(i);
				} else if (numOfSameValue[i] == 1) {
					kicker = Value.fromInt(i);
				}
			}
			break;
		case TWO_PAIR:
			// both pairs
			int where = 0;
			for (int i = 0; i < numOfSameValue.length; i++) {
				if (numOfSameValue[i] == 2) {
					twoPair[where] = Value.fromInt(i);
					where++;
				} else if (numOfSameValue[i] == 1) {
					kicker = Value.fromInt(i);
				}
			}
			break;
		case SET:
			// set and kicker
			for (int i = 0; i < numOfSameValue.length; i++) {
				if (numOfSameValue[i] == 3) {
					set = Value.fromInt(i);
				} else if (numOfSameValue[i] == 1) {
					kicker = Value.fromInt(i);
				}
			}

			break;
		case STRAIGHT:
			// all
			int[] valuesOfStraight = values();
			Value topValueStraight = Value.fromInt(valuesOfStraight[4]);

			// straight may go from ace to five
			if (topValueStraight == Value.ACE && topCards.get(0).getValue() == Value.TWO) {
				topOfStraight = Value.FIVE;
			} else {
				topOfStraight = topValueStraight;
			}
			break;
		case FLUSH:
			// all
			int[] valuesOfFlush = values();
			topOfFlush = Value.fromInt(valuesOfFlush[4]);
			break;
		case FULL_HOUSE:
			// all
			for (int i = 0; i < numOfSameValue.length; i++) {
				if (numOfSameValue[i] == 3) {
					fullHouse[0] = Value.fromInt(i);
				} else if (numOfSameValue[i] == 2) {
					fullHouse[1] = Value.fromInt(i);
				}
			}
			break;
		case QUADS:
			// quads
			for (int i = 0; i < numOfSameValue.length; i++) {
				if (numOfSameValue[i] == 4) {
					quads = Value.fromInt(i);
				}
			}
			break;
		case STRAIGHT_FLUSH:
			// all
			int[] valuesOfStraightFlush = values();
			Value topValueStaightFlush = Value.fromInt(valuesOfStraightFlush[4]);

			// straight may go from ace to five
			if (topValueStaightFlush == Value.ACE && topCards.get(0).getValue() == Value.TWO) {
				topOfStraightFlush = Value.FIVE;
			} else {
				topOfStraightFlush = topValueStaightFlush;
			}
			break;
		default:
			break;
		}

		holeCardsInTopCards();
	}

	/**
	 * calcs hole cards in top cards count
	 */
	private void holeCardsInTopCards() {
		for (int i = 0; i < topCards.size(); i++) {
			if (topCards.get(i).equals(holeCards[0]) || topCards.get(i).equals(holeCards[1])) {
				holeCardsInTopCards++;
			}
		}
	}

	/**
	 * calcs amount of same card values of top cards
	 */
	private void numOfSameValue() {
		numOfSameValue = new int[13];
		for (int i = 0; i < topCards.size(); i++) {
			numOfSameValue[topCards.get(i).getValue().getValue()]++;
		}
	}

	/**
	 * turns card array(list) to card value array
	 * 
	 * @return card value array
	 */
	private int[] values() {
		int[] values = new int[5];
		for (int i = 0; i < topCards.size(); i++) {
			values[i] = topCards.get(i).getValue().getValue();
		}
		return values;
	}

	/**
	 * converts this hand outcome into a 16 dimensional vector by
	 * 
	 * [0] hand outcome name; [1 - 3] first card(value,suit,isHolecard) of top five
	 * cards; [4 - 6] second card(..., ..., ...) of top five cards;...;
	 * 
	 * @return
	 */
	public int[] toVector() {
		int[] res = new int[16];
		res[0] = handName.getValue();

		// just looping over cards and vectorizing each
		for (int i = 0; i < topCards.size(); i++) {
			Card c = topCards.get(i);

			int[] tempCardVec = c.toVector();
			int[] cardAsVec = new int[3];

			cardAsVec[0] = tempCardVec[0];
			cardAsVec[1] = tempCardVec[1];
			cardAsVec[2] = c.equals(holeCards[0]) || c.equals(holeCards[1]) ? 1 : 0;

			for (int j = 0; j < cardAsVec.length; j++) {
				// writing values at right spot
				res[1 + j + i * 3] = cardAsVec[j];
			}
		}
		return res;
	}

	public int compareTo(HandOutcome other) {
		if (this.handName.compareTo(other.handName) != 0) {
			return this.handName.compareTo(other.handName);
		} else {
			switch (handName) {
			case HIGH_CARD:
				return compareTopFive(other);
			case PAIR:
				if (this.pair.compareTo(other.pair) != 0) {
					return this.pair.compareTo(other.pair);
				} else {
					return compareTopFive(other);
				}
			case TWO_PAIR:
				if (this.twoPair[1].compareTo(other.twoPair[1]) != 0) {
					return this.twoPair[1].compareTo(other.twoPair[1]);
				} else if (this.twoPair[0].compareTo(other.twoPair[0]) != 0) {
					return this.twoPair[0].compareTo(other.twoPair[0]);
				} else {
					return compareTopFive(other);
				}
			case SET:
				if (this.set.compareTo(other.set) != 0) {
					return this.set.compareTo(other.set);
				} else {
					return compareTopFive(other);
				}
			case STRAIGHT:
				return this.topCards.get(4).getValue().compareTo(other.topCards.get(4).getValue());
			case FLUSH:
				return this.topCards.get(4).getValue().compareTo(other.topCards.get(4).getValue());
			case FULL_HOUSE:
				if (this.fullHouse[0].compareTo(other.fullHouse[0]) != 0) {
					return this.fullHouse[0].compareTo(other.fullHouse[0]);
				} else {
					return this.fullHouse[1].compareTo(other.fullHouse[1]);
				}
			case QUADS:
				if (this.quads.compareTo(other.quads) != 0) {
					return this.quads.compareTo(other.quads);
				} else {
					return compareTopFive(other);
				}
			case STRAIGHT_FLUSH:
				return this.topCards.get(4).getValue().compareTo(other.topCards.get(4).getValue());
			default:
				return 0;
			}
		}
	}

	private int compareTopFive(HandOutcome other) {
		boolean found = false;
		int i = 4;
		do {
			found = this.topCards.get(i).getValue() != other.topCards.get(i).getValue();
			i += !found ? 1 : 0;

		} while (found && i >= 0);

		return this.topCards.get(i).getValue().compareTo(other.topCards.get(i).getValue());
	}

	@Override
	public String toString() {
		String res = "";
		res = res.concat(handName.toString()) + "\n";
		res = res.concat(topCards.toString()) + "\n";
		return res;

	}

}
