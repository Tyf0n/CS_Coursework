

public class ExchOperator implements Operator {

	
	public void apply(PS interpreter) {
		Object firstIn = interpreter.pop();
		Object lastIn = interpreter.pop();
		interpreter.push(firstIn);
		interpreter.push(lastIn);
	}


	public Identifier getName() {
		return new Identifier("exch");
	}

}
