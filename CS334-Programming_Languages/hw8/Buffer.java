/*
 * Nathaniel Lim CS334 HW8
 */


/**
 * A very simple abstraction of a text editor buffer.  A Buffer
 * contains a sequence of characters and a cursor.  The cursor is the
 * position in the sequence where operations are applied.  For
 * example, the insert method inserts a sequence of characters at the
 * cursor position inside the buffer.
 */
public class Buffer {

    /** The contents are stored in a StringBuffer object. */
    protected StringBuffer contents;

    /** 
     *	Current location of the cursor.  
     *  Invariant: 0 <= cursor <= contents.size()
     */
    protected int cursor;
	
    /** 
     * Create a new empty Buffer with the cursor at 0.
     */
    public Buffer() {
	cursor = 0;
	contents = new StringBuffer("");
    }
	
    /** 
     * Return the current location of the cursor.
     */
    public int getCursor() {
	return cursor;
    }
	
    /** 
     * Move the cursor to loc
     * <pre>pre:  0 <= loc <= size() </pre>
     */
    public void setCursor(int loc) {
	assert 0 <= loc && loc <= size() : 
	    ("Bad call to setCursor(loc): " + 
	     "buffer size=" + size() + 
	     ", loc=" + loc);

        cursor = loc;
    }
	
    /** 
     * Insert the given string at the current cursor location.
     */
    public void insert(String str) {
	contents.insert(cursor, str);
    }
	
    /** 
     * Delete count characters to the right of the cursor.  Thus if
     * cursor is 10 and you delete 5 characters, characters at
     * positions 10-14 are deleted and cursor stays at position 10.
     * You must not delete characters past the end of the buffer.
     *
     * <pre>pre: getCursor() + count <= size()</pre>
     */
    public void delete(int count) {
	assert cursor + count <= size() : 
	    ("Bad call to delete(count): " + 
	     "buffer size=" + size() + 
	     ", cursor=" + cursor + ", count=" + count);


	contents.delete(cursor, cursor + count);
    }

    /**
     * Return the characters in positions [start..end) from the
     * buffer.
     *
     * <pre>pre: 0 <= start <= end <= size() </pre>
     */
    public String getText(int start, int end) {
	assert 0 <= start && start <= end && end <= size() : 
	    ("Bad call to getText(start,end): buffer size=" + size() + 
	     ", start=" + start +", end=" + end);

        return contents.substring(start, end);
    }
	
    /**
     * Return the number of characters stored in the buffer.
     */
    public int size() {
	return contents.length();
    }
	
    /**
     * Return a string showing the contents of the buffer and the
     * current cursor location.
     */
    public String toString() {
	String text = "Buffer: " + contents.toString();
	text += "\n        ";  // new line + space for "Buffer: ";
	for (int i = 0; i < cursor; i++) {
	    text += ' ';
	}
	text += '^';
	return text;
    }	

    /**
     * Test code for Buffer.
     */
    public static void main(String args[]) {
	Buffer b = new Buffer();
	b.insert("moo");
	System.out.println(b);
	b.setCursor(1);
	System.out.println(b);
	b.insert("moo");
	b.setCursor(1);
	System.out.println(b);
	b.delete(2);
	System.out.println(b);
	
	Buffer c = new Buffer();
	c.insert("moo");
	c.setCursor(1);
	c.delete(2);
    }
}
