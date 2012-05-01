//Nathaniel Lim
/**
 * A thread safe queue class.  It stores the data in an array 
 * and uses start/end indexes to indicate where to remove/add
 * elements.  It also keeps the size in elementCount so we
 * don't need to compute it ourselves.
 */
public class Buffer<T> { 
    
    private T[] elementData;   // array of values stored in buffer

    private int elementCount;  // current number of elements stored in buffer

    private int start;         // index of first value to be removed
	
    private int end;           // index to put the next value added
    
    /**
     * Create a new buffer with the given maximum size.
     */
    @SuppressWarnings("unchecked") // suppress warning caused by array creation (more on this later...)
    public Buffer(int size) { 
	end = -1; 
	start = 0; 
	elementCount = 0; 
	elementData = (T[])(new Object[size]);
    } 
   
    /**
     * Add value t to the queue.
     * This operation will block until the queue is not full.
     */
    public synchronized void insert(T t) throws InterruptedException { 
	while (elementCount == elementData.length) { 
	    wait(); 
	} 
	end = (end + 1) % elementData.length; 
	elementData[end] = t; 
	elementCount++; 
	notifyAll(); 
    } 
    
    /**
     * Remove value from queue.
     * This operation will block until the queue is not empty.
     */
    public synchronized T delete() throws InterruptedException { 
	while (elementCount == 0) { 
	    wait(); 
	} 
	T elem = elementData[start]; 
	start = (start + 1) % elementData.length; 
	elementCount--; 
	notifyAll(); 
	return elem; 
    } 
} 
