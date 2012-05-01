

public class DupOperator implements Operator {

	
	public void apply(PS interpreter) {
		Object val = interpreter.pop();
		interpreter.push(val);
		interpreter.push(val);
	}

	
	public Identifier getName() {
		return new Identifier ("dup");
	}

}
