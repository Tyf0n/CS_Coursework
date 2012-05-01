package project;


public enum KingAction implements PieceAction {
        
		RIGHT(1, 0) ,
        DOWN(0, -1),
        LEFT(-1, 0),
        UP(0, 1), 
        UP_RIGHT(1, 1),
        UP_LEFT(-1, 1),
        DOWN_RIGHT(1, -1),
        DOWN_LEFT(-1, -1);


        private int xChange, yChange;

        private KingAction(int xChange, int yChange) {
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
	
	
