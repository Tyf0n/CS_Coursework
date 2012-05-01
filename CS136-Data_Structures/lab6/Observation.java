import java.awt.Point;

/** Returned by Creature.look */
public final class Observation {

    /** Type of object at this location. */
    public Type      type;

    /** If type == CREATURE, this is the classname of that 
        creature. Otherwise, null.*/
    public String    className;

    /** If type == CREATURE, this is the ID of that creature.  Otherwise, 0. 
     */
    public int       id;

    /** If type == CREATURE, this is the direction that creature is facing. */
    public Direction direction;

    /** Point that was observed.*/
    public Point     position;

    /** Value returned from Creature.getTime() when this
        observation was made. */
    public int       time;       

    public Observation(int x, int y, int t) {
        position = new Point(x, y);
        type = Type.EMPTY;
        time = t;
    }

    public Observation(Point p, int t) {
        position = p;
        type = Type.EMPTY;
        time = t;
    }

    /** @param tm Time of observation */
    public Observation(Point p, Type t, int tm) {
        assert (t != Type.EMPTY) && (t != Type.CREATURE);
        position = p;
        type = t;
        time = tm;
    }

    /** @param tm Time of observation */
    public Observation(Point p, String c, int i, Direction dir, int tm) {
        position = p;
        type = Type.CREATURE;
        className = c;
        id = i;
        direction = dir;
        time = tm;
    }
}
