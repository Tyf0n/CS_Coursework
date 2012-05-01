import java.util.*;

/*
 * Nathaniel Lim CS334 HW8
 */

/**
 * A simple TextEditor class.  The Editor maintains a buffer of
 * characters in the current "document", and updates the contents of
 * the documents based on commands from the user.  While a real text
 * editor would recieve keystroke and mouse events through the GUI, we
 * will use commands entered a terminal prompt to change the buffer.
 * See the handout for a list of supported commands.
 *       
 */
public class TextEditor {

    /** The "document" that the editor operates on. */
    protected Buffer buffer;

    /** The scanner for reading input. */
    protected Scanner input;
 

    /** The Stack of EditCommands performed that have been executed */
    protected Stack<EditCommand> history = new Stack<EditCommand>();

    /** The Stack of EditCommands performed that have been undone */
    protected Stack<EditCommand> commandsUndone = new Stack<EditCommand>();


    /** 
     * Default constructor creates an empty buffer.  Pass in the
     * Scanner from which to read commands.
     */
    public TextEditor(Scanner input) {
	buffer = new Buffer();
	this.input = input;
    }
	
    /**
     * Return the current cursor position in the buffer.
     */
    protected int getCursor() {
	return buffer.getCursor();
    }
	
    /**
     * Set the current cursor position in the buffer.  
     * <p> 
     * Clips the cursor movement to the ends of the buffer.  Ie, if loc
     * < 0, move cursor to 0, and if loc > buffer.size() move cursor
     * to buffer.size().
     */
    protected void setCursor(int loc) {
	if (loc < 0) {
	    loc = 0;
	}
	if (loc > buffer.size()) {
	    loc = buffer.size();
	}
	buffer.setCursor(loc);
    }

    /**
     * Insert the given text into the buffer at the current cursor
     * position and move the cursor to the end of the inserted text.
     */
    protected void insert(String text) {
	buffer.insert(text);
	buffer.setCursor(buffer.getCursor() + text.length());

    }

    /**
     * Delete count characters to the right of the cursor.
     *
     * <pre> pre: You must not delete past end of buffer </pre>
     */
    protected void delete(int count) {
	buffer.delete(count);
    }

    /**
     * Undo the previous editing command
     * (either a move, insert, or delete).
     */
    protected void undo() {
	if (!history.isEmpty()){
		EditCommand toBeUndone = history.pop();		
		toBeUndone.undo();
		commandsUndone.push(toBeUndone);
	}
    }

    /**
     * Print the commands on the undo stack.
     */
    protected void printHistory() {
	Stack<EditCommand> toPrint = new Stack<EditCommand>();
	Iterator<EditCommand> it = history.iterator();
	while (it.hasNext()){
		toPrint.push(it.next());
	}
	Iterator<EditCommand> it2 = toPrint.iterator();
	while(it2.hasNext()){
		System.out.println(it2.next());
	}
    }
	
    /**
     * Redo the last undone editing command, if possible
     */
    protected void redo() {
	if (!commandsUndone.isEmpty()){
	
		EditCommand toBeRedone = commandsUndone.pop();
		toBeRedone.execute();
		history.push(toBeRedone);
	}
    }

    /**
     * Return the contents of the buffer.
     */
    public String toString() {
	return buffer.toString();
    }

    /**
     * Read the next piece of input from Scanenr in as an integer, if
     * it is in fact an integer.  Otherwise, return 1 if there is no
     * input or generate an exception if there is something
     * unexpected.
     */
    protected int readOptionalInt(Scanner in) {
	if (in.hasNextInt()) {
	    return in.nextInt();
	} else if (in.hasNext()) {
	    throw new InputMismatchException("Missing number");
	} else {
	    return 1;
	}
    }

    /**
     * Read and process one command from the user.  Returns true if
     * additional commands should be read, or false if the user has
     * quit.
     */    
    public boolean processOneCommand() {
	System.out.print("? ");
	if (!input.hasNext()) return false;  // end of input

	// create temp scanner for one line of input
	String commandStr = input.nextLine();
	Scanner commandScanner = new Scanner(commandStr);  

	try {
	    String letter = commandScanner.next().toUpperCase();
	    if (letter.equals("I")) {
		commandScanner.skip(" ");  // skip space after 'I'
		String text = commandScanner.nextLine();

		//Construct a new InsertCommmand
		InsertCommand ic = new InsertCommand(buffer, text);
		ic.execute();
		history.push(ic);

	    } else if (letter.equals("D")) {
		//Construct a new DeleteCommand		
		DeleteCommand dc = new DeleteCommand(buffer, readOptionalInt(commandScanner));
	        dc.execute();
		history.push(dc);

		//delete(readOptionalInt(commandScanner));
	    } else if (letter.equals("<")) {
		boolean left = true;
		//Constuct a new MoveCommand, with the left boolean flag
		MoveCommand mc = new MoveCommand(buffer, readOptionalInt(commandScanner), left);
		mc.execute();
		history.push(mc); 
		//setCursor(getCursor() - readOptionalInt(commandScanner));
	    } else if (letter.equals(">")) {
		boolean left = false;
		//Constuct a new MoveCommand, with the left boolean flag
		MoveCommand mc = new MoveCommand(buffer, readOptionalInt(commandScanner), left);
		mc.execute();
		history.push(mc); 				
		//setCursor(getCursor() + readOptionalInt(commandScanner));
	    } else if (letter.equals("U")) {
		undo();
	    } else if (letter.equals("R")) {
		redo();
	    } else if (letter.equals("P")) {
		printHistory();
	    } else if (letter.equals("Q")) {
		return false;
	    } else {
		System.out.println("Invalid command: '" + commandStr + "'");
	    }
	} catch (InputMismatchException e) {
	    // when there is a non-number where number expected
	    System.out.println("Invalid Command: '" + commandStr + "'");
	} catch (NoSuchElementException e) {
	    // when there is no input left when input is still expected
	    System.out.println("Invalid Command: '" + commandStr + "'");
	}
	return true;
    }


    /**
     * Create a new TextEditor that reads commands from the terminal
     * window.  Process commands until the user enters "Q".
     */
    public static void main(String[] args) {
	Scanner input = new Scanner(System.in);
	TextEditor editor = new TextEditor(input);
	while (true) {
	    System.out.println(editor.toString());
	    if (!editor.processOneCommand()) break;
	}
    }
}
