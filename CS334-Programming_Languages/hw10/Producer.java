import java.io.*;

/**
 * A Simple Producer class that illustrates how to 
 * use a Buffer of Characters.
 * <p>
 * This producer reads input from the terminal and inserts
 * the characters into a buffer.
 */
class Producer extends Thread { 
    /** the buffer to insert characters into */
    private final Buffer<Character> buffer; 

    /** an input stream to read chars from the terminal */
    private final InputStreamReader in = 
	new InputStreamReader(System.in); 

    /**
     * Create a new Producer that puts data
     * into the buffer b.
     */
    public Producer(Buffer<Character> b) { 
	buffer = b; 
    } 

    /**
     * Repeatedly read characters from the terminal
     * until eof is seen, putting each character into b.
     */
    public void run() { 
	try { 
	    while (true) { 
		int c = in.read(); 
		if (c == -1) break; // -1 is eof 
		buffer.insert((char)c); 
	    } 
	} catch (Exception e) {} 
    } 
} 
