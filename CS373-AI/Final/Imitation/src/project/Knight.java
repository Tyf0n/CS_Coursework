package project;

public class Knight extends Piece {
	
	public Knight (ChessBoard world, boolean isObserver, Piece mentor){
		super(world, isObserver, KnightAction.values(), mentor);
	}	

}
