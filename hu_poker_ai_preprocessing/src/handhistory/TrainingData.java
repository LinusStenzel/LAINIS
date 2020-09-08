package handhistory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import handhistory.Action.ActionType;
import handhistory.Action.Who;
import handhistory.BettingRound.Turn;

/**
 * Speicherung und Verarbeteitung der Trainingsdaten
 * 
 * @author stenz
 *
 */
public class TrainingData {
	/*
	 * output of main and raise nn
	 */
	private Action actionToTake;
	private int actionToTakeAmount;

	/*
	 * single value data
	 */
	private int handNumber;
	private Who button;
	private int bbAmount;
	private int allMoney;
	private int myMoney;

	private Turn turn;
	private Card[] holeCards;
	private Card[] board;

	private Card[] opHoleCards;

	private Action latestAction;
	private Who preflopAggressor;

	/*
	 * pot(odds)
	 */
	private int pot;
	private int toCallAmount;
	private int whenCallPot;

	/*
	 * playing style each player
	 */
	private int[] meAggrPass = new int[4];
	private int[] meLooseTight = new int[4];
	private int[] opAggrPass = new int[4];
	private int[] opLooseTight = new int[4];

	/*
	 * extra data each player
	 */
	private int meWinningShowdown;
	private int meWinningFolded;
	private int opWinningShowdown;
	private int opWinningFolded;
	private int meSeenFlopAsSB;
	private int opSeenFlopAsSB;

	private Hand hand;
	private int[] potAtStarts;

	private HandOutcome handOutcome;
	private Outs outs;

	private static float[] amountOutputHelp = new float[32];

	private static BufferedWriter raPreIn;
	private static BufferedWriter raPostIn;
	private static BufferedWriter acPreIn;
	private static BufferedWriter acPostIn;
	private static BufferedWriter amPreIn;
	private static BufferedWriter amPostIn;

	private static BufferedWriter raPreOut;
	private static BufferedWriter raPostOut;
	private static BufferedWriter acPreOut;
	private static BufferedWriter acPostOut;
	private static BufferedWriter amPreOut;
	private static BufferedWriter amPostOut;

	static {
		float value = 0;
		for (int i = 0; i < 32; i++) {
			value = (float) (i + 1) / 8;
			amountOutputHelp[i] = value;
		}

		try {
			raPreIn = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "input/rangePre.csv")));
			raPostIn = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "input/rangePost.csv")));
			acPreIn = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "input/actionPre.csv")));
			acPostIn = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "input/actionPost.csv")));
			amPreIn = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "input/amountPre.csv")));
			amPostIn = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "input/amountPost.csv")));

			raPreOut = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "output/rangePre.csv")));
			raPostOut = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "output/rangePost.csv")));
			acPreOut = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "output/actionPre.csv")));
			acPostOut = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "output/actionPost.csv")));
			amPreOut = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "output/amountPre.csv")));
			amPostOut = new BufferedWriter(
					new FileWriter(new File(util.TRAINING_DATA_PATH + "output/amountPost.csv")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeWriters() {
		try {
			raPreIn.close();
			raPostIn.close();
			acPreIn.close();
			acPostIn.close();
			amPreIn.close();
			amPostIn.close();

			raPreOut.close();
			raPostOut.close();
			acPreOut.close();
			acPostOut.close();
			amPreOut.close();
			amPostOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * setter
	 */
	public void setActionToTake(Action actionToTake) {
		this.actionToTake = actionToTake;
	}

	public void setActionToTakeAmount(int actionToTakeAmount) {
		this.actionToTakeAmount = actionToTakeAmount;
	}

	public void setHandNumber(int handNumber) {
		this.handNumber = handNumber;
	}

	public void setButton(Who button) {
		this.button = button;
	}

	public void setBbAmount(int bbAmount) {
		this.bbAmount = bbAmount;
	}

	public void setAllMoney(int allMoney) {
		this.allMoney = allMoney;
	}

	public void setMyMoney(int myMoney) {
		this.myMoney = myMoney;
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
	}

	public void setHoleCards(Card[] holeCards) {
		this.holeCards = holeCards;
	}

	public void setBoard(Card[] board) {
		this.board = board;
	}

	public void setOpHoleCards(Card[] opHoleCards) {
		this.opHoleCards = opHoleCards;
	}

	public void setLatestAction(Action latestAction) {
		this.latestAction = latestAction;
	}

	public void setPreflopAggressor(Who preflopAggressor) {
		this.preflopAggressor = preflopAggressor;
	}

	public void setPot(int pot) {
		this.pot = pot;
	}

	public void setToCallAmount(int toCallAmount) {
		this.toCallAmount = toCallAmount;
	}

	public void setWhenCallPot(int whenCallPot) {
		this.whenCallPot = whenCallPot;
	}

	public void setMeAggrPass(int[] meAggrPass) {
		this.meAggrPass = meAggrPass;
	}

	public void setMeLooseTight(int[] meLooseTight) {
		this.meLooseTight = meLooseTight;
	}

	public void setOpAggrPass(int[] opAggrPass) {
		this.opAggrPass = opAggrPass;
	}

	public void setOpLooseTight(int[] opLooseTight) {
		this.opLooseTight = opLooseTight;
	}

	public void setMeWinningShowdown(int meWinningShowdown) {
		this.meWinningShowdown = meWinningShowdown;
	}

	public void setMeWinningFolded(int meWinningFolded) {
		this.meWinningFolded = meWinningFolded;
	}

	public void setOpWinningShowdown(int opWinningShowdown) {
		this.opWinningShowdown = opWinningShowdown;
	}

	public void setOpWinningFolded(int opWinningFolded) {
		this.opWinningFolded = opWinningFolded;
	}

	public void setMeSeenFlopAsSB(int meSeenFlopAsSB) {
		this.meSeenFlopAsSB = meSeenFlopAsSB;
	}

	public void setOpSeenFlopAsSB(int opSeenFlopAsSB) {
		this.opSeenFlopAsSB = opSeenFlopAsSB;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public void setPotAtStarts(int[] potAtStarts) {
		this.potAtStarts = potAtStarts;
	}

	public void setHandOutcome(HandOutcome handOutcome) {
		this.handOutcome = handOutcome;
	}

	public void setOuts(Outs outs) {
		this.outs = outs;
	}

	/*
	 * getter
	 */
	public Turn getTurn() {
		return turn;
	}

	public HandOutcome getHandOutcome() {
		return handOutcome;
	}

	public Outs getOuts() {
		return outs;
	}

	private float[] looseTightMe() {
		float[] res = new float[5];
		res[0] = handNumber;
		res[1] = meLooseTight[0];
		res[2] = meLooseTight[1];
		res[3] = meLooseTight[2];
		res[4] = meLooseTight[3];
		return res;
	}

	private float[] looseTightOp() {
		float[] res = new float[5];
		res[0] = handNumber;
		res[1] = opLooseTight[0];
		res[2] = opLooseTight[1];
		res[3] = opLooseTight[2];
		res[4] = opLooseTight[3];
		return res;
	}

	private float[] aggrPassMe() {
		float[] res = new float[5];
		res[0] = handNumber;
		res[1] = meAggrPass[0];
		res[2] = meAggrPass[1];
		res[3] = meAggrPass[2];
		res[4] = meAggrPass[3];
		return res;
	}

	private float[] aggrPassOp() {
		float[] res = new float[5];
		res[0] = handNumber;
		res[1] = opAggrPass[0];
		res[2] = opAggrPass[1];
		res[3] = opAggrPass[2];
		res[4] = opAggrPass[3];
		return res;
	}

	private float[] holeCards() {
		float[] res = new float[4];
		int[] cardVec = holeCards[0].toVector();
		res[0] = cardVec[0];
		res[1] = cardVec[1];

		cardVec = holeCards[1].toVector();
		res[2] = cardVec[0];
		res[3] = cardVec[1];
		return res;
	}

	private float[] preflop() {
		return hand.getBettingRounds().get(0).toVector(bbAmount, potAtStarts[0], button);
	}

	private float[] flop() {
		if (hand.getBettingRounds().size() <= 1) {
			return new float[7];
		}
		return hand.getBettingRounds().get(1).toVector(bbAmount, potAtStarts[1], button);
	}

	private float[] turn() {
		if (hand.getBettingRounds().size() <= 2) {
			return new float[7];
		}
		return hand.getBettingRounds().get(2).toVector(bbAmount, potAtStarts[2], button);
	}

	private float[] river() {
		if (hand.getBettingRounds().size() <= 3) {
			return new float[7];
		}
		return hand.getBettingRounds().get(3).toVector(bbAmount, potAtStarts[3], button);
	}

	private float[] board() {
		float[] res = new float[10];
		int i = 0;
		for (Card c : board) {
			int[] cardVec = c.toVector();
			res[i * 2] = cardVec[0];
			res[i * 2 + 1] = cardVec[1];
			i++;
		}
		return res;
	}

	private float[] handOutcome() {
		int[] vec = handOutcome.toVector();
		float[] res = new float[16];

		int i = 0;
		for (int f : vec) {
			res[i++] = f;
		}
		return res;
	}

	private float[] outs() {
		float[] res = new float[27];
		System.arraycopy(outs.toVector(), 0, res, 0, 26);
		res[26] = turn.getValue();
		return res;
	}

	public void generateTrainingVectors() {
		float[][] action = extractActionData();

		float[][] amount = null;
		if (actionToTakeAmount > 0) {
			amount = extractAmountData();
		}

		float[][] rangeMe = extractRangeData(Who.ME, true);

		float[][] rangeOp = null;
		if (opHoleCards != null) {
			rangeOp = extractRangeData(Who.OP, true);
		}

		if (turn == Turn.PREFLOP) {
			writeToCSV("actionPre", action);
			if (amount != null) {
				writeToCSV("amountPre", amount);
			}
			writeToCSV("rangePre", rangeMe);
			if (rangeOp != null) {
				writeToCSV("rangePre", rangeOp);
			}
		} else {
			writeToCSV("actionPost", action);
			if (amount != null) {
				writeToCSV("amountPost", amount);
			}
			writeToCSV("rangePost", rangeMe);
			if (rangeOp != null) {
				writeToCSV("rangePost", rangeOp);
			}
		}
	}

	private void writeToCSV(String fileName, float[][] data) {
		BufferedWriter in;
		BufferedWriter out;

		if (fileName.contains("Pre")) {
			if (fileName.startsWith("ra")) {
				in = raPreIn;
				out = raPreOut;
			} else if (fileName.startsWith("ac")) {
				in = acPreIn;
				out = acPreOut;
			} else {
				in = amPreIn;
				out = amPreOut;
			}
		} else {
			if (fileName.startsWith("ra")) {
				in = raPostIn;
				out = raPostOut;
			} else if (fileName.startsWith("ac")) {
				in = acPostIn;
				out = acPostOut;
			} else {
				in = amPostIn;
				out = amPostOut;
			}
		}

		try {
			String input = Arrays.toString(data[0]);
			input = input.substring(1);
			input = input.substring(0, input.length() - 1);
			in.write(input + "\n");

			String output = Arrays.toString(data[1]);
			output = output.substring(1);
			output = output.substring(0, output.length() - 1);
			out.write(output + "\n");

			in.flush();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private float[][] extractRangeData(Who who, boolean inAndOut) {
		float[] output = new float[37];
		float[] input;

		if (inAndOut) {
			if (who == Who.ME) {
				output[0] = holeCards[0].isPaired(holeCards[1]) ? 1 : 0;
				output[1] = holeCards[0].isSuited(holeCards[1]) ? 1 : 0;
				output[2] = holeCards[0].isConnected(holeCards[1]) ? 1 : 0;

				output[3 + holeCards[0].getValue().getValue()] = 1;
				output[3 + 13 + holeCards[0].getSuit().getValue()] = 1;
				output[3 + 13 + 4 + holeCards[1].getValue().getValue()] = 1;
				output[3 + 13 + 4 + 13 + holeCards[1].getSuit().getValue()] = 1;
			} else {
				output[0] = opHoleCards[0].isPaired(opHoleCards[1]) ? 1 : 0;
				output[1] = opHoleCards[0].isSuited(opHoleCards[1]) ? 1 : 0;
				output[2] = opHoleCards[0].isConnected(opHoleCards[1]) ? 1 : 0;

				output[3 + opHoleCards[0].getValue().getValue()] = 1;
				output[3 + 13 + opHoleCards[0].getSuit().getValue()] = 1;
				output[3 + 13 + 4 + opHoleCards[1].getValue().getValue()] = 1;
				output[3 + 13 + 4 + 13 + opHoleCards[1].getSuit().getValue()] = 1;
			}
		}

		if (turn.getValue() == 0) {
			float[] singleValues = new float[8];
			singleValues[0] = handNumber;
			singleValues[1] = button.getValue() == who.getValue() ? 1 : 0;
			singleValues[2] = bbAmount;
			singleValues[3] = (float) myMoney / allMoney;
			singleValues[4] = pot;

			float[] preflop = preflop();
			preflop[0] = button.getValue() == who.getValue() ? 1 : 0;

			float[] aggrPass;
			float[] looseTight;

			if (who == Who.ME) {
				singleValues[5] = meWinningShowdown;
				singleValues[6] = meWinningFolded;
				singleValues[7] = meSeenFlopAsSB;
				aggrPass = aggrPassMe();
				looseTight = looseTightMe();

			} else {
				singleValues[5] = opWinningShowdown;
				singleValues[6] = opWinningFolded;
				singleValues[7] = opSeenFlopAsSB;
				aggrPass = aggrPassOp();
				looseTight = looseTightOp();
			}

			input = new float[24];
			System.arraycopy(singleValues, 0, input, 0, singleValues.length);
			System.arraycopy(aggrPass, 0, input, 8, aggrPass.length);
			System.arraycopy(looseTight, 0, input, 13, looseTight.length);
			System.arraycopy(preflop, 0, input, 18, preflop.length);
		} else {
			float[] singleValues = new float[10];
			singleValues[0] = handNumber;
			singleValues[1] = button.getValue() == who.getValue() ? 1 : 0;
			singleValues[2] = bbAmount;
			singleValues[3] = (float) myMoney / allMoney;
			singleValues[4] = pot;
			singleValues[5] = turn.getValue();
			singleValues[6] = preflopAggressor != null ? preflopAggressor.getValue() : -1;

			float[] board = board();

			float[] preflop = preflop();
			preflop[0] = button.getValue() == who.getValue() ? 1 : 0;

			float[] flop = flop();
			flop[0] = button.getValue() == who.getValue() ? 1 : 0;

			float[] turn = turn();
			turn[0] = button.getValue() == who.getValue() ? 1 : 0;

			float[] river = river();
			river[0] = button.getValue() == who.getValue() ? 1 : 0;

			float[] aggrPass;
			float[] looseTight;

			if (who == Who.ME) {
				singleValues[7] = meWinningShowdown;
				singleValues[8] = meWinningFolded;
				singleValues[9] = meSeenFlopAsSB;
				aggrPass = aggrPassMe();
				looseTight = looseTightMe();
			} else {
				singleValues[7] = opWinningShowdown;
				singleValues[8] = opWinningFolded;
				singleValues[9] = opSeenFlopAsSB;
				aggrPass = aggrPassOp();
				looseTight = looseTightOp();
			}

			input = new float[57];
			System.arraycopy(singleValues, 0, input, 0, singleValues.length);
			System.arraycopy(aggrPass, 0, input, 10, aggrPass.length);
			System.arraycopy(looseTight, 0, input, 15, looseTight.length);
			System.arraycopy(preflop, 0, input, 20, preflop.length);
			System.arraycopy(flop, 0, input, 26, flop.length);
			System.arraycopy(turn, 0, input, 33, turn.length);
			System.arraycopy(river, 0, input, 40, river.length);
			System.arraycopy(board, 0, input, 47, board.length);
		}
		return new float[][] { input, output };
	}
	
	private float[][] extractActionData() {
		float[] output = new float[3];
		float[] input;

		if (actionToTake.getActionType() == ActionType.FOLD) {
			output[0] = 1;
		} else if (actionToTake.getActionType() == ActionType.CHECK
				|| actionToTake.getActionType() == ActionType.CALL) {
			output[1] = 1;
		} else if (actionToTake.getActionType() == ActionType.BET
				|| actionToTake.getActionType() == ActionType.RAISE) {
			output[2] = 1;
		}

		if (turn.getValue() == 0) {
			input = preflopInput();
		} else {
			input = postflopInput();
		}
		return new float[][] { input, output };
	}

	private float[][] extractAmountData() {
		float[] output = new float[32];
		float[] input;

		float amountToPot = (float) actionToTakeAmount / pot;
		float distance = Math.abs(amountOutputHelp[0] - amountToPot);
		int idx = 0;
		for (int i = 1; i < amountOutputHelp.length; i++) {
			float tempDistance = Math.abs(amountOutputHelp[i] - amountToPot);
			if (tempDistance < distance) {
				idx = i;
				distance = tempDistance;
			}
		}

		output[idx] = 1;

		if (turn.getValue() == 0) {
			input = preflopInput();
		} else {
			input = postflopInput();
		}

		return new float[][] { input, output };
	}
	
	private float[] preflopInput() {
		float[] input;
		float[] singleValues = new float[13];
		singleValues[0] = handNumber;
		singleValues[1] = button.getValue();
		singleValues[2] = bbAmount;
		singleValues[3] = (float) myMoney / allMoney;
		singleValues[4] = pot;
		singleValues[5] = toCallAmount;
		singleValues[6] = whenCallPot;
		singleValues[7] = meWinningShowdown;
		singleValues[8] = meWinningFolded;
		singleValues[9] = meSeenFlopAsSB;
		singleValues[10] = opWinningShowdown;
		singleValues[11] = opWinningFolded;
		singleValues[12] = opSeenFlopAsSB;

		float[] aggrPassMe = aggrPassMe();
		float[] looseTightMe = looseTightMe();
		float[] aggrPassOp = aggrPassOp();
		float[] looseTightOp = looseTightOp();
		float[] holeCards = holeCards();
		float[] latestAction = this.latestAction != null ? this.latestAction.toVector(bbAmount)
				: new float[3];
		float[] preflop = preflop();
		float[] rangeMe = extractRangeData(Who.ME, false)[0];
		float[] rangeOp = extractRangeData(Who.OP, false)[0];

		input = new float[94];
		System.arraycopy(singleValues, 0, input, 0, singleValues.length);
		System.arraycopy(aggrPassMe, 0, input, 13, aggrPassMe.length);
		System.arraycopy(looseTightMe, 0, input, 18, looseTightMe.length);
		System.arraycopy(aggrPassOp, 0, input, 23, aggrPassOp.length);
		System.arraycopy(looseTightOp, 0, input, 28, looseTightOp.length);
		System.arraycopy(holeCards, 0, input, 33, holeCards.length);
		System.arraycopy(latestAction, 0, input, 37, latestAction.length);
		System.arraycopy(preflop, 0, input, 40, preflop.length);
		System.arraycopy(rangeMe, 0, input, 46, rangeMe.length);
		System.arraycopy(rangeOp, 0, input, 70, rangeOp.length);
		return input;
	}

	private float[] postflopInput() {
		float[] input;
		float[] singleValues = new float[15];
		singleValues[0] = handNumber;
		singleValues[1] = button.getValue();
		singleValues[2] = bbAmount;
		singleValues[3] = (float) myMoney / allMoney;
		singleValues[4] = pot;
		singleValues[5] = toCallAmount;
		singleValues[6] = whenCallPot;
		singleValues[7] = turn.getValue();
		singleValues[8] = preflopAggressor != null ? preflopAggressor.getValue() : -1;
		singleValues[9] = meWinningShowdown;
		singleValues[10] = meWinningFolded;
		singleValues[11] = meSeenFlopAsSB;
		singleValues[12] = opWinningShowdown;
		singleValues[13] = opWinningFolded;
		singleValues[14] = opSeenFlopAsSB;

		float[] aggrPassMe = aggrPassMe();
		float[] looseTightMe = looseTightMe();
		float[] aggrPassOp = aggrPassOp();
		float[] looseTightOp = looseTightOp();
		float[] holeCards = holeCards();
		float[] latestAction = this.latestAction != null ? this.latestAction.toVector(bbAmount)
				: new float[3];
		float[] preflop = preflop();
		float[] flop = flop();
		float[] turn = turn();
		float[] river = river();
		float[] board = board();
		float[] handOutcome = handOutcome();
		float[] outs = outs();
		float[] rangeMe = extractRangeData(Who.ME, false)[0];
		float[] rangeOp = extractRangeData(Who.OP, false)[0];

		input = new float[236];

		System.arraycopy(singleValues, 0, input, 0, singleValues.length);
		System.arraycopy(aggrPassMe, 0, input, 15, aggrPassMe.length);
		System.arraycopy(looseTightMe, 0, input, 20, looseTightMe.length);
		System.arraycopy(aggrPassOp, 0, input, 25, aggrPassOp.length);
		System.arraycopy(looseTightOp, 0, input, 30, looseTightOp.length);
		System.arraycopy(holeCards, 0, input, 35, holeCards.length);
		System.arraycopy(latestAction, 0, input, 39, latestAction.length);
		System.arraycopy(preflop, 0, input, 42, preflop.length);
		System.arraycopy(flop, 0, input, 48, flop.length);
		System.arraycopy(turn, 0, input, 55, turn.length);
		System.arraycopy(river, 0, input, 62, river.length);
		System.arraycopy(board, 0, input, 69, board.length);
		System.arraycopy(handOutcome, 0, input, 79, handOutcome.length);
		System.arraycopy(outs, 0, input, 95, outs.length);
		System.arraycopy(rangeMe, 0, input, 122, rangeMe.length);
		System.arraycopy(rangeOp, 0, input, 179, rangeOp.length);
		return input;
	}

	

	public String toString() {
		String res = "";

		res = res.concat("Action: " + actionToTake) + "\n";
		res = res.concat("Action Amount: " + actionToTakeAmount) + "\n";

		res = res.concat("Hand Number: " + handNumber) + "\n";
		res = res.concat("Button: " + button) + "\n";
		res = res.concat("BB Amount: " + bbAmount) + "\n";
		res = res.concat("All Money: " + allMoney) + "\n";
		res = res.concat("My Money: " + myMoney) + "\n";

		res = res.concat("Turn: " + turn) + "\n";
		res = res.concat("Hole Cards: " + Arrays.toString(holeCards)) + "\n";
		res = res.concat("Board: " + Arrays.toString(board)) + "\n";
		res = res.concat("Op Hole Cards: " + Arrays.toString(opHoleCards)) + "\n";

		res = res.concat("Latest Action: " + latestAction) + "\n";
		res = res.concat("Preflop Agressor: " + preflopAggressor) + "\n";

		res = res.concat("Pot: " + pot) + "\n";
		res = res.concat("To Call Amount: " + toCallAmount) + "\n";
		res = res.concat("Pot When Called: " + whenCallPot) + "\n";

		res = res.concat("Aggr/Pass Me: " + Arrays.toString(meAggrPass)) + "\n";
		res = res.concat("Loose/Tight Me: " + Arrays.toString(meLooseTight)) + "\n";
		res = res.concat("Aggr/Pass Op: " + Arrays.toString(opAggrPass)) + "\n";
		res = res.concat("Loose/Tight Op: " + Arrays.toString(opLooseTight)) + "\n";

		res = res.concat("Me Winning Showdown: " + meWinningShowdown) + "\n";
		res = res.concat("Me Winning Folded: " + meWinningFolded) + "\n";
		res = res.concat("Op Winning Showdown: " + opWinningShowdown) + "\n";
		res = res.concat("Op Winning Folded: " + opWinningFolded) + "\n";
		res = res.concat("Me Seen Flop As SB: " + meSeenFlopAsSB) + "\n";
		res = res.concat("Op Seen Flop As SB: " + opSeenFlopAsSB) + "\n";
		res = res.concat("Hand: " + "\n" + hand.toString()) + "\n";

		if (turn != Turn.PREFLOP) {
			res = res.concat("Outs: " + outs.toString()) + "\n";
			res = res.concat("Hand Outcome: " + handOutcome.toString()) + "\n";
		}
		return res;
	}

}
