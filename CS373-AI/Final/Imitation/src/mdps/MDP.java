package mdps;

public interface MDP {

    int numStates();
    int numActions();

    // the next state distribution. If s' is the numerical index of a given next state,
    // then nextStateDistribution(s, a)[s'] is the probability of that state given s, a.
    // P(s' | s, a)
    double[] nextStateDistribution(int state, int action);
    // returns the immediate reward for this state
    double getReward(int state, int action);

    // should the current episode terminate when the agent reaches this state?
    public boolean isTerminalState(int stateId);

}
