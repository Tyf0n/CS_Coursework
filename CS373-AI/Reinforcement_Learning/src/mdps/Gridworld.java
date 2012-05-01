package mdps;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class implements the MDP interface. It scans a text file to construct a gridworld,
 * with grid locations which an agent can navigate, using NORTH/SOUTH/EAST/WEST actions, and
 * obstacles.
 *
 * The format for gridworld files is:
 *
 * line 1: x dimension (integer)
 * line 2: y dimension (integer)
 * Then an x X y matrix of values, either "W" (for walls) or a real value representing the
 * immediate reward in each accessible state.
 *
 * For example:
 *
 * 3
 * 5
 * 0 0 W 0 4
 * W 0 0 0 0
 * -2 0 0 W 0
 *
 * @author pippin
 *
 */

public class Gridworld implements MDP {
	/**
	 * An x/y coordinate in the gridworld.
	 *
	 * @author pippin
	 *
	 */
    public class Coordinate {
        private int x, y;

        public Coordinate(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

    /**
     * For this assignment, you'll be using the SIDE noise type.
     * @author pippin
     *
     */
    public enum NoiseType {
        BASIC, SIDE;

    }

    /**
     * East, west, north, south actions. These are consistent with each other, but may not correspond with
     * up = north, etc, in the GUI.
     * @author pippin
     *
     */
    public enum GridworldAction {
        EAST(1, 0) {
            public GridworldAction getClockwiseAction() {return SOUTH;}
            public GridworldAction getCounterClockwiseAction() {return NORTH;}
        },
        SOUTH(0, -1){
            public GridworldAction getClockwiseAction() {return WEST;}
            public GridworldAction getCounterClockwiseAction() {return EAST;}
        },
        WEST(-1, 0){
            public GridworldAction getClockwiseAction() {return NORTH;}
            public GridworldAction getCounterClockwiseAction() {return SOUTH;}
        },
        NORTH(0, 1){
            public GridworldAction getClockwiseAction() {return EAST;}
            public GridworldAction getCounterClockwiseAction() {return WEST;}
        };


        private int xChange, yChange;

        private GridworldAction(int xChange, int yChange) {
            this.xChange = xChange;
            this.yChange = yChange;
        }

        public int getActionId() {
            return ordinal();
        }

        public abstract GridworldAction getClockwiseAction();

        public abstract GridworldAction getCounterClockwiseAction();
    }


    private int[] actionIds;
    private GridworldAction[] actions = GridworldAction.values();

    private boolean[][] wallMatrix;
    private double[][] rewardMatrix;

    private int numStates;

    private NoiseType noiseType = NoiseType.SIDE;
    private double noise = 0.2;

    private int[][] stateLabels;
    private ArrayList<Coordinate> stateList = new ArrayList<Coordinate>();
    private int xDim;
    private int yDim;

    /**
     * Reads in a gridworld from a file. See Gridworld class comments for file format.
     * @param fileName
     */
    public Gridworld(String fileName) {
        // read in the array of locations
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            StreamTokenizer token = new StreamTokenizer(reader);
            token.nextToken();
            xDim = (int) token.nval;
            token.nextToken();
            yDim = (int) token.nval;
            wallMatrix = new boolean[xDim][yDim];
            rewardMatrix = new double[xDim][yDim];
            for (int i = 0; i < xDim; i++) {
                for (int j = 0; j < yDim; j++) {
                    token.nextToken();
                    if (token.ttype == StreamTokenizer.TT_NUMBER) {
                        rewardMatrix[i][j] = token.nval;
                        wallMatrix[i][j] = false;
                    } else {
                        wallMatrix[i][j] = true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialize();
    }


    private void initialize() {
        actionIds = new int[actions.length];
        for (int i = 0; i < actions.length; i++) {
            actionIds[i] = actions[i].getActionId();
        }
        stateLabels = new int[xDim][yDim];
        int count = 0;
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                if(!wallMatrix[i][j]) {
                    stateLabels[i][j] = count;
                    stateList.add(new Coordinate(i, j));
                    count++;

                }
            }
        }
        numStates = count;
    }





    /*
     * the following methods implement the MDP interface.
     */

    /**
     * the number of actions available. All actions are available in all states.
     */
    public int numActions() {
        return actions.length;
    }

    /**
     * The number of states (non-wall locations).
     */
    public int numStates() {
        return numStates;
    }

    /**
     * The next state distribution for the given state and action. Actions are noisy, with errors resulting in
     * moves to the side (if the intended action is NORTH, noise may cause the agent to move EAST or WEST instead).
     *
     * If a location has a non-zero reward value, it is assumed to be a terminal state and the transition vector
     * consists entirely of zeros.
     *
     * This made it easier for me to implement ValueIteration, however, if you would prefer a different approach
     * you may comment out the terminal state handling lines below.
     */
    public double[] nextStateDistribution(int stateId, int actionNum) {
        double[] transitionRow = new double[numStates()];
        Arrays.fill(transitionRow, 0.0);
        Coordinate coords = getStateCoords(stateId);

        // if this is a terminal state, return immediately
        if (rewardMatrix[coords.getX()][coords.getY()] != 0)
            return transitionRow;

        int nextState = getNeighborState(coords, actions[actionNum]);
        transitionRow[nextState] = 1.0-noise;
        switch(noiseType) {
        case BASIC:
        {
            transitionRow[stateId] = (noise + transitionRow[stateId]);
            break;
        }
        case SIDE: {

            int cwState = getNeighborState(coords, actions[actionNum].getClockwiseAction());
            transitionRow[cwState] =  noise/2.0 + transitionRow[cwState];
            int ccwState = getNeighborState(coords, actions[actionNum].getCounterClockwiseAction());
            transitionRow[ccwState] = noise/2.0 + transitionRow[ccwState];
            break;
        }
        }
        return transitionRow;
    }

    /**
     * Find the state index of the state in the indicated direction from the original coordinates.
     * If the action is invalid (goes off the grid or into a wall), the state at the original coordinates is returned.
     * @param coords
     * @param action
     * @return
     */
    private int getNeighborState(Coordinate coords, GridworldAction action) {
        int nextX = coords.getX() + action.xChange;
        int nextY = coords.getY() + action.yChange;
        if (nextX < 0 || nextY < 0 || nextX >= xDim || nextY >= yDim || wallMatrix[nextX][nextY]) {
            nextX = coords.getX();
            nextY = coords.getY();
        }
        return getStateId(nextX, nextY);
    }


    /**
     * Get the state id corresponding to a particular coordinate location.
     * @param x
     * @param y
     * @return
     */
    protected int getStateId(int x, int y) {
        return stateLabels[x][y];
    }

    /**
     * Get the coordinates corresponding to a particular state label.
     * @param stateId
     * @return
     */
    public Coordinate getStateCoords(int stateId) {
        return stateList.get(stateId);
    }

    public String toString() {
        return wallMatrix.toString();
    }


    /**
     * Return a boolean matrix indicating the location of walls.
     * @return
     */
    public boolean[][] getWallMatrix() {
        return wallMatrix;
    }


    /**
     * Any state with a non-zero immediate reward value is considered to be a terminal
     * state in this MDP.
     */
    public boolean isTerminalState(int stateId) {
        Coordinate coords = getStateCoords(stateId);
        if (rewardMatrix[coords.getX()][coords.getY()] != 0)
            return true;
        else
            return false;
    }

    /**
     * Returns the immediate reward value specified by the input text file.
     */
    public double getReward(int state, int action) {
        Coordinate coord = getStateCoords(state);
        return rewardMatrix[coord.getX()][coord.getY()];
    }





}
