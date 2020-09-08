package game;

import java.util.Comparator;

/**
 * represents a single card of a 52 card deck 2 to Ace diamond to club
 * 
 * @author stenz
 *
 */
public class Card {

	/**
	 * all possible suits
	 *
	 */
	public enum Suit {
		DIAMOND(0), HEART(1), SPADE(2), CLUB(3);

		/**
		 * for (de)vectorizing and easier handling
		 */
		private int value;

		public int getValue() {
			return value;
		}

		private Suit(int value) {
			this.value = value;
		}

		public static Suit fromInt(int num) {
			Suit s;
			switch (num) {
			case 0:
				s = DIAMOND;
				break;
			case 1:
				s = HEART;
				break;
			case 2:
				s = SPADE;
				break;
			case 3:
				s = CLUB;
				break;
			default:
				s = null;
				break;
			}
			return s;
		}
	}

	/**
	 * all possible values
	 *
	 */
	public enum Value {
		TWO(0), THREE(1), FOUR(2), FIVE(3), SIX(4), SEVEN(5), EIGHT(6), NINE(7), TEN(8), JACK(9),
		QUEEN(10), KING(11), ACE(12);

		/**
		 * for (de)vectortizing and easier handling
		 */
		private int value;

		public int getValue() {
			return value;
		}

		private Value(int value) {
			this.value = value;
		}

		public static Value fromInt(int num) {
			Value v;

			switch (num) {
			case 0:
				v = TWO;
				break;
			case 1:
				v = THREE;
				break;
			case 2:
				v = FOUR;
				break;
			case 3:
				v = FIVE;
				break;
			case 4:
				v = SIX;
				break;
			case 5:
				v = SEVEN;
				break;
			case 6:
				v = EIGHT;
				break;
			case 7:
				v = NINE;
				break;
			case 8:
				v = TEN;
				break;
			case 9:
				v = JACK;
				break;
			case 10:
				v = QUEEN;
				break;
			case 11:
				v = KING;
				break;
			case 12:
				v = ACE;
				break;
			default:
				v = null;
				break;
			}
			return v;
		}
	}

	private Suit suit;
	private Value value;

	/*
	 * getters
	 */

	public Suit getSuit() {
		return suit;
	}

	public Value getValue() {
		return value;
	}

	public Card(Suit suit, Value value) {
		this.suit = suit;
		this.value = value;
	}

	/**
	 * cards are the same if they have the same suit and value
	 * 
	 * @param o to compare to
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Card)) {
			return false;
		}
		Card card = (Card) o;
		return card.suit == this.suit && card.value == this.value;
	}

	/**
	 * creates a card from chars
	 * 
	 * @param value
	 * @param suit
	 * @return
	 */
	public static Card getByChars(char value, char suit) {
		Suit s;
		Value v;

		switch (value) {
		case '2':
			v = Value.TWO;
			break;
		case '3':
			v = Value.THREE;
			break;
		case '4':
			v = Value.FOUR;
			break;
		case '5':
			v = Value.FIVE;
			break;
		case '6':
			v = Value.SIX;
			break;
		case '7':
			v = Value.SEVEN;
			break;
		case '8':
			v = Value.EIGHT;
			break;
		case '9':
			v = Value.NINE;
			break;
		case 'T':
			v = Value.TEN;
			break;
		case 'J':
			v = Value.JACK;
			break;
		case 'Q':
			v = Value.QUEEN;
			break;
		case 'K':
			v = Value.KING;
			break;
		case 'A':
			v = Value.ACE;
			break;
		default:
			v = null;
			break;
		}

		switch (suit) {
		case 'd':
			s = Suit.DIAMOND;
			break;
		case 'h':
			s = Suit.HEART;
			break;
		case 's':
			s = Suit.SPADE;
			break;
		case 'c':
			s = Suit.CLUB;
			break;
		default:
			s = null;
			break;
		}

		return new Card(s, v);
	}

	/**
	 * vectorizes the card by creating a two dim (value) vector - [0] value(1-13),
	 * [1] suit(1-4)
	 * 
	 * @return
	 */
	public int[] toVector() {
		int[] bothVec = new int[2];
		bothVec[0] = value.value + 1;
		bothVec[1] = suit.value + 1;
		return bothVec;
	}

	/**
	 * for sorting melted board and hole cards prefers value for suit
	 * 
	 * @return
	 */
	public static Comparator<Card> getCompByValue() {
		Comparator<Card> comp = new Comparator<Card>() {
			@Override
			public int compare(Card c1, Card c2) {
				int valueComp = Integer.valueOf(c1.getValue().value).compareTo(c2.getValue().value);

				if (valueComp != 0) {
					return valueComp;
				} else {
					return Integer.valueOf(c1.getSuit().value).compareTo(c2.getSuit().value);
				}

			}
		};
		return comp;
	}

	/**
	 * for sorting melted board and hole cards prefers suit for value
	 * 
	 * @return
	 */
	public static Comparator<Card> getCompBySuit() {
		Comparator<Card> comp = new Comparator<Card>() {
			@Override
			public int compare(Card c1, Card c2) {
				int suitComp = Integer.valueOf(c1.getSuit().value).compareTo(c2.getSuit().value);

				if (suitComp != 0) {
					return suitComp;
				} else {
					return Integer.valueOf(c1.getValue().value).compareTo(c2.getValue().value);
				}

			}
		};
		return comp;
	}

	public boolean isSuited(Card c) {
		return this.suit == c.suit;
	}

	public boolean isPaired(Card c) {
		return this.value == c.value;
	}

	public boolean isConnected(Card c) {
		return this.value.getValue() - c.value.getValue() == 1
				|| this.value.getValue() - c.value.getValue() == -1;
	}

	@Override
	public String toString() {
		return "(" + suit.toString() + " " + value.toString() + ")";
	}

}
