
import java.util.*;

/**
 * Represents a non-terminal in the grammar.
 */
public class NonTerminal extends GrammarElement {

    private String s;

    public NonTerminal(String s){
	this.s = s;
    }


    public void expand(Grammar g) {
	Definition d = g.getMap().get(this);
	d.expand(g);
    }

    public String toString() {
	return s;
    }

    public String getString(){
	return s;
    }

    public boolean equals(Object o){
	if (o instanceof NonTerminal){
		NonTerminal other = (NonTerminal)o;
		return s.equals(other.getString());
	} else {
		return false;
	}
    }

    public int hashCode() {
	return s.hashCode();
    }

}
