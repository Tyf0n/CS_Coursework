/*
 * Nathaniel Lim CS334 HW8
 */


public class DeleteCommand extends EditCommand{

    
    private int num = 1;
    private String deletedString;
	
    /**
     * The constructor just initializes the instance variable to be
     * the buffer on which to operate.  You must call this constructor
     * from inside your subclass constructors.
     */

     public DeleteCommand(Buffer target, int n) {
	super(target);
	assert target.getCursor() + n <= target.size() : 
	    ("Bad call to delete(count): " + 
	     "buffer size=" + target.size() + 
	     ", cursor=" + target.getCursor() + ", count=" + n);
	this.deletedString = target.getText(target.getCursor(), target.getCursor() + n);
	this.num = n;
    }

    /** Perform the command on the target buffer */
    public void execute(){
	target.delete(num);
    }

    /** Undo the command on the target buffer */
    public void undo(){
	target.insert(deletedString);
	target.setCursor(target.getCursor() + deletedString.length());	
    }
	
    /** Print out what this command represents */
    public String toString(){
	return "[Delete " + num + "]";
    }
}
