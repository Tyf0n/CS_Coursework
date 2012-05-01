package project;

public enum TwoStepAction implements PieceAction {
    
	M1(-2, -2),
    M2(-2, 2),
    M3(2, 2),
    M4(2, -2), 
	M5(0, 2) ,
    M6(2, 0),
    M7(0, -2),
    M8(0, 2),
	RIGHT(1, 0) ,
    DOWN(0, -1),
    LEFT(-1, 0),
    UP(0, 1), 
    UP_RIGHT(1, 1),
    UP_LEFT(-1, 1),
    DOWN_RIGHT(1, -1),
    DOWN_LEFT(-1, -1);


    private int xChange, yChange;

    private TwoStepAction(int xChange, int yChange) {
        this.xChange = xChange;
        this.yChange = yChange;
    }

    public int getActionId() {
        return ordinal();
    }

	public int dx() {
		return xChange;
	}

	public int dy() {
		return yChange;
	}
}
