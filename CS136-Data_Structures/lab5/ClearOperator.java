

public class ClearOperator implements Operator {

	
	public void apply(PS interpreter) {
		interpreter.clear();

	}

	
	public Identifier getName() {
		return new Identifier("clear");
	}

}
