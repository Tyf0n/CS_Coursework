package search;
/**
 * A* Search implementation.
 *
 *Relies on implementation of an h() function
 *inside state to return a hueristic cost to the goal
 *
 * CS373 Assignment #1
 * @author: Joey Kiernan and Nathaniel Lim
 * @version: 1.2
 * @date: 2/19/10 
 */
import java.util.*;

public class AStarSearch implements Search{
   
   
    //This keeps track of the total cost
    //up to each node so far
    private Map<GraphNode, Integer> costs;
    
    public AStarSearch(){
	costs = new HashMap<GraphNode, Integer>();
    }

    
    /**
     *Takes a problem and a start configuration and solves for the goal state
     *using A* Search.
     *It will return a list of states (in reverse order) to the goal
     */
    public List<State> pathSearch(ProblemGraph graph, String startStateString){
	
	//frontier vector for keeping track of nodes to visit
	//We could think about making this  a priorityQueue
	//to get log(n) complexity
	Vector<GraphNode> frontier = new Vector<GraphNode>();
	GraphNode startNode = new GraphNode(graph.getState(startStateString));
	

	frontier.add(startNode);
	costs.put(startNode, 0);

	//Very similar to BFS
	//in fact, if we had made Frontier a PriorityQueue
	//the code would have looked almost identical and finding
	//the minNode wouldn't be quite so expensive
	while(!frontier.isEmpty()){

	    //find the next step
	    GraphNode minNode = frontier.get(0);
	    for(GraphNode node : frontier){
		if(pathCost(node) < pathCost(minNode)){
		    minNode = node;
		} 
	    }

	    
	    //take that step
	    frontier.remove(minNode);
	    
	    if(minNode.getState().isGoal()){
		return minNode.getPath();
	    }

	    //expand Node
	    List<GraphNode> children = minNode.getState().expandNode();
	    for(GraphNode childNode : children){
		if(!costs.containsKey(childNode)){
		    childNode.setParent(minNode);
		    frontier.add(childNode);
		    
		    //this assumes uniform cost
		    costs.put(childNode, costs.get(minNode)+1);
		    
		}
	    }
	    


	}
	return null;
	
    }


    /**
     *Returns the total estimated cost of taking any node
     *given the cost up to that node and that state's
     *hueristic function.
     */
    private int pathCost(GraphNode node){

	//should return cost(node) + hueristic
	return costs.get(node)+1 + node.getState().h();

    }
}