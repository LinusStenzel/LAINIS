package handhistory;

import java.util.ArrayList;

/**
 * represents a pokerhand(=game) by listing each betting round
 * 
 * @author stenz
 *
 */
public class Hand {

	/**
	 * list of betting rounds
	 */
	private ArrayList<BettingRound> bettingRounds;

	/**
	 * big blind amount of this hand
	 */
	private int bigBlind;

	public Hand() {
		bettingRounds = new ArrayList<BettingRound>();
	}

	/*
	 * getters
	 */
	public ArrayList<BettingRound> getBettingRounds() {
		return bettingRounds;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	/**
	 * adds betting round to internal list
	 * 
	 * @param bettingRound to add
	 */
	public void addBettingRound(BettingRound bettingRound) {
		// extracting big blind from pre-flop betting round
		if (bettingRounds.isEmpty()) {
			// bb has to sb*2
			bigBlind = bettingRound.getBettingActions().get(0).getAmount() * 2;
		}

		bettingRounds.add(bettingRound);
	}

	public Hand copy() {
		Hand newHand = new Hand();
		ArrayList<BettingRound> newBettingRounds = new ArrayList<>();

		for (BettingRound br : this.bettingRounds) {
			BettingRound tempBR = new BettingRound(br.getTurn());
			tempBR.setBettingActions(new ArrayList<Action>(br.getBettingActions()));
			newBettingRounds.add(tempBR);
		}

		newHand.bettingRounds = newBettingRounds;
		newHand.bigBlind = this.bigBlind;
		return newHand;
	}

	@Override
	public String toString() {
		String all = "";
		for (BettingRound bettingRound : bettingRounds) {
			all = all.concat(bettingRound.toString()).concat("\n");
		}
		return all;
	}

}
