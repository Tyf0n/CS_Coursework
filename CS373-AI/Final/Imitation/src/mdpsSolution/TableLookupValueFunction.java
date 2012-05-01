package mdpsSolution;

import java.util.Arrays;

import mdps.MDP;
import mdps.QValueFunction;

public class TableLookupValueFunction implements QValueFunction {
    double[]values;
    MDP mdp;
	private double gamma;

    TableLookupValueFunction(MDP mdp, double initialValue, double gamma) {
        values = new double[mdp.numStates()];

        Arrays.fill(values, initialValue);
        this.mdp = mdp;
        this.gamma = gamma;
    }
    public void updateValue(int state, double value) {
        values[state] = value;
    }

    public double getValue(int state) {
        return values[state];
    }

    public double getValue(int state, int action) {
    	double value = mdp.getReward(state, action);
		double[] nextStateDistr = mdp.nextStateDistribution(state, action);
    	for (int i = 0; i < mdp.numStates(); i++) {
    		value += gamma * values[i] * nextStateDistr[i];
    	}
    	return value;
    }

    public String toString() {
        String str = "";
        for (int i = 0; i < values.length; i++) {
            str += Arrays.toString(values);
        }
        return str;
    }
}
