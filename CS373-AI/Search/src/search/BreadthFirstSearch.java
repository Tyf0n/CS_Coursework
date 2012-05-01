package search;

import java.util.*;

/**
 * Breadth First Search implemented with a Queue (Linked List).
 * Relies on GraphNode and State constructs
 * 
 * Substantial portion of this code was written by
 * Pippin in DLS search file and adapted for BFS
 *
 * CS 373 - AI
 * Assignment #1 - Search
 *
 * @author Joey Kiernan and Nathaniel Lim
 * @version 1.0
 * @date 2/18/10
 *
 */
public class BreadthFirstSearch implements Search {

	
	public BreadthFirstSearch() {}

	/**
	 * Implements the pathSearch method of the Search interface. 
	 * This implementation uses a queue
	 *
	 * @returns a list of states on the path to the goal or null if the
	 * goal was not found.
	 */
	public List<State> pathSearch(ProblemGraph graph, String startStateString) {
	    HashSet<GraphNode> visited = new HashSet<GraphNode>();

	    //Main queue to track progress
	    Queue<GraphNode> frontier = new LinkedList<GraphNode>();
	    GraphNode startNode = new GraphNode(graph.getState(startStateString));
	    frontier.add(startNode);
	    
	    //until all connected are explored
	    while(!frontier.isEmpty()){
		//Pop the first element off
			GraphNode current = frontier.poll();
			visited.add(current);
			if(current.getState().isGoal())
				return current.getPath();
			
			//and then add its children to the queue
			List<GraphNode> children = current.getState().expandNode();
			for(GraphNode childNode : children){
				
				if(!visited.contains(childNode)){
					childNode.setParent(current);
					frontier.add(childNode);
				}
			}
	    }
	    return null;
	    
	}

}
