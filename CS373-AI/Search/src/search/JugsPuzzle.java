package search;

/**
 * Water Jugs Implementation. 
 * CS373 Assignment #1
 * @author: Joey Kiernan and Nathaniel Lim
 * @version: 0.2
 * @date: 2/22/10 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class JugsPuzzle implements ProblemGraph {

	
	public static final int EMPTY_FIRST = 1;
	public static final int EMPTY_SECOND = 2;
	public static final int FILL_FIRST = 3;
	public static final int FILL_SECOND = 4;
	public static final int FIRST_TO_SECOND = 5;
	public static final int SECOND_TO_FIRST = 6;
	
	private JugsState goalNode;
	
	public static void main (String [] args){
		System.out.println("Hello World!");
		System.out.println(JugsPuzzleAction.values().length);
	}
	
	//The Goal state is one with no water in the first
	//3 Gallon Jug, and 2 Gallons in the 4 Gallon Jug
	public JugsPuzzle (){
		setGoalNode(new JugsState("(0 2)"));
	}
	
	public State getState(String stateString) {
		return new JugsState(stateString);
	}
	
	public class JugsState implements State {

		private int[] jugs = new int[2];  	
		
		public JugsState(String configurationString){
			StringTokenizer tokens = new StringTokenizer(configurationString, "() ");
			if(tokens.countTokens() != 2){
				throw new RuntimeException("The string: " + configurationString +
						" is not a valid Coin Puzzle state string.");
			}
			for (int i = 0; i < jugs.length; i++){
				String str = tokens.nextToken();
				int val = Integer.decode(str);
				//Check to make sure the first value isn't greater than 3
				//Or make sure the second value isn't greater than 4
				//Or make sure the val is not negative
				if ( (val > 3 && i ==0) || (val > 4 && i == 1) || val < 0){
					throw new RuntimeException("The string: " + configurationString +
							" is not a valid CoinPuzzle state string. The value " + val +
							" is not a valid entry.");
				} else {
					jugs[i] = val;
				}
			}
			
		}
		
		/**
		 * String representation: number of gallons of water in each jug.
		 */
		public String toString() {
			String strBuf = "(";
			for (int i = 0; i < jugs.length; i++) {
				strBuf += "" + jugs[i];
				if (i < jugs.length - 1) {
					strBuf += " ";
				}
			}
			strBuf += ")";
			return strBuf;
		}
		
		/*
		 * Writing a Heuristic for the 
		 * The Goal State is: 
		 * Goal State (0 2)
		 */
		public int h(){
			int output = 0;
			if (jugs[0] != 0){
				output++;
			}
			if (jugs[1] != 2){
				output+=2;
			}
			return output;						
		}
		
		
		public boolean isValidAction(JugsPuzzleAction action){
			//Invalid Actions: overflow, and
			//Already taken care and protected against
			
			//So now some actions do not change the state
			//So we will consider these actions "invalid"
			int[] testJugs = action.newJugs(jugs);
			if (testJugs[0] == jugs[0] && testJugs[1] == jugs[1]){
				return false;
			} else{
				return true;
			}

		}		
		
		private JugsState(JugsState parent, JugsPuzzleAction action) {
			// initialize to clone parent
			for (int i = 0; i < jugs.length; i++){
				jugs[i] = parent.jugs[i];
			}
			//Jugs now points to a new Jug array with the action performed on it
			jugs = action.newJugs(jugs);
		}
		
		public List<GraphNode> expandNode() {
			List<GraphNode> list = new ArrayList<GraphNode>();

			for (JugsPuzzleAction a : JugsPuzzleAction.values()) {
				if (this.isValidAction(a)) {
					JugsState newNode = new JugsState(this, a);
					list.add(new GraphNode(newNode));
				}
			}
			return list;
		}
		
		public boolean isGoal() {
			return this.equals(goalNode);
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
			final JugsState other = (JugsState) obj;
			if (!Arrays.equals(jugs, other.jugs))
				return false;
			return true;
		}
		
		/**
		 * The hash function is constructed from the two rows of the configuration
		 * array.
		 */
		@Override
		public int hashCode() {
			final int PRIME = 31;			
			return PRIME + Arrays.hashCode(jugs);
		}
		
	}
	
	
	public void setGoalNode(JugsState goalNode) {
		this.goalNode = goalNode;
	}

	public JugsState getGoalNode() {
		return goalNode;
	}

	enum JugsPuzzleAction {
		EMPTYFIRST(EMPTY_FIRST), EMPTYSECOND(EMPTY_SECOND), 
		FILLFIRST(FILL_FIRST), FILLSECOND(FILL_SECOND), FIRSTTOSECOND(FIRST_TO_SECOND), SECONDTOFIRST(SECOND_TO_FIRST);

		// the change in the x coordinate of the blank space under this action
		private final int type;				
		
		private JugsPuzzleAction(int type){
			this.type = type;	
		}
		/**
		 * The change in the x coordinate of the blank space under the action
		 * @return integer x coordinate delta
		 */
		public int getType(){
			return type;
		}
		
		/*
		 * This method generates the new amounts of water in 
		 * the jugs based on whatever action type it this is.
		 */
		public int[] newJugs (int[] jugs){
			int room;
			int[] output = new int[2];
			if (type==EMPTY_FIRST){
				output[0] = 0;
				output[1] = jugs[1];
			} else if (type == EMPTY_SECOND){
				output[0] = jugs[0];
				output[1] = 0;
			} else if (type == FILL_FIRST){
				output[0] = 3;
				output[1] = jugs[1];
			} else if (type == FILL_SECOND){
				output[0] = jugs[0];
				output[1] = 4;
			} else if (type == FIRST_TO_SECOND){
				room = 4-jugs[1];
				if (jugs[0] < (room)){
					//pour it all in
					output[1] = jugs[1] + jugs[0];
					output[0] = 0;					
				} else {
					output[1] = 4;
					output[0] = jugs[0] - room;
				}
			} else if (type == SECOND_TO_FIRST){
				room = 3-jugs[0];
				if (jugs[1] < (room)){
					//pour it all in
					output[0] = jugs[0] + jugs[1];
					output[1] = 0;					
				} else {
					output[0] = 3;
					output[1] = jugs[1] - room;
				}
			}
			return output;
		}
		
	}
	


}
