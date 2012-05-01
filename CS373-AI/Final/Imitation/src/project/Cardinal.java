package project;

public class Cardinal extends Piece {
	public Cardinal (ChessBoard world, boolean isObserver, Piece mentor){
		super(world, isObserver, CardinalAction.values(), mentor);
	}

}
