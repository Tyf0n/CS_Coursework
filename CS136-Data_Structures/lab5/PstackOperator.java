

public class PstackOperator implements Operator {

	
	public void apply(PS interpreter) {
		interpreter.pstack();

	}

	
	public Identifier getName() {
		return new Identifier("pstack"); 
	}

}
