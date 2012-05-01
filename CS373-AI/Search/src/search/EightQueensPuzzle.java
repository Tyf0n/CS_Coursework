package search;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Eight Queens Implementation
 * 
 * CS 373 - AI
 * Assignment #1 - Search
 *
 * @author Joey Kiernan and Nathaniel Lim
 * @version 1.0
 * @date 2/18/10
 *
 */


public class EightQueensPuzzle implements ProblemGraph {
	
	public EightQueensPuzzle (){
	}
		
	
	@Override
	public EQState getState(String stateString) {
		return new EQState(stateString);
	}
	

	public class EQState implements State {
		
		public static final int BLANK = -1;
		public static final int QUEEN = 1;
		private int[][] board = new int[8][8];
		private int[] queenPos = new int[8];
		//board: 2D array to be able to check for conflicts
		//queenPos: 1D array to be able to quickly find the position
		//of a queen in a given row.
		
		// All spaces are initialized as BLANK!
		// board[rownum][colnum]


	    /*
	     *These two are for construction of subsequent states
	     */		
	    public int[][] getBoard(){
	    	return board;
	    }

	    public int[] getQueenPos(){
	    	return queenPos;
	    }


		public EQState(EQState eqState, EightQueenPuzzleAction a) {
		   //Cloning a 2d array doesn't work so need to manually do it
			board = new int[eqState.getBoard().length][eqState.getBoard().length];
			for(int row=0; row<board.length; row++){
				for(int col=0; col<board[0].length; col++){
					board[row][col]=eqState.getBoard()[row][col];
				}
			}
			queenPos = eqState.getQueenPos().clone();
		    
		    for(int i=0; i<queenPos.length; i++){
		    	if(queenPos[i] == BLANK){
		    		queenPos[i] = a.getRow();
		    		board[a.getRow()][i] = QUEEN;
		    		return;
		    	}
		    }

		    //if we get through the whole loop (i.e. all columns are full)
		    //then throw an error
		    throw new RuntimeException("Cannot generate new state, board full");
		    
		}
		
		public EQState(String configurationString){
			//Parsing a configurationString and returning a EQState
		    for(int row=0; row<board.length; row++){
		    	for(int col=0; col<board[0].length; col++){
		    		board[row][col]= BLANK;
		    	}
		    }

		    for(int i=0; i<queenPos.length; i++){
		    	queenPos[i] = BLANK;
		    }

		    StringTokenizer tokens = new StringTokenizer(configurationString, "() ");
			if(tokens.countTokens() > 8){
				throw new RuntimeException("The string: " + configurationString +
						" is not a valid Eight Queens state string.");
			}
			int numTokens = tokens.countTokens();
			for (int i = 0; i < numTokens; i++){
				String str = tokens.nextToken();
				int val = Integer.decode(str);
				if (val > 8 || val < 1){
					throw new RuntimeException("The string: " + configurationString +
							" is not a valid 8Queens state string. The value " + val +
						" is not a valid entry.");					
				} else {
					board[val-1][i] = QUEEN;
					queenPos[i] = val-1;
				}				
			}
		}
		
		public String toString(){
			String out = "(";
			for (int i = 0; i < 8; i++){
				if (queenPos[i] != BLANK){
				    out += "" + (queenPos[i] + 1) + " ";
				} else {
					break;
				}
			}
			out +=  ")";
			return out;
		}

		public List<GraphNode> expandNode() {
			List<GraphNode> list = new ArrayList<GraphNode>();

			for (EightQueenPuzzleAction a : EightQueenPuzzleAction.values()) {
				if (this.isValidAction(a)) {
					EQState newNode = new EQState(this, a);
					list.add(new GraphNode(newNode));
				}
			}
			return list;
		}
		
		private boolean isValidAction(EightQueenPuzzleAction a) {
		    EQState potential = new EQState(this, a);
		    return isValidBoard(potential.getBoard());
			
		}



	    /**
	     *Enourmous method that calculates if any Queen is in conflict
	     *with any other based on a 2d board of BLANKS and QUEENS
	     */
	    private boolean isValidBoard(int[][] board){
		//check rows
		for(int row = 0; row < board.length; row++){
		    int numQueens = 0;
		    for(int col = 0; col < board[0].length; col++){
		    	if(board[row][col] == QUEEN){
		    		numQueens++;
		    	}
		    }
		    if(numQueens > 1){
		    	return false;
		    }
		}

		//we know columns are ok based on action generation
		//so now check diagonals


		//left-to-right
		//starting with leftmost column
		
		for(int counter=0; counter < board.length; counter++){
		    int row = counter;
			int numQueens = 0;
		    int col = 0;
		    
		    while(row >= 0){
		    	if(board[row][col]==QUEEN){
		    		numQueens++;
		    	}
		    	col++;
		    	row--;
		    }
		    if(numQueens > 1){
		    	return false;
		    }
		}


		//left-to-right
		//starting with bottom row
		
		for(int counter=0; counter < board[0].length; counter++){
		    int col = counter;
			int numQueens = 0;
		    int row = 7;
		    while(col <= 7){
		    	if(board[row][col]==QUEEN){
		    		numQueens++;
		    	}
		    	col++;
		    	row--;
		    }
		    if(numQueens > 1){
		    	return false;
		    }
		}

		//right-to-left
		//starting with rightmost column
		
		for(int counter=0; counter < board.length; counter++){
		    int row = counter;
			int numQueens = 0;
		    int col = 7;
		    while(row >= 0){
		    	if(board[row][col]==QUEEN){
		    		numQueens++;
		    	}
		    	col--;
		    	row--;
		    }
		    if(numQueens > 1){
		    	return false;
		    }
		}


		//right-to-left
		//starting with bottom row
		
		for(int counter=0; counter < board[0].length; counter++){
		    int col = counter;
			int numQueens = 0;
		    int row = 7;
		    while(col >= 0){
		    	if(board[row][col]==QUEEN){
		    		numQueens++;
		    	}
				col--;
				row--;
		    }
		    if(numQueens > 1){
		    	return false;
		    }
		}
		return true;
	    }

		@Override
		public int h() {
			//Return the number of queens left to be placed on the board
			//This is an underestimate, usually of the number of steps left.
			int numQueens = 0;
			for (int i = 0; i < queenPos.length; i++){
				if (queenPos[i] != BLANK){
					numQueens++;
				}
			}
			return 8-numQueens;
		}

		@Override
		public boolean isGoal() {
		    for(int i=0; i<queenPos.length; i++){
		    	if(queenPos[i] == BLANK){
		    		return false;
		    	}
		    }		     
		    return isValidBoard(board);
		}
		
		//Two States are equal if their queenPos arrays are equal 
		//or if they reference the same Object.
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final EQState other = (EQState) obj;
			if (!Arrays.equals(queenPos, other.queenPos))
				return false;			
			return true;
		}
		
		public int hashcode(){
			final int PRIME = 31;
			return PRIME + Arrays.hashCode(queenPos);
		}		
		
	}
	
	/*
	 * An Enum class to enumerate the actions available to 
	 * trying to solve Eight Queens.  Tells you what row to place
	 * the queen into, and the action later decides the column.
	 */
	enum EightQueenPuzzleAction {
		MOVEQ1(0), MOVEQ2(1), MOVEQ3(2), MOVEQ4(3), 
		MOVEQ5(4), MOVEQ6(5), MOVEQ7(6), MOVEQ8(7);

		private final int row;
				
		private EightQueenPuzzleAction(int row){
			this.row = row;	
		}
		/**
		 * The change in the x coordinate of the blank space under the action
		 * @return integer x coordinate delta
		 */
		public int getRow(){
			return row;
		}
	}

}
