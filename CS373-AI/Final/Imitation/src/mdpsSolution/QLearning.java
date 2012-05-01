package mdpsSolution;

import java.util.Random;
//
import mdps.MDP;
import mdps.QValueFunction;

public class QLearning {
    TableLookupQValueFunction valFunc;
    private MDP mdp;
    private double epsilon = 0.3;
    private double alpha = 0.2;
    private double gamma = 0.9;

    Random rand = new Random();

    QLearning(MDP mdp) {
        this.mdp = mdp;
        valFunc = new TableLookupQValueFunction(mdp, 0);
    }

    public int greedyAction(int state) {
        double maxVal = Double.NEGATIVE_INFINITY;
        int maxAct = 0;
        for (int i = 0; i < mdp.numActions(); i++) {
            double val = valFunc.getValue(state, i);
            if (val > maxVal) {
                maxVal = val;
                maxAct = i;
            }
        }
        return maxAct;

    }

    public int nextState(int state, int action) {
        double[] stateDistr = mdp.nextStateDistribution(state, action);
        double choiceIndex = rand.nextDouble();
        double sum = 0;
        for (int i = 0; i < stateDistr.length; i++) {
            sum += stateDistr[i];
            if (sum >= choiceIndex)
                return i;
        }
        // code should never reach this point
        return -1;

    }

    public void updateValues(int state, int action, int nextState) {
        double reward = mdp.getReward(state, action);
        // end the episode
        if (mdp.isTerminalState(state)) {
            double currentValue = valFunc.getValue(state, action);

            double newValue = (alpha)*currentValue + (1-alpha)*(reward);
            valFunc.updateValue(state, action, newValue);
        }else {
            double nextValue = valFunc.getValue(nextState);
            double currentValue = valFunc.getValue(state, action);

            double newValue = (alpha)*currentValue + (1-alpha)*(reward + gamma*nextValue);
            valFunc.updateValue(state, action, newValue);

        }


    }

    public int espilonGreedyStep(int currentState) {
        int action = greedyAction(currentState);
        double choiceIndex = rand.nextDouble();
        if (epsilon > choiceIndex)
            action = rand.nextInt(mdp.numActions());

        if (mdp.isTerminalState(currentState)) {
            updateValues(currentState, action, -1);
            return rand.nextInt(mdp.numStates());
        } else {
            int newState = nextState(currentState, action);
            updateValues(currentState, action, newState);
            return newState;
        }
    }

    public QValueFunction getValueFunctionReference() {
        return valFunc;
    }

    public MDP getMDP() {
        return mdp;
    }

}
