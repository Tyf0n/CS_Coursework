import java.awt.Point;
import java.util.Random;

/** Cardinal direction on a grid. */
public enum Direction {
    NORTH {
        public Direction left() { return WEST; }
        public Direction right() { return EAST; }
        public Direction opposite() { return SOUTH; }
        public Point forward(Point p, int n) { return new Point(p.x, p.y - n); }
        public int toInt() { return 0; }
    },

    WEST {
        public Direction left() { return SOUTH; }
        public Direction right() { return NORTH; }
        public Direction opposite() { return EAST; }
        public Point forward(Point p, int n) { return new Point(p.x - n, p.y); }
        public int toInt() { return 1; }
    },

    SOUTH {
        public Direction left() { return EAST; }
        public Direction right() { return WEST; }
        public Direction opposite() { return NORTH; }
        public Point forward(Point p, int n) { return new Point(p.x, p.y + n); }
        public int toInt() { return 2; }
    },

    EAST {
        public Direction left() { return NORTH; }
        public Direction right() { return SOUTH; }
        public Direction opposite() { return WEST; }
        public Point forward(Point p, int n) { return new Point(p.x + n, p.y); }
        public int toInt() { return 3; }
    };

    private static Random rand = new Random();

    /** Uniformly distributed random direction. */
    public static Direction random() {
        switch (rand.nextInt(4)) {
        case 0: return NORTH;
        case 1: return WEST;
        case 2: return SOUTH;
        case 3: return EAST;
        default: return NORTH;
        }
    }
    
    /** The direction 90-degrees to the left. */
    public abstract Direction left();

    /** The direction 90-degrees to the right. */
    public abstract Direction right();

    public abstract Direction opposite();

    /** The point one step forward in this direction. */
    public Point forward(Point p) { return forward(p, 1); }

    /** The point <i>n</i> steps forward in this direction. */
    public abstract Point forward(Point p, int n);

    public abstract int toInt();
}
