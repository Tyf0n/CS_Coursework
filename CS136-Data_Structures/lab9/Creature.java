import java.awt.Point;
import java.awt.Dimension;

/** 
 Extend this class to create your own Creature.
 
 <p>Override the {@link #run} method to implement the Creature's
 AI. Within that method the following actions are available (each of
 which takes some time steps to execute):
 <ul>
   <li> {@link #moveForward}
   <li> {@link #moveBackward}
   <li> {@link #turnLeft}
   <li> {@link #turnRight}
   <li> {@link #attack}
   <li> {@link #look}
 </ul>
</p>

 <p>Example:
<pre>
<font color=888888>
public class Rover extends Creature {
    public void run() {
           while (isAlive()) {
</font>
                if (! moveForward()) {
                    attack();
                    turnLeft();
                }
<font color=888888>
            }
    }
 }
</font></pre>
</p>


<p>Each creature executes in its own thread.  This means that you must
 be very careful when using static fields to always used threadsafe
 classes or to access them through synchronized accessor methods.</p>
 
 <p>Morgan McGuire
 <br>morgan@cs.williams.edu</p>
 */
// Most methods that will be called by subclasses are synchronized.
// This is intended to help prevent a Creature that spawns Threads
// from corrupting itself.
public abstract class Creature implements Entity, Runnable {

    private int id;

    private Simulator simulator;

    /** Cached to avoid locking the simulator too frequently*/
    private Point position;

   /** Cached to avoid locking the simulator too frequently*/
    private Direction direction;

    /** Called by creature operations to enforce the time cost of
        those operations. Can also be called to make a creature sit
        still for "n time steps". */
    final protected void delay(int n) {
        simulator.delay(n);
    }

    final public Type getType() {
        return Type.CREATURE;
    }

    /** Returns the number of time steps since the simulation started.
        Creatures can call this even after they have been converted.
    */
    final public int getTime() {
        return simulator.getTime();
    }

    /** Subclass constructors must not invoke any of the parent class
        methods from their constructor.  Instead, perform
        initialization at the beginning of the run() method. */
    protected Creature() { }

    /** Name of this species of creature. */
    final public String getClassName() {
        return getClass().getName();
    }

    /** Allows GUI browsers to display your name as author of this creature.*/
    public String getAuthorName() {
        return "Anonymous";
    }

    /** Allows GUI browsers to display information and credits about your creature.*/
    public String getDescription() {
        return "Override getDescription to display information about your Creature subclass.";
    }

    /** Returns the size of the map.  Fast. */
    public Dimension getMapDimensions() {
        return simulator.getDimensions();
    }

    /** Each creature has a unique number that it can use to
        distinguish itself from others.  The id is not valid until
        run() is called on the creature; do not reference it from the
        constructor.*/
    final public int getId() {
        return id;
    }

    /** Create an observation describing this creature */
    public Observation observeSelf() {
        return new Observation(getPosition(), getClassName(), getId(), getDirection(), getTime());
    }

    /** The coordinates of the next position this Creature will enter
      if it moves <i>n</i> times, regardless of whether that position is
      currently empty. (fast)*/
    protected Point getMovePosition(int n) {
        return getDirection().forward(getPosition(), n);
    }

    /** Same as {@link #getMovePosition} with the argument <code>n = 1</code> */
    protected Point getMovePosition() {
        return getMovePosition(1);
    }

    /** Returns true if this observation describes a Creature that is 
        not of this species */
    protected boolean isEnemy(Observation obs) {
        assert obs != null;
        return 
            (obs.type == Type.CREATURE) && 
            (! obs.className.equals(getClassName()));
    }

    /** Returns the manhattan distance from current position to p2. */
    protected int distance(Point p2) {
        Point p = getPosition();
        return distance(p, p2);
    }

    /** Returns the manhattan distance between p1 and p2 */
    static protected int distance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    /** Uses the pointer hashCode from Object.*/
    final public int hashCode() {
        // Prevent users from overriding hashcode, which is used by
        // the simulator.
        return super.hashCode();
    }

    /** Uses the pointer comparision from Object. */
    final public boolean equals(Object ob) {
        // Prevent users from overriding equals, which is used by
        // the simulator.
        return super.equals(ob);
    }

    final public char getLabel() {
        return getClassName().charAt(0);
    }
    
    /** Override this method to make your creature think and move.
        Executes as soon as the creature is injected into the world.
        Your creature will stop moving when the run method ends (but
        stay alive), so most implementations will be an intentional
        infinite loop.  If you want to know when your creature has
        been converted, test {@link isAlive} or attempt an action and
        catch the {@link ConvertedError}.
    */
    public void run() {
    }

    /** Returns the position of this Creature.  This is fast. If the Creature is dead,
        throws Error.*/
    final public synchronized Point getPosition() {
        if (position == null) {
            position = simulator.getPosition(this);
        }
        if (position == null) {
            throw new ConvertedError("Creature " + getClassName() + "_" + getId() + 
                                     " can't find its position because it has been converted.");
        }
        return position;
    }

    /** Direction this creature is facing.  This is fast. If the
        creature is dead, throws Error.*/
    final public synchronized Direction getDirection() {
        if (direction == null) {
            direction = simulator.getDirection(this);
        }
        if (direction == null) {
            throw new ConvertedError("Creature " + getClassName() + "_" + getId() + 
                                     " can't find its direction because it is dead.");
        }
        return direction;
    }

    /** Returns true if this creature is alive.  A creature that is
        not alive should allow its run method to exit. */
    final public synchronized boolean isAlive() {
        return simulator.isAlive(this);
    }

    /** Prints a point to a string concisely. */
    static public String toString(Point point) {
        return "(" + point.x + ", " + point.y + ")";
    }

    /** The simulation calls this on the creature when it is first added
        to the world.  Do not invoke this yourself. */
    final void setSimulator(Simulator s, int _id) {
        if (simulator != null) {
            throw new IllegalArgumentException("Cannot invoke setSimulation twice.");
        }
        simulator = s;
        id = _id;
        getPosition();
        getDirection();
    }

    /** Call to move your creature forward 1 square.  If the creature
        is blocked, it will not move. Moving takes about {@link Simulator#MOVE_FORWARD_COST} time steps.

        @return true if the creature successfully moved forward.*/
    synchronized protected boolean moveForward() {
        Direction d = getDirection();
        delay(simulator.MOVE_FORWARD_COST);
        
        // Our old position will become stale
        boolean r = simulator.move(this, 1);
        if (r && position != null) {
            position = d.forward(position);
        }
        return r;
    }

    /** Call to move your creature backward 1 square without changing
        its facing direction.  If the creature is blocked, it will not
        move. Moving takes about {@link Simulator#MOVE_BACKWARD_COST}
        time steps.

        @return true if the creature successfully moved forward.*/
    synchronized protected boolean moveBackward() {
        Direction d = getDirection().opposite();

        delay(simulator.MOVE_BACKWARD_COST);

        // Our old position will become stale
        boolean r = simulator.move(this, -1);
        if (r && position != null) {
            position = d.forward(position);
        }
        return r;
    }

    /** Look forward.  Returns a description of the first non-empty
        square observed, which may be any distance away.

        <p>Takes about {@link Simulator#LOOK_COST} time steps.
        The result of the look is accurate at the end of the 
        delay, but of course whatever is seen might have moved
        by the time the creature actually makes its response. */
    synchronized protected Observation look() {
        delay(simulator.LOOK_COST);
        return simulator.look(this);
    }

    /** Rotate counter-clockwise 90 degrees.  This takes {@link Simulator#TURN_COST} time
        steps. */
    synchronized protected void turnLeft() {
        delay(simulator.TURN_COST);
        simulator.turnLeft(this);
        if (direction != null) {
            direction = direction.left();
        }
    }

    /** Rotate clockwise 90 degrees.  This takes {@link Simulator#TURN_COST} time steps. */
    synchronized protected void turnRight() {
        delay(simulator.TURN_COST);
        simulator.turnRight(this);
        if (direction != null) {
            direction = direction.right();
        }
    }

    /** Attack the creature right in front of you.  If there is a
        creature of a different species present in that spot, that
        creature will be destroyed and a new creature of the same type
        as this one will be created in its place.  The new creature
        will face in the opposite direction as this one; i.e., they will
        be face to face.

        <p>Whether there is a creature present or not, this takes
        about {@link Simulator#ATTACK_COST} time steps.  The actual
        attack occurs at the <b>beginning</b> the delay.

        @return true if the attack succeeded. 
    */
    synchronized protected boolean attack() {
        boolean result = simulator.attack(this);
        delay(simulator.ATTACK_COST);
        return result;
    }
}


