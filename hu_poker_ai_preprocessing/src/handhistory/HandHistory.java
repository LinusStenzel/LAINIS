package handhistory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import handhistory.Action.ActionType;
import handhistory.Action.Who;
import handhistory.BettingRound.Turn;

/**
 * represents a complete hand history by analyzing a textfile and extracting
 * data from it - could be called "main class" of this package
 * 
 * @author stenz
 *
 */
public class HandHistory {

	private String fileLocation;

	/**
	 * list of hands in history
	 */
	private ArrayList<Hand> hands = new ArrayList<Hand>();

	/**
	 * each textrow of current hand
	 */
	private ArrayList<String> currHand = new ArrayList<String>();

	/*
	 * data for analyzing text
	 */
	private String myName = "officialStnz";
	private String opName;

	/**
	 * needed for correct chances of hitting outcomes
	 */
	Deck deck;

	/**
	 * needed from flop on
	 */
	private ArrayList<Card> board = new ArrayList<Card>();

	/**
	 * instance to calc outs
	 */
	Draw draw;

	/**
	 * just for storing data for nn(seen from every of my decisions)
	 */
	private ArrayList<TrainingData> trainingDatas = new ArrayList<TrainingData>();

	private int decisionsTook;


//	----------------------------------------------------
//	******************TRAINING DATA*********************
//	----------------------------------------------------

	/*
	 * output of main and raise nn
	 */
	private Action actionToTake;

	/*
	 * ai single value training data; seen from my current decision perspective
	 */
	private int handNumber;
	private Who button;
	private int bbAmount;
	private int allMoney;
	private int myMoney;

	private Turn turn;
	private Card[] holeCards = new Card[2];

	private Card[] opHoleCards;

	private Action latestAction;
	private Who preflopAggressor;

	/*
	 * for pot odds
	 */
	private int pot;
	private int toCallAmount;
	private int whenCallPot;

	/*
	 * tracking aggresive/passive playing style
	 */
	private int[] meAggrPass = new int[4];
	private int[] opAggrPass = new int[4];

	/*
	 * tracking loose/tight playing style
	 */
	private int[] meLooseTight = new int[4];
	private int[] opLooseTight = new int[4];

	/*
	 * tracking some extra data for each player
	 */
	private int meWinningShowdown;
	private int meWinningFolded;

	private int opWinningShowdown;
	private int opWinningFolded;

	private int meSeenFlopAsSB;
	private int opSeenFlopAsSB;

	/**
	 * for vectorizing
	 */
	private int[] potAtStarts = new int[4];

	/**
	 * handoutcome and outs
	 */
	private HandOutcome handOutcome;
	private Outs outs;

//	----------------------------------------------------
//	****************************************************
//	----------------------------------------------------

	public HandHistory(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	/**
	 * reads through the whole textfile and splits it into its hands + gets some
	 * inforamtion for training data
	 * 
	 * @param withPrinting should the content of each hand get printed
	 * @throws IOException
	 */
	public void readAllHands(boolean withPrinting) throws IOException {
		// buffered reader for simpler handling
		BufferedReader br = new BufferedReader(new FileReader(fileLocation));

		while (extractNextHand(br)) {
			handNumber++;
			if (hands.isEmpty()) {
				// filling vars pre game because needed during game
				extractOpName();
				extractOverAllMoney();

			} else {
				// clearing board, not needed on first hand
				board = new ArrayList<Card>();
			}

			extractMyMoney();
			extractOpHoleCards();

			// creating full deck
			deck = new Deck();
			deck.fill();
			// order of next three lookFor is important
			// because they build on each other
			Hand hand = lookForBettingRounds();
			hands.add(hand);

			lookForBigBlind(hand);
			lookForDecisions(hand);

			if (withPrinting) {
				logHand(hand);
			}
			extractWhoWon();
		}

		for (TrainingData td : trainingDatas) {
			td.generateTrainingVectors();
		}
	}

	private void logHand(Hand hand) {
		// TODO work with log(List<String>) for string debugging

		System.out.println(opName);
		System.out.println(hand.toString());
		for (int i = 0; i < board.size(); i++) {
			System.out.println(board.get(i).toString());
		}
		System.out.print('\n');
		for (int i = 0; i < holeCards.length; i++) {
			System.out.println(holeCards[i].toString());
		}
		System.out.println(util.SEPERATOR);

		int tempDecisionsTook = 0;
		for (int i = decisionsTook; i < trainingDatas.size(); i++) {
			System.out.println(trainingDatas.get(i).toString());
			tempDecisionsTook++;
		}

		decisionsTook += tempDecisionsTook;

	}

	// -------------------------------------------------------------
	// extracting in this class means reading from string of hh file
	// -------------------------------------------------------------

	/**
	 * reads exactly and only the next hand of the text file and filling each text
	 * row into internal list
	 * 
	 * @param br
	 * @return true -> game goes on
	 * @throws IOException
	 */
	private boolean extractNextHand(BufferedReader br) throws IOException {
		String line = br.readLine();

		// between hands in the textfile are two unread empty rows
		if (!hands.isEmpty()) {
			br.skip((util.EMPTY_ROWS_AFTER_HAND - 1) * 2);
			line = br.readLine();
		}

		boolean isOver = false;
		// tournament is over when readLine() returned null -> nothing to read
		if (line != null) {
			currHand.clear();
			do {
				currHand.add(line);
			} while ((line = br.readLine()) != null && !line.matches(""));
		} else {
			isOver = true;
		}
		return !isOver;
	}

	/**
	 * processes every line of current hand by looking for buzzwords(from util) not
	 * just looking for betting round, also filling resp. empty key vars like board,
	 * hole cards and deck
	 * 
	 * @return a new filled hand instance
	 */
	private Hand lookForBettingRounds() {
		Hand hand = new Hand();
		// first turn is always flop and always needed
		BettingRound bettingRound = new BettingRound(Turn.PREFLOP);
		for (int i = 0; i < currHand.size(); i++) {
			bettingRound = processLine(currHand.get(i), hand, bettingRound);
		}

		// last betting round needs to be added
		// but not empty ones - possible after call all in
		if (bettingRound.getTurn() != Turn.PREFLOP && !bettingRound.isEmpty()) {
			hand.addBettingRound(bettingRound);
		}
		return hand;
	}

	/**
	 * saving big blind amount and who
	 * 
	 * @param hand to look at(get data from)
	 */
	private void lookForBigBlind(Hand hand) {
		String line;
		boolean found = false;
		// loops over extracted currHand lines(string) and looks for buzzwords(util)
		for (int i = 0; i < currHand.size() && !found; i++) {
			line = currHand.get(i);
			if (line.contains(util.BIG_BLIND)) {
				if (line.contains(myName)) {
					button = Who.ME;
					line = line.substring(myName.length() + 2, line.length());
				} else {
					button = Who.OP;
					line = line.substring(opName.length() + 2, line.length());
				}
				// extract bb amount
				String[] lineAsArr = line.split(" ");
				bbAmount = Integer.valueOf(lineAsArr[3]);

				found = true;
			}
		}

	}

	/**
	 * analyzes the given line by searching for buzzwords and saving key data - like
	 * hand instance with betting rounds and board - for later processing; weakness
	 * is after someone called all in, board gets filled with flop but not necessary
	 * too much data that little does not cause any damage
	 * 
	 * @param line         single line from hand history
	 * @param hand         instance to fill
	 * @param bettingRound also to fill
	 * @return maybe changed betting round
	 */
	private BettingRound processLine(String line, Hand hand, BettingRound bettingRound) {
		// betting rounds get filled here
		// searching for names because they indicate an action
		if (line.startsWith(myName)) {
			extractAndAddAction(line, bettingRound, Who.ME);
		} else if (line.startsWith(opName)) {
			extractAndAddAction(line, bettingRound, Who.OP);
		}

		Action action = lookForLatestAction(bettingRound);

		// when one player calls all in, no further decision will be made in this hand
		if (action != null && !(action.isAllIn() && action.getActionType() == ActionType.CALL)) {

			if (line.startsWith(util.HOLE_CARDS + myName)) {
				// till now only blinds were posted
				hand.addBettingRound(bettingRound);

				String both = line.substring((util.HOLE_CARDS + myName).length() + 2,
						line.length() - 1);

				String firstCard = both.substring(0, 2);
				String secondCard = both.substring(3, 5);
				dealToMe(firstCard, secondCard);
			} else
			// pre flop betting round done
			if (line.startsWith(util.FLOP)) {
				String flop = line.substring((util.FLOP).length() + 2, line.length() - 1);

				String firstCard = flop.substring(0, 2);
				String secondCard = flop.substring(3, 5);
				String thirdCard = flop.substring(6, 8);
				dealFlop(firstCard, secondCard, thirdCard);

				bettingRound = new BettingRound(Turn.FLOP);
			} else
			// flop betting round done
			if (line.startsWith(util.TURN)) {
				// adding flop to hand
				hand.addBettingRound(bettingRound);

				String turn = line.substring((util.TURN).length() + 2 + 11, line.length() - 1);
				String firstCard = turn.substring(0, 2);
				// dealing turn
				board.add(Card.getByChars(firstCard.charAt(0), firstCard.charAt(1)));
				deck.removeCard(board.get(3));

				bettingRound = new BettingRound(Turn.TURN);
			} else
			// turn betting round
			if (line.startsWith(util.RIVER)) {
				// adding turn
				hand.addBettingRound(bettingRound);

				String river = line.substring((util.RIVER).length() + 2 + 14, line.length() - 1);
				String firstCard = river.substring(0, 2);
				// dealing river
				board.add(Card.getByChars(firstCard.charAt(0), firstCard.charAt(1)));
				deck.removeCard(board.get(4));

				bettingRound = new BettingRound(Turn.RIVER);
			}
		}

		return bettingRound;
	}

	/**
	 * extracts opponent name by searching for it in current hand
	 */
	private void extractOpName() {
		String line;
		boolean found = false;
		for (int i = 0; i < currHand.size() && !found; i++) {
			line = currHand.get(i);
			// row with opponent name can be found by searching for substring "seat"
			// row must not contain my name
			if (line.startsWith("Seat") && !line.contains(myName)) {
				// remove "Seat X: "
				line = line.substring(8, line.length());
				// only works for names that dont have '(' in it
				opName = line.substring(0, line.indexOf('(') - 1);
				found = true;
			}
		}
	}

	/**
	 * extracts overall money by searching for money of seat 1 in before the first
	 * hand and multiplying it with two
	 */
	private void extractOverAllMoney() {
		String line;
		boolean found = false;
		for (int i = 0; i < currHand.size() && !found; i++) {
			line = currHand.get(i);

			if (line.startsWith("Seat") && line.contains("in chips")) {
				String afterBracket = line.substring(line.indexOf("(") + 1);
				// because only calling on first hand
				// just taking amount of first player *2
				allMoney = Integer.valueOf(afterBracket.substring(0, afterBracket.indexOf(" ")))
						* 2;
				found = true;
			}
		}
	}

	/**
	 * extracts my money by searching for money of the seat where i sit before every
	 * hand
	 */
	private void extractMyMoney() {
		String line;
		boolean found = false;

		for (int i = 0; i < currHand.size() && !found; i++) {
			line = currHand.get(i);
			if (line.startsWith("Seat") && line.contains(myName) && line.contains("in chips")) {
				String afterBracket = line.substring(line.indexOf("(") + 1);
				myMoney = Integer.valueOf(afterBracket.substring(0, afterBracket.indexOf(" ")));
				found = true;
			}
		}
	}

	private void extractWhoWon() {
		String[] lines = new String[] { currHand.get(currHand.size() - 1),
				currHand.get(currHand.size() - 2) };
		for (String line : lines) {
			if (line.contains(util.WON_WITH_SHOWDOWN)) {
				if (line.contains(myName)) {
					meWinningShowdown++;
				} else {
					opWinningShowdown++;
				}
			} else if (line.contains(util.WON_WITHOUT_SHOWDOWN)) {
				if (line.contains(myName)) {
					meWinningFolded++;
				} else {
					opWinningFolded++;
				}
			}
		}
	}

	private void extractOpHoleCards() {
		String[] lines = new String[] { currHand.get(currHand.size() - 1),
				currHand.get(currHand.size() - 2) };

		for (String line : lines) {
			if (line.contains(opName) && line.contains(util.SHOWED)) {

				line = line.substring(line.indexOf(opName) + opName.length());
				line = line.substring(line.indexOf(util.SHOWED) + util.SHOWED.length() + 1);

				String both = line.substring(1, 6);
				String firstCard = both.substring(0, 2);
				String secondCard = both.substring(3, 5);

				opHoleCards = new Card[2];
				opHoleCards[0] = Card.getByChars(firstCard.charAt(0), firstCard.charAt(1));
				opHoleCards[1] = Card.getByChars(secondCard.charAt(0), secondCard.charAt(1));
				
				Arrays.sort(opHoleCards, Card.getCompByValue());
			}
		}
	}

	/**
	 * just adds hole cards to my "hand" and removing them from deck
	 * 
	 * @param firstCard  to add
	 * @param secondCard to add
	 */
	private void dealToMe(String firstCard, String secondCard) {
		holeCards[0] = Card.getByChars(firstCard.charAt(0), firstCard.charAt(1));
		holeCards[1] = Card.getByChars(secondCard.charAt(0), secondCard.charAt(1));
		Arrays.sort(holeCards, Card.getCompByValue());
		deck.removeCard(holeCards[0]);
		deck.removeCard(holeCards[1]);
	}

	/**
	 * just adds cards of flop to board and removing them from deck
	 * 
	 * @param firstCard  to add
	 * @param secondCard to add
	 * @param thirdCard  to add
	 */
	private void dealFlop(String firstCard, String secondCard, String thirdCard) {
		board.add(Card.getByChars(firstCard.charAt(0), firstCard.charAt(1)));
		board.add(Card.getByChars(secondCard.charAt(0), secondCard.charAt(1)));
		board.add(Card.getByChars(thirdCard.charAt(0), thirdCard.charAt(1)));
		for (int j = 0; j < board.size(); j++) {
			deck.removeCard(board.get(j));
		}
	}

	/**
	 * gets the latest action of given betting round
	 * 
	 * @param bettingRound to look at
	 * @return latest action - null if next action is first
	 */
	private Action lookForLatestAction(BettingRound bettingRound) {
		if (!bettingRound.isEmpty()) {
			return bettingRound.getBettingActions()
					.get(bettingRound.getBettingActions().size() - 1);
		} else {
			return null;
		}
	}

	/**
	 * pre processing line and adding action to given betting round
	 * 
	 * @param line         to create action from
	 * @param bettingRound to add on
	 * @param who          takes action
	 */
	private void extractAndAddAction(String line, BettingRound bettingRound, Who who) {
		String actionStr;
		if (who == Who.ME) {
			actionStr = line.substring(myName.length() + 2, line.length());
		} else {
			actionStr = line.substring(opName.length() + 2, line.length());
		}
		bettingRound.addAction(who, actionStr);
	}

	/**
	 * runs over the extracted betting rounds from the current hand; on every
	 * decision i am taking a new training data gets filled with the given data main
	 * method when it comes to storing and calculating key training data
	 * 
	 * @param hand         betting rounds needed
	 * @param withPrinting should the content of each hand get printed
	 */
	private void lookForDecisions(Hand hand) {
		Hand tempHand = new Hand();
		int potAtStartOfBetRound = 0;
		pot = 0;

		boolean myFirstAction;

		// looping over betting rounds
		for (BettingRound bettingRound : hand.getBettingRounds()) {
			turn = bettingRound.getTurn();

			// saving who is smallblind(button) only once
			if (turn == Turn.PREFLOP) {
				button = bettingRound.getBettingActions().get(0).getWho();
			}

			BettingRound tempBettingRound = new BettingRound(turn);

			int myMoneyBettingRound = myMoney;
			int myChipsInFront = 0;
			int opChipsInFront = 0;

			myFirstAction = true;
			boolean firstAction = true;
			for (Action action : bettingRound.getBettingActions()) {
				// getting action data
				actionToTake = action;
				ActionType actionType = action.getActionType();
				Who actionWho = action.getWho();

				if (firstAction) {
					// only calling one time each betting round
					updateSeenAs(turn);
				}

				if (!(actionType == ActionType.SB || actionType == ActionType.BB)) {

					// preflop aggressor action must be bet or raise
					if (bettingRound.getTurn() == Turn.PREFLOP && actionType.getValue() >= 4) {
						preflopAggressor = actionWho;
					}

					// only looking/learning at/from my decisions
					if (actionWho == Who.ME) {
						if (!tempBettingRound.getBettingActions().isEmpty()) {
							latestAction = tempBettingRound.getBettingActions()
									.get(tempBettingRound.getBettingActions().size() - 1);
						} else {
							latestAction = null;
						}

						if (opChipsInFront > myChipsInFront) {
							toCallAmount = opChipsInFront - myChipsInFront;
							whenCallPot = potAtStartOfBetRound + opChipsInFront * 2;
						} else {
							toCallAmount = 0;
							whenCallPot = pot;
						}

						// analyze my decision, generate and fill training data
						analyzeMyDecision(myFirstAction, tempHand.copy());
						myFirstAction = false;
					}
				}

				tempBettingRound.getBettingActions().add(action);

				// adding amount to chips in front
				if (actionWho == Who.ME) {
					myChipsInFront = updatingChipsInFront(myChipsInFront, action);

					// changing money when blind or call, bet, raise
					if (actionType.getValue() == 0 || actionType.getValue() >= 3) {
						myMoney = myMoneyBettingRound - myChipsInFront;
					}

				} else {
					opChipsInFront = updatingChipsInFront(opChipsInFront, action);
				}

				pot = 0;
				pot = potAtStartOfBetRound + myChipsInFront + opChipsInFront;

				// updating stats
				updateLooseTightStats(turn, action);
				updateAggrPassStats(action);

				// adding betting round to hand now
				// using temp hand to calc at my decisions
				if (firstAction) {
					tempHand.addBettingRound(tempBettingRound);
					potAtStarts[turn.getValue()] = potAtStartOfBetRound;
				}
				firstAction = false;
			}
			// adding chips in front to pot
			potAtStartOfBetRound += myChipsInFront + opChipsInFront;
		}
		preflopAggressor = null;
	}

	/**
	 * analyzes my decision(=action, but not sb or bb), calcs and fills data for
	 * training data, for each turn some vars need to be fill different
	 * 
	 * @param myFirstAction
	 */
	private void analyzeMyDecision(boolean myFirstAction, Hand tempHand) {
		if (turn == Turn.FLOP || turn == Turn.TURN) {
			if (myFirstAction) {
				// calc hand outcome and outs
				handOutcome = calcHandOutcome(turn);
				draw = new Draw();
				draw.calculateDraws(deck, realBoard(board, turn), holeCards, 1);
				outs = draw.combineOutcomes();
			} else {
				// hand outcome and outs did not change because same turn
				outs = trainingDatas.get(trainingDatas.size() - 1).getOuts();
				handOutcome = trainingDatas.get(trainingDatas.size() - 1).getHandOutcome();
			}
		}
		// river
		else if (turn != Turn.PREFLOP) {
			if (myFirstAction) {
				handOutcome = calcHandOutcome(turn);
			} else {
				// hand outcome did not change because same turn
				handOutcome = trainingDatas.get(trainingDatas.size() - 1).getHandOutcome();
			}
			// calc outs
			draw = new Draw();
			draw.calculateDraws(deck, realBoard(board, turn), holeCards, 1);
			outs = draw.combineOutcomes();
		}

		TrainingData trainingData = new TrainingData();
		fillTrainingData(tempHand, trainingData);
		trainingDatas.add(trainingData);
	}

	private void fillTrainingData(Hand tempHand, TrainingData trainingData) {
		trainingData.setActionToTake(actionToTake);
		trainingData.setActionToTakeAmount(actionToTake.getAmount());
		trainingData.setAllMoney(allMoney);
		trainingData.setBbAmount(bbAmount);
		List<Card> boardList = realBoard(board, turn);
		Card[] boardArr = new Card[boardList.size()];
		trainingData.setBoard(boardList.toArray(boardArr));
		trainingData.setButton(button);
		trainingData.setHand(tempHand);
		trainingData.setHandNumber(handNumber);
		trainingData.setHandOutcome(handOutcome);
		trainingData.setHoleCards(holeCards.clone());
		trainingData.setLatestAction(latestAction);
		trainingData.setMeAggrPass(meAggrPass.clone());
		trainingData.setMeLooseTight(meLooseTight.clone());
		trainingData.setMeSeenFlopAsSB(meSeenFlopAsSB);
		trainingData.setMeWinningFolded(meWinningFolded);
		trainingData.setMeWinningShowdown(meWinningShowdown);
		trainingData.setMyMoney(myMoney);
		trainingData.setOpAggrPass(opAggrPass.clone());
		trainingData.setOpHoleCards(opHoleCards != null ? opHoleCards.clone() : null);
		trainingData.setOpLooseTight(opLooseTight.clone());
		trainingData.setOpSeenFlopAsSB(opSeenFlopAsSB);
		trainingData.setOpWinningFolded(opWinningFolded);
		trainingData.setOpWinningShowdown(opWinningShowdown);
		trainingData.setOuts(outs);
		trainingData.setPot(pot);
		trainingData.setPotAtStarts(potAtStarts);
		trainingData.setPreflopAggressor(preflopAggressor);
		trainingData.setToCallAmount(toCallAmount);
		trainingData.setTurn(turn);
		trainingData.setWhenCallPot(whenCallPot);
	}

	/**
	 * board and hole cards get evaluated
	 * 
	 * @param turn current
	 * @return evaluation as vector
	 */
	private HandOutcome calcHandOutcome(Turn turn) {
		HandEvaluator handEvaluator = new HandEvaluator(realBoard(board, turn), holeCards, turn);
		HandOutcome handOutcome = handEvaluator.evaluate();
		return handOutcome;
	}

	/**
	 * just for adding/updating chips in front
	 * 
	 * @param chipsInFront to add on
	 * @param action
	 * @return changed chipsInFront value
	 */
	private int updatingChipsInFront(int chipsInFront, Action action) {
		if (action.getActionType() == ActionType.RAISE) {
			// when raising setting to "to"
			chipsInFront = action.getTo();
		} else {
			chipsInFront += action.getAmount();
		}
		return chipsInFront;
	}

	/**
	 * update statistics for tracking aggressive or passive my/ops playing style
	 * 
	 * @param actionType what was done
	 * @param actionWho  who did it
	 */
	private void updateAggrPassStats(Action action) {
		ActionType actionType = action.getActionType();
		if (action.getWho() == Who.ME) {
			switch (actionType) {
			case CHECK:
				meAggrPass[0]++;
				break;
			case CALL:
				meAggrPass[1]++;
				break;
			case BET:
				meAggrPass[2]++;
				break;
			case RAISE:
				meAggrPass[3]++;
				break;
			default:
				break;
			}
		} else {
			switch (actionType) {
			case CHECK:
				opAggrPass[0]++;
				break;
			case CALL:
				opAggrPass[1]++;
				break;
			case BET:
				opAggrPass[2]++;
				break;
			case RAISE:
				opAggrPass[3]++;
				break;
			default:
				break;
			}
		}

	}

	/**
	 * update statistics for tracking how loose or tight my/ops playing style is by
	 * counting the number of folds on each turn for each player
	 * 
	 * @param turn
	 * @param actionType
	 * @param actionWho
	 */
	private void updateLooseTightStats(Turn turn, Action action) {
		if (action.getActionType() == ActionType.FOLD) {
			boolean isMe = action.getWho() == Who.ME;
			switch (turn) {
			case PREFLOP:
				if (isMe) {
					meLooseTight[0]++;
				} else {
					opLooseTight[0]++;
				}
				break;
			case FLOP:
				if (isMe) {
					meLooseTight[1]++;
				} else {
					opLooseTight[1]++;
				}
				break;
			case TURN:
				if (isMe) {
					meLooseTight[2]++;
				} else {
					opLooseTight[2]++;
				}
				break;
			case RIVER:
				if (isMe) {
					meLooseTight[3]++;
				} else {
					opLooseTight[3]++;
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * just updating flopSeenAs vars for each player
	 * 
	 * @param turn
	 */
	private void updateSeenAs(Turn turn) {
		if (turn == Turn.FLOP) {
			if (button == Who.ME) {
				meSeenFlopAsSB++;// so op saw as bb
			} else {
				opSeenFlopAsSB++;// so i saw as bb
			}
		}
	}

	/**
	 * because when searching for my decisions already all cards - at latest
	 * situation of hand - are on board, needing to shorten board
	 * 
	 * @param board not changing
	 * @param turn  to get new length
	 * @return new array list of cards
	 */
	private ArrayList<Card> realBoard(ArrayList<Card> board, Turn turn) {
		int length;
		switch (turn) {
		case FLOP:
			length = 3;
			break;
		case TURN:
			length = 4;
			break;
		case RIVER:
			length = 5;
			break;
		default:
			length = 0;
			break;
		}
		return new ArrayList<Card>(board.subList(0, length));
	}

}