package mdpsSolution;

import mdps.MDP;
import mdps.QValueFunction;

public class ValueIteration {

    double gamma = 0.9;
    private MDP mdp;
    private TableLookupValueFunction valFunc;


    public ValueIteration(MDP mdp) {
        this.mdp = mdp;
        valFunc = new TableLookupValueFunction(mdp, 0, gamma);
    }

    /**
     * Do one "sweep" of value iteration
     * @param mdp
     * @param valFunc
     * @returns the maximum change to any single state's value function
     */
    public double updateValueFunction() {
    	double maxChange = Double.NEGATIVE_INFINITY;
        for (int s = 0; s < mdp.numStates(); s++) {
        	double maxValue = Double.NEGATIVE_INFINITY;
            for (int a = 0; a < mdp.numActions(); a++) {
                double[] nextStateDistribution = mdp.nextStateDistribution(s, a);
                double reward = mdp.getReward(s, a);
                double newValue = reward;
                for (int nextState = 0; nextState < mdp.numStates(); nextState++) {
                    double nextValue = valFunc.getValue(nextState);

                    newValue += gamma*nextStateDistribution[nextState]*nextValue;

                }
                if (newValue > maxValue)
                	maxValue = newValue;
            }
            maxChange = Math.max(Math.abs(maxValue - valFunc.getValue(s)), maxChange);
            valFunc.updateValue(s, maxValue);
        }
        return maxChange;

    }

    /**
     * Gets a reference to the value function. This value function will be updated as value iteration proceeds.
     * @return
     */
    public QValueFunction getValueFunctionReference() {
        return valFunc;
    }

}
