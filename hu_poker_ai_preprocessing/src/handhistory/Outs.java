package handhistory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import handhistory.HandEvaluator.HandName;

/**
 * stores and sets/adds chance values at right spot in arr lists; additional
 * removes always zero values
 * 
 * @author stenz
 *
 */
public class Outs {

	/*
	 * all arr lists to store vecs for nn representation - some are an arr itself
	 */
	private ArrayList<Float> highCard;
	private ArrayList<Float> pair;
	private ArrayList<Float> twoPair;
	private ArrayList<Float> set;
	private ArrayList<Float> straight;
	private ArrayList<Float> flush;
	private ArrayList<Float> fullHouse;
	private ArrayList<Float> quads;
	private ArrayList<Float> straightFlush;

	/*
	 * getters
	 */

	public ArrayList<Float> getHighCard() {
		return highCard;
	}

	public ArrayList<Float> getPair() {
		return pair;
	}

	public ArrayList<Float> getTwoPair() {
		return twoPair;
	}

	public ArrayList<Float> getSet() {
		return set;
	}

	public ArrayList<Float> getStraight() {
		return straight;
	}

	public ArrayList<Float> getFlush() {
		return flush;
	}

	public ArrayList<Float> getFullHouse() {
		return fullHouse;
	}

	public ArrayList<Float> getQuads() {
		return quads;
	}

	public ArrayList<Float> getStraightFlush() {
		return straightFlush;
	}

	/**
	 * inits all vecs with arr (list) [13] - of some only top layer
	 */
	public Outs() {
		highCard = new ArrayList<Float>(Arrays.asList(new Float[13]));
		pair = new ArrayList<Float>(Arrays.asList(new Float[13]));
		twoPair = new ArrayList<Float>(Arrays.asList(new Float[13]));
		set = new ArrayList<Float>(Arrays.asList(new Float[13]));
		straight = new ArrayList<Float>(Arrays.asList(new Float[13]));
		flush = new ArrayList<Float>(Arrays.asList(new Float[13]));
		fullHouse = new ArrayList<Float>(Arrays.asList(new Float[13]));
		quads = new ArrayList<Float>(Arrays.asList(new Float[13]));
		straightFlush = new ArrayList<Float>(Arrays.asList(new Float[13]));
		init();
	}

	/**
	 * just setting every value of vectors to 0
	 */
	private void init() {
		for (int i = 0; i < highCard.size(); i++) {
			highCard.set(i, 0.0F);
		}
		for (int i = 0; i < pair.size(); i++) {
			pair.set(i, 0.0F);
		}
		for (int i = 0; i < twoPair.size(); i++) {
			twoPair.set(i, 0.0F);
		}
		for (int i = 0; i < set.size(); i++) {
			set.set(i, 0.0F);
		}
		for (int i = 0; i < straight.size(); i++) {
			straight.set(i, 0.0F);
		}
		for (int i = 0; i < flush.size(); i++) {
			flush.set(i, 0.0F);
		}
		for (int i = 0; i < fullHouse.size(); i++) {
			fullHouse.set(i, 0.0F);
		}
		for (int i = 0; i < quads.size(); i++) {
			quads.set(i, 0.0F);
		}
		for (int i = 0; i < straightFlush.size(); i++) {
			straightFlush.set(i, 0.0F);
		}
	}

	/**
	 * adds chance of hitting either high card, straight, flush, quads or straight
	 * flush at "associated" vector spot
	 * 
	 * @param handName which arr list to work with
	 * @param index    obviously first layer
	 * @param chance   to add
	 */
	public void addChance(HandName handName, int index, Float chance) {
		switch (handName) {
		case HIGH_CARD:
			highCard.set(index, highCard.get(index) + chance);
			break;
		case PAIR:
			pair.set(index, pair.get(index) + chance);
			break;
		case TWO_PAIR:
			twoPair.set(index, twoPair.get(index) + chance);
			break;
		case SET:
			set.set(index, set.get(index) + chance);
			break;
		case STRAIGHT:
			straight.set(index, straight.get(index) + chance);
			break;
		case FLUSH:
			flush.set(index, flush.get(index) + chance);
			break;
		case FULL_HOUSE:
			fullHouse.set(index, fullHouse.get(index) + chance);
			break;
		case QUADS:
			quads.set(index, quads.get(index) + chance);
			break;
		case STRAIGHT_FLUSH:
			straightFlush.set(index, straightFlush.get(index) + chance);
			break;
		default:
			break;
		}

	}

	/**
	 * removes useless spots in chance arrays; only considering view on river ->
	 * seven cards
	 * 
	 * @throws Exception
	 */
	public void removeUselessSpots() throws Exception {
		// lowest possible high card is 9
		for (int i = 0; i < 7; i++) {
			removeFromSpot(HandName.HIGH_CARD, 0);
		}

		// higher two pair can not be 2
		removeFromSpot(HandName.TWO_PAIR, 0);

		// lowest possible "highest straight card" is 5
		for (int i = 0; i < 3; i++) {
			removeFromSpot(HandName.STRAIGHT, 0);
			removeFromSpot(HandName.STRAIGHT_FLUSH, 0);
		}

		// lowest possible "highest flush card" is 7
		for (int i = 0; i < 5; i++) {
			removeFromSpot(HandName.FLUSH, 0);
		}
	}

	/**
	 * remove value of either high pair, two pair, flush, set or full house at given
	 * vector spot
	 * 
	 * @param index obviously first layer
	 * @throws Exception when zero value is removed
	 */
	private void removeFromSpot(HandName handName, int index) throws Exception {
		Float removedValue;

		switch (handName) {
		case HIGH_CARD:
			removedValue = highCard.remove(index);
			break;
		case TWO_PAIR:
			removedValue = twoPair.remove(index);
			break;
		case STRAIGHT:
			removedValue = straight.remove(index);
			break;
		case FLUSH:
			removedValue = flush.remove(index);
			break;
		case STRAIGHT_FLUSH:
			removedValue = straightFlush.remove(index);
			break;
		default:
			removedValue = Float.MAX_VALUE;
			break;
		}

		if (removedValue != 0) {
			throw new Exception();
		}
	}

	/**
	 * vector representation is slimed down -> [26] 
	 * every hand outcome is just a two or three dim vector
	 * @return
	 */
	public float[] toVector() {
		float[] res = new float[26];

		float[] highCard = new float[2];
		for (int i = 0; i < this.highCard.size(); i++) {
			if (i < 3) {
				highCard[0] += this.highCard.get(i);//9-J
			} else {
				highCard[1] += this.highCard.get(i);//Q-A
			}
		}
		res[0] = highCard[0];
		res[1] = highCard[1];

		float[] pair = new float[3];
		for (int i = 0; i < this.pair.size(); i++) {
			if (i < 5) {
				pair[0] += this.pair.get(i);//2-6
			} else if (i < 9) {
				pair[1] += this.pair.get(i);//7-10
			} else {
				pair[2] += this.pair.get(i);//J-A
			}
		}
		res[2] = pair[0];
		res[3] = pair[1];
		res[4] = pair[2];

		float[] twoPair = new float[3];
		for (int i = 0; i < this.twoPair.size(); i++) {
			if (i < 4) {
				twoPair[0] += this.twoPair.get(i);//3-6
			} else if (i < 8) {
				twoPair[1] += this.twoPair.get(i);//7-10
			} else {
				twoPair[2] += this.twoPair.get(i);//J-A
			}
		}
		res[5] = twoPair[0];
		res[6] = twoPair[1];
		res[7] = twoPair[2];

		float[] set = new float[3];
		for (int i = 0; i < this.set.size(); i++) {
			if (i < 5) {
				set[0] += this.set.get(i);//2-6
			} else if (i < 9) {
				set[1] += this.set.get(i);//7-10
			} else {
				set[2] += this.set.get(i);//J-A
			}
		}
		res[8] = set[0];
		res[9] = set[1];
		res[10] = set[2];

		float[] straight = new float[3];
		for (int i = 0; i < this.straight.size(); i++) {
			if (i < 4) {
				straight[0] += this.straight.get(i);//5-8
			} else if (i < 7) {
				straight[1] += this.straight.get(i);//9-J
			} else {
				straight[2] += this.straight.get(i);//Q-A
			}
		}
		res[11] = straight[0];
		res[12] = straight[1];
		res[12] = straight[2];

		float[] flush = new float[3];
		for (int i = 0; i < this.flush.size(); i++) {
			if (i < 3) {
				flush[0] += this.flush.get(i);//7-9
			} else if (i < 6) {
				flush[1] += this.flush.get(i);//10-Q
			} else {
				flush[2] += this.flush.get(i);//K-A
			}
		}
		res[13] = flush[0];
		res[14] = flush[1];
		res[15] = flush[2];

		float[] fullHouse = new float[3];
		for (int i = 0; i < this.fullHouse.size(); i++) {
			if (i < 5) {
				fullHouse[0] += this.fullHouse.get(i);//2-6
			} else if (i < 9) {
				fullHouse[1] += this.fullHouse.get(i);//7-10
			} else {
				fullHouse[2] += this.fullHouse.get(i);//J-A
			}
		}
		res[16] = fullHouse[0];
		res[17] = fullHouse[1];
		res[18] = fullHouse[2];

		float[] quads = new float[3];
		for (int i = 0; i < this.quads.size(); i++) {
			if (i < 5) {
				quads[0] += this.quads.get(i);//2-6
			} else if (i < 9) {
				quads[1] += this.quads.get(i);//7-10
			} else {
				quads[2] += this.quads.get(i);//J-A
			}
		}
		res[19] = quads[0];
		res[20] = quads[1];
		res[21] = quads[2];

		float[] straightFlush = new float[3];
		for (int i = 0; i < this.straightFlush.size(); i++) {
			if (i < 4) {
				straightFlush[0] += this.straightFlush.get(i);//5-8
			} else if (i < 7) {
				straightFlush[1] += this.straightFlush.get(i);//9-J
			} else {
				straightFlush[2] += this.straightFlush.get(i);//Q-A
			}
		}
		res[22] = straightFlush[0];
		res[23] = straightFlush[1];
		res[24] = straightFlush[2];

		return res;
	}

	/**
	 * converts Float value to percent with two decimal places
	 * 
	 * @param value
	 * @return
	 */
	private String percent(Float value) {
		DecimalFormat df = new DecimalFormat("##.##%");
		return df.format(value);
	}

	/**
	 * just showing top level chances
	 */
	@Override
	public String toString() {
		String name = "";

		float chance = 0;
		for (int i = 0; i < highCard.size(); i++) {
			chance += highCard.get(i);
		}
		name = name.concat("High Card: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < pair.size(); i++) {
			chance += pair.get(i);
		}
		name = name.concat("Pair: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < twoPair.size(); i++) {
			chance += twoPair.get(i);
		}
		name = name.concat("Two Pair: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < set.size(); i++) {
			chance += set.get(i);

		}
		name = name.concat("Set: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < straight.size(); i++) {
			chance += straight.get(i);
		}
		name = name.concat("Straight: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < flush.size(); i++) {
			chance += flush.get(i);
		}
		name = name.concat("Flush: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < fullHouse.size(); i++) {
			chance += fullHouse.get(i);
		}
		name = name.concat("Full House: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < quads.size(); i++) {
			chance += quads.get(i);
		}
		name = name.concat("Quads: " + percent(chance)).concat("\n");
		// ---------------------------------------------------------
		chance = 0;
		for (int i = 0; i < straightFlush.size(); i++) {
			chance += straightFlush.get(i);
		}
		name = name.concat("Straight Flush: " + percent(chance)).concat("\n");

		return name;
	}
}
