package mdpsSolution;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import mdps.QValueFunction;

import mdps.Gridworld;
import mdps.ValueFunctionPanel;


class ValueIterationUpdateControls extends JPanel implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ValueIteration learner;
    private JButton stepButton = new JButton("Update");
    private JSlider slider = new JSlider(0, 50);
    private JLabel sliderValue = new JLabel();
    private JPanel display;

    public ValueIterationUpdateControls(ValueIteration learner, JPanel display) {
        this.learner = learner;
        //setLayout(new GridLayout(3, 1));
        add(stepButton);
        add(slider);
        add(sliderValue);
        stepButton.addActionListener(this);
        slider.setMajorTickSpacing(500);
        slider.setPaintLabels(true);
        this.display = display;
    }

    public void actionPerformed(ActionEvent event) {
        // the only action is the button being pressed, so perform that action
        int numIters = slider.getValue();
        for (int i = 0; i < numIters; i++) {
            learner.updateValueFunction();
        }
        System.out.println(learner.getValueFunctionReference());
        display.repaint();

    }

    @Override
    public void paint(Graphics graphics) {
        sliderValue.setText(""+ slider.getValue());
        super.paint(graphics);
    }
}

public class ValueIterationGUI {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Null Value Function Demo");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Gridworld gridworld = new Gridworld("gridworldTest.txt");
        ValueIteration learner = new ValueIteration(gridworld);
        QValueFunction valFunc = learner.getValueFunctionReference();
        JPanel display = new ValueFunctionPanel(gridworld, valFunc);
        frame.getContentPane().add(new ValueIterationUpdateControls(learner, display), BorderLayout.EAST);
        frame.getContentPane().add(display, BorderLayout.CENTER);

        frame.pack();

        frame.setVisible(true);
    }

}
