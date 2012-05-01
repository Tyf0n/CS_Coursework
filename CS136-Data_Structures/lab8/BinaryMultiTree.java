/*
 * Nathaniel Lim
 * CS 136 Williams College
 * Lab: Binary-Multi-Tree
 * Mon, April 14, 2008
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;



/*
 * The BinaryMultiTree class supports a binary search tree structure, that 
 * sorts elements by a given Comparator<T> and places elements that are equivalent
 * according that Comparator<T> into the same Node.  
 * 
 * The structure of the BMT has a reference to a root Node, which has the 
 * fields that resemble a tree structure.  The reason for this choice over
 * having the BMT class have a left and right BMT instance was for the 
 * integration of the remove method, which required Nodes to be 
 * manipulated.
 * 
 * Keeping track of the size was a challenging part of the lab.
 * The choice was to keep a size variable that kept track of elements
 * within a Node and elements below it.  This would make the size() constant
 * time. This was easily maintained in the
 * add method where at each level of recursion, it is given that the element
 * x will be added somewhere on a certain subtree.  
 * 
 * However, with the structure of the remove operation, and having no reference
 * to the parent Node, made the update of the size hard to implement.  As solution
 * a recursive method, called findSizes was written to traverse through the entire
 * tree and set all the size variable of every Node in the tree to the correct value.
 * This makes remove quite expensive when is does remove something.  I assumed that
 * size() would be called more often than remove, so having size maintained made size()
 * constant time, which is better at the expense of having an expensive remove method. 
 */
public class BinaryMultiTree<T> implements Iterable<T> {

    private Comparator<T> comparator;
    private Node<T> root;
       
    public BinaryMultiTree(Comparator<T> comparator){
        this.comparator = comparator; 
        root = new Node<T>(comparator);
    }    

    /*
     * Calls upon the field size contained in the Node of the structure
     */
    public int size(){
    	return root.size();
    }

    public void add(T x){
        root.add(x);    	
    }
    
    
    /*
     * locateNode traverses through the tree structure starting at 'node'
     * and returns the Node in which according to the comparator the value
     * x may reside.  This is useful for many methods that require a recursive
     * traversal, such as contains, get, remove.
     * It returns null when there is no such Node on the tree that
     * could contain x
     * 
     * O(log n)
     */
    public Node<T> locateNode (Node<T> node, T x){
    	int c = comparator.compare(x, node.getValues().get(0));
    	Node<T> L = node.getLeft();
    	Node<T> R = node.getRight();
        if (c == 0) {
            return node;
        } else if (c < 0) {
            if (L != null){
                return locateNode(L, x);
            } else {
                return null;
            }
        } else {
            if (R != null){
                return locateNode(R, x);
            } else {
                return null;
            }
        }
    }
    
    
    /*
     * Finds the Node where x may reside, returns false if there is 
     * no such node, otherwise it returns whether or not x is contained in
     * the values of the Node 
     * 
     * locating call: 	O(log n)
     * Search within node: O(m), where m is 
     * the number of values within the node 
     */
    public boolean contains(T x){
    	Node<T> spot = locateNode(root, x);
    	if (spot == null){
    		return false;
    	} else {
    		return spot.getValues().contains(x);
    	}
    }

    /*
     * Set the root reference to a new empty Node
     */
    public void clear(){
    	root = new Node<T>(comparator);
    }

    /*
     * Locates what node the removal should be from.
     * If there are 2 or more things contained in the Node
     * Simply remove if its there.
     * Otherwise the Node will have one thing, which is removed
     * and then the Node itself is removed from the structure.
     * 
     * Remove takes O(log n) to find the node
     * and O (n) to maintain the size field
     */
    public T remove(T x){
    	Node<T> toGo = locateNode(root, x);
    	if (toGo == null){
            return null;
    	} else {
            ArrayList<T> y = toGo.getValues();
            if (y.size() > 1){
                int index = y.indexOf(x);
                if (index == -1){
                    //Not Found
                    return null;
                } else {
                    T out = y.remove(index);
                    findSizes(root);
                    return out;
                }    			
            } else {
                T out = y.remove(0);
                //Now with the Node toGo storing no values
                //The Node must be removed from the tree structure
                toGo = removeTopNode(toGo);
                findSizes(root);
                return out;
            }    		
    	}
    }
    
    //This a method that can figure out all the sizes for all the nodes,
    //if the remove or add method fails to update the size field
    // O(n) where n is the number of nodes (not values)
	
    private int findSizes(Node<T> n){
    	int s = 0;
    	s += n.getValues().size();
    	
    	if (n.getLeft() != null){
            s+= findSizes(n.getLeft());
    	}   	
    	if (n.getRight() != null){
            s+= findSizes(n.getRight());
    	}
    	n.setSize(s);
    	return s;
    }
    
    /*
     * Handles the four cases for removing a Node from a BinaryTree
     * Method design from Java Structures by Duane A. Bailey
     */
    private Node<T> removeTopNode(Node<T> topNode) {
        //pre: topNode contains value wanted to remove
    	
    	Node<T> left = topNode.getLeft();
    	Node<T> right = topNode.getRight();
    	
    	//disconnect the top Node
    	topNode.setLeft(null);
    	topNode.setRight(null);
    	
    	//Case 1
    	if (left == null){
            return right;
    	}
    	
    	//Case 2
    	if (right == null){
            return left;
    	}
    	
    	Node<T> predecessor = left.getRight();
    	
    	//Case 3
    	if (predecessor == null){
            left.setRight(right);
            return left;
    	}
    	
    	//Case 4: General
    	
    	Node<T>  parent = left;
    	while (!(predecessor.getRight() == null)){
            parent = predecessor;
            predecessor = predecessor.getRight();
    	}
    	
    	// 'predecessor' is the predecessor of the root
    	
    	parent.setRight(predecessor.getLeft());
    	predecessor.setLeft(left);
    	predecessor.setRight(right);
    	return predecessor;
    	
    }

    /*
     * Locates where the value x may be, 
     * and iterates through the value of the node
     * until it finds an obj that equals x, returns it
     * Returns null otherwise
     * 
     * O(log n)
     */
    public T get(T x){
    	Node<T> spot = locateNode(root, x);
    	if (spot != null){
            ArrayList<T> vals = spot.getValues();
            for (T obj: vals){
                if (obj.equals(x)){
                    return obj;
                }    		
            }
            return null;
    	} else {
            return null;
    	}    	
    }    
   
    public Iterator<T> iterator() {
    	return new InOrderIterator();    	
    }
    
    
    /*
     * This inorder iterator simply does an inorder
     * recursive traversal of the tree structure and 
     * adds all the values at each node to an ArrayList
     * which is then iterated through.
     * 
     *  Instantiating and running the InOrderIterator class
     *  is O(n), which is necessary for any implementation of
     *  an iterator, however there is extra space allocated 
     *  (an ArrayList of size n) that is not necessarily needed
     *  in other implementations. However this implementation is 
     *  much easier to write because of having to deal with iterating 
     *  through nodes and values within those nodes.
     */
    
    private class InOrderIterator implements Iterator<T>{

        ArrayList<T> allValues = new ArrayList<T>();
        private int index = 0;
    	
        public InOrderIterator(){
            buildHelper(root);
        }
        
        public void buildHelper(Node<T> current){
            //Inorder recursion:  L, Root, R
            
            if (current.left != null){
                buildHelper(current.left);
            }			
            
            allValues.addAll(current.getValues());			
            
            if(current.right!=null){
                buildHelper(current.right);
            }
			
        }
        
        public boolean hasNext() {
            return index < allValues.size();
        }
        
        
        public T next() {
            T out =  allValues.get(index);
            index++;
            return out;
        }
        
        //Implemented to do nothing.
        public void remove() {
        }
    	
    }
    
    public class Name {   	
    	private String first, last;
    	public Name (String first, String last){
    		this.first = first;
    		this.last = last;
    	}
    	public String getFirst(){
    		return first;
    	}
    	public String getLast(){
    		return last;
    	}
    	public boolean equals (Object o){
            Name other = (Name)o;
            return last.equals(other.getLast()) && 
                first.equals(other.getFirst());
    	}

    	public String toString(){
            return first + " " + last;
    	}	    	
    }

    public class LastNameComparator implements Comparator<Name>{
    	/*
    	 * The compare method uses the fact that Strings are Comparable
    	 * The Java Documentation says that the String compareTo method
    	 * "compares two strings lexicographically". Which is how they need
    	 * to be compared in this context.     
    	 */    
    	public int compare (Name a, Name b){
            return a.getLast().compareTo(b.getLast());
         
    	}
    	
    	// No one ever sees if two Comparators are equal
    	// Never truly implemented.
    	public boolean equals (Object obj) {
            return false;
    	}
    }
    
    /*
     * This helper class Node maintains the structure of the 
     * Binary Multi Tree.  Each node has references to right and left
     * and contains an ArrayList of values of type T.
     * 
     * Size of a Node keeps track of how many values are in the current Node
     * plus the number of values contained in both subTree Nodes
     * 
     */
  
    private class Node<T> {
    	private ArrayList<T> values = new ArrayList<T>();
    	
    	private Node<T> left;
    	private Node<T> right;
    	private int size = 0;
    	private Comparator<T> comparator;
  	    	
    	public Node (Comparator<T> c){
            comparator = c;
    	}
    	public ArrayList<T> getValues() {
            return values;
    	}
        
    	public Node<T> getLeft() {
            return left;
    	}
    	public void setLeft(Node<T> left) {
            this.left = left;
    	}
        public Node<T> getRight() {
            return right;
        }
        public void setRight(Node<T> right) {
            this.right = right;
        }
        public int size() {
            return size;
        }
        
        public void setSize(int size) {
            this.size = size;
        }		
		
		
        /*
         * Recursively adds a value to a Node tree
         * Takes care of making new Nodes when needed, 
         * and adding to existing Nodes, while maintaining
         * the Binary Search structure, and the size field
         * 
         * O(log n)
         */
        public void add(T x){	
            //This clause is only run when the this Node was
            //recently instantiated.
            if (values.size()==0){
                values.add(x);
                size++;
            } else {
                int c = comparator.compare(x, values.get(0));
                if (c == 0){
                    values.add(x);
                    size++;
                } else if (c < 0){
                    if (left == null){
                        left = new Node<T>(comparator);
                    }
                    left.add(x);
                    size++;
                } else {
                    if (right == null){
                        right = new Node<T>(comparator);
                    }
                    right.add(x);
                    size++;
                }      
            }		
        }        
    }    
}
