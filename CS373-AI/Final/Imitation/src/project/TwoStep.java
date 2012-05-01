package project;

public class TwoStep extends Piece {
	public TwoStep (ChessBoard world, boolean isObserver, Piece mentor){
		super(world, isObserver, TwoStepAction.values(), mentor);
	}
}
