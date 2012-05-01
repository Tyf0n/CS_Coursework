

public class NeOperator implements Operator {

	
	public void apply(PS interpreter) {
		Double b = (Double)interpreter.pop();
		Double a = (Double)interpreter.pop();
		interpreter.push(new Boolean (!a.equals(b)));

	}

	
	public Identifier getName() {
		return new Identifier("ne");
	}

}
