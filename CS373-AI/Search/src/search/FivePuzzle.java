package search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * An enumeration representing the 4 possible actions in the
 * FivePuzzle domain (DOWN, UP, LEFT and RIGHT).
 *
 * @author pippin
 *
 */
enum FivePuzzleAction {
	DOWN(1, 0), UP(-1, 0), LEFT(0, -1), RIGHT(0, 1);

	// the change in the x coordinate of the blank space under this action
	private final int x;
	// the change in the y coordinate of the blank space under this action
	private final int y;
	/**
	 * Each action specifies a particular change to the x and y coordinates
	 * of the blank space in the FivePuzzle problem. For example, the action
	 * LEFT decrements the x coordinate by 1, while leaving the y coordinate
	 * unchanged.
	 * @param x x coordinate delta
	 * @param y y coordinate delta
	 */
	private FivePuzzleAction(int x, int y){
		this.x = x;
		this.y = y;
	}
	/**
	 * The change in the x coordinate of the blank space under the action
	 * @return integer x coordinate delta
	 */
	public int getX(){
		return x;
	}
	/**
	 * The change in the y coordinate of the blank space under the action
	 * @return integer y coordinate delta
	 */
	public int getY() {
		return y;
	}
}

/**
 * The ProblemGraph implementation for the FivePuzzle problem.
 *
 * @author pippin
 *
 */
public class FivePuzzle implements ProblemGraph {

	public static int BLANK = -1;
	// dimensions of the puzzle
	public static int XSIZE = 2;
	public static int YSIZE = 3;

	private FPState goalNode;

	/**
	 * Implementation of the State interface for the FivePuzzle problem.
	 * @author pippin
	 *
	 */
	public class FPState implements State {
		// array of tiles. The blank tile has value BLANK
		private int[][] configuration = new int[XSIZE][YSIZE];
		int blankX, blankY;

		/**
		 * Constructs a state from a string, formatted as a list enclosed in parenthesis.
		 *
		 * For example, the goal state of the five puzzle has the 5 numbered tiles in order,
		 * with the blank tile in the upper left corner. This state would be represented by
		 * the string "(B 1 2 3 4 5)". The first three elements occupy the first row
		 * of the five puzzle, while the last 3 elements occupy the second row.
		 *
		 * This method has fairly minimal error checking.
		 *
		 * @param configurationString
		 */
		public FPState(String configurationString) {
			StringTokenizer tokens = new StringTokenizer(configurationString, "() ");
			if(tokens.countTokens() != XSIZE*YSIZE)
				throw new RuntimeException("The string: " + configurationString +
						" is not a valid 5Puzzle state string.");
			for(int i = 0; i < XSIZE; i++)
				for (int j = 0; j < YSIZE; j++) {
					String str = tokens.nextToken();
					if (str.equals("B")) {
						configuration[i][j] = BLANK;
						blankX = i;
						blankY = j;
					} else {
						int val = Integer.decode(str);
						if (val > XSIZE*YSIZE-1 || val < 0)
							throw new RuntimeException("The string: " + configurationString +
							" is not a valid 5Puzzle state string. The value " + val +
							" is not a valid entry.");
						configuration[i][j] = val;

					}
				}
		}

		/**
		 * Constructs a new FP state from a parent state, and an action taken in this state.
		 * This method assumes that the test isValidAction() has passed. It is not a public
		 * method.
		 *
		 * @param parent initial state
		 * @param action action taken from initial state
		 */
		private FPState(FPState parent, FivePuzzleAction action) {

			// initialize to clone parent
			for (int i = 0; i < configuration.length; i++)
				for (int j = 0; j < configuration[i].length; j++)
					configuration[i][j] = parent.configuration[i][j];

			blankX = parent.blankX;
			blankY = parent.blankY;

			// calculate the new location of the blank square
			int newX = blankX + action.getX();
			int newY = blankY + action.getY();

			// Switch the two squares
			configuration[blankX][blankY] = configuration[newX][newY];
			configuration[newX][newY] = BLANK;
			blankX = newX;
			blankY = newY;
		}

		/**
		 * Determines whether a particular action would have a valid effect if taken in
		 * this state. For the FivePuzzle, this method checks whether or not the action
		 * would move the blank square outside the bounds of the puzzle, if executed.
		 * @param action the action being tested
		 * @return true if the action produces a valid next state, false otherwise
		 */
		public boolean isValidAction(FivePuzzleAction action) {
			int newX = blankX + action.getX();
			int newY = blankY + action.getY();
			if (newX < 0 || newX >= XSIZE || newY < 0 || newY >= YSIZE)
				return false;
			return true;
		}

		/**
		 * Constructs the set of next states that would result from executing
		 * each of the actions in the action set.
		 *
		 * @return a list of child nodes, one for each valid action
		 */
		public List<GraphNode> expandNode() {
			List<GraphNode> list = new ArrayList<GraphNode>();

			for (FivePuzzleAction a : FivePuzzleAction.values()) {
				if (this.isValidAction(a)) {
					FPState newNode = new FPState(this, a);
					list.add(new GraphNode(newNode));
				}
			}
			return list;
		}

		/**
		 * Determines whether or not the state is a goal state.
		 * @return true if a goal state, false otherwise.
		 */
		public boolean isGoal() {
			return this.equals(goalNode);
		}

		/**
		 * The hash function is constructed from the two rows of the configuration
		 * array.
		 */
		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + Arrays.hashCode(configuration[0]);
			result = PRIME * result + Arrays.hashCode(configuration[1]);
			return result;
		}

		/**
		 * The equals function is constructed from the two rows of the configuration
		 * array.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final FPState other = (FPState) obj;
			if (!Arrays.equals(configuration[0], other.configuration[0]))
				return false;
			if (!Arrays.equals(configuration[1], other.configuration[1]))
				return false;
			return true;
		}

		/**
		 * Converts the state to a state string. Follow the formatting guidelines in the
		 * assignment handout for each domain. The state string output should in the same
		 * format as the strings accepted by the constructor for this class.
		 */
		public String toString() {
			String strBuf = "(";
			for (int i = 0; i < configuration.length; i++) {
				for (int j = 0; j < configuration[i].length; j++) {
					if(configuration[i][j] != BLANK)
						strBuf += configuration[i][j];
					else
						strBuf += "B";
					if ((i < configuration.length - 1) || (j < configuration[i].length - 1))
						strBuf += " ";
				}
			}
			strBuf += ")";
			return strBuf;
		}

	    //========================================================
	    /**
	     *returns manhattan distance to goal
	     */
	    public int h(){
		return blankX + blankY;
		/*int total = 0;
		for(int row = 0; row < configuration.length; row++;){
		    for(int col = 0; col < configuration[0].length; col++;){
			//correct row = value / #cols
			
		    }
		}
		*/

	    }
	    //========================================================


	}

	/**
	 * Constructs the goal state for the five puzzle instance.
	 *
	 */
	public FivePuzzle() {
		 goalNode = new FPState("(B 1 2 3 4 5)");
	}



	/**
	 * Constructs an FPState (which implements the State interface) from a state string.
	 *
	 * This method is part of the ProblemGraph interface.
	 *
	 * @param stateString a string in the correct format (see FPState constructor)
	 * @return an instance of the FPState class
	 */
	public FPState getState(String stateString) {
		return new FPState(stateString);
	}


}
