
import java.util.*;

/**
 * Represents a grammar as a map from non-terminal names (Strings) to
 * Defintions.
 */
public class Grammar {

    private Map<NonTerminal, Definition> map;

    public Grammar(){
	map = new HashMap<NonTerminal, Definition>();
    }

    public Map<NonTerminal, Definition> getMap(){
	return map;
    }		

    // add a new non-terminal, with the given definition
    public void add(String nt, Definition def) {
	NonTerminal temp = new NonTerminal(nt);	
	if (!map.containsKey(temp)){
		map.put(temp, def);
	}	
    }
	
    // look up a non-terminal, and return the definition, or null
    // if not def exists.
    public Definition get(String nt) {
	NonTerminal temp = new NonTerminal(nt);	
	if (map.containsKey(temp)){
		return map.get(temp);
	}
	return null;
    }

    // Expand the start symbol for the grammar.
    public void expand() {
	NonTerminal start = new NonTerminal("<start>");
	if (map.containsKey(start)){
		start.expand(this);
	}
	
    }

    // return a String representation of this object.
    public String toString() {
	String output = "";
	Set<NonTerminal> s = map.keySet();
	Iterator<NonTerminal> it = s.iterator();
	while(it.hasNext()){
		NonTerminal nt = it.next();
		Definition d = get(nt.getString());
		output += " " + nt.getString() + " == " + d + "\n";
	}
	return output;  
    }

}
