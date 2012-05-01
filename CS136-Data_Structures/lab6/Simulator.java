import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
   Darwin 2.0 simulator.  Executes the simulation on a set of
   Creatures.

   <p>Maps are ASCII files. The first line must contain two integers
   (width and height of the map) separated by spaces and terminated by
   a newline (anything else on that line is ignored).  The remaining
   lines are a picture of the map.  The elements are:

   <ul>
    <li> ' ' empty square
    <li> 'X' wall ('#' alternative color)
    <li> '+' thorns
    <li> 'a' for Apples (which are Creatures)
    <li> 'f' for Flytraps (which are Creatures)
    <li> '0'...'9' spawn locations of Creature subclasses
   </ul>

   The array passed to the Simulator constructor indicates which
   subclass should be loaded for each spawn point.  The outer edge of
   the map is forced to wall blocks regardless of whether it is
   specified that way or not.
   </p>

   <p>Simulator implements Icon so that it can be rendered.  It can
   also be displayed in text mode in text mode using toString().</p>

   <p>To complete the lab assignment, you do not need to read the
   source code for this class, or understand how it works.</p>

   <p>Morgan McGuire
   <br>morgan@cs.williams.edu
   </p>

   <p><i>Inspired by the RoboRally board game, Steve Freund's "Darwin"
   lab assignment, and Nick Parlante's "Darwin's World"
   assignment.</i></p>
 */
public final class Simulator implements javax.swing.Icon {
    static public final int LOOK_COST          = 1;
    static public final int ATTACK_COST        = 4;
    static public final int TURN_COST          = 3;
    static public final int MOVE_FORWARD_COST  = 2;

    // turn + turn + move = 8
    // backward = 4
    // turn + turn + move + move = 10
    // backward + backward = 8
    // turn + turn + move + move + move = 12
    // backward + backward + backward = 12
    /** Note: at moving backwards 3*/
    static public final int MOVE_BACKWARD_COST = 4;

    /** The game ends after this many time steps, even if no Creature
        has won. */
    static public final int TIME_STEP_LIMIT = 25000;

    /** Map title */
    private String title;
    
    /** Dimensions of the map */
    private int width;
    private int height;

    /** If true, paintIcon renders a 3D view */
    private boolean view3D = true;
    
    /** Positions of Creatures in map. */
    private Map<Creature, Information> infoTable = new HashMap<Creature, Information>();

    private Entity[][] map;

    private int nextID = new Random().nextInt(100000);

    /** Time cost of each fundamental operation, in milliseconds.*/
    private int delayTime = 10000000;

    private TimeKeeper timeKeeper;

    /** Number of each type of creature present in the world. */
    private Map<Class, Integer> creatureCount = new HashMap<Class, Integer>();

    /** Drawn highlighted */
    private Creature selectedCreature;

    /** Reports the version of this simulator. */
    static public String getVersion() {
        return "The Darwin Game 2.0 Beta 5";
    }

    /** Loads the specified map, instantiating each of the creatures
       for the numbered spots in the map. */
    public Simulator(String mapFilename, Class[] creatures) {
        assert creatures != null;

        prepareColors();

        try {

            Reader reader = new BufferedReader(new FileReader(mapFilename));
            // Make the file reader so that we can back up after reading
            // the first line.
            reader.mark(2000);
            Scanner scanner = new Scanner(reader);
        
            width = scanner.nextInt();
            height = scanner.nextInt();
            title = scanner.nextLine().trim();
            if (title.length() == 0) {
                title = mapFilename;
            }
            
            map = new Entity[width][height];

            loadMap(reader, creatures);
            timeKeeper = new TimeKeeper(this);
            new Thread(timeKeeper, "Time Keeper").start();

        } catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }

    /** Name of this map */
    public String getTitle() {
        return title;
    }

    /** Returns the approximate number of time steps that have elapsed
        since the simulation began. */
    public int getTime() {
        return timeKeeper.getTime();
    }

    /** Wait the delayTime * n. 
        If delaytime is changed while in delay.
     */
    // This must *not* be synchronized, since it makes one
    // creature sleep while others execute.
    public void delay(int n) {
        try {

            // Sleep in relatively small increments to ensure
            // that the system is responsive to sleep time changes
            // when it is paused or running slowly.

            long timePassed = 0;
            long timeLeft = (long)getDelayTime() * n - timePassed;
            while (timeLeft > 0) {
                // Never sleep more than 100ms or less than 1ms
                long sleepTime = Math.min(Math.max(1, timeLeft), 100);

                Thread.sleep(sleepTime);
                timePassed += sleepTime;
                
                // We must update this every time around the loop because
                // the delay time may have changed while we were sleeping.
                timeLeft = (long)getDelayTime() * n - timePassed;
            }
        } catch (InterruptedException e) {
            System.err.println("Interupted during delay");

            // The Creature is likely trying to cheat by interrupting
            // the thread during the delay. Restart a full delay in
            // retaliation (this will cause a stack overflow if it
            // keeps interrupting, crashing the Creature.)
            delay(n);
        }
    }

    /** Returns the time of delay(1) in milliseconds. Initially huge,
     * so that the simulator is "paused". */
    public synchronized int getDelayTime() {
        return delayTime;
    }

    public synchronized void setDelayTime(int t) {
        delayTime = t;
    }

    /** If true, the view renders in 3D */
    public void setView3D(boolean b) {
        view3D = b;
    }

    public boolean getView3D() {
        return view3D;
    }

    public String toString() {
        String s = title + " ("  + width + " x " + height + ")\n";
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Entity e = map[x][y];
                if (e == null) {
                    s += ' ';
                } else {
                    s += e.getLabel();
                }
            }
            s += '\n';
        }
        return s;
    }

    /** Called from the constructor. */
    private void loadMap(Reader reader, Class[] creatures) {
        // Now read directly from the file
        try {
            // Skip the first line
            reader.reset();
            readToEndOfLine(reader);

            // Force walls around outside
            for (int x = 0; x < width; ++x) {
                map[x][0] = map[x][height - 1] = StaticEntity.wall;
            }

            readToEndOfLine(reader);
            for (int y = 1; y < height - 1; ++y) {
                readMapLine(reader, creatures, y);
            }
            readToEndOfLine(reader);
        } catch (IOException e) {
            // Done!
        }
    }

    /** Read one line from the map file. Called from loadMap. */
    private void readMapLine(Reader reader, Class[] creatures, int y) throws IOException {
        // Most recently read character
        int c = '\n';
        map[0][y] = StaticEntity.wall;
        map[width - 1][y] = StaticEntity.wall;

        // Ignore the first character on the line.
        c = reader.read();
        for (int x = 1; x < width - 1; ++x) {
            c = reader.read();

            if (c == '\n') {
                // End of this line; abort the for loop
                break;
            } else if ((c == 'X') || (c == 'x')) {
                // Wall
                map[x][y] = StaticEntity.wall;
            } else if (c == '#') {
                // Alternative color wall
                map[x][y] = StaticEntity.wall2;
            } else if (c == '+') {
                // Thorn
                map[x][y] = StaticEntity.thorn;
            } else if (c == 'a') {
                // Apple
                spawn(Apple.class, new Point(x, y), Direction.random());
            } else if (c == '*') {
                // Treasure
                spawn(Treasure.class, new Point(x, y), Direction.random());
            } else if (c == 'f') {
                // Flytrap
                spawn(Flytrap.class, new Point(x, y), Direction.random());
            } else if (Character.isDigit(c)) {
                // Creature
                int i = c - '0';
                if (creatures.length > i) {
                    Class creatureClass = creatures[i];

                    if (creatureColor.get(creatureClass) == null) {
                        // Set up a unique color for this creature
                        creatureColor.put(creatureClass, colorStack.pop());
                    }

                    if (creatureClass != null) {
                        spawn(creatureClass, new Point(x, y), Direction.random());
                    } else {
                        System.err.println("Warning: creature #" + i + " could not be instantiated.");
                    }
                    
                } else {
                    System.err.println("Warning: ignored unspecified creature #" + i + " in map.");
                }
            }
            // Anything else should be left null; it is an empty spot
        }
        
        // Read to the end of the line, discarding additional characters
        if (c != '\n') {
            readToEndOfLine(reader);
        }
    }

    /** Throws IOException if the end of file is reached. */
    static private void readToEndOfLine(Reader reader) throws IOException {
        int c = 0;
        while (c != '\n') {
            c = reader.read();
            if (c == -1) {
                throw new IOException();
            }
        }
    }

    /** Called by a creature to turn */
    public synchronized void turnRight(Creature e) {
        checkThread(e);
        Information info = getInformation(e);
        info.direction = info.direction.right();
    }

    public synchronized void turnLeft(Creature e) {
        checkThread(e);
        Information info = getInformation(e);
        info.direction = info.direction.left();
    }

    public synchronized Observation look(Creature e) {
        checkThread(e);
        Information info = getInformation(e);

        Point p = info.position;
        Observation obs = null;
        do {
            // Advance to the new position (this guarantees
            // that p is a new Point as well)
            p = info.direction.forward(p);

            if (! inBounds(p)) {
                return new Observation(p, Type.WALL, getTime());
            } else {
                obs = observe(p);
            }

        } while (obs == null);

        return obs;
    }

    private Observation observe(Point p) {
        if (inBounds(p)) {
            Entity e = map[p.x][p.y];
            if (e == null) {
                // Empty
                return null;
            } else if (e instanceof Creature) {
                Creature c = (Creature)e;
                Information info = infoTable.get(c);
                return new Observation(p, c.getClassName(), c.getId(), info.direction, getTime());
            } else if (e instanceof StaticEntity) {
                return new Observation(p, e.getType(), getTime());
            } else {
                assert false : "Internal error; unknown Entity type: " + e;
                return null;
            }
        } else {
            // Out of bounds
            return new Observation(p, Type.WALL, getTime());
        }
    }

    /** Attack the creature in front of this one. If there is a
        creature there that is not a subclass of this creature, kill
        it and spawn another instace of this creature facing in the same
        direction as this creature.*/
    public synchronized boolean attack(Creature e) {
        checkThread(e);
        Information info = getInformation(e);
        
        Point attackPos = info.direction.forward(info.position);
        if (! inBounds(attackPos)) {
            return false;
        }

        // See what was attacked
        Entity target = map[attackPos.x][attackPos.y];
        if ((target == null) || ! (target instanceof Creature)) {
            // Nothing to attack
            return false;
        }

        Creature targetCreature = (Creature)target;
        
        if (e.getClass().isAssignableFrom(targetCreature.getClass())) {
            // Same class
            return false;
        }

        kill(targetCreature);

        // Spawn a new one of the attacking creature
        spawn(e.getClass(), attackPos, info.direction.opposite());

        return true;
    }

    /** Kill target creature, removing it from the world. */
    private void kill(Creature targetCreature) {
        Information targetInfo = infoTable.get(targetCreature);
        assert targetInfo != null : "Tried to kill an already dead creature.";
        
        decCount(targetCreature.getClass());

        // Remove target from world (this prevents it from taking further actions)
        infoTable.remove(targetCreature);
        map[targetInfo.position.x][targetInfo.position.y] = null;
    }

    private void incCount(Class c) {
        Integer i = creatureCount.get(c);
        if (i == null) {
            i = 0;
        }
        creatureCount.put(c, (int)i + 1);
    }

    private void decCount(Class c) {
        int i = (int)creatureCount.get(c);
        --i;
        if (i == 0) {
            // Removed the last instance of this creature
            creatureCount.remove(c);
        } else {
            creatureCount.put(c, i);
        }
    }

    /** Returns the number of different species left alive, excluding Flytraps.*/
    public synchronized int getNumSpeciesLeft() {
        int count = creatureCount.size();

        // Exclude Flytraps
        if (creatureCount.get(Flytrap.class) != null) {
            --count;
        }

        return count;
    }

    /** Return value of {@link Simulation#getResult} */
    static public class Result {
        /** Winning species, null if timeout or draw. */
        public Class  species;

        public String result;

        /** Explanation of why the game ended. */
        public String why;

        /** Image of the winner */
        public Image  icon;

        /** Number of time steps until win occured. */
        public int    timeSteps;

        public Result(String r, Class s, String w, Image i, int t) {
            result = r;
            species = s;
            why = w;
            icon = i;
            timeSteps = t;
        }
    }

    /** Returns the number of creatures of this species alive in the map. */
    public synchronized int getCreatureCount(Class c) {
        Integer i = creatureCount.get(c);
        if (i == null) {
            return 0;
        } else {
            return i;
        }
    }

    /**
       Returns a description of the final outcome, or null if the game has not yet ended.
       
       A creature has won if:
       <ul>
         <li> It is the only species other than Flytraps and Apples remaining, OR
         <li> Time has elapsed and it has more instances than any other Creature and there are no Treasures on the map.
       </ul>
     */
    public synchronized Result getResult() {
        // Number of instances
        int numFlytraps  = getCreatureCount(Flytrap.class);
        int numApples    = getCreatureCount(Apple.class);
        int numTreasures = getCreatureCount(Treasure.class);

        // Number of species
        int numSpecies   = creatureCount.size() - 
            (((numFlytraps  > 0) ? 1 : 0) +
             ((numApples    > 0) ? 1 : 0) + 
             ((numTreasures > 0) ? 1 : 0));
        
        int t = getTime();

        if (numSpecies == 0) {
            // Everyone lost
            return new Result("Total Loss", null, "becasue there are no living Creatures", null, t);
        }

        if ((numSpecies == 1) && (numTreasures == 0)) {
            // Find the winner
            Class species = getMostPopulousSpecies();
            assert species != null;
            return new Result(species.getName() + " Wins", species, "by total domination", 
                              getImage(species, Direction.EAST), t);
        }
        
        if (t > TIME_STEP_LIMIT) {
            // Time is up!
            Class species = getMostPopulousSpecies();
            if ((numTreasures == 0) && (species != null)) {
                return new Result(species.getName() + " Wins", species, 
                                  "by population majority at time limit", getImage(species, Direction.EAST), t);
            } else if (numTreasures == 0) {                
                return new Result("Stalemate", null, "because time limit exceeded with no majority", null, t);
            } else {                
                return new Result("Total Loss", null, "because time limit exceeded with Treasures present", null, t);
            }
        }

        return null;
    }

    /** Returns the most populus creature's class, excluding Flytraps, Treasures, and Apples. 
        In the event of a tie, returns null;
     */
    @SuppressWarnings("unchecked")
    private Class getMostPopulousSpecies() {
        Class c = null;
        int count = -1;
        boolean tie = false;

        for (Map.Entry<Class, Integer> e : creatureCount.entrySet()) {
            Class<Object> key = e.getKey();
            int value = e.getValue();

            if (! ((key.isAssignableFrom(Treasure.class)) || 
                   (key.isAssignableFrom(Flytrap.class)) ||
                   (key.isAssignableFrom(Apple.class)))) {
                if (value > count) {
                    tie = false;
                    count = value;
                    c = key;
                } else if (value == count) {
                    tie = true;
                    c = null;
                }
            }
        }

        return c;
    }

    /** Suppresses the thread stop deprecation warning. */
    @SuppressWarnings("deprecation")
    private void stopThread(Thread t) {
        assert ! t.holdsLock(this) : 
            "Thread " + Thread.currentThread().getName() + " tried to stop " + 
            "thread " + t.getName() + ", which was locking the simulator.";
        t.stop();
    }

    /** Ensures that a creature is alive and running on the right
        thread.  Must be called from the thread that the creature is
        simulated on; that ensures that a Creature is not cheating by
        using another member of its species to execute its moves. */
    private void checkThread(Creature e) {
        Information L = infoTable.get(e);

        if (L == null) {
            throw new ConvertedError("Creature " + e.getClassName() + "_" + e.getId() + 
                            " was prohibited from taking an action because it is dead.");
        } else if (Thread.currentThread() != L.thread) {
            throw new Error("getInformation called on the wrong Creature's thread. (" + 
                            Thread.currentThread() + " vs. " + L.thread + ")");
        }
    }

    /** Returns the underlying object (if mutated, that will affect
        the Creature.) */
    private Information getInformation(Creature e) {
        Information L = infoTable.get(e);
        return L;
    }

    /** A creature's thread can't be stopped without potentially
      releasing locks that it is using for synchronization, so this
      method tells a creature when it should die.  Creatures are not allowed
      to move when dead.
    */
    public synchronized boolean isAlive(Creature c) {
        return infoTable.get(c) != null;
    }

    /** Spawns a Creature of class c at position p. c must be a
        subclass of Creature.*/
    // Get the most derived type of this class
    // Class c = e.getClass();
    private void spawn(Class c, Point p, Direction d) {
        assert inBounds(p);
        assert map[p.x][p.y] == null;

        Creature creature = null;
        
        incCount(c);

        // Instantiate it
        try {
            creature = (Creature)c.newInstance();
        } catch (ClassCastException e) {
            System.err.println("Spawned creature was not a Creature");
            return;
        } catch (InstantiationException e) {
            System.err.println(e);
            return;
        } catch (IllegalAccessException e) {
            System.err.println(e);
            return;
        } catch (ExceptionInInitializerError e) {
            System.err.println(e);
            return;
        }

        final Creature runCreature = creature;

        // Insert into the world
        Thread t = new Thread(new Runnable() {
                public void run() {
                    // Catch uncaught converted exceptions so that
                    // they don't print at the console.
                    try {
                        runCreature.run();
                    } catch (ConvertedError e) {}
                }}, 
            c.getName() + "_" + nextID);
        infoTable.put(creature, new Information(p, d, t));
        map[p.x][p.y] = creature;

        creature.setSimulator(this, nextID);
        ++nextID;

        // Start simulating the creature
        t.start();
    }

    /** Clones the position.  Throws an exception if the Creature is
        not in the world. */
    public synchronized Point getPosition(Creature e) {
        Information L = getInformation(e);
        if (L == null) {
            return null;
        } else {
            return (Point)(L.position.clone());
        }
    }

    /** Throws an exception if the Creature is not in the world. */
    public synchronized Direction getDirection(Creature e) {
        Information L = getInformation(e);
        if (L == null) {
            return null;
        } else {
            return L.direction;
        }
    }

    /** Moves this creature forward if the space in front if it is
        empty, otherwise does not move.  
        <p>
        Moving into a Thorn causes a Creature to be converted into an Apple.
        Throws ConvertedError if the Creature is not in the world.
        @param distance must be -1 or +1
        @return true if moved.
    */
    public synchronized boolean move(Creature e, int distance) {
        if ((distance != -1) && (distance != +1)) {
            throw new IllegalArgumentException("bad distance on move: " + distance);
        }

        checkThread(e);
        Information info = getInformation(e);
        Point    nextPos = info.direction.forward(info.position, distance);

        if (isEmpty(nextPos)) {
            map[info.position.x][info.position.y] = null;

            info.position = nextPos;
            map[info.position.x][info.position.y] = e;
            
            return true;
        } else if (inBounds(nextPos) && 
                   (map[nextPos.x][nextPos.y].getType() == Type.THORN)) {
            // Moved onto a thorn
            kill(e);
            spawn(Apple.class, info.position, Direction.random());
            return false;
        } else {
            // Blocked by something else
            return false;
        }
    }
    
    /** Returns true if this location is on the map */
    private boolean inBounds(Point p) {
        return inBounds(p.x, p.y);
    }

    private boolean inBounds(int x, int y) {
        return (x >= 0) && (y >= 0) && (x < width) && (y < height);
    }

    /** Returns true if the position is out of bounds or empty */
    private boolean isEmpty(Point p) {
        if (inBounds(p)) {
            Entity e = map[p.x][p.y];
            return e == null;
        } else {
            // Out of bounds is not empty
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////

    public synchronized void setSelectedCreature(Creature c) {
        selectedCreature = c;
    }

    // Size of a grid square (which is drawn on a 45-degree diagonal)
    final static int XSCALE = 20;
    final static int YSCALE = 10;

    public int getIconHeight() {
        return (width + height) * YSCALE + YSCALE * 5;
    }

    public int getIconWidth() {
        return (width + height) * XSCALE;
    }

    public synchronized void paintIcon(Component c, Graphics _g, int tx, int ty) {
        if (view3D) {
            paintIcon3D(c, _g, tx, ty);
        } else {
            paintIcon2D(c, _g, tx, ty);
        }
    }

    private void paintIcon3D(Component c, Graphics _g, int tx, int ty) {
        Graphics2D g = (Graphics2D)_g;
        g.translate(tx, ty + YSCALE * 5);

        drawGrid3D(g);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                drawEntity3D(g, map[x][y], x, y);
            }
        }

        g.translate(-tx, -ty - YSCALE * 3);
    }

    private void drawEntity3D(Graphics2D g, Entity e, int x, int y) {
        Image im = null;

        if (e instanceof StaticEntity) {
            im = ((StaticEntity)e).image;
        } else if (e instanceof Creature) {
            Creature c = (Creature)e;
            im = getImage(c, infoTable.get(c).direction);
        }

        if (im != null) {
            int w = im.getWidth(null);
            int h = im.getHeight(null);
            int x0 = (x + height - y + 1) * XSCALE - w;
            int y0 = (x + y + 2) * YSCALE - h;
            g.drawImage(im, x0, y0, null);
        }
    }

    private void drawGrid3D(Graphics2D g) {
        g.setColor(Color.GRAY);

        int centerX = height * XSCALE;

        int dx = width * XSCALE;
        int dy = width * YSCALE;
        for (int y = 0; y <= height; ++y) {
            int x0 = -y * XSCALE + centerX;
            int y0 =  y * YSCALE;
            g.drawLine(x0, y0, x0 + dx, y0 + dy);
        }

        dx = -height * XSCALE;
        dy =  height * YSCALE;
        for (int x = 0; x <= width; ++x) {
            int x0 =  x * XSCALE + centerX;
            int y0 =  x * YSCALE;
            g.drawLine(x0, y0, x0 + dx, y0 + dy);
        }
    }

    //////////////////////////////////////////////////////////////////////////////

    /** 2D grid size in pixels. Must be an odd number */
    public static final int SCALE2D = 17;

    /** For 2D rendering */
    private static Font  font = new Font("Arial", Font.PLAIN, SCALE2D - 3);

    private Map<Class, Color> creatureColor = new HashMap<Class, Color>();

    /** Colors to be used for new creatures.*/
    private Stack<Color> colorStack = new Stack<Color>();
    
    private Point lastDrawPoint = new Point(0, 0);

    /** Returns the coordinates at which the 2D grid was last drawn */
    public synchronized Point getGridDrawXY2D() {
        return lastDrawPoint;
    }
    
    /** Returns the creature at x, y on the grid. */
    public synchronized Creature getCreature(int x, int y) {
        if (! inBounds(x, y)) {
            return null;
        }

        Entity e = map[x][y];
        if ((e != null) && (e instanceof Creature)) {
            return (Creature)e;
        } else {
            return null;
        }
    }

    private void paintIcon2D(Component c, Graphics _g, int tx, int ty) {
        Graphics2D g = (Graphics2D)_g;

        // Center the display
        tx += (getIconWidth() - SCALE2D * width) / 2;
        ty += (getIconHeight() - SCALE2D * height) / 2;
        
        g.translate(tx, ty);

        lastDrawPoint = new Point(tx, ty);
        
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                drawEntity2D(g, map[x][y], x, y);
            }
        }

        drawGrid2D(g);

        g.translate(-tx, -ty);
    }

    // 2D polygon
    private static int[] xpoints = {-SCALE2D/2, -SCALE2D/2, SCALE2D/2-4, SCALE2D/2,  SCALE2D/2-4};
    private static int[] ypoints = {-SCALE2D/2,  SCALE2D/2,   SCALE2D/2,    0,        -SCALE2D/2};

    private void drawEntity2D(Graphics2D g, Entity e, int x, int y) {
        if (e == null) {
            // Nothing to draw
            return;
        }

        if (e instanceof StaticEntity) {
            if (e.getType() == Type.WALL) {
                g.setColor(Color.BLACK);
                g.fillRect(x * SCALE2D, y * SCALE2D, SCALE2D, SCALE2D);
            } else {
                // Thorn
                g.setColor(Color.GREEN);
                g.fillRect(x * SCALE2D + 3, y * SCALE2D + 3, SCALE2D - 6, SCALE2D - 6);
            }
        } else if (e instanceof Treasure) {
            // Treasure
            g.setColor(Color.YELLOW);
            g.fillArc(x * SCALE2D, y * SCALE2D, SCALE2D - 1, SCALE2D - 1, 0, 360);
            g.setColor(Color.BLACK);
            g.drawArc(x * SCALE2D, y * SCALE2D, SCALE2D - 1, SCALE2D - 1, 0, 360);
            
        } else if (e instanceof Creature) {

            char label = e.getLabel();
            Direction d = infoTable.get(e).direction;

            Information info = infoTable.get(e);
            g.setColor(creatureColor.get(e.getClass()));
            
            int tx = x * SCALE2D + SCALE2D/2;
            int ty = y * SCALE2D + SCALE2D/2;
            AffineTransform old = g.getTransform();
            g.translate(tx, ty);
            g.rotate(Math.toRadians(270 - 90 * d.toInt()));
            g.fillPolygon(xpoints, ypoints, 5);

            if (e == selectedCreature) {
                // Highlight
                Stroke s = g.getStroke();
                g.setStroke(new BasicStroke(3));
                g.setColor(Color.WHITE);
                g.drawPolygon(xpoints, ypoints, 5);
                g.setStroke(s);
            }

            g.setColor(Color.BLACK);
            g.drawPolygon(xpoints, ypoints, 5);

            g.setTransform(old);

            g.setFont(font);
            FontMetrics m = g.getFontMetrics();

            // Center the label
            int fx = x * SCALE2D + (SCALE2D - m.charWidth(label)) / 2;
            int fy = y * SCALE2D + (SCALE2D + m.getAscent()) / 2 - 1;
            if (e == selectedCreature) {
                g.setColor(Color.WHITE);
                for (int dx = -1; dx <= 1; ++dx) {
                    for (int dy = -1; dy <= 1; ++dy) {
                        g.drawString("" + label, fx + dx, fy +dy);
                    }
                }
                g.setColor(Color.BLACK);
            }
            g.drawString("" + label, fx, fy);
            
        }
    }

    /** Draws gridlines of the map.  Called from paint. */
    private void drawGrid2D(Graphics2D g) {
        g.setColor(Color.GRAY);

        for (int x = 0; x <= width; ++x) {
            g.drawLine((int)(x * SCALE2D), 0, (int)(x * SCALE2D), (int)(height * SCALE2D));
        }

        for (int y = 0; y <= height; ++y) {
            g.drawLine(0, (int)(y * SCALE2D), (int)(width * SCALE2D), (int)(y * SCALE2D));
        }
    }

    //////////////////////////////////////////////////////////////////

    static private Map<Class, Image[]> imageCache = new HashMap<Class, Image[]>();

    public static Image getImage(Creature c, Direction d) {
        return getImage(c.getClass(), d);
    }

    /** Returns the image for this Creature subclass. */
    public static Image getImage(Class cs, Direction d) {
        Image[] array = imageCache.get(cs);
        if (array == null) {
            array = loadImages(cs.getName());
            imageCache.put(cs, array);
        }
        
        return array[d.toInt()];        
    }

    private static Image[] loadImages(String base) {
        if (new File(base + ".png").exists()) {
            Image i = loadImage(base + ".png");
            Image[] array = {i, i, i, i};
            return array;
        } else {
            String[] ext = {"N", "W", "S", "E"};
            Image[] array = new Image[4];

            for (int e = 0; e < 4; ++e) {
                String filename = base + "-" + ext[e] + ".png";
                array[e] = loadImage(filename);
            }

            return array;
        }         
    }

    private static Image loadImage(String filename) {
        if (! new File(filename).exists()) {
            System.err.println("Warning: Missing image: " + filename);
            return bogusImage(filename);
        }

        try {
            // Faster on some old macs:
            // return Toolkit.getDefaultToolkit().getImage(file.getPath());
            // Wait for image to load
            //while (im.getWidth(null) == 0);
            
            BufferedImage im = javax.imageio.ImageIO.read(new File(filename));

            if (im == null) {
                throw new java.io.IOException("corrupt image file");
            }

            // Ensure that the image is not too big
            if (im.getWidth() > XSCALE * 2) {
                System.out.println("Warning: Rescaled " + filename + " because it was too big.");
                return im.getScaledInstance(XSCALE * 2, XSCALE * 2 * im.getHeight() / im.getWidth(), 
                                            Image.SCALE_AREA_AVERAGING);
            }
            
            return im;
        } catch (java.io.IOException e) {
            System.err.println("Warning: While loading " + filename + " encountered " + e);
            return bogusImage(filename);
        }
    }

    static private Image bogusImage(String filename) {
        // TODO:
        BufferedImage im = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        return im;
    }

    private void prepareColors() {
        creatureColor.put(Apple.class, Color.RED);
        creatureColor.put(Flytrap.class, Color.GREEN);
        

        colorStack.push(new Color(0.5f, 0.5f, 0.5f));
        colorStack.push(new Color(0.5f, 0, 0));
        colorStack.push(Color.MAGENTA);
        colorStack.push(Color.BLUE);
        colorStack.push(Color.ORANGE);
        colorStack.push(new Color(0.5f, 0.5f, 0.0f));
        colorStack.push(Color.WHITE);
        colorStack.push(Color.PINK);
        colorStack.push(Color.YELLOW);
        colorStack.push(new Color(0, 0.7f, 0.7f));
    }

    /** All walls and all thorns are the same instance. */
    private static class StaticEntity implements Entity {
        static public StaticEntity wall  = new StaticEntity(Type.WALL,  'X', "Wall.png");
        static public StaticEntity wall2 = new StaticEntity(Type.WALL,  '#', "Wall2.png");
        static public StaticEntity thorn = new StaticEntity(Type.THORN, '+', "Thorn.png");
        
        public Image image;
        public Type  type;
        public char  label;

        private StaticEntity(Type t, char L, String imageFile) {
            type = t;
            label = L;
            image = loadImage(imageFile);
        }

        public Type getType() {
            return type;
        }
        public char getLabel() {
            return label;
        }
    }

    /** Information about one creature in the map. */
    private class Information {
        public Point     position;
        public Direction direction;
        public Thread    thread;

        public Information(Point p, Direction d, Thread t) {
            position  = p;
            direction = d;
            thread    = t;
        }
    }

    /** Counts the number of time steps that have elapsed. */
    private class TimeKeeper implements Runnable {

        private Simulator simulator;
        private int time;
        
        public TimeKeeper(Simulator s) {
            simulator = s;
        }
        
        /** Returns the number of time steps that have elapsed since started.*/
        public synchronized int getTime() {
            return time;
        }

        public void run() {
            while (true) {
                simulator.delay(1);
                ++time;
            }
        }
    }
}
