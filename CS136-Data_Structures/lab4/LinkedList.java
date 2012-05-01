/*
 * Nathaniel Lim
 * Williams College CS136 Lab5
 * 3/11/08
 * njl2@williams.edu

 */

import java.util.Iterator;
import java.util.Comparator;
import java.util.Random;
/**
   A linked list with efficient access to the last element.
This design involves a doubly linked list that has instance variables first and last.
When the list has zero elements first and last are null, list has 1 element, first
and last point to the same Node, and list has > 1 elements, first and last point to
the first and last nodes.  With both these instance variables, accessing the first and 
last elements are both constant time operations with respect to the size of the list. 

This design was chosen because it mirrors the structure of a singly linked list (not
circular), but adds some more functionality for the traversal of the list.  The changes
of references to the the next of previous nodes are fairly intuitive.  This ease of 
manipulation made me choose this design over the circular version because I didn't want
to worry about the problem of iterating through a circular data structure (infinite
traversals).
 */
public class LinkedList<Value> implements java.lang.Iterable<Value> {

    private Node<Value>first;
    private Node<Value>last;
    private int size = 0;
    
    /**
     * @param args
     */
    public static void main(String [] args) {
    	LinkedList<Integer> blah = new LinkedList<Integer>();
    	int numItems = 10;
    	Random r = new Random();
    	while (numItems > 0){
    		blah.add(r.nextInt(40));
    		numItems--;
    	}
    	System.out.println(blah);
    	blah.sort(new IntegerComparator());
    	System.out.println(blah);
        System.out.println(Float.POSITIVE_INFINITY);
        System.out.println(Float.NEGATIVE_INFINITY);

    }
    
    
    //  This implementation of a Node, which instances to
    // a previous and a next node enables the structure of a 
    // doubly linked List.  
    private class Node<Value> {
        private Value cargo;
        private Node<Value> next = null;
        private Node<Value> previous = null;

        public Node (Value v){
            cargo = v;
        }

        public Node (Value v, Node<Value> p, Node<Value> n){
            cargo = v;
            previous = p;
            next = n;
        }

        public void setNext (Node<Value> n){
            next = n;
        }

        public void setPrevious (Node<Value> p){
            previous = p;
        }

    }


    private class LLIterator<Value> implements Iterator<Value> {
        @SuppressWarnings("unchecked")

        Node<Value> currentNode = (Node<Value>)first;

        public LLIterator(){
            
        }

        /** Returns the next element in the iteration. */
        public Value next() {
            assert hasNext() : "Out of elements";
            Value out = currentNode.cargo;
            currentNode = currentNode.next;
            return out;
        }

        /** Returns true if the iteration has more elements. */
        public boolean hasNext() {
            return currentNode!=null;
        }
        
        /** Removes from the underlying collection the last element
            returned by the iterator.  Not implemented.*/
        public void remove() {
            assert false : "Not supported";
        }
    }


    /** Appends the specified element to the end of this list.*/
    public void add(Value v) {
        addLast(v);
    }

    /** Inserts the specified element at the specified position in this list. */
    // Runs in O(1) if the adding is to the beginning or the end of the list.
    // Otherwise it runs in O(i) time.
    public void add(int i, Value v) {
        assert !(i<0) && i < size  : "Index out of bounds";
        if (i == 0) {
            addFirst(v);
        } else if (i == size-1) {
        	addLast(v);
        } else {
            Node<Value> current = this.first;
            for (int index = 0; index<i; index++){
                current = current.next;
            }
            //Reached the Node
            //Create a new Node
            Node<Value> insertion = new Node<Value> (v, current.previous, current);
            current.previous.setNext(insertion);
            current.setPrevious(insertion);
            size++;
        }
    }

    /**  Inserts the given element at the beginning of this list.*/
    // Runs in constant time: O(1)
    public void addFirst(Value v) {
        if (size == 0){
            first = new Node<Value>(v);
            last = first;
        } else {
            Node<Value> newFirst = new Node<Value>(v);
            newFirst.setNext(first);
            first.setPrevious(newFirst);
            first = newFirst;
        }
        size++;
    }

    /** Appends the given element to the end of this list. */
    // Runs in constant time: O(1)
    public void addLast(Value v) {
        if (size == 0) {
            first  = new Node<Value>(v);
            last = first;
        } else {
            Node<Value> newLast = new Node<Value>(v);
            newLast.setPrevious(last);
            last.next = newLast;
            last = newLast;
        }
        size++;
    }

    /** Removes all of the elements from this list. */
    // Runs in constant time: O(1)
    public void clear() {
        size = 0;
        first = null;
        last = null;
    }

    /** Returns the element at the specified position in this list. */
 // Runs in O(i) time, because it must traverse the list to get at i.
    public Value get(int i) {
        assert i < size && !(i<0)  : "Index out of bounds";
        Value out = null;
        Iterator<Value> it = this.iterator();
        for (int j = 0; j <= i; j++){
            out = it.next();
        }
        return out;
    }

    /** Returns the first element in this list.*/
    // Runs in constant time: O(1)
    public Value getFirst() {
        return first.cargo;
    }
    
    /** Returns the last element in this list.*/
    // Runs in constant time: O(1)
    public Value getLast() {
        return last.cargo;
    }

    /** Returns the index in this list of the first occurrence of
       the specified element, or -1 if the List does not contain
       this element.*/
    //Runs in O(n) time.  It must 'touch' element to check to see in v is in this.
    public int indexOf(Value v) {
        if (size == 0) {
            return -1;
        }else{
            
            Iterator<Value> it = this.iterator();
            Value temp;
            int currentIndex = 0;
            while (it.hasNext()){
                temp = it.next();
                if (temp.equals(v)){
                    return currentIndex;
                }
                currentIndex++;
            }
            return -1;
        }
    }
    
    /** Returns true if this value is in the list */
    //Runs in O(n) because it invokes indexOf
    public boolean contains(Value v) {
        return indexOf(v) >= 0;
    }

    /** Replaces the element at the specified position in this list
     * with the specified element and returns the old value. */
    //Runs in O(i) time.  It must traverse the list to get at element i to alter it. 
    public Value set(int i, Value v) {
        assert !(i<0) && i < size : "Index out of bounds";
        Node<Value> currentNode = first;
        for (int index = 0; index < i; index++){
            currentNode = currentNode.next;
        }
        //Reached the Node
        // Get the value of the node, replace the value that the node 
        //is holding with v
        Value out = currentNode.cargo;
        currentNode.cargo = v;
        return out;
    }

    /**  Removes the element at the specified position in this list
     *  and returns the old value of that element. */
    //Runs in O(i) time.  It must traverse the list to get at element i to remove it. 
    public Value remove(int i) {
        assert !(i<0) && i < size  : "Index out of bounds";
        if (i==0){
            return removeFirst();
        }else if (i == size-1){
            return removeLast();
        }else {
            Node<Value> desiredNode = this.first;
            for (int index = 0; index < i; index++){
                desiredNode = desiredNode.next;
            }
            //Reached the Node
            //Get the Value, made the references skip over the currentNode
            //As far as the LinkedList knows, it doesn't exist anymore
            //Should be collected from the heap by Java garbage collection
            Value out = desiredNode.cargo;
            desiredNode.previous.setNext(desiredNode.next);
            desiredNode.next.setPrevious(desiredNode.previous);
            size--;
            return out;
        }
    }

    /** Removes and returns the first element from this list. */
    //Constant time operation: O(1)
    public Value removeFirst() {
        Value out = getFirst();
        first = first.next;
        if (size >1) {
            first.setPrevious(null);
        }
        size--;
        return out;
    }
    
    /** Removes and returns the last element from this list. */
    //Constant time operation: O(1)
    public Value removeLast() {
        Value out = getLast();
        last = last.previous;
        if (size > 1) {
            last.setNext(null);
        }
        size--;
        return out;
    }

    /** Number of elements in the list */
    //Constant time operation: O(1)
    public int size() {
        return size;
    }

    /** Returns true if there are no elements in this list.*/
    //Constant time operation: O(1)
    public boolean isEmpty() {
        return size == 0;
    }

    /** Sorts the elements of this list as ranked by the comparator. */
    //Invokes mergeSort, which runs in O( nlog(n)  ) time.
    public void sort(java.util.Comparator<Value> comparator) {
        mergeSort(this, comparator);
    }
    
    //MergeSort runs in O (  nlog(n)  ) time. 
    private void mergeSort(LinkedList<Value> list, java.util.Comparator<Value> comparator){
        if (list.size() > 1){
            //Split the LinkedLists in Half
            int splitIndex = list.size()/2;
            LinkedList<Value> newList1 = new LinkedList<Value>();
            LinkedList<Value> newList2 = new LinkedList<Value>();
            for (int i = 0; i < splitIndex; i++){
                newList1.addFirst(list.removeFirst());
            }
            while (!list.isEmpty()){
                newList2.addFirst(list.removeFirst());
            }
            //recursive call;
            mergeSort(newList1, comparator);
            mergeSort(newList2, comparator);
            merge(list, newList1, newList2, comparator);
        }
    }
    //This method runs in O (s) where s = newList1.size() + newList2.size()
    private void merge(LinkedList<Value> list, LinkedList<Value> newList1, 
                       LinkedList<Value> newList2, 
                       java.util.Comparator<Value> comparator){

        assert list.isEmpty();
        while (!newList1.isEmpty() && !newList2.isEmpty()){
            if (comparator.compare(newList1.getFirst(), newList2.getFirst()) <0){
                list.add(newList1.removeFirst());
            } else {
                list.add(newList2.removeFirst());
            }
        }
        if (newList1.isEmpty()){
            while (!newList2.isEmpty()){
                list.add(newList2.removeFirst());
            }
        } else if (newList2.isEmpty()){
            while (!newList1.isEmpty()){
                list.add(newList1.removeFirst());
            }
        }
    }

    /** Returns an iterator over the elements in this list (in proper sequence). */
    public Iterator<Value> iterator() {
         return new LLIterator<Value>();
    }

    //Runs in O(n) time.  
    public String toString() {
        if (isEmpty()) {
            return "{}";
        } else {
            String out = "{";
            Iterator it = this.iterator();
            int index = 0;
            while (it.hasNext()){
            	out += it.next() ;
                if (index != size() -1){
                	out+= ",";
                }
                index++;
            }
            out += "}";
            return out;
        }       
    }
    
}
