package search;

import java.util.List;

/**
 * Interface for a State.
 *
 * @author pippin
 *
 */
public interface State {
	/**
	 * Generate the children of the State, using the set of actions available in the state.
	 *
	 * @return A list of child states, generated using the action set of the problem instance.
	 */
	public List<GraphNode> expandNode();

	/**
	 * Goal test for a single state.
	 *
	 * @return whether or not this state is a goal state for its problem instance.
	 */
	public boolean isGoal();
    
    /**
     *Returns the estimated distance to goal
     */
    public int h();
}
