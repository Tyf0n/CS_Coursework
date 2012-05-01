/*
 * Nathaniel Lim
 * 4/29/08
 * Darwin 2.0 Part 2 Lab
 * CS136 Williams College
 * njl2@williams.edu
 */

import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.*;
import java.awt.Point;
import java.awt.Dimension;
import java.util.ArrayList;
import java.lang.Boolean;

public class Chimera extends Creature {

    /*
     * The idea behind Chimera is to exist in two forms, one form which
     * moves around sort of like a Pirate does, and another form which moves
     * like a sheep (clumping together).  This design choice relies on the global
     * understanding of the map only for the positions of other instances of Chimera in
     * order to promote clumping
     * I chose to do this because, the understanding of where the enemies are is transient
     * and what is known about where other guys are is current.
     * 
     * Clumped up Creatures tend to do better as they can cover each other
     * But they don't get anywhere if they clump up all the  time, so
     * some other Chimeras go explore.  
    */

    
    //Static Variables shared by all instances of SmartCreature
    public static ObservationArray2D map;
    public static AtomicInteger  numAlive = new AtomicInteger(0);    
    public static AtomicInteger numScouts = new AtomicInteger(0);
    public static AtomicInteger numSheep = new AtomicInteger(0);
    public static Boolean hasMadeMap = new Boolean(false);

    //Constants
    public static final double SHEEP_TO_SCOUTS = 1.7;
    public static final double DELTA = 0.3;
    public static final int SHEEP = 1;
    public static final int SCOUT = 2;
    public static final int NOTHING = 0;  
    
    //Instance Variables
    private Stack<Point> lastVisited = new Stack<Point>();
    private boolean isScout = false;  
    

    public String getAuthorName() {	
        return "Nathaniel Lim";
    }

    public String getDescription() {
        return "Chimera's strategy is to either be a scout, which wanders around looking for things, or be a sheep which tries to clump up with other Chimeras ";		
    }
	

    /*
     * Figures out what type of Chimera needs to be added in order to
     * maintain a certain ratio of sheep to scouts
     */
    public int populationAssay(){
        int sheep = numSheep.get();
        int scouts = numScouts.get();
        if (scouts == 0){
            return SCOUT;
        } else if ( sheep == 0){
            return SHEEP;
        } else {
            double sheepDouble = (double)sheep;
            double scoutsDouble = (double)scouts;
            //System.out.println("Sheep: " + sheepDouble + " Scouts: " + scoutsDouble);
            if ( (sheepDouble/scoutsDouble) > (SHEEP_TO_SCOUTS + DELTA) ){
                //need more scouts
                return SCOUT;
            } else if ( (sheepDouble/scoutsDouble) < (SHEEP_TO_SCOUTS - DELTA)) {
                //need more sheep				
                return SHEEP;
            } else {
                return NOTHING;
            }
        }
    }    
	

    public void addScout(){
        isScout = true;
        numScouts.getAndAdd(1);	
    }
	
    public void addSheep(){
        isScout = false;
        numSheep.getAndAdd(1);
    }
    
    public void run(){        
        synchronized(hasMadeMap){
            if (map == null) {
                map = new ObservationArray2D(getMapDimensions(), getClass().getName());
            }
        }
        update();        
        numAlive.getAndAdd(1);		
        
        //What should this Chimera spawn as?
        int decision = populationAssay();		
        if(decision == SHEEP){
            addSheep();
        } else if (decision == SCOUT){
            addScout();
        } else {
            Random r = new Random();
            if (r.nextInt(2) == 0){
                addScout();
            } else {
                addSheep();
            }
        }
        
        
        try{
            while (true){
                //Help maintain population ratio
                //The Chimera can change its character
                //depending on the population of scouts and 
                //sheep.  Does nothing if decision==NOTHING                
                decision = populationAssay();				
                if (decision == SHEEP) {
                    if (isScout){
                        //Convert Self to a Sheep
                        synchronized(numSheep) {
                            isScout = false;
                            numScouts.getAndAdd(-1);
                            numSheep.getAndAdd(1);					
                        }							
                    }				
                } else if (decision == SCOUT){
                    if (!isScout){  //Its a sheep
                        //Convert Self to a Scout
                        synchronized(numSheep) {
                            isScout = true;
                            numSheep.getAndAdd(-1);
                            numScouts.getAndAdd(1);					
                        }							
                    }                    
                }
                //End of population management
                
                
                //Scouts and Sheep act differently                
            	if (isScout){
                    scout();            	
                } else {
                    sheep();
                } 
									               
            } 
        }  catch (ConvertedError e){
            //Conversion occured
            synchronized(map){
                map.set(getPosition(), new Observation(getPosition(), "ENEMY",  0, Direction.EAST,  getTime()));
            }            
        } finally{
            //Regardless of how the creature died/stopped running
            //The population must be atomically decremented.
            numAlive.getAndAdd(-1);
            if (isScout){
                numScouts.getAndAdd(-1);
            } else {
                numSheep.getAndAdd(-1);
            }
            
        }
    }    
	
	
    public void scout(){
        //Is aggressive and does a certain amount of exploration
        
        Observation o = look();
        int d = distance(o.position);
        
        if (isEnemy(o)){
            //If its right in front, attack() !!!
            if (o.position.equals(getMovePosition())){
                attack();
                turn90Random();
            } else {
                //Move towards the enemy this turn,
                //if something jumps in front, attack it!
                if(!moveForward()){
                    attack();
                    turn90Random();
                }
            }
        } else {			
            if (o.position.equals(getMovePosition())){
                //Obstacle
                turn90Random();
            } else {
                wander(d);				
            }
        }
    }
	               
    public void sheep(){	
        //Tries to stick together, as well as attack stuff
        
        //Gets Information about whats in front
        //and where friends are.		
        Observation o = look();  
        int d = distance(o.position);		
        ArrayList<Observation> friends = new ArrayList<Observation>();
        synchronized(map){
            friends = map.getFriends(getPosition());
        }	
        Point p = avgPosition(friends);
	
         if (isEnemy(o)){
            //If its right in front, attack() !!!
            if (o.position.equals(getMovePosition())){
                attack();
                turn90Random();
            } else {
                //Move towards the enemy this turn,
                //if something jumps in front, attack it!
                if(!moveForward()){
                    attack();
                    turn90Random();
                }
            }
         } else if (!isFriend(o) && o.position.equals(getMovePosition())){
             //obstacle
             turn90Random(); 
         }else if(isFriend(o)){
            // see a friend in front, go to them
            if (d > 1){
                if(!moveForward(d)){
                    attack();
                    turn90Random();
                }
            } else {
                //Next to friends, look for enemies
                turn180();
            }

         } else if (p!= null && distance(p) > 4){
            //This Chimera is far from its buddies
            //turn towards the average position of its
            //buddies, go forward attacking anything
            // that gets in its way
             turn(p);
             o = look();
             if (o.position.equals(getMovePosition())){
                 if (isEnemy(o)){
                     attack();
                 }
                 turn90Random();                 
             } else {
                 moveForward();
             }            
         } else {
             wander(d);
         }
    }
   
	
    private void wander(int d) {
        //1/10 of the time it turns
        //Rest of the time it moves a random
        //Number of times forward
        //to the obstacle
        Random r = new Random();
        int i = r.nextInt(10);
        if(i == 0){
            turn90Random();
        } else {
            int spaces = 1;
            if ( (d-2) > spaces){
                spaces = d-2;
            }
          
            if(!moveForward(  r.nextInt(spaces) + 1  )){
                attack();
                turn90Random();
            }
        }
    }
    
	
    private Point avgPosition(ArrayList<Observation> things) {
        int xSum = 0;
        int ySum = 0;
        
        if (things == null || things.size() == 0){
            return null;
        }
        
        for (Observation o: things) {
            xSum+=o.position.x;
            ySum+= o.position.y;
        }
        
        return new Point(xSum/things.size(), ySum/things.size());
    }    
    
    private Observation closestObservation(ArrayList<Observation> things) {
        Observation out = null;
        int testDistance = Integer.MAX_VALUE;
        
        for (Observation o : things){
            int d = distance (o.position);
            if ( d < testDistance){
                out = o;
                testDistance = d;
            }
        }		
        return out;		
    }   

	
	
    private boolean isFriend(Observation o){
        if (o.type == Type.CREATURE && o.className.equals(getClass().getName())){
            return true;
        } else {
            return false;
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
        int x2 = p.y;
        int y2 = p.x;
        
        int x1 = here.y;
        int y1 = here.x;
        
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
     * Returns the global understanding of the map
     * Static var: map
     * (Should be the same for all instances) 
     */
    public String toString(){        
        synchronized(map){
           return map.toString();
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
        


        private ArrayList<Observation> getFriends(Point here) {
            ArrayList<Observation> out = new ArrayList<Observation>();
            for (ArrayList<Observation> row : map){
                for (Observation obv : row){
                    if (obv != null &&
                        obv.type == Type.CREATURE && 
                        obv.className.equals(myName) &&
                        !obv.position.equals(here)){

                        out.add(obv);
                    }
                }
            }    
            return out;      
        }
        
        public void set(Observation obs){
            Point p = obs.position;
            set(p, obs);
        }
        
        private ArrayList<Observation> getApplesAndEnemies(){
            //Iterate Through the map to get all wanted things
            ArrayList<Observation> out = new ArrayList<Observation>();
            for (ArrayList<Observation> row : map){
                for (Observation obv : row){
                    if (isAppleOrEnemy(obv)){
                        out.add(obv);
                    }
                }
            }    
            return out;        
        }
            
        private boolean isAppleOrEnemy(Observation o){
            if (o == null){
                return false;
            }
            if (o.type == Type.CREATURE){
                String ident = o.className;
                if (ident.equals(myName)){
                    return false;
                } else if (ident.equals( "Apple")){
                    return true;
                } else if (ident.equals("Flytrap")){
                 return false;
                } else {
                    //Must be an enemy
                    return true;         
                }  
            }
            return false;
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
