package game;

import java.util.ArrayList;
import java.util.Collections;

/**
 * represents a 52 card deck by listing each possible card
 * 
 * @author stenz
 *
 */
public class Deck {

	/**
	 * all contained cards
	 */
	private ArrayList<Card> cards = new ArrayList<Card>();

	public Deck() {
	}

	/**
	 * fills the deck by creating each possible card and adding it to list
	 */
	public void fill() {
		for (Card.Suit suit : Card.Suit.values()) {
			for (Card.Value value : Card.Value.values()) {
				Card card = new Card(suit, value);
				cards.add(card);
			}
		}
	}

	/**
	 * shuffles the deck
	 */
	public void shuffle() {
		Collections.shuffle(cards);
	}

	/**
	 * removes first card from deck and returns it
	 * @return removes card
	 */
	public Card pop() {
		return cards.remove(0);
	}

	/**
	 * 
	 * @param index
	 * @return card in deck at index
	 */
	public Card getCardAt(int index) {
		return cards.get(index);
	}

	/**
	 * removes the given Card from the deck and returns it
	 * @param card
	 * @return given Card if is in deck - null if not
	 */
	public Card removeCard(Card card) {
		//searching for it by looping over cards
		for (int i = 0; i < cards.size(); i++) {
			Card tempCard = cards.get(i);
			if (tempCard.equals(card)) {
				return cards.remove(i);
			}
		}
		return null;
	}

	/**
	 * 
	 * @return deck size by card number
	 */
	public int size() {
		return cards.size();
	}
	
	/**
	 * copies whole deck by creating new and filling it with same cards
	 * @return
	 */
	public Deck copy() {
		Deck deck = new Deck();
		for (int i = 0; i < cards.size(); i++) {
			deck.cards.add(cards.get(i));
		}
		return deck;
	}

}
