

/** 
A Postscript identifier, as distinguished from a quoted string or symbol.

<p>
Morgan McGuire
<br>morgan@cs.williams.edu
*/
public class Identifier {

    /** The string contents of the symbol. */
    private String s;

    /** Creates a new identifier, which must follow the same formatting
     * rules as Java identifiers. */
    public Identifier(String sym) {
        assert sym != null;
        assert ((sym.indexOf(' ') == -1) &&
                (sym.indexOf('\t') == -1) &&
                (sym.indexOf('\n') == -1)) : "Identifiers may not contain whitespace";
        assert sym.length() > 0 : "Identifiers must contain at least one character";
        assert Character.isLetter(sym.charAt(0)) : "Identifiers must begin with a letter";
        s = sym;
    }

    /** See Object.toString */
    public String toString() {
        return s;
    }

    /** Returns true only if obj is a Identifier with the same value */
    public boolean equals(Object obj) {
        if (obj instanceof Identifier) {
            return s.equals(((Identifier)obj).s);
        } else {
            return false;
        }
    }

    /** See Object.hashCode */
    public int hashCode() {
        return s.hashCode();
    }

    /** Returns the string value of this symbol. */
    public String getValue() {
        return s;
    }
    
}
