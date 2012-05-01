package project;

public class King extends Piece {

	public King (ChessBoard world, boolean isObserver, Piece mentor){
		super(world, isObserver, KingAction.values(), mentor);
	}
	
}
