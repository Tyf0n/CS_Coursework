/* Nathaniel Lim
 * CS373 - Final Project
 * May 18, 2010
 * Implicit Imitation 
 */

package project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Random;
import project.ImitationSim.PieceType;


public class ChessBoard{
	
	private static final double NOISE_LEVEL = 0.05;
	public boolean QLEARNING = false;
	public final double ACTION_COST = 0.05;
	private int[][] stateLabels;
    private ArrayList<Coordinate> stateList = new ArrayList<Coordinate>();
    private int numStates;
	
	Piece[][] board;
	private int xDim, yDim;
	private boolean[][] wallMatrix;
	private double[][] rewardMatrix;
	private PieceType mentorType;
	private PieceType observerType;
	private PieceAction[] mentorActions;
	private PieceAction[] observerActions;
	private int[] observerActionIds;
	private int[] mentorActionIds;
	
	public int sizeX(){
		return xDim;
	}
	
	public int sizeY(){
		return yDim;
	}
	
	public int numStates(){
		return numStates;
	}
	
	public PieceAction[] getMentorActions(){
		return mentorActions;
	}
	
	public PieceAction[] getObserverActions(){
		return observerActions;
	}
	
	public PieceType getMentorType(){
		return mentorType;
	}
	public PieceType getObserverType(){
		return observerType;
	}

	public ChessBoard(String fileName, PieceType m, PieceType o) {
		mentorType = m;
		observerType = o;
		if (m == PieceType.KING){
			mentorActions = KingAction.values();
		} else if (m == PieceType.KNIGHT){
			mentorActions = KnightAction.values();
		} else if (m == PieceType.TWOSTEP){
			mentorActions = TwoStepAction.values();
		} else {
			mentorActions = CardinalAction.values();
		}
		if (o == PieceType.KING){
			observerActions = KingAction.values();
		} else if (o == PieceType.KNIGHT){
			observerActions = KnightAction.values();
		} else if (o == PieceType.TWOSTEP){
			observerActions = TwoStepAction.values();
		} else {
			observerActions = CardinalAction.values();
		}
		
        // read in the array of locations
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            StreamTokenizer token = new StreamTokenizer(reader);
            token.nextToken();
            xDim = (int) token.nval;
            token.nextToken();
            yDim = (int) token.nval;
            wallMatrix = new boolean[xDim][yDim];
            rewardMatrix = new double[xDim][yDim];
            for (int i = 0; i < xDim; i++) {
                for (int j = 0; j < yDim; j++) {
                    token.nextToken();
                    if (token.ttype == StreamTokenizer.TT_NUMBER) {
                        rewardMatrix[i][j] = token.nval;
                        wallMatrix[i][j] = false;
                    } else {
                        wallMatrix[i][j] = true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialize();        
    }

	public void setLearning(boolean qlearning){
		this.QLEARNING = qlearning;
	}
	
	private void initialize() {		 	
	        observerActionIds = new int[observerActions.length];
	        mentorActionIds = new int[mentorActions.length];
	        for (int i = 0; i < observerActions.length; i++) {
	            observerActionIds[i] = observerActions[i].getActionId();
	        }	   
	        for (int i = 0; i < mentorActions.length; i++) {
	            mentorActionIds[i] = mentorActions[i].getActionId();
	        }
	        stateLabels = new int[xDim][yDim];
	        int count = 0;
	        for (int i = 0; i < xDim; i++) {
	            for (int j = 0; j < yDim; j++) {
	                if(!wallMatrix[i][j]) {
	                    stateLabels[i][j] = count;
	                    stateList.add(new Coordinate(i, j));
	                    count++;

	                }
	            }
	        }
	        numStates = xDim*yDim;
	 }
	 
	 public Coordinate nextState(Coordinate s, PieceAction a){
		 //Implement this with a small amount of noise
		 Random r = new Random();
		 int newX = s.getX() + a.dx();
		 int newY = s.getY() + a.dy();
		 //If the action goes off the board, or into a wall, return to starting state.
		 if (newX < 0 || newX > xDim -1 || newY < 0 || newY > yDim -1 ||
			 (wallMatrix[newX][newY])){
			 return s;
		 } else {
			 if (r.nextDouble()<NOISE_LEVEL){
				 //Noise, Randomly Move to some cardinal direction, if you can.
				 int dir = r.nextInt(4);
				 switch (dir){
				 	case 0: if (newY > 0 &&      !wallMatrix[newX][newY-1]){newY--;} 
				 	case 1: if (newX > 0 &&      !wallMatrix[newX-1][newY]){newX--;} 
				 	case 2: if (newY < yDim-1 && !wallMatrix[newX][newY+1]){newY++;} 
				 	case 3: if (newX < xDim-1 && !wallMatrix[newX+1][newY]){newX++;} 
				 }
			 }			 
		 }
		 return new Coordinate(newX, newY);
		 
	 }
	 
	  
	 public boolean isTerminalState(int stateId) {
	        Coordinate coords = getStateCoords(stateId);
	        if (rewardMatrix[coords.getX()][coords.getY()] != 0){
	            return true;
	 		}else{
	            return false;
	 		}
	 }
	 
	 public double getReward(int state) {
	        Coordinate coord = getStateCoords(state);
	        return rewardMatrix[coord.getX()][coord.getY()];
	  }
	 
	 public double getReward(int state, int action) {
	        Coordinate coord = getStateCoords(state);
	        return rewardMatrix[coord.getX()][coord.getY()] - ACTION_COST;
	  }
	 public Coordinate getStateCoords(int stateId) {
	        return stateList.get(stateId);
	 }
	 
	 public int getNextState(int stateId, PieceAction action){
		 Coordinate coords = getStateCoords(stateId);
		 Coordinate next = nextState(coords, action);
		 return getStateId(next.getX(), next.getY());
	 }
	 
	
	 protected int getStateId(int x, int y) {
		 return stateLabels[x][y];
	 }
	 
	 public String toString(){
		 String out = "";
		 for (int i = 0; i < rewardMatrix.length; i++){
			 for (int j = 0; j< rewardMatrix[i].length; j++){
				 if (wallMatrix[i][j]){
					 out += "W ";
				 } else {
					 out += " " + rewardMatrix[i][j];
				 }
			 }
			 out += "\n";
		 }
		 return out;
	 }
}
