import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
   Graphical display of the Darwin Game 2.0 simulator for single matches.

   Run with:
   <pre>
    java Darwin [-3D | -2D] <i>mapfile</i> Creature0 Creature1 ...
   </pre>

   e.g.,
   <pre>
    java Darwin -3D ns_arena.txt Rover Pirate
   </pre>

   @sa Tournament

   <p>Morgan McGuire
   <br>morgan@cs.williams.edu
   <br>http://graphics.cs.williams.edu
 */
public class Darwin extends JFrame {

    public final static String SYNTAX_HELP = 
        "java Darwin [-3D | -2D] mapname Class0 Class1 ...";

    private Simulator simulator;

    /** GUI elements for setting delay time. */
    private JToggleButton[] speedButton = new JToggleButton[4];

    final static private long MS = 1000000;
    /** Delay time in nanoseconds corresponding to each of the speedButtons. */
    final static private long[] delayTime = {100000 * MS, 200 * MS, 30 * MS, 1 * MS};
    
    final private JLabel timeDisplay = new JLabel("0");

    /** List of all current competitors */
    private ArrayList<Class> remainingCompetitors = new ArrayList<Class>();

    private Simulator.PopulationGraph populationGraph;

    private boolean announcedTimeLimit = false;
    private boolean announcedHalfTime = false;

    private Debugger debugger;
    private JFrame populationWindow;

    private String mapFilename;
    private String[] creatureClassNames;

    final JToggleButton view2DButton = makeToggleToolButton("2D.gif");
    final JToggleButton view3DButton = makeToggleToolButton("3D.gif");

    private JLabel display;

    public Darwin(boolean view3D, String mapFilename, String[] creatureClassNames) {
        this.mapFilename = mapFilename;
        this.creatureClassNames = creatureClassNames;

        debugger = new Debugger();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Display the simulator
        makeGUI();

        if (view3D) {
            view3DButton.setSelected(true);
        }

        System.setSecurityManager(new MaximumSecurityManager(new String[]{"readFileDescriptor"}));

        reload();

        // Render at most desiredFPS or once per time step
        float desiredFPS = 20;
        new java.util.Timer().schedule(new java.util.TimerTask() {
                public void run() {
                    tick();
                }}, 0, (int)(1000 / desiredFPS));
    }

    private synchronized void reload() {
        if (simulator != null) {
            if (simulator.isRunning()) {
                simulator.stop();
            }
            populationWindow.getContentPane().removeAll();
            simulator = null;
            display.setIcon(null);
        }

        lastTime = -1;
        remainingCompetitors.clear();
        Class[] creatureClasses = new Class[creatureClassNames.length];
        
        for (int i = 0; i < creatureClasses.length; ++i) {
            try {

                creatureClasses[i] = Simulator.loadClass(creatureClassNames[i]);
                
                if (! remainingCompetitors.contains(creatureClasses[i])) {
                    remainingCompetitors.add(creatureClasses[i]);
                }
            } catch (Exception e) {
                System.err.println("Warning: while loading " + creatureClassNames[i]);
                e.printStackTrace();
                creatureClasses[i] = null;
            }

        }

        simulator = new Simulator(mapFilename, creatureClasses);
        setTitle("The Darwin Game - " + simulator.getTitle());

        simulator.setView3D(view3DButton.isSelected());
        display.setIcon(simulator);

        populationGraph = simulator.getPopulationGraph();

        populationWindow.getContentPane().add(populationGraph);
        populationWindow.pack();

        speedButton[0].setSelected(true);
    }

    /** Reports the simulator version number. */
    static public String getVersion() {
        return Simulator.getVersion();
    }

    private int lastTime = -1;
    private void tick() {
        if ((simulator == null) || ! simulator.isRunning()) {
            // The simulator is not live
            return;
        }

        int t = simulator.getTime();
        if (t == lastTime) {
            // Nothing has happened since the last step
            return;
        } else {
            synchronized (this) {
                lastTime = t;
            }
        }

        timeDisplay.setText("" + t);

        repaint();

        debugger.tick();
        populationGraph.tick();

        maybeAnnounceTime(t);

        Simulator.Result result = simulator.getResult();
        
        if (result != null) {
            // Game over!
            simulator.stop();

            // Pause:
            speedButton[0].setSelected(true);
            simulator.setDelayTime(delayTime[0]);

            say(result.result + " " + result.why);

            // Show dialog
            String message = result.result + "\n" + result.why + "\nin " + result.timeSteps + " time steps";
            if (result.icon == null) {
                JOptionPane.showMessageDialog(this, message, result.result, JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, message, result.result, JOptionPane.PLAIN_MESSAGE, 
                                              new ImageIcon(result.icon));
            }
            return;
        }

        // Run elimination messages after checking for a winner so that we don't announce a final elimination
        maybeAnnounceElimination();
    }

    private void maybeAnnounceTime(int t) {
        if (! announcedHalfTime && (t > Simulator.TIME_STEP_LIMIT * 0.5)) {
            say("Half-time!");
            announcedHalfTime = true;
        }

        if (! announcedTimeLimit && (t > Simulator.TIME_STEP_LIMIT * 0.9)) {
            say("Time limit approaching!");
            announcedTimeLimit = true;
        }
    }

    /** Check to see if a species was eliminated. */
    private void maybeAnnounceElimination() {
        for (int i = 0; i < remainingCompetitors.size(); ++i) {
            Class c = remainingCompetitors.get(i);
            int n = simulator.getCreatureCount(c);
            if (n == 0) {
                say(c.getName() + " was eliminated.");
                remainingCompetitors.remove(i);
                --i;
            }
        }
    }


    /** Speak text out loud on OS X */
    static private void say(String message) {
        boolean osx = System.getProperty("os.name").toUpperCase().contains("MAC OS X");
        if (osx) {
            try { Runtime.getRuntime().exec("say -v Zarvox \"" + message + "\""); } catch (Exception e) {}
        }
    }
        
    /** Called from constructor. */
    private void makeGUI() {
        Container pane = getContentPane();
        
        JToolBar controls = new JToolBar("Darwin Controls");

        JButton reloadButton = makeToolButton("Redo24.gif");
        controls.add(reloadButton);
        controls.addSeparator(new Dimension(24, 24));
        reloadButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        reload();
                    }});

        {
            view2DButton.setSelected(true);
            view3DButton.setSelected(false);
            ActionListener L = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        simulator.setView3D(view3DButton.isSelected());
                        Darwin.this.repaint();
                    }};
            view2DButton.addActionListener(L);
            view3DButton.addActionListener(L);
            ButtonGroup group = new ButtonGroup();
            group.add(view2DButton);
            group.add(view3DButton);
            
            controls.add(view2DButton);
            controls.add(view3DButton);
        }

        controls.addSeparator(new Dimension(24, 24));

        {
            ButtonGroup speedGroup = new ButtonGroup();
            ActionListener L = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        for (int i = 0; i < speedButton.length; ++i) {
                            if (speedButton[i].isSelected()) {
                                simulator.setDelayTime(delayTime[i]);
                            }
                        }
                    }};
            for (int i = 0; i < 4; ++i) {
                speedButton[i]  = makeToggleToolButton("speed" + i + ".gif");
                speedButton[i].addActionListener(L);
                speedGroup.add(speedButton[i]);
                controls.add(speedButton[i]);
            }
            speedButton[0].setSelected(true);
        }

        controls.addSeparator(new Dimension(24, 24));
        JButton debuggerButton = makeToolButton("Information24.gif");
        debuggerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    debugger.setVisible(true);
                }});
        controls.add(debuggerButton);

        {
            populationWindow = new JFrame("Population");
            populationWindow.setAlwaysOnTop(true);
            populationWindow.setVisible(false);

            final JButton viewPopGraph = makeToolButton("Graph.gif");
            viewPopGraph.setSelected(false);
            ActionListener L = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        populationWindow.setVisible(true);
                    }};
            viewPopGraph.addActionListener(L);
            controls.add(viewPopGraph);
        }


        controls.addSeparator(new Dimension(40, 24));
        JButton helpButton = makeToolButton("Help24.gif");
        helpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog
                        (Darwin.this, 
                         getVersion() + "\n\nWilliams College\n" + 
                         "http://cs.williams.edu/~morgan/cs136/darwin2.0\n\n" +
                         SYNTAX_HELP, "About The Darwin Game", 
                         JOptionPane.PLAIN_MESSAGE, new ImageIcon("Flytrap-E.png"));
                }});
        controls.add(helpButton);
        
        
        controls.addSeparator(new Dimension(24, 24));

        Font fixedFont = new Font("Monospaced", Font.PLAIN, 16);
        timeDisplay.setFont(fixedFont);
        controls.add(new JLabel("Elapsed Time:"));
        controls.add(timeDisplay);


        pane.setLayout(new BorderLayout());
        pane.add(controls, BorderLayout.PAGE_START);
        
        // Map view in the center
        display = new JLabel();
        pane.add(display, BorderLayout.CENTER);
        display.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    click(e.getX(), e.getY());
                }});

        setSize(1024, 768);
    }

    /** Called when the user clicks on the display */
    private void click(int x, int y) {
        if (simulator.getView3D()) {
            // In 3D view; no click allowed
            return;
        }

        Point offset = simulator.getGridDrawXY2D();
        x = (x - offset.x) / Simulator.SCALE2D;
        y = (y - offset.y) / Simulator.SCALE2D;
        
        debugger.setCreature(simulator.getCreature(x, y));
    }

    static protected JToggleButton makeToggleToolButton(String icon) {
        JToggleButton b = new JToggleButton(new ImageIcon(icon));
        b.setSize(24, 24);
        return b;
    }

    static protected JButton makeToolButton(String icon) {
        JButton b = new JButton(new ImageIcon(icon));
        b.setSize(24, 24);
        return b;
    }

    public static void main(String[] arg) {
        if (arg.length < 1) {
            // Default arguments
            System.out.println(getVersion());
            System.out.println();
            System.out.println("Syntax:");
            System.out.println("     " + SYNTAX_HELP);
            System.out.println();

            String[] s = {"-3D", "ns_faceoff.txt", "Rover", "Pirate"};
            arg = s;
        }
        
        int a = 0;
        boolean view3D = false;
        if (arg[a].toUpperCase().equals("-3D")) {
            view3D = true;
            ++a;
        } else if (arg[a].toUpperCase().equals("-2D")) {
            view3D = false;
            ++a;
        }

        String mapname = arg[a];
        ++a;

        String[] creatureClassNames = new String[arg.length - a];
        System.arraycopy(arg, a, creatureClassNames, 0, creatureClassNames.length);

        new Darwin(view3D, mapname, creatureClassNames).setVisible(true);
    }

    private class Debugger extends JFrame {
        private Creature  creature;
        private JLabel    authorLabel;
        private JLabel    classLabel;
        private JLabel    idLabel;
        private JTextArea descriptionDisplay;
        private JLabel    iconLabel;

        private JLabel    positionLabel;

        private JTextArea stringDisplay;

        public Debugger() {
            super("Creature Debugger");

            setAlwaysOnTop(true);

            JPanel pane = new JPanel();
            getContentPane().add(new JScrollPane(pane));

            pane.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.FIRST_LINE_START;

            iconLabel = new JLabel();
            
            c.gridx = 0; c.gridy = 0;

            classLabel = new JLabel();
            pane.add(new JLabel("Class:"), c); ++c.gridx;
            pane.add(classLabel, c); ++c.gridx;
            c.gridheight = 4;
            c.anchor = GridBagConstraints.LINE_END;
            pane.add(iconLabel, c);
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.gridheight = 1;

            c.gridx = 0; ++c.gridy;
            idLabel = new JLabel();
            pane.add(new JLabel("ID:"), c); ++c.gridx;
            pane.add(idLabel, c);

            c.gridx = 0; ++c.gridy;
            c.gridwidth = 1;
            authorLabel = new JLabel();
            pane.add(new JLabel("Author:"), c); ++c.gridx;
            pane.add(authorLabel, c);

            c.gridx = 0; ++c.gridy;
            c.gridwidth = 1;
            positionLabel = new JLabel();
            pane.add(new JLabel("Location:"), c); ++c.gridx;
            pane.add(positionLabel, c);

            int W = 40;
            int H = 33;

            c.gridx = 0; ++c.gridy; c.gridwidth = 1;
            descriptionDisplay = new JTextArea(3, W/2);
            descriptionDisplay.setEditable(false);
            descriptionDisplay.setLineWrap(true);
            descriptionDisplay.setWrapStyleWord(true);
            pane.add(new JLabel("Description:"), c); ++c.gridx;
            c.gridwidth = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            pane.add(new JScrollPane(descriptionDisplay), c);
            c.fill = GridBagConstraints.NONE;

            c.gridx = 0; ++c.gridy; c.gridwidth = 1;
            stringDisplay = new JTextArea(H, W);
            stringDisplay.setFont(new Font("Monospaced", Font.PLAIN, 11));
            pane.add(new JLabel("toString():"), c);  ++c.gridx;
            c.gridwidth = 2;
            pane.add(new JScrollPane(stringDisplay), c);
            stringDisplay.setEditable(false);

            c.gridx = 1; ++c.gridy; c.gridwidth = 2;
            pane.add(new JLabel("Click on a creature in 2D mode to debug"), c);
            pack();
            setSize(new Dimension(380, 565));
        }

        public synchronized void setCreature(Creature c) {
            creature = c;
            simulator.setSelectedCreature(c);

            if (c != null) {
                setTitle("Debugger - " + c.getClassName() + " " + c.getId());
                
                classLabel.setText(c.getClassName());
                idLabel.setText("" + c.getId());
                authorLabel.setText(c.getAuthorName());
                descriptionDisplay.setText(c.getDescription());
                Image im = simulator.getImage(c, Direction.SOUTH);
                if (im == null) {
                    iconLabel.setIcon(null);
                } else {
                    iconLabel.setIcon(new ImageIcon(im));
                }
                tick();

                setVisible(true);
            } else {
                setTitle("Debugger");
                classLabel.setText("");
                idLabel.setText("");
                authorLabel.setText("");
                descriptionDisplay.setText("");
                stringDisplay.setText("");
                positionLabel.setText("");
                iconLabel.setIcon(null);
            }
        }

        public synchronized Creature getCreature() {
            return creature;
        }

        public synchronized void tick() {
            if (creature != null) {
                stringDisplay.setText(creature.toString());

                Point pos = simulator.getPosition(creature);
                Direction dir = simulator.getDirection(creature);
                if (pos != null && dir != null) {
                    positionLabel.setText("(" + pos.x + ", " + pos.y + ") " + dir);
                } else {
                    positionLabel.setText("Not alive.");
                }
            }
        }
    }

}
