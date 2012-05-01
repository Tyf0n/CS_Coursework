
import java.util.*;

/**
 * Represents a Definition as a Vectors of productions
 * (Vector<Production>).
 */
public class Definition extends GrammarElement {

    Vector<Production> prods;

    public Definition (){
	this.prods = new Vector<Production>();
    }

    public void addProduction(Production p){
	prods.add(p);
    }

    public void expand(Grammar g) {
	Random r = new Random();
	int randIndex = r.nextInt(prods.size());
	Production toExpand = prods.get(randIndex);
	toExpand.expand(g);
    }

    public String toString() {
	String output = "";
	for (int i = 0; i < prods.size(); i++){
		output +=  prods.get(i);
		if (i != prods.size() -1){
			output+=  " | ";
		}
	}
	return output;
	
    }

}
