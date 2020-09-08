package handhistory;

import java.util.ArrayList;

import handhistory.Action.ActionType;
import handhistory.Action.Who;

/**
 * represents a bettinground - either Pre-Flop, Flop, Turn or River - contains a
 * list of actions with no size limit
 * 
 * @author stenz
 *
 */
public class BettingRound {

	/**
	 * all possible turns
	 */
	public enum Turn {
		PREFLOP(0), FLOP(1), TURN(2), RIVER(3);

		/**
		 * for vector notation
		 */
		private int value;

		private Turn(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

	}

	/**
	 * list of actions with
	 */
	private ArrayList<Action> bettingActions;
	private Turn turn;

	public BettingRound(Turn turn) {
		bettingActions = new ArrayList<Action>();
		this.turn = turn;
	}

	/*
	 * getters
	 */

	public ArrayList<Action> getBettingActions() {
		return bettingActions;
	}

	public Turn getTurn() {
		return turn;
	}

	public void setBettingActions(ArrayList<Action> bettingActions) {
		this.bettingActions = bettingActions;
	}

	/**
	 * 
	 * @return bettingActions is empty => true
	 */
	public boolean isEmpty() {
		return bettingActions.isEmpty();
	}

	/**
	 * adds an action to internal list
	 * 
	 * @param who    is taking the action
	 * @param action as string - with type and amount
	 */
	public void addAction(Who who, String action) {
		// actiontype name capsuled
		String capsuledAction = action.substring(0, action.indexOf(' '));

		Action a;
		switch (capsuledAction) {
		case util.CHECK:
			a = new Action(ActionType.CHECK, who, 0);
			break;
		case util.CALL:
			a = extractAmount(who, action, ActionType.CALL);
			break;
		case util.BET:
			a = extractAmount(who, action, ActionType.BET);
			break;
		case util.RAISE:
			a = extractAmount(who, action, ActionType.RAISE);
			break;
		case util.FOLD:
			a = new Action(ActionType.FOLD, who, 0);
			break;
		default: // = SB or BB
			int blindAmount;
			String amountStr;
			if (action.startsWith(util.SMALL_BLIND)) {
				// get blind amount from string
				amountStr = action.substring(util.SMALL_BLIND.length() + 1, action.length());

				// maybe all in
				if (amountStr.endsWith(util.ALL_IN)) {
					amountStr = amountStr.substring(0, amountStr.indexOf(" "));
				}
				blindAmount = Integer.valueOf(amountStr);

				a = new Action(ActionType.SB, who, blindAmount);
			} else if (action.startsWith(util.BIG_BLIND)) {
				// get blind amount from string
				amountStr = action.substring(util.BIG_BLIND.length() + 1, action.length());

				// maybe all in
				if (amountStr.endsWith(util.ALL_IN)) {
					amountStr = amountStr.substring(0, amountStr.indexOf(" "));
				}
				blindAmount = Integer.valueOf(amountStr);

				a = new Action(ActionType.BB, who, blindAmount);
				// bigBlind = blindAmount;
			} else {
				return;
			}
			break;
		}
		bettingActions.add(a);
	}

	/**
	 * creates an action by analyzing a string and extracting the amount
	 * 
	 * @param who    is doing the action
	 * @param action as string - with type and amount
	 * @param aType  type of the action
	 * @return the created action
	 */
	private Action extractAmount(Who who, String action, ActionType aType) {
		String aName = "";

		switch (aType) {
		case CHECK:
			aName = util.CHECK;
			break;
		case CALL:
			aName = util.CALL;
			break;
		case BET:
			aName = util.BET;
			break;
		case RAISE:
			aName = util.RAISE;
			break;
		default:
			break;
		}

		Action a;
		int callBetAmount;
		int toAmount;
		String tempAction;

		// removes the name of the action from the string
		tempAction = action.substring(aName.length() + 1, action.length());

		// move is not all-in
		if (!tempAction.endsWith(util.ALL_IN)) {
			// because a raise got the value "to" other processing is needed
			if (aType == ActionType.RAISE) {

				callBetAmount = Integer.valueOf(tempAction.substring(0, tempAction.indexOf(" ")));
				// searching for "to" amount by cutting string
				toAmount = Integer.valueOf(action.substring(
						aName.length() + 1 + String.valueOf(callBetAmount).length() + 4,
						action.length()));
				a = new Action(aType, who, callBetAmount, toAmount);
			} else {
				callBetAmount = Integer.valueOf(tempAction);
				a = new Action(aType, who, callBetAmount);
			}
		}
		// move is all-in
		else {
			tempAction = tempAction.substring(0, tempAction.indexOf(" "));
			callBetAmount = Integer.valueOf(tempAction);

			// because a raise got the value "to" other handling is needed
			if (aType == ActionType.RAISE) {

				// searching for "to" amount by cutting string
				toAmount = Integer.valueOf(action.substring(
						aName.length() + 1 + String.valueOf(callBetAmount).length() + 4,
						// all in -> searching for " " to extract "to"
						action.indexOf(" ", aName.length() + 1
								+ String.valueOf(callBetAmount).length() + util.TO.length() + 1)));
				a = new Action(aType, who, callBetAmount, toAmount, true);
			} else {
				a = new Action(aType, who, callBetAmount, true);
			}
		}
		return a;
	}

	/**
	 * converts this betting round into a 6 or 7 dimensional vector by looping over the
	 * actions and converting them into a six dimensional vector space for four
	 * actions - the following get stacked on the corresponding elements - and
	 * adding three more single values
	 * 
	 * [0] buttonWho;[1] how many action were took; [2] maybe pot to big blind;
	 * [3-4] my Action as vector, maybe stacked ...; [5 - 6] op action vector, maybe
	 * stacked
	 * 
	 * @param bigBlind
	 * @param pot
	 * @return the created and filled vector
	 */
	public float[] toVector(int bigBlind, int pot, Who buttonWho) {
		float[] resVec;
		resVec = turn == Turn.PREFLOP ? new float[6] : new float[7];

		Action[] blinds = new Action[2];
		// because blinds should not be considered in the vector representation
		if (turn == Turn.PREFLOP) {
			// saving for adding later again
			blinds = removeBlinds();
		} else {
			resVec[2] = (float) pot / bigBlind;
		}

		// single data
		resVec[0] = buttonWho.getValue();
		resVec[1] = bettingActions.size();

		int staticAmount = turn == Turn.PREFLOP ? 2 : 3;

		// arraylist for simpler handling
		ArrayList<float[]> vecAsArrList = new ArrayList<float[]>();

		float[] vecElement = new float[2];

		for (int j = 0; j < bettingActions.size(); j++) {
			// fill element with actionvector
			vecElement = bettingActions.get(j).toVector(bigBlind);
			if (j < 2) {
				// adding up to two elements in arraylist
				vecAsArrList.add(vecElement);
			} else {
				// stacking element on corresponding
				if ((j % 2) == 0) {
					addCorr(vecAsArrList, vecElement, 0);
				} else {
					addCorr(vecAsArrList, vecElement, 1);
				}
			}
		}

		// writing arraylist content in result vector
		for (int i = 0; i < vecAsArrList.size(); i++) {
			float[] currAction = vecAsArrList.get(i);
			// writing all action vec values in betting round sub vec
			for (int j = 0; j < currAction.length; j++) {
				resVec[i * 2 + staticAmount + j] = currAction[j];
			}
		}
		// adding blinds again for later processing
		if (turn == Turn.PREFLOP) {
			addBlinds(blinds);
		}
		return resVec;
	}

	/**
	 * adds two vectors element wise together from a given array
	 * 
	 * @param vec  to add on
	 * @param eVec element
	 * @param pos  position
	 */
	private void addCorr(ArrayList<float[]> vec, float[] eVec, int pos) {
		float[] temp;
		temp = vec.get(pos);
		vec.remove(pos);
		for (int i = 0; i < temp.length; i++) {
			temp[i] += eVec[i];
		}
		vec.add(pos, temp);
	}

	/**
	 * for better reading
	 */
	private Action[] removeBlinds() {
		Action[] blinds = new Action[2];
		for (int i = 0; i < 2; i++) {
			blinds[i] = bettingActions.remove(0);
		}
		return blinds;
	}

	/**
	 * 
	 * @param blinds that were removed before
	 */
	private void addBlinds(Action[] blinds) {
		for (int i = 1; i >= 0; i--) {
			bettingActions.add(0, blinds[i]);
		}
	}

	@Override
	public String toString() {
		return turn.toString().concat(" " + bettingActions.toString());
	}

}
