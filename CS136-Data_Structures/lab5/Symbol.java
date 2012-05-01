

/** 
A symbol, which is denoted in postscript by a leading /.

<p>
Morgan McGuire
<br>morgan@cs.williams.edu
*/
public class Symbol {

    /** The string contents of the symbol. */
    private String s;

    /** Creates a new symbol, which must follow the same formatting
        rules as Java identifiers. Do not put a slash at the front of
        sym. */
    public Symbol(String sym) {
        assert sym != null;
        assert ((sym.indexOf(' ') == -1) &&
                (sym.indexOf('\t') == -1) &&
                (sym.indexOf('\n') == -1)) : "Symbols may not contain whitespace";
        assert sym.length() > 0 : "Symbols must contain at least one character";
        assert Character.isLetter(sym.charAt(1)) : "Symbols must begin with a letter";
        s = sym;
    }

    /** See Object.toString */
    public String toString() {
        return s;
    }

    /** Returns true only if obj is a Symbol with the same value */
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            return s.equals(((Symbol)obj).s);
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
