/**
 * Nathaniel Lim
 * 2/20/08
 * Lab 2  CS136
*/

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class CoinStrip {	
	private ArrayList<Square> strip = new ArrayList<Square>();
    private ArrayList<Integer> coinsToSpots = new ArrayList<Integer>();
    private int turn = 1;
	private int coins;
	private final int boardSize = 15;
	
	public static void main (String [] args) throws Exception{	
		CoinStrip myBoard = new CoinStrip();
		System.out.println(myBoard);
		boolean gameOver = false;
		while (!gameOver){			
			if (myBoard.turn == 1){
				System.out.println("FIRST PLAYER'S MOVE!");
			} else {
				System.out.println("SECOND PLAYER'S MOVE!");
			}
			System.out.println("ENTER MOVE:   'COIN#,SPACES'  ");
			Scanner s = new Scanner(System.in);
			String input = s.nextLine();
			String [] inputs = input.split(",");
			if (inputs.length != 2){
				System.out.println("Wrong input syntax");
			} else {
					try {
						gameOver = myBoard.move(Integer.parseInt(inputs[0]), 
							Integer.parseInt(inputs[1]));
						System.out.println(myBoard);
					} catch(Exception e) {
						System.out.println("Please enter numbers");
					}
			}
		}	
	}
	
	// This method is a O(p) time operation, where p is the number of pieces.
	// This method is a constant time operation.  This works because the rules state that 
	// coins can never jump over other pieces and two coins cannot occupy the same square, 
	// and that the coins are numbered in ascending order from left to right starting at 0
	public boolean isGameOver(){
		int lastPieceNum = coins - 1;
		if (coinsToSpots.get(lastPieceNum).intValue() == lastPieceNum ){
			return true;
		} else {
			return false;
		}
		
		
		/**
		for (int i = 0; i < coins; i++){
			if (strip.get(i).isEmpty){
				return false;
			}
		}
		return true;
		*/
	}
	
	// This is a constant time operation O(1)
	public int indexOfCoin(int coinNum) throws Exception{
		if (!(coinNum < coinsToSpots.size() ) ){
			throw new Exception("Coin Number out of bounds");
		} 
	    return coinsToSpots.get(coinNum).intValue();
    }
	
	// Returns true if the game is over, false otherwise (including when a move is illegal)
	public boolean move(int coinNum, int moveNum) throws Exception{ 
		if (!(coinNum < coinsToSpots.size() ) ){
			throw new Exception("Coin Number out of bounds");
		} 
		if (moveNum < 0 ) {
			throw new Exception("No negative move number allowed");
		} 
		//Testing if the move is illegal
        //If the move is off the board (to the left),
		//don't even try the next test for illegality.
		int index = indexOfCoin(coinNum);
		if (index - moveNum <  0){
			System.out.println("Illegal Move");
			return false;
		}		
		if (coinNum !=0){ //no need to check for the first coin
			//This checks if the theoretical new spot would jump over (<) or land
			//on (=) the next piece to the left
			if (coinsToSpots.get(coinNum)- moveNum  <= coinsToSpots.get(coinNum-1)){
				System.out.println("Illegal Move");
				return false;
			}
		}
		
		//Moving the piece and updating the coinsToSpots ArrayList
		strip.get(index).moveOut();
		strip.get(index-moveNum).moveIn(coinNum);
		coinsToSpots.set(coinNum,index-moveNum);		
		
		if (isGameOver()){
			if (turn == 1){
				System.out.println("GAME OVER: FIRST PLAYER WINS");
			} else {
				System.out.println("GAME OVER: SECOND PLAYER WINS");
			}
			return true;
		}
		if (turn == 1){
			turn = 2;
		} else {
			turn = 1;
		}
		return false;
	}	
	
	/**
	 * This constructor sets up the game board with the number of squares, a random number of pieces, 
	 * and arranges them randomly on the board. 
	 */
	public CoinStrip () throws Exception{	    
		Random r = new Random();		
	    this.coins = r.nextInt(10) + 1;				
		//Adding all empty squares to the CoinStrip
		for (int i = 0; i < boardSize; i++){
			strip.add(new Square());
		}		
		//Create an ArrayList that contains all the possible possible positions for pieces	
		ArrayList<Integer> possibleSpots = new ArrayList<Integer> ();
		for (int i = 0; i < boardSize; i++){
			possibleSpots.add(i);
		}
		// There are (squares - pieces) empty spots that need to be eliminated from the 
		// set of possibilities of piece positions
		for (int i = 0; i< boardSize - coins; i++){
			int removedSpot = r.nextInt(possibleSpots.size());
			possibleSpots.remove(removedSpot);
		}
		//Now the pieces are filled in left to right, 
		//and the coinsToSpots ArrayList is filled out.
		for (int i = 0; i < possibleSpots.size(); i++){		    
			this.strip.set(possibleSpots.get(i), new Square(i));
			this.coinsToSpots.add(possibleSpots.get(i));
		}		
	}
	
	public int getSize(){
		return strip.size();
	}
	
	public String toString(){
		String out = "";
		for (int i = 0; i < strip.size(); i++){
			Square currentSquare = (Square)strip.get(i);
			if (!currentSquare.isEmpty){
				out+= "[" +currentSquare.pieceNum+ "] ";
			} else {
				out+= "[_] ";
			}
		}
		return out;
	}	
	
	//Helper class
	private class Square {
		public boolean isEmpty = true;
		public int pieceNum = -1;
		public Square (){ // Creates an empty square			
		}		
		public Square(int pieceNum){
			this.pieceNum = pieceNum;
			isEmpty = false;
		}
		public void moveIn (int pieceNum) {
			this.pieceNum = pieceNum;
			isEmpty = false;
		}		
		public void moveOut () {
			this.pieceNum = -1;
			isEmpty = true;
		}		
	}
}