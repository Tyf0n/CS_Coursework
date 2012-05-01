

public class ExecOperator implements Operator {

	
	public void apply(PS interpreter) {
		Object proc = interpreter.pop();
		assert proc instanceof Procedure;
		interpreter.exec(proc);

	}

	public Identifier getName() {
		return new Identifier("exec");
	}

}
