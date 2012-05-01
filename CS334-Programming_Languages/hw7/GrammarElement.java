
import java.util.*;

/**
 * Abstract class the all expandable parts of
 * a grammar extend (Terminal, NonTerminal, Production, Definition).
 */
public abstract class GrammarElement {

    /**
     * Expand the grammar element as part of a random 
     * derivation.  Use grammar to look up the definitions
     * of any non-terminals encountered during expansion.
     */
    public abstract void expand(Grammar grammar);

    /**
     * Return a string representation of this grammar element.
     * This is useful for debugging.  (Even though we inherit a
     * default version of toString() from the Object superclass, 
     * I include it as an abstract method here to ensure that 
     * all subclasses provide their own implmementaiton.)
     */
    public abstract String toString();	
	
}
