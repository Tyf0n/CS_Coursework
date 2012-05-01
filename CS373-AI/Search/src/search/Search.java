package search;

import java.util.List;

/**
 * Search algorithm interface.
 *
 * @author pippin
 *
 */
public interface Search {
	/**
	 * This method should be implemented in the subclasses - DLS, BFS and AStar - to
	 * find a solution path given a problem graph.
	 *
	 * @param graph State graph for the problem
	 * @param startStateString String representing a starting state
	 * @return a list of states representing a path to the goal, null if no such path is found.
	 */
	public List<State> pathSearch(ProblemGraph graph, String startStateString);
}
