/*
 * Nathaniel Lim CS334 HW8
 */

public class InsertCommand extends EditCommand{

    private String insertion;
	
    /**
     * The constructor just initializes the instance variable to be
     * the buffer on which to operate.  You must call this constructor
     * from inside your subclass constructors.
     */
    public InsertCommand(Buffer target, String s) {
	super(target);
	this.insertion = s;	
    }
	
    /** Perform the command on the target buffer */
    public void execute(){
	target.insert(insertion);
	target.setCursor(target.getCursor() + insertion.length());
    }

    /** Undo the command on the target buffer */
    public void undo(){
	target.setCursor(target.getCursor() - insertion.length());
	target.delete(insertion.length());
    }
	
    /** Print out what this command represents */
    public String toString(){
	return "[Insert \"" + insertion + "\"]";
    }
}
