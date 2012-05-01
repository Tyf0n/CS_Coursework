

public class LtOperator implements Operator {

	
	public void apply(PS interpreter) {
		
		Double b = (Double)interpreter.pop();
		Double a = (Double)interpreter.pop();
		interpreter.push(new Boolean (a.doubleValue() < b.doubleValue()));
	}

	
	public Identifier getName() {
		return new Identifier("lt");
	}

}
