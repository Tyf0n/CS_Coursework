import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/**
   Graphical display and management of Darwin Game tournaments.

   @sa Darwin for single matches and debugging.

   <pre>
     java Tournament <i>mapfile</i> Creature0 Creature1 ...
   </pre>

   If mapfile begins with "mz_" it is assumed to have one creature on it.  If
   mapfile begins with "ns_" it is assumed to have two creatures on it.

   <p>
   The Tournament class runs each trial with a fresh instance of the classes,
   intentionally reloading them from disk.  This prevents classes from
   sharing any information with themselves between runs using static variables.
 */
public class Tournament extends JFrame {
    
    /** Description of a creature */
    private static class Description implements Comparable {
        public String className;
        public String authorName;
        public Icon   icon;

        public int    bestTime = Integer.MAX_VALUE;
        public int    worstTime = 0;
        public int    meanTime = Integer.MAX_VALUE;

        public ArrayList<Integer> times = new ArrayList<Integer>();
        
        public Description(String cname) throws ClassNotFoundException, 
                                                InstantiationException, 
                                                IllegalAccessException,
                                                IOException {
            className = cname;
            Class c = Simulator.loadClass(className);
            assert c != null;
            Creature instance = (Creature)c.newInstance();
            authorName = instance.getAuthorName();

            icon = new ImageIcon(Simulator.getImage(c, Direction.SOUTH));
        }

        public int compareTo(Object obj) {
            Description that = (Description)obj;
            return this.meanTime - that.meanTime;
        }
        
        /** Run one trial and update this description with the result. */
        public void runTrial(String mapName) {
            try {
                System.out.println(className + " on " + mapName + ": ");
                Class[] creatures = {Simulator.loadClass(className)};
                Simulator simulator = new Simulator(mapName, creatures);

                // Launch the simulation
                simulator.setDelayTime(10000);

                // Run until completion.
                Simulator.Result result = simulator.getResult();
                while (result == null) {
                    try {
                        // Don't sleep too long; if we don't notice
                        // that the game has ended we might let it run
                        // for too long past the end, which affects 
                        // maze running timings.
                        Thread.sleep(25);
                    } catch (InterruptedException e) {}
                    result = simulator.getResult();
                }

                simulator.stop();

                addTime(result.timeSteps);

                if (result.species != creatures[0]) {
                    // Lose!
                }

                System.out.println(className + " completed " + mapName + " in " + result.timeSteps);
                
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /** Update times to reflect this change. */
        public void addTime(int time) {
            bestTime = Math.min(bestTime, time);
            worstTime = Math.max(worstTime, time);
            times.add(time);
            meanTime = 0;
            for (int t : times) {
                meanTime += t;
            }
            meanTime /= times.size();
        }
    }

    private Description[] creatureArray;
    private Description[] sortedCreatureArray;
    private String mapFilename;
    private ResultData resultData;
    private JProgressBar progressBar;
    private int currentTrial;
    private int numTrials;

    private static final int NUM_REPEATS = 8;

    private Tournament(String mapfile, String[] creatureClassNames) {
        super("Darwin Game Tournament - " + mapfile);
 
        System.setSecurityManager(new MaximumSecurityManager(new String[]{"readFileDescriptor"}));

        creatureArray = new Description[creatureClassNames.length];
        for (int i = 0; i < creatureArray.length; ++i) {
            try {
                
                creatureArray[i] = new Description(creatureClassNames[i]);

            } catch (Exception e) {

                System.err.println("Error while loading " + creatureClassNames[i] + ":\n" + e);
                e.printStackTrace();
                System.exit(-1);

            }
        }
        sortedCreatureArray = (Description[])creatureArray.clone();
        mapFilename = mapfile;

        // Must happen before GUI is created.
        currentTrial = 0;
        numTrials = NUM_REPEATS * creatureArray.length;

        makeGUI();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        runAllTrials(mapfile);
    }

    private void runAllTrials(String mapfile) {
        for (int i = 0; i < NUM_REPEATS; ++i) {
            for (Description d : creatureArray) {
                d.runTrial(mapfile);
                synchronized (Tournament.this) {
                    // Update the data
                    Arrays.sort(sortedCreatureArray);
                    resultData.fireTableChanged(new TableModelEvent(resultData));
                }
                ++currentTrial;
                progressBar.setValue(currentTrial);
            }
        }

        progressBar.setVisible(false);
        System.out.println("Trials complete");
    }


    private void makeGUI() {
        resultData = new ResultData();
        JTable resultTable = new JTable(resultData);

        resultTable.setRowHeight(70);
        resultTable.setShowHorizontalLines(true);
        resultTable.getColumnModel().getColumn(RANK_COL).setPreferredWidth(40);
        resultTable.getColumnModel().getColumn(AUTHOR_COL).setPreferredWidth(120);
        resultTable.getColumnModel().getColumn(IMAGE_COL).setPreferredWidth(45);

        DefaultTableCellRenderer timeRenderer = new DefaultTableCellRenderer();
        timeRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        timeRenderer.setFont(new Font("Monospaced", Font.PLAIN, 14));

        resultTable.getColumnModel().getColumn(BEST_COL).setCellRenderer(timeRenderer);
        resultTable.getColumnModel().getColumn(WORST_COL).setCellRenderer(timeRenderer);
        resultTable.getColumnModel().getColumn(MEAN_COL).setCellRenderer(timeRenderer);

        progressBar = new JProgressBar(currentTrial, numTrials);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        pane.add(new JScrollPane(progressBar), BorderLayout.PAGE_END);

        getContentPane().add(pane);
    }
    
    // For ResultData
    static final private int RANK_COL = 0;
    static final private int IMAGE_COL = 1;
    static final private int CREATURE_COL = 2;
    static final private int AUTHOR_COL = 3;
    static final private int BEST_COL = 4;
    static final private int WORST_COL = 5;
    static final private int MEAN_COL = 6;
    static private final String[] columnNames = 
    {"Rank", "Image", "Creature", "Author", "Best Time", "Worst Time", "Mean Time"};

    private class ResultData extends AbstractTableModel {
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public int getColumnCount() {
            return columnNames.length; 
        }

        public Class getColumnClass(int col) {
            if (col == IMAGE_COL) {
                return Icon.class;
            } else {
                return String.class;
            }
        }

        public int getRowCount() { 
            return creatureArray.length;
        }

        public Object getValueAt(int row, int col) {
            Description d = sortedCreatureArray[row];
            switch (col) {
            case RANK_COL:
                return "" + (row + 1);

            case CREATURE_COL:
                return d.className;

            case IMAGE_COL:
                return d.icon;

            case AUTHOR_COL:
                return d.authorName;

            case BEST_COL:
                if (d.bestTime == Integer.MAX_VALUE) {
                    return "";
                } else {
                    return "" + d.bestTime;
                }

            case WORST_COL:
                if (d.bestTime == Integer.MAX_VALUE) {
                    return "";
                } else {
                    return "" + d.worstTime;
                }

            case MEAN_COL:
                if (d.bestTime == Integer.MAX_VALUE) {
                    return "";
                } else {
                    return "" + d.meanTime;
                }
                
            }

            return "";
        }
    }

    public static void main(String[] arg) {
        String mapfile = arg[0];
        String[] creatures = new String[arg.length - 1];
        for (int c = 1; c < arg.length; ++c) {
            creatures[c - 1] = arg[c];
        }

        new Tournament(mapfile, creatures);
    }

    
}
