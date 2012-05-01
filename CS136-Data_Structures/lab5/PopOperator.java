
public class PopOperator implements Operator {

	
	public void apply(PS interpreter) {
		Object trash = interpreter.pop();

	}

	
	public Identifier getName() {
		return new Identifier("pop");
	}

}
