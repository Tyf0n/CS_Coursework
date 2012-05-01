

/**
 * A Simple Consumer class that illustrates how to 
 * use a Buffer of Characters.
 * <p>
 * This consumer extracts data from the character buffer
 * and prints the data to the screen.
 */
class Consumer extends Thread { 

    /** The buffer to read from. */
    private final Buffer<Character> buffer; 

    /**
     * Create a consumer with the given input buffer.
     */
    public Consumer(Buffer<Character> b) { 
	buffer = b; 
    } 

    /**
     * Read characters and print them until interrupted.
     */
    public void run() { 
	try {
	    while (true) {
		char c = buffer.delete(); 
		System.out.print(c); 
	    } 
	} catch (InterruptedException e) {
	}
    } 
} 
