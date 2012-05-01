

import java.util.ArrayList;

/** Represents a PostScript procedure. */
public class Procedure implements java.lang.Iterable<Object> {

    // Note that this could also be a Queue!
    private ArrayList<Object> array = new ArrayList<Object>();

    /** Adds a new instruction to the end of the procedure, which must
        be a legal PostScript value. */
    public void add(Object e) {
        assert StreamIterator.isLegalPostScriptValue(e); array.add(e);
    }

    /** Iterates through the commands in this procedure.*/
    public java.util.Iterator<Object> iterator() {
        return array.iterator();
    }
}
