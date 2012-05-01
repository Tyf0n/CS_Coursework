

import java.io.*;
import java.util.Stack;

/**
   Parses the input stream (e.g., System.in) into the tokens used for
   processing PostScript: Double, Boolean, Identifier, Symbol, and Procedure.
 */
public class StreamIterator implements java.util.Iterator<Object>, java.lang.Iterable<Object> {
    private StreamTokenizer st;
    
    /** */
    public StreamIterator(InputStream in) {
        //        Reader r = new BufferedReader(new InputStreamReader(in));
        Reader r = new InputStreamReader(in);
        st = new StreamTokenizer(r);
        st.parseNumbers();
        st.slashSlashComments(false);
        st.slashStarComments(false);
        st.lowerCaseMode(true);

        // Make '/' not a comment, which was the Java default
        st.wordChars('/', '/');
        st.commentChar('%');

        // Mark the PostScript delayed execution operators
        st.ordinaryChar('{');
        st.ordinaryChar('}');
    }

    /** You can use this class either as an iterator or as a container
        to iterate over.  Because it is a stream, tokens that are
        iterated over are consumed, so iteration changes the object.

        <p>
        Allows the syntax:
        <pre>
          for (Object value : stream) {
             ... process value
          }
        </pre>
    */
    public java.util.Iterator<Object> iterator() {
        return this;
    }

    public boolean hasNext() {
        return st.ttype != StreamTokenizer.TT_EOF;
    }

    private static class CloseBrace {}
    private final static CloseBrace CLOSE_BRACE = new CloseBrace();

    /** Returns Double, Boolean, Symbol, Identifier, or Procedure.
        Returns null when the stream is done. */
    public Object next() {
        Object value = null;

        try {
            // Read until EOF or a useful token
            do {
                st.nextToken();
            } while (st.ttype == StreamTokenizer.TT_EOL);
        } catch (Exception e) {
            System.err.println(e);
        }

        switch (st.ttype) {
        case '{':
            value = readProcedure();
            break;

        case '}':
            value = CLOSE_BRACE;
            break;

        case StreamTokenizer.TT_NUMBER:
            value = new Double(st.nval);
            break;

        case StreamTokenizer.TT_WORD:
            if (st.sval.equals("true")) {

                value = new Boolean(true);

            } else if (st.sval.equals("false")) {

                value = new Boolean(false);

            } else if ((st.sval.length() > 1) && (st.sval.charAt(0) == '/')){

                // Strip the leading slash
                value = new Symbol(st.sval.substring(1));

            } else if ((st.sval.length() > 0) && (st.sval.charAt(0) == '(')) {

                assert false : "This implementation does not support Postscript strings in parentheses.";

            } else {
                value = new Identifier(st.sval);
            }
            break;

        default:
            //System.out.println("End of File");
        }

        return value;
    }

    /** Assumes that the current token is '{'.  Reads until the
        matching '}' and returns the resulting Procedure.  Will
        recursively handle procedures within procedures.  

        Called from next(). */
    private Procedure readProcedure() {
        assert st.ttype == '{';

        // Begin a new procedure
        Procedure procedure = new Procedure();

        // Read until that procedure is done
        for (Object token = next(); token != CLOSE_BRACE; token = next()) {
            procedure.add(token);
        }
        
        return procedure;
    }

    /** Returns true if obj is not null and is one of the
        types of values that can be returned from next(). */
    static public boolean isLegalPostScriptValue(Object obj) {
        return 
            (obj != null) && 
            ((obj instanceof Procedure) ||
             (obj instanceof Boolean) ||
             (obj instanceof Double) ||
             (obj instanceof Symbol) ||
             (obj instanceof Identifier));
    }

    public void remove() {
        assert false : "Not supported";
    }
}
