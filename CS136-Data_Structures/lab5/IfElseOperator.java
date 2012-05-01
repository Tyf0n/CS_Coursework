

public class IfElseOperator implements Operator {

	
	public void apply(PS interpreter) {
		
		Procedure otherwise = (Procedure)interpreter.pop();
		Procedure proc = (Procedure)interpreter.pop();
		Boolean clause = (Boolean)interpreter.pop();
		if (clause.booleanValue()){
			interpreter.exec(proc);
		} else {
			interpreter.exec(otherwise);
		}
	}

	
	public Identifier getName() {
		return new Identifier("ifelse");
	}

}
