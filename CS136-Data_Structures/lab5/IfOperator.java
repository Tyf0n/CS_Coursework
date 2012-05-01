

public class IfOperator implements Operator {

	
	public void apply(PS interpreter) {
		Procedure proc = (Procedure)interpreter.pop();
		Boolean clause = (Boolean)interpreter.pop();
		if (clause.booleanValue()){
			interpreter.exec(proc);
		} // else do nothing

	}

	
	public Identifier getName() {
		return new Identifier("if");
	}

}
