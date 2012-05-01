/** Thrown by Simulator when a Creature that has been converted to
 * another species attempts to take an action. */
public class ConvertedError extends Error {
    public ConvertedError(String e) {
        super(e);
    }
}
