package search;

/**
 * Coins Puzzle Implementation. 
 * CS373 Assignment #1
 * @author: Joey Kiernan and Nathaniel Lim
 * @version: 1.2
 * @date: 2/22/10 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class CoinPuzzle implements ProblemGraph {
    
    //Constants to denote blank number, action types, boardsize
    public static final int BOARD_SIZE = 4;
    public static final int BLANK = 0;
    public static final int MOVE_PENNY = 1;
    public static final int MOVE_NICKLE = 2;
    public static final int MOVE_QUARTER = 3;	
    private CoinState goalNode;
    
    public CoinPuzzle(){
	setGoalNode(new CoinState("(1 2 3 B)"));
    }
    
    public class CoinState implements State {
	
	private int[] board = new int[BOARD_SIZE];  
	private int blankX;		
	
	public CoinState(String configurationString){
	    StringTokenizer tokens = new StringTokenizer(configurationString, "() ");
	    if(tokens.countTokens() != BOARD_SIZE){
		throw new RuntimeException("The string: " + configurationString +
					   " is not a valid Coin Puzzle state string.");
	    }
	    for (int i = 0; i < BOARD_SIZE; i++){
		String str = tokens.nextToken();
		if (str.equals("B")) {
		    board[i] = BLANK;
		    blankX = i;
		} else {
		    int val = Integer.decode(str);
		    if (val > BOARD_SIZE-1 || val < 0)
			throw new RuntimeException("The string: " + configurationString +
						   " is not a valid CoinPuzzle state string. The value " + val +
						   " is not a valid entry.");
		    board[i] = val;
		}
	    }
	    
	}
	
	/**
	 * Uses the board integer array to produce a String representation
	 * of the state. 
	 */
	public String toString() {
	    String strBuf = "(";
	    for (int i = 0; i < board.length; i++) {
		if(board[i] != BLANK){
		    strBuf += board[i];
		}else {
		    strBuf += "B";
		}
		if (i < board.length - 1) {
		    strBuf += " ";
		}
	    }
	    strBuf += ")";
	    return strBuf;
	}
	
	/**
	 * Writing a Heuristic for the 
	 * The Goal State is: Goal State (1 2 3 B)
	 * Returning the sum of how far each piece
	 * is away from their ending goal position.
	 * 
	 * This may be an overestimation given that we may be double
	 * counting the distance if we are indeed switching coins
	 * with blanks, thus we divide by two in order to be
	 * admissable
	 */
	public int h(){
	    int mh = 0;
	    for (int i = 0; i<BOARD_SIZE; i++){
		if (board[i] == BLANK){
		    mh += Math.abs(i-3);
		} else if (board[i] == 1) {
		    mh += Math.abs(i);
		}else if (board[i] == 2) {
		    mh += Math.abs(i-1);
		}else if (board[i] == 3) {
		    mh += Math.abs(i-2);
		}				
	    }
	    return mh/2;
	    
	}
	
	/*
	 * All actions are valid: Just switching one of three coins 
	 * with the blank spot.  
	 */
	public boolean isValidAction(CoinPuzzleAction action){
	    return true;
	}
	
	
	private CoinState(CoinState parent, CoinPuzzleAction action) {
	    // initialize to clone parent
	    for (int i = 0; i < board.length; i++){
		board[i] = parent.board[i];
	    }
	    this.blankX = parent.blankX;
	    
	    //Find the position of the coin type of the action
	    //And switch it with the blank square.
	    
	    int type = action.getType();
	    for (int i = 0; i < board.length; i++){
		if (board[i] == type){
		    //Switch the values;
		    board[blankX] = board[i];
		    board[i] = BLANK;
		    blankX = i;
		    break;
		}
	    }
	    
	}
	
	//Expand the node, all actions are valid, switching coins with the
	//blank space.
	public List<GraphNode> expandNode() {
	    List<GraphNode> list = new ArrayList<GraphNode>();
	    for (CoinPuzzleAction a : CoinPuzzleAction.values()) {
		if (this.isValidAction(a)) {
		    CoinState newNode = new CoinState(this, a);
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
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final CoinState other = (CoinState) obj;
	    if (!Arrays.equals(board, other.board))
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
	    return PRIME + Arrays.hashCode(board);
	}
	
    }
    
    @Override
	public CoinState getState(String stateString) {
	return new CoinState(stateString);
    }
    
    public void setGoalNode(CoinState goalNode) {
	this.goalNode = goalNode;
    }
    
    public CoinState getGoalNode() {
	return goalNode;
    }
    
    enum CoinPuzzleAction {
	MOVEPENNY(MOVE_PENNY), MOVENICKLE(MOVE_NICKLE), MOVEQUARTER(MOVE_QUARTER);
	
	private final int type;
	
	private CoinPuzzleAction(int type){
	    this.type = type;	
	}
	
	public int getType(){
	    return type;
	}		
    }
    
}
