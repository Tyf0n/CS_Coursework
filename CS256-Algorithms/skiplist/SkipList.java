/*
 * (c) Nathaniel Lim
 * CS 256
 * Homework 9, 5/5/09
 */

import java.util.*;

/**
 * Skip Lists support the following operations in expected O(log n) time w.h.p:
 * Addition
 * Removal
 * Containment
 * Predecessor
 * Successor
 *
 */
public class SkipList {

  private Node root;
  private Node lowerLeft;
  private Node lowerRight;
  private Random rng = new Random(47);
  private int size = 0;
  
  
  public SkipList() {
      root = new LeftBoundaryNode();
      
      //All 8 initial linkages
      
      root.right = new RightBoundaryNode();
      root.right.left = root;
      
      root.right.down = new RightBoundaryNode();
      root.right.down.up = root.right;
      
      root.right.down.left = new LeftBoundaryNode();
      root.right.down.left.right = root.right.down;
      
      root.down = root.right.down.left;
      root.right.down.left.up = root;
      
      lowerLeft = root.down;
      lowerRight = root.right.down;
      
      
  }
    
  public int size(){
      return this.size;
  }
    
    
    
  public Integer predecessor(int i){
	
      Node current = root;
      boolean found = false;
      while (!found){
	  if (isBottom(current)){
	      if (current.right.value < i &&
		  !current.right.isRightBoundary()){
		  
		  current = current.right;
	      } else{
		  found = true;				
	      }
	  } else {
	      if (current.right.value < i &&
		  !current.right.isRightBoundary()){
		  
		  current = current.right;
	      } else {
		  current = current.down;
	      }
	  }
      }
      
      if (current.isLeftBoundary()){
	  return null;
      } else {
	  return new Integer(current.value);
      }
      
  }
    
    
    
    
    
  public boolean remove(int i){
      //Find the node, and traverse to bottom.
      Node current = root;
      boolean found = false;
      while (!found){
	  if (current.value == i && !current.isLeftBoundary()){
	      if(isBottom(current)){
		  found = true;
	      } else {
		  current = current.down;
	      }
	  } else if (current.right.value <= i && !current.right.isRightBoundary()){
	      current = current.right;
	  } else { // if right.value > i || right.isRightBoundary()
	      if (isBottom(current)){
		  return false;
	      } else {
		  current = current.down;
	      }
	  }
      }
      
      //Current is now the node on the bottom, remove success levels
	  Node next;
	  Node leftBound = lowerLeft;
	  boolean checkExtraTop = false;
	  
	  
	  while (current != null){
	      checkExtraTop = current.left.isLeftBoundary() && current.right.isRightBoundary();		  
	      
	      next = current.up;
	      leftBound = leftBound.up;
	      
	      //Insert
	      current.left.right = current.right;
	      current.right.left = current.left;
		  current = next;
	  }
	  
	  //Remove extra top
	  if(checkExtraTop){
	      Node rightBound = leftBound.right;
	      leftBound.down.up = null;
	      rightBound.down.up = null;
	      root = leftBound.down;
	  }
	  this.size--;
	  return true;
	  
	  
  }
    
  public boolean contains(int i){
      //Find the node, and doesn't traverse to the bottom if found on higher level
      Node current = root;
      boolean found = false;
      while (!found){
	  if (current.value == i && !current.isLeftBoundary()){
	      if(isBottom(current)){
		  found = true;
	      } else {
		  current = current.down;
	      }
	  } else if (current.right.value <= i && !current.right.isRightBoundary()){
	      current = current.right;
	  } else { // if right.value > i || right.isRightBoundary()
	      if (isBottom(current)){
		  return false;
	      } else {
		  current = current.down;
	      }
	  }
      }
      return true;
      
  }
    
    public Integer successor(int i){
	Node current = root;
	boolean found = false;
	while (!found){
	    if (isBottom(current)){
		if (current.right.value < i &&
		    !current.right.isRightBoundary()){
		    
		    current = current.right;
		} else{
		    current = current.right;
		    found = true;				
		}
	    } else {
		if (current.right.value < i &&
		    !current.right.isRightBoundary()){
		    
		    current = current.right;
		} else {
		    current = current.down;
		}
	    }
	}
	
	if (current.isRightBoundary()){
	    return null;
	} else {
	    return new Integer(current.value);
	}
	
    }
    
    
    
    
    public boolean add (int i){
	boolean spotFound = false;
	Node current = root;
	Node insert = new Node();
	insert.value = i;	  
	
	while (!spotFound){
	    if (isBottom(current)){//If it is farthest to bottom
		if (current.right.isRightBoundary() ||
		    insert.value < current.right.value) {
		    
		    insert.right = current.right;
		    insert.left = current;
		    current.right.left = insert;
		    current.right = insert;	
		    
		    spotFound = true;
		} else if (insert.value == current.right.value){
		    return false; //The number already exists in the SkipList
		}else {
		    current = current.right;
		}
	    } else { //In the Skip levels (not the bottom)
		if (current.right.isRightBoundary() ||
		    current.right.value > insert.value){
		    //Skipping forward, passes where the insert should go
		    current = current.down;
		} else if (current.right.value == insert.value){
		    return false; //The number already exists in the SkipList
		} else { //current.right not R and current.right.val < insert.val
		    current = current.right;
		    //Skipping makes sense
		}
	    }		
	} //End the loop, reached the bottom, inserted
	
	  // Now go back up
	boolean done = false;
	Node leftBound = lowerLeft;
	Node rightBound = lowerRight;
	while (!done){
	    
	    if (isTop(leftBound)){				  
		//Add an upper boundary
		//6 Connections
		leftBound.up = new LeftBoundaryNode();
		rightBound.up = new RightBoundaryNode();
		leftBound.up.down = leftBound;
		rightBound.up.down = rightBound;				  
		leftBound.up.right = rightBound.up;
		rightBound.up.left = leftBound.up;
		root = leftBound.up;
		
		done = true;
	    } else {
		if (rng.nextBoolean()){//flip a coin			  
		    //Promote insert to another level
		    leftBound = leftBound.up;
		    rightBound = rightBound.up;
		    current = leftBound;
		    while (!current.right.isRightBoundary() && 
			   insert.value >current.right.value){
			current = current.right;
			//Traverse until the right is a boundary or less than insert.value
		    }
		    Node insert2 = new Node();
		    insert2.value = insert.value;//Clone the insert node;
		    
		    insert2.right = current.right;
		    insert2.left = current;
		    current.right.left = insert2;
		    current.right = insert2;	
		    insert.up = insert2;
		    insert2.down = insert;
		    
		    insert = insert2;			  
		    
		} else {
		    done = true;
		}
	    }
	}
	this.size++;
	return true;	  
	
    }
    
    public boolean isBottom(Node n){
	return n.down == null;
    }
    
    public boolean isTop(Node leftBound){
	if (leftBound==null){
	    return false;
	}
	
	if (!leftBound.isLeftBoundary()){
	    return false; //Should never happen when I call it 
	}
	
	return leftBound.up == null;
    }
    
    public Node findLeft(Node n){
	Node temp = n;
	while (!temp.isLeftBoundary()){
	    temp = temp.left;
	}
	return temp;
    }
    
    public Node findRight(Node n){
	Node temp = n;
	while (!temp.isRightBoundary()){
	    temp = temp.right;
	}
	return temp;
    }
  
    
    public String toString() {
	return root.toString();
    }
    
    
    // ------------------------------------------------------
    
    
    /**
   * Inner class representing a non-boundary Node of the skip list
   */
    private class Node {
	
	public int value;
	public Node left, right, up, down;
	
	public Node() {
	    left = right = up = down = null;
	    value = 0;
	}
	
	public boolean isLeftBoundary() {
	    return false;
	}
	
	public boolean isRightBoundary() {
	    return false;
	}
	
	/**
	 * Find the node with the largest value not exceeding i
	 */
	public Node find(int i) {
	    return null;
	}
	
	/**
	 * A simple string representation of the node.  Null Values don't appear
	 */
	public String toString() {
	    String s = "[" + this.value + " ";
	    if (up != null) {
		s = s + "U: " + up.value + " ";
	    } 
	    if (down != null) {
		s = s + "D: " + down.value + " ";
	    }
	    if (left != null) {
		s = s + "L: " + left.value + " ";		
	    }
	    if (right != null) {
		s = s + "R: " + right.value + " ";	
	    }
	    s = s + "]";
	    return s;
	}
    }
    
    /**
     * A node representing a left boundary
   */
    private class LeftBoundaryNode extends Node {
	
	public boolean isLeftBoundary() {
	    return true;
	}
	
	public String toString() {
	    String s = super.toString() + " -- ";
	    Node n = this.right;
	    while (!n.isRightBoundary()) {
		s = s + n.toString() + " -- ";
		n = n.right;
	    }
	    s = s + n.toString() + "\n";
	    if (this.down != null) {
		s = s + this.down.toString();
	    }
	    return s;
	}
    }
    
    /**
     * A node representing a right boundary
     */
    private class RightBoundaryNode extends Node {
	
	public boolean isRightBoundary() {
	    return true;
	}
	
    }
    
}
