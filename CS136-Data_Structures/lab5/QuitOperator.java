

public class QuitOperator implements Operator {

	
	public void apply(PS interpreter) {
		interpreter.quit();

	}

	
	public Identifier getName() {
		return new Identifier ("quit");
	}

}
