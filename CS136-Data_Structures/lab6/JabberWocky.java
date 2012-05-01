/**
 * Nathaniel Lim
 * Williams College CS136
 * Lab 7 4/7/2008
 * njl2@williams.edu
 */

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
/*
 * This extension of Creature, named JabberWocky uses the recursive strategy to 
 * traverse a maze.  It considers at every square where it can go, and then 
 * moves into each open square and repeats.  Each recursive call carries a HashSet
 * of the points its already visited. This way if it moves onto a Point that it has 
 * already visited, it traverse method returns (treating the loop as effectively a 
 * dead end.  After every move into a square and traversal the JabberWocky moves back
 * place where it was before traverse() was called.

 * This class also supports random choosing of paths at intersections. When it is not
 * choosing randomly, it searches in this priority order: Forward, Left, Right.    
 * This introduction of randomness does not 'break' the algorithm, since it touches every
 * place in the maze, but it does make the solution time variable. 
 */
public class JabberWocky extends Creature {
    private static final boolean randomMode = false;
    private HashSet<Point> nodes = new HashSet<Point>();
    //A node is a point that the JabberWocky has visited that it
    //only had more than one option at the intersection. 

    public void run(){	       
        HashSet<Point> visited = new HashSet<Point>();
        //Check if the JabberWocky is cornered from the start
        //This causes this algorithm to fail from the start
        //Because if it starts cornered, there is no where to 
        //go back to because it is the top level of recursion.
        //Hereby the JabberWocky can successfully start facing a dead end,
        //by correcting itself before is traverses the maze;

        if (!isLeftOpen() && !isRightOpen() && !isForwardOpen()){
            //turn 180
            turnRight();
            turnRight();
        }
        traverse(visited);                		    
    }
	
    public String getAuthorName(){
        return "Nathaniel Lim";
    }
    public String getDescription(){
        return  "A JabberWocky traverses a maze using the classic ternary tree structure and supports random order of going down the Forward, Left and Right paths.";
    }

    private boolean isGoalInFront(Observation o){
        return(o.type == Type.CREATURE && o.className.equals("Treasure"));		
    }
    
    private void traverse(HashSet<Point> visited) {	
        Observation o = this.look();
        Point here = this.getPosition();
        
        //Base Case (has priority over all other considerations
        //If there is a straight shot to the treasure, GO FOR IT!
        if (isGoalInFront(o)){
            int d = this.distance(o.position);
            //Assumes that nothing will jump in the way of the Jabberwocky
            for (int i = 0; i < (d - 1); i++){
                moveForward();
            }
            //Treasure should be right in front!
            attack();                           
        }
        
        if (visited.contains(here)){
            //Already Been here, consider it a dead end
            return;
        } 
        
        visited.add(here);
        
        //Check the surroundings
        //The only way to do this is to turn and look repeatedly
        // for the left and right. 
        boolean forwardOpen  = isForwardOpen();
        boolean leftOpen = isLeftOpen();
        boolean rightOpen = isRightOpen();
        
        // Is this point a node
        int numways= 0;
        if (forwardOpen){
            ++numways;
        }
        if (leftOpen){
            ++numways;
        }
        if (rightOpen){
            ++numways;
        }
        //Its a node!
        if (numways > 1){
            nodes.add(here);
        }
        //The following makes the JabberWocky completely traverse a maze
        //randomly.  This is done by creating picking off random paths orders at each
        //traversal
        //Numbers are associated as such
        //0: Forward, 1: Left, 2: Right
        //This is a normal order if randomMode == false
        ArrayList<Integer> waysOpen = new ArrayList<Integer>();
        if (forwardOpen){
            waysOpen.add(0);
        }
        if (leftOpen){
            waysOpen.add(1);
        }
        if (rightOpen){
            waysOpen.add(2);
        }
        
        //Randomly pick off one by one
        Random r = new Random();
        while (!waysOpen.isEmpty()){
            int wayIndex;
            if (randomMode){
                wayIndex = r.nextInt(waysOpen.size());
            } else {
                wayIndex = 0;
            }
            
            int way = waysOpen.remove(wayIndex);
            //Additions here
            Point pos = here;
            Direction d = getDirection();
            if (way == 0){
                forwardTraversal(visited, pos, d);
            } else if (way == 1){
                leftTraversal(visited, pos, d);
            } else if (way == 2) {
                rightTraversal(visited, pos, d);
            } else {
                System.out.println("There no way associated with this number: " + way);
            }
        }
               
    }
    
    /* The decision was made to go forward when returning to the position at which
     * the creature was at the beginning of the traversal.  It was better than 
     * moving backward all the time, because in a maze, there are often straight-aways
     * longer that 4 squares (4 being the threshold for it being less costly turn around
     * and go forward than moving backward x times).  
     *
     * This method looks spins around looking for the its original position.
     * If the original position is not a node, it doesn't bother with restoring
     * the original direction. If it is a node, then it restores the position so
     * that the JabberWocky can start performing the subsequent traversals facing
     * the direction that is expected.  Thus, the JabberWocky can handle going
     * back on straight paths, and going back on non branching paths, getting back the 
     * node that the traversal was originally called from.  
     *
     */
    private void moveBack(Point pos, Direction d){
        //System.out.println("Get back to:  " + pos + " From: " + getPosition());
        if (!getPosition().equals(pos)){
            while (!getMovePosition().equals(pos)){
                turnRight();
            }
            moveForward();
        }
        //Should have moved into pos
        
        //If the position was a node, make sure its in the direction it started.
        if (nodes.contains(pos)){
            while (!getDirection().equals(d)){
                turnRight();
            }
        }
    }

    //Helper Methods.
    private void forwardTraversal(HashSet<Point> visited, Point pos, Direction d){
        moveForward();
        traverse(visited);
        //   moveBackward();       
         moveBack(pos, d);              
    }
    private void leftTraversal(HashSet<Point> visited, Point pos, Direction d){
        turnLeft();
        moveForward();
        traverse(visited);
        //moveBackward();
        // turnRight();
    
        moveBack(pos, d);		
    }    
    private void rightTraversal(HashSet<Point> visited, Point pos, Direction d){
        turnRight();
        moveForward();
        traverse(visited);
        //moveBackward();
        //turnLeft();
        moveBack(pos, d);
     }
    
    private boolean isLeftOpen() {
        boolean out = false;
        turnLeft();
        Observation o = this.look();
        if (this.distance(o.position) > 1){
            out = true;
        }
        turnRight();
        return out;
    }
    
    private boolean isRightOpen() {
        boolean out = false;
        turnRight();
        Observation o = this.look();
        if (this.distance(o.position) > 1){
            out = true;
        }
        turnLeft();
        return out;
    }
    
    private boolean isForwardOpen() {
        boolean out = false;		
        Observation o = this.look();
        if (this.distance(o.position) > 1){
            out = true;
        }		
        return out;
    }
}
