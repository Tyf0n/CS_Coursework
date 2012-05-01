

public class MulOperator implements Operator {

	public void apply(PS interpreter) {
		
		double b = (Double)interpreter.pop();
		double a = (Double)interpreter.pop();
		
		interpreter.push(a*b);
	}

	
	public Identifier getName() {
		return new Identifier("mul");
	}

}
