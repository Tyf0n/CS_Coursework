import java.util.*;

/*
 * A Terminal in the grammar.  This is any element
 * from a grammar's production other than a non-terminal.
 */
public class Terminal extends GrammarElement {

    private String s;

    public Terminal (String s){
	this.s = s;
    }
 
    public void expand(Grammar g) {
	if (Character.isLetterOrDigit(s.charAt(0))){
		System.out.print(" ");
	}
	System.out.print(s);
    }

    public String toString() {
	return s;
    }

}

