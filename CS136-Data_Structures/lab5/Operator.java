

/**
   Interface for operators in postscript, like add, sub, dup, etc.

   Morgan McGuire
   <br>morgan@cs.williams.edu
 */
public interface Operator {
    /** Reads arguments off the stack and then puts the result back
     * on. */
    public void apply(PS interpreter);

    /** The name that this operator is bound to, e.g. "add" for the AddOperator.*/
    public Identifier getName();
}

