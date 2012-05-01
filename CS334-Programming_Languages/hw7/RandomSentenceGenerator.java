
import java.util.*;

/*
 * A Random Sentence Generator
 * 
 * This class currently parses a grammar file from standard input.
 * Your job is to extend it to print random sentences from the
 * grammar.
 */
class RandomSentenceGenerator {

    // the grammar for this generator
    protected Grammar grammar;
	
    /**
     * Read in a grammar from standard input
     */
    public RandomSentenceGenerator() {
	grammar = readGrammar(new Scanner(System.in));
    }

    
    //////////////// Grammar parsing routines ////////////////

    /**
     * Read tokens up to the end of a production and return 
     * them as a Production.
     *
     * Parses "production ::= [ word ]*"
     * where word is any terminal/non-terminal.
     */
    protected Production readProduction(Scanner in) {
	Production currentProd = new Production();
	while (true) {
	    if (!in.hasNext()) {
		RandomSentenceGenerator.fail("EOF reached in middle of production");
	    } else {
		// check either punctation that could and a production.
		// hasNext takes a regular expression, so we use "\\|"
		// to match a | character in the actual input.
		if (in.hasNext(";") || in.hasNext("\\|")) break;

		String word = in.next();
		// word is next word in production
		if (word.charAt(0) == '<'){
			//We have a non terminal
			currentProd.addGrammarElement(new NonTerminal(word));
		} else {
			currentProd.addGrammarElement(new Terminal(word));
		}
	    }
	}

	return currentProd;
    }
	
    /**
     * Read a group of productions and return them as a Definition.
     *
     * Parses "<definition> ::= <production> [ '|' <production> ]*" 
     */
    protected Definition readDefinition(Scanner in) {
	Definition currentDef = new Definition();
	while (true) {
	    Production production = readProduction(in);
	    currentDef.addProduction(production);
	    // move past terminating punctuation (should be | or ;).
	    String punctuation = in.next();
	    if (punctuation.equals(";")) {
		break;
	    } else if (!punctuation.equals("|")) {
		RandomSentenceGenerator.fail("expected |, but found: " + 
					     punctuation);
	    }
	}
	return currentDef;
    }
	
    /**
     * Repeatedly read non-terminal definitions and insert them into
     * the grammar.
     *
     * Parses "<grammar> ::= [ <non-terminal> '=' <definition> ';' ]*" 
     */
    protected Grammar readGrammar(Scanner in) {
	Grammar myGrammar = new Grammar();
	while (in.hasNext()) {
	    String name = in.next();

	    // skip over '='
	    if (!in.hasNext("=")) {
		RandomSentenceGenerator.fail("expected =");
	    }
	    in.next();  // throw away the = string

	    Definition def = readDefinition(in);
	    myGrammar.add(name, def);
	
	}
	return myGrammar;
    }


    /////////////////////////////////////////////////////


    /**
     * Print the grammar to the screen.
     */
    public void printGrammar() {
	System.out.println("Grammar:\n" + grammar);
    }
	

    /**
     * Helper method to abort gracefully when an error occurs.
     * <p>
     * Usage: RandomSentenceGenerator.fail("Error Message");
     */
    public static void fail(String msg) {
	throw new RuntimeException(msg);
    }

    /**
     * Create a random sentence generator and print out
     * three random productions.
     */
    public static void main(String args[]) {
	RandomSentenceGenerator rsg = new RandomSentenceGenerator();

	rsg.printGrammar();

	System.out.println("Derivations:  ");

	for (int i = 0; i < 3; i++){
		System.out.print((i+1) + ": " );
		rsg.grammar.expand();    
		System.out.print("\n");
	}
	
    }
}
