/*
 * Nathaniel Lim CS334 HW8
 */

public class MoveCommand extends EditCommand{

    
    private int dPos = 1;
    private boolean isLeft;
    private int prevPos;
    private int newPos;
	
    /**
     * The constructor just initializes the instance variable to be
     * the buffer on which to operate.  You must call this constructor
     * from inside your subclass constructors.
     */
	
    public MoveCommand(Buffer target, int n, boolean isLeft) {
	super(target);
	this.prevPos = target.getCursor();
	this.isLeft = isLeft;
	if (isLeft){
		this.dPos = -1*n;
	} else {
		this.dPos = n;
	}
	    
    }

    public boolean isLeft(){
	return isLeft;
    }

    /** Perform the command on the target buffer */
    public void execute(){
	int loc = prevPos + dPos;
	if (loc < 0) {
	    loc = 0;
	}
	if (loc > target.size()) {
	    loc = target.size();
	}
	this.newPos = loc;
	target.setCursor(loc);
    }

    /** Undo the command on the target buffer */
    public void undo(){
	target.setCursor(prevPos);
    }
	
    /** Print out what this command represents */
    public String toString(){
	return "[Move to " +  newPos+"]";
    }
}
