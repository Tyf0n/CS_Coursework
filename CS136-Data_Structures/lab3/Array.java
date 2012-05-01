/*
 * Nathaniel Lim
 * Williams College: CSCI 136
 * 3/3/08
 * Lab 3:  Dynamic Array
 */

import java.util.Comparator;

public class Array<Value> {
    private Value[] data;
    private int count;

    public Array(){
        data = allocateArray(0);
        count = 0;
    }
    
    public static void main (String [] args){
    	
    }
    
    //For debugging
    public static void printArray(Object[] x){
    	for (int i = 0; i < x.length; i++){
    		System.out.print (x[i] + ", ");  
    	}
    }
    
    //Ignore the issues that Java takes with this cast
    @SuppressWarnings("unchecked")
     private Value[] allocateArray(int n){
        return (Value[])(new Object[n]);
    }

    /**
     * setSize sets count = n and
     * increases the data size if needed by doubling the data array
     * decreases the data if n is smaller than half the data array
     * truncates the data if n < count 
     * by setting the remaining elements to null
     * if n > count, but n < data.length, no adjustments are done to
     * data (this case should happen a majority of the time.
     */
    public void setSize(int n){
    	assert n >= 0;
    	assert data!=null;
    	if (data.length == 0){
    		data = allocateArray(n);
    	} else {    	
	    	if (n > data.length){
	  	    Value[] output = allocateArray(2*data.length);
	            System.arraycopy(data, 0, output, 0, count);
	            data = output;
	    	} else if ( n < data.length/2){
		    Value[] output = allocateArray(data.length/2);
		    System.arraycopy(data, 0, output, 0, n);
		    data=output;      
	    	} else if (n < count){
                    for (int i = n; i < count; i++){
                        data[i] = null;
	            }
                }
    	}
        count = n;
    }

    //User should not be aware of data.length
    public int size(){
        return count;
    }

    //make the ith element equal to v
    public void set(int i, Value v){
        assert (i >= 0) && (i<count);
        data[i] = v;
    }

    public String toString(){
        String out = "{";
        for (int i = 0; i < count; i++) {
            out += data[i] + ", ";
        }
        out+="}";
        return out;
    }

    public Value get(int i){
        assert (i > 0) && (i < count);
        return data[i];
    }
    
    //Add to the end
    public void add (Value v){
        setSize(count +1);
        set (count-1, v);        
    }

    // "insert" v into data at 'i' by first moving all the elements
    // to the right, making room to set data[i] to v
    public void add (int i, Value v){
        assert i > 0 && i < count;
        setSize(count+1);
        System.arraycopy(data, i, data, i+1, count-i);
        data[i] = v;
    }

    public void clear(){
        data = allocateArray(0);
        count = 0;
    }

    public boolean isEmpty() {
        return count == 0 && data.length == 0;
    }
    
    public Value[] toArray(){
        Value[] out = allocateArray(count);
        System.arraycopy(data, 0, out, 0, count);
        return out;
    }
    
    public Value remove(int i){
        assert i >= 0 && i < count;
        //Remove by getting the value at i, and then shifting
        //all the elements to the right of i one to the left.
        Value out = get(i);
        System.arraycopy(data, i+1, data, i, count-i);
        return out;
    }

    public void trimToSize(){
        Value[] out = allocateArray(count);
        System.arraycopy(data, 0, out, 0, count);
        data = out;
    }

    
    public void sort(Comparator<Value> comparator) {
        trimToSize();
        mergeSort(data, comparator);
    }

    //Recursive implementation of mergeSort, done in place.
    private void mergeSort(Value[] c, Comparator<Value> comparator) {
        assert c != null;        
        //Do nothing if the size of the array is 1 or less. 
        if (c.length > 1) {            
            Value[] a = allocateArray(c.length / 2);
            Value[] b = allocateArray(c.length - a.length);
            System.arraycopy(c, 0, a, 0, a.length);
            System.arraycopy(c, a.length, b, 0, b.length);
            mergeSort(a, comparator);
            mergeSort(b, comparator);
            merge(a, b, c, comparator);
        }
    }

    
    private void merge(Value[] a, Value[] b, Value[] c, Comparator<Value> comparator) {
        assert a!=null && b!=null && c!=null;
        assert a.length + b.length == c.length;
    	int i = 0;
        int j = 0;
        int k;
        for (k = 0;(i < a.length) && (j < b.length); k++) {
            if (comparator.compare(a[i], b[j]) < 0) {
                c[k] = a[i];
                i++;
            } else {
                c[k] = b[j];
                j++;
            }       
        }
        if (i < a.length) {            
            System.arraycopy(a, i, c, k, a.length - i);
        } else{
            System.arraycopy(b, j, c, k, b.length - j);
        }
    }   


}
