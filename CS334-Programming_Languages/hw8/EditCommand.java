/*
 * Nathaniel Lim CS334 HW8
 */


/**
 * An abstract super class of all Edit Commands on Buffers.  This
 * class simply specifies the interface to all commands and stores the
 * target buffer of the command.
 */
public abstract class EditCommand {

    /** buffer to operate on */
    protected Buffer target;
	
    /**
     * The constructor just initializes the instance variable to be
     * the buffer on which to operate.  You must call this constructor
     * from inside your subclass constructors.
     */
    public EditCommand(Buffer target) {
	this.target = target;		
    }
	
    /** Perform the command on the target buffer */
    public abstract void execute();

    /** Undo the command on the target buffer */
    public abstract void undo();
	
    /** Print out what this command represents */
    public abstract String toString();
}
