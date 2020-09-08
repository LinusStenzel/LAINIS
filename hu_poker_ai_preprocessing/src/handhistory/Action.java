package handhistory;

/**
 * represents a single action considering the betting options of a player in a
 * heads up no limit texas holdem game
 * 
 * @author stenz
 *
 */
public class Action {

	/**
	 * all possible actiontypes
	 */
	public enum ActionType {
		FOLD(0), CHECK(1.2F), CALL(2.2F), BET(3.3F), RAISE(4.3F), SB(-1), BB(-1);

		/**
		 * strength of the action higher -> stronger
		 */
		private float value;

		private ActionType(float value) {
			this.value = value;
		}

		public float getValue() {
			return value;
		}
	}

	/**
	 * who is taking the action
	 */
	public enum Who {
		// could be done by a boolean but for better reading
		ME(1), OP(0);
		private int value;

		private Who(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/*
	 * guaranteed vars
	 */
	private ActionType actionType;
	private Who who;
	private int amount;// of chips

	/**
	 * only a raise got a value for "to"
	 */
	private Integer to = null;

	/**
	 * only few actions are all-in
	 */
	private boolean isAllIn = false;

	/**
	 * different constructors because of raising and a possible all-in
	 * 
	 * @param actionType
	 * @param who
	 * @param amount
	 */
	public Action(ActionType actionType, Who who, int amount) {
		this.actionType = actionType;
		this.who = who;
		this.amount = amount;
	}

	public Action(ActionType actionType, Who who, int amount, int to) {
		this.actionType = actionType;
		this.who = who;
		this.amount = amount;
		this.to = to;
	}

	public Action(ActionType actionType, Who who, int amount, boolean isAllIn) {
		this.actionType = actionType;
		this.who = who;
		this.amount = amount;
		this.isAllIn = isAllIn;
	}

	public Action(ActionType actionType, Who who, int amount, int to, boolean isAllIn) {
		this.actionType = actionType;
		this.who = who;
		this.amount = amount;
		this.to = to;
		this.isAllIn = isAllIn;
	}

	/*
	 * getters
	 */

	public ActionType getActionType() {
		return actionType;
	}

	public Who getWho() {
		return who;
	}

	public int getAmount() {
		return amount;
	}

	public int getTo() {
		return to;
	}

	public boolean isAllIn() {
		return isAllIn;
	}

	/**
	 * converts the action into a two dimensional vector. first: actiontype, second:
	 * amount)
	 * 
	 * @param blind to make comparing different actions at different blindlevels
	 *              possible
	 * @return the created vector
	 */
	public float[] toVector(int blind) {
		float[] resVec = new float[2];
		if (actionType != ActionType.SB && actionType != ActionType.BB) {
			resVec[0] = actionType.value;
			resVec[1] = !isAllIn ? ((float) amount / blind) : 1.5F * ((float) amount / blind);
		}
		return resVec;
	}

	@Override
	public String toString() {

		String name = who.name() + " " + actionType.name();

		if (isAllIn) {
			name = name.concat(" ALL-IN ");
		}

		if (actionType == ActionType.CALL || actionType == ActionType.BET) {
			name = name.concat(" " + amount);
		} else if (actionType == ActionType.RAISE) {
			name = name.concat(" " + amount + " to " + to);
		}

		return name;
	}

}
