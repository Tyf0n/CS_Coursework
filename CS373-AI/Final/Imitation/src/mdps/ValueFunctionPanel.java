package mdps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class displays a given value function in a panel.
 *
 * I've included a main method to illustrate how the class is used - you will need to create two other classes
 * (QLearningGUI and ValueIterationGUI) that incorporate this panel along with buttons and input
 * widgets controlling how many steps of Q Learning or iterations of Value Iteration are executed between
 * displays.
 *
 * Values are draw in shades of green (positive values) and red (negative values) scaled according to the
 * maximum value in any state.
 *
 * @author pippin
 *
 */
public class ValueFunctionPanel extends JPanel {
    Gridworld gridworld;
    QValueFunction valueFunc;
    private JLabel[][] labels;

    public ValueFunctionPanel(Gridworld gridworld, QValueFunction valueFunc) {
        super();
        this.gridworld = gridworld;
        this.valueFunc = valueFunc;

        // labels in a grid - each label corresponds to a state.
        boolean[][] wallMatrix = gridworld.getWallMatrix();
        setLayout(new GridLayout(wallMatrix.length, wallMatrix[0].length));
        labels = new JLabel[wallMatrix.length][wallMatrix[0].length];
        for (int i = 0; i < wallMatrix.length; i++) {
            for (int j = 0; j < wallMatrix[0].length; j++) {
                labels[i][j] = new JLabel(" ");
                add(labels[i][j]);
            }
        }
    }

    public double maxCurrentValue() {
        if (valueFunc == null)
            return 1.0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < gridworld.numStates(); i++) {
            for (int j = 0; j < gridworld.numActions(); j++) {
                if (valueFunc.getValue(i, j) > max)
                    max = valueFunc.getValue(i, j);
            }
        }
        return max;
    }


    @Override
    public void paint(Graphics graphics) {
        boolean[][] wallMatrix = gridworld.getWallMatrix();
        double maxValue = maxCurrentValue();
        for (int i = 0; i < wallMatrix.length; i++) {
            for (int j = 0; j < wallMatrix[0].length; j++) {
                labels[i][j].setText(" ");
                labels[i][j].setOpaque(true);
                if (!wallMatrix[i][j]) {
                    float value;
                    if (valueFunc != null) {
                    	// scale the value
                        value = (float) (valueFunc.getValue(gridworld.getStateId(i, j))/maxValue);
                        if (value > 0)
                        	// positive values are green
                            labels[i][j].setBackground(new Color(1-value, (float)1-(value/2), 1-value));
                        else
                        	// negative values are red
                            labels[i][j].setBackground(new Color((float)1+(value/2), 1+value, 1+value));
                    }
                } else {
                	// walls/obstacles are black
                    labels[i][j].setText("W");
                    labels[i][j].setBackground(new Color(0,0,0));

                }
            }
        }

        super.paint(graphics);
    }

    public static void main(String[] args) {
    	if (args.length < 1) {
    		System.out.println("One argument is required: the filename for the Gridworld text file.");
    		return;
    	}

        JFrame frame = new JFrame("Null Value Function Demo");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Gridworld gridworld = new Gridworld(args[0]);
        frame.getContentPane().add(new ValueFunctionPanel(gridworld, null), BorderLayout.CENTER);

        frame.pack();

        frame.setVisible(true);
    }
}
