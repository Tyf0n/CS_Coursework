package mdpsSolution;

import java.util.Arrays;

import mdps.MDP;
import mdps.QValueFunction;

public class TableLookupQValueFunction implements QValueFunction {
    double[][]values;

    TableLookupQValueFunction(MDP mdp, double initialValue) {
        values = new double[mdp.numStates()][mdp.numActions()];
        for (int i = 0; i < mdp.numStates(); i++) {
            Arrays.fill(values[i], initialValue);
        }
    }
    public void updateValue(int state, int action, double value) {
        values[state][action] = value;
    }

    public double getValue(int state) {
        double[] actVals = values[state];
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < actVals.length; i++) {
            if (actVals[i] > maxVal)
                maxVal = actVals[i];
        }
        return maxVal;
    }

    public double getValue(int state, int action) {
        return values[state][action];
    }

    public String toString() {
        String str = "";
        for (int i = 0; i < values.length; i++) {
            str += Arrays.toString(values[i]);
        }
        return str;
    }
}
