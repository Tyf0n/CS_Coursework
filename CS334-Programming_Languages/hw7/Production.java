
import java.util.*;

/**
 * Represents a production from the grammar.  It
 * contains a sequence of GrammarElements.
 */
public class Production extends GrammarElement {

    Vector<GrammarElement> gels;

    public Production (){
	gels = new Vector<GrammarElement>();
    }

    public void addGrammarElement(GrammarElement elem){
	gels.add(elem);
    }

    public void expand(Grammar g) {
	for (int i = 0; i < gels.size(); i++){
		gels.get(i).expand(g);
	}
    }

    public String toString() {
	String output = "";
	for (int i = 0; i < gels.size(); i++){
		output +=  gels.get(i);

		if (i != gels.size() -1){
			output+=  " , ";
		}
	}
	return output;
    }

}
