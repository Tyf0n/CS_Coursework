package search;

/**
 * Most of the functionality of the ProblemGraph will be implemented via the State interface. This
 * interface provides a method for constructing a single state in the graph given a state string.
 *
 * @author pippin
 *
 */
public interface ProblemGraph {
	/**
	 * Construct a State given a state string. Subclasses should define the format of the state string and specific
	 * subclasses of the State interface.
	 *
	 * @param stateString String representing a state. See the handout for the required string formats.
	 * @return An object that implements the State interface.
	 */
	public State getState(String stateString);
}
