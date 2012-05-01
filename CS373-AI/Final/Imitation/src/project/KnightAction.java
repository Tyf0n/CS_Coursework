package project;

public enum KnightAction implements PieceAction {
    
	L1(-2, 1),
    L2(-1, 2),
    L3(1, 2),
    L4(2, 1), 
	L5(2, -1) ,
    L6(1, -2),
    L7(-1, -2),
    L8(-2, -1); 


    private int xChange, yChange;

    private KnightAction(int xChange, int yChange) {
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