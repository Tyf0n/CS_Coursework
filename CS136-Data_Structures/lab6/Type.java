/**
   Types of Entitys that can be in a square.
 */
public enum Type {

    /** A square that is empty and can be entered (note that it may no
     * longer be empty by the time you actually get around to entering
     * it, though!. */
    EMPTY,
 
    /** A square that cannot be entered because it is blocked by an
        immovable and indestructible wall.*/
    WALL,

    /** A square that cannot be entered because it is blocked by an immovable
        and indestructible thorn hedge.  Attempting to move into the hedge
        converts a creature into an Apple. */
    THORN,

    CREATURE;
}
