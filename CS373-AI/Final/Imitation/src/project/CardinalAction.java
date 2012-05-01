package project;

public enum CardinalAction implements PieceAction {
    
	L1(0, 1),
    L2(1, 0),
    L3(-1, 0),
    L4(0, -1); 


    private int xChange, yChange;

    private CardinalAction(int xChange, int yChange) {
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