/*
 * Nathaniel Lim
 * 4/21/08
 * Darwin 2.0 Part 1 Lab
 * CS136 Williams College
 * njl2@williams.edu
 */

import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.Point;
import java.awt.Dimension;
import java.util.ArrayList;
import java.lang.Boolean;

public class SmartCreature extends Creature {
    
    //Static Variables shared by all instances of SmartCreature
    public static ObservationArray2D map;
    public static AtomicInteger  numAlive = new AtomicInteger(0);
    public static Boolean hasMadeMap = new Boolean(false);

    public Stack<Point> lastVisited = new Stack<Point>();
    
    public String getAuthorName() {
        return "Nathaniel Lim";
    }
    public String getDescription() {
        return  " SmartCreature have a global understanding of the playing " +
            "field by looking around and recording Observations of items " +
            "in a static map variable. There are issues concerning the synchronizing " +
            "of the operations to the map, so that operations in one thread are not" +
            "interrupted by other threads.";		
    }
    
    public void run(){
        
        synchronized(hasMadeMap){
            if (map == null) {
                map = new ObservationArray2D(getMapDimensions(), getClass().getName());
           }
        }

        update();
        numAlive.getAndAdd(1);
        
        try{        
            while (true){
            	//Implement a strategy using SmartCreature framework 
            	strategy();            	
            }                      
        } catch (ConvertedError e){
            //Conversion occured
            synchronized(map){
               map.set(getPosition(), new Observation(getPosition(), "ENEMY",  0, Direction.EAST,  getTime()));
            }            
        } finally{
            //Regardless of how the creature died/stopped running
            //The population must be atomically decremented.
            numAlive.getAndAdd(-1);
        }
    } 

    
    /*
     * Override this method for extending SmartCreature
     * making it utilize its brain power.
     * 
     * This simple strategy does not utilize the global
     * understanding of the map
     */
    private void strategy() {
        Observation o = look();    	
        //Turns around from thorns or Flytraps
    	//Attack if its an Apple, Enemy, 
    	if (o.type == Type.CREATURE && 
            !o.className.equals(getClass().getName())){
            if (!o.className.equals("Flytrap")) {
                int d = this.distance(o.position);
                if (d == 1){
                    attack();
                } else {

                    if (moveForward(d-1)){
                        attack();
                    }  else {	
                        attack();
                        turn180();
                    }
                }
            }
    	}   	    	
    	if(o.type == Type.THORN){
            turn180();
    	}    	
    	Random r = new Random();
    	int i = r.nextInt(2);
    	if (i == 0){
            if (! moveForward()){
                turn90Random();
            }
    	} else {
            turn90Random();
    	}        
    }
    
    
    
    public void turn180(){
        turnRight();
        turnRight();
    }
    
    //Does nothing if this is facing d already
    public void turn(Direction d){
        Direction current  = getDirection();
        if(current.opposite() == d){
            turn180(); 
        } else  if (current.left() == d){
            turnLeft();
        } else if( current.right() == d){
            turnRight();
        }      
    }    

    public void turn90Random(){
        Random r = new Random();
        int x = r.nextInt(2);// 0 or 1
        if(x == 0){
            turnLeft();
        } else {
            turnRight();
        }
    }
    
    
    /*
     * Returns false if it didn't move n spaces
     * True if it was unimpeded.
     * If it was impeded, it stops trying to move
     * forward.
     */
    public boolean moveForward(int n){
        boolean moved = false;
        for (int i = 0; i < n; i++) {
            moved = moveForward();
            if (!moved){
                break;
            }
        }
        return moved;
    }
    
    /*
     * Figures out which axis[ (N-S) or (E-W) ] has the greatest 
     * distance to be traveled on to get closer to p, and \
     * determines which way to point towards p.
     */
    public Direction directionTo(Point p){
        Point here = getPosition();        
        int x2 = p.x;
        int y2 = p.y;
        
        int x1 = here.x;
        int y1 = here.y;
        
        int dx = x2-x1;
        int dy = x2-x1;
        
        if (Math.abs(dx) > Math.abs(dy)){
            if (dx > 0){
                return Direction.EAST;
            } else {
                return Direction.WEST;
            }
        } else {
            if (dy > 0){
                return Direction.SOUTH;
            } else {
                return Direction.NORTH;
            }
        }
    }
    
    //Turn to the direction that will
    //get SmartCreature to p.
    public void turn(Point p) {
        turn(directionTo(p));
    }
    
    public int getPopulation(){
        return numAlive.intValue();
    }

    public void update(){
        //	System.out.println("Num Alive: " + numAlive.intValue());
    	Point here = getPosition();
    	Point last = null;
        if (!lastVisited.isEmpty()){
            last = lastVisited.peek();
        }        
        if (!here.equals(last)){
            
            //Add the current position to the Stack of last visited points
            lastVisited.push(here);
            
            //This insures that the multiple Runnable SmartCreatures on the map
            //do not try to update their understanding of the map at the same time.
            //This prevents problems with writing to the same spot at the same time.
            
            synchronized(map){
                //Set the spot that the creature moved out of to be empty
                if (last!= null){
                    map.set(new Observation(last, Type.EMPTY, getTime()));
                }    		
                //Set the spot the creature is currently on to an Observation of itself
                //System.out.println("Width: " + map.map.size() + " Height: " + map.map.get(0).size());
                map.set(here, new Observation(here, map.myName, getId(), getDirection(), getTime()));
            }
            
        }    	
        //If the creature didn't move, there is no new information, no update needed.
        //        System.out.println(this);
    }
    
    /*
     * These moving methods are overridden to 
     * update the understanding of the map after
     * each move;
     */      
    public boolean  moveForward(){
        boolean b = super.moveForward();
        update();
        return b;
    }
    
    public void turnLeft(){
        super.turnLeft();
        update();
    }

    public void turnRight(){
        super.turnRight();
        update();
    }

    /*
v     * Returns the global understanding of the map
     * Static var: map
     * (Should be the same for all instances) 
     */
    public String toString(){
        //change this

        String out = "" + numAlive.intValue();
        synchronized(map){
            return out +="\n"+ map.toString();
        }
    }

    public synchronized Observation recall(Point p){                
        Observation o = map.get(p);
        return o;        
    }
    
    
   
    protected synchronized Observation look() {
        //Although no other instance of SmartCreature can look
        //while another is looking, one SmartCreature could be 
        //looking while another is updating, potentially doing 
        //operations on the map at the same time, leaving the threat
        //of interruption. Therefore, within this method, map operations
        //must be synchronized.
        Observation o = super.look();		
        synchronized(map){
            map.set(o);			
        }		
        //The spaces between the SmartCreature instance and the position
        //of the observed object are all empty.  And these "observations"
        //occurred at the time time as o
        int space = 1;
        while (!(this.getMovePosition(space).equals(o.position))){
            Point spotOn = this.getMovePosition(space);
            synchronized(map){
                map.set(spotOn, new Observation(spotOn, Type.EMPTY, o.time));
            }
            space++;
        }		
        return o;
    }
    
    //General Helper Class  for a 2D Map
    protected static class Array2D<T>{
        ArrayList<ArrayList<T>>  map;
       
        
        public Array2D(Dimension d){
            map = new ArrayList<ArrayList<T>>();
            for (int i = 0; i < d.height; i ++) {
                ArrayList<T> row  = new ArrayList<T>();
                for (int j = 0; j < d.width; j++){
                    row.add(null);
                }               
                map.add(row);
            }
           
        }
        
        public T get(Point p){
            return get(p.x, p.y);
        }
        
        public T get(int x, int y){
            return (map.get(y)).get(x);
        }
        
        public void set(int x, int y, T v){
            (map.get(y)).set(x, v);
        }
        
        public void set (Point p, T v){
            set(p.x, p.y, v);
        }
        
        public int getWidth(){
           return (map.get(0)).size();
        }
        
        public int getHeight(){
            return map.size();
        }
        
        public boolean inBounds(Point p){
            return inBounds(p.x, p.y);
        }
        
        public boolean inBounds(int x, int y) {
            return (x < getWidth() && x >= 0) &&
                (y < getHeight() && y >=0);
        }
        
        public String toString(){
            
            String out = "";
            for (ArrayList<T> row : map){
                for (T item: row){
                    out+= toString(item);
                }
                out+= "\n";
            }
            return out;
        }
        
        protected  String toString (T t) {
            return t.toString();
        }
        
    }
    
    //Helper Class: Extension of Array2D, for Observations
    protected static class ObservationArray2D extends Array2D<Observation>{
        
        private String myName;
        
        public ObservationArray2D(Dimension d, String myClassName){
            super(d);
   
            myName = myClassName;
        }
        
        public void set(Observation obs){
            Point p = obs.position;
            set(p, obs);
        }
        
        protected String toString(Observation  obs) {
            if (obs == null){
            	return "?";
            }
            if (obs.type == Type.EMPTY){
                return " " ;
            } else if (obs.type == Type.WALL){
                return "X";
            } else if (obs.type == Type.CREATURE){
            	String ident = obs.className;
                if (ident.equals( myName)){
                    return "m";
            	} else if (ident.equals( "Apple")){
                    return "a";
                } else if (ident.equals("Flytrap")){
                    return "f";
                } else {
                    return "c";                   
            	}            		
            } else {
            	return "?";
            }
        }
        
    }
    
}
