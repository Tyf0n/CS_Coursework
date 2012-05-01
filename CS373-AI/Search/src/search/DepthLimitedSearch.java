package search;

import java.util.List;

/**
 * Depth first search with a limit on the search depth. This version of depth first search
 *  is a "tree search" style algorithm implemented using recursion. It does check for loops,
 *  by avoiding visiting the same state more than once on a single search path from the root to
 *  the bottom of the search recursion, however, it can visit states more than once if they
 *  occur on multiple search paths.
 *
 * @author pippin
 *
 */
public class DepthLimitedSearch implements Search {
	private int limit = 0; // depth limit

	/**
	 * Constructor
	 *
	 * @param limit: the depth limit at which the search will halt
	 */
	public DepthLimitedSearch(int limit) {
		this.limit = limit;
	}

	/**
	 * Implements the pathSearch method of the Search interface. This implementation is
	 * recursive and depth limited.
	 *
	 * @returns a list of states on the path to the goal, or null, if the goal
	 * was not found within the given depth limit.
	 */
	public List<State> pathSearch(ProblemGraph graph, String startStateString) {

		GraphNode startNode = new GraphNode(graph.getState(startStateString));
		if (startNode.getState().isGoal())
			return startNode.getPath();
		return expandNode(startNode, 0);
	}

	/**
	 * Recursively expands a single node in the graph.
	 *
	 * @param node: the node being recursively expanded
	 * @param depth: maximum search depth
	 * @return a list of states on the path to the goal, or null if the goal was not found
	 * on this particular branch of the search within the given depth.
	 */
	private List<State> expandNode(GraphNode node, int depth) {
		// if depth has passed the limit, return null
		if (depth >= limit)
			return null;

		// get children
		List<GraphNode> children = node.getState().expandNode();
		for (GraphNode childNode : children) {
			// if the next child has already been visited on this specific search path, skip it
			if (node.visited(childNode))
				continue;
			childNode.setParent(node);

			// check for goal - return parent chain from goal node if found.
			if (childNode.getState().isGoal())
				return childNode.getPath();

			// if not a goal, expand each child, setting the parent
			List<State> childResult = expandNode(childNode, depth+1);

			// if the childResult is null, keep searching. Otherwise, return the
			// solution found.
			if(childResult != null)
				return childResult;
		}
		// no more children to check - bottom of the recursion
		return null;
	}

}
