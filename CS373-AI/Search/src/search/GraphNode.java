package search;

import java.util.LinkedList;
import java.util.List;

/**
 * A node in the search graph constructed by a search algorithm.
 * @author pippin
 *
 */
public class GraphNode {
	GraphNode parent = null;
	State state;

	/**
	 * Each node contains a state from the problem graph being searched.
	 *
	 * @param state a problem state
	 */
	public GraphNode(State state) {
		this.state = state;
	}


	/**
	 * Sets the parent node in the search graph (the node that was expanded to
	 * reach this node.
	 * @param parent the parent node
	 */
	public void setParent(GraphNode parent) {
		this.parent = parent;
	}


	/**
	 * Returns the corresponding state in the problem graph.
	 * @return a state
	 */
	public State getState() { return state; }


	/**
	 * Returns a the list of states on the path from the starting point to this
	 * node. This is typically called once a goal has been found, in which case
	 * this would be the path to the goal.
	 * @return A list of states
	 */
	public List<State> getPath() {
		LinkedList<State> list = new LinkedList<State>();
		addParentToPathList(list);
		return list;
	}


	/**
	 * Places the state of the parent in the path list, and recusively calls
	 * itself on the parent node.
	 *
	 * @param list a list of states visited on this search path, in order from
	 * leaf to root.
	 */
	private void addParentToPathList(List<State> list) {
		list.add(state);
		if (parent != null) {
			parent.addParentToPathList(list);
		}
	}

	/**
	 * I've used the default hash code implementation provided by Eclipse.
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	/**
	 * I've used the default equals implementation provided by Eclipse.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GraphNode other = (GraphNode) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	/**
	 * Prints out the state string.
	 */
	@Override
	public String toString() {
		return "" + state;
	}

	/**
	 * Checks to see if the specified node has been encountered on the path from the starting
	 * state to this graph node.
	 * @param childNode
	 * @return true if the childNode is a repeated node, false if it is not.
	 */

	public boolean visited(GraphNode childNode) {
		if (this.equals(childNode))
			return true;
		if (parent == null)
			return false;
		return parent.visited(childNode);
	}
}
