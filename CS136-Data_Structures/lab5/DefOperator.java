

public class DefOperator implements Operator {

	
	public void apply(PS interpreter)  {
		Object var = interpreter.pop();
		Object name = interpreter.pop();
		if (name instanceof Symbol){
			String sym = ((Symbol)name).getValue();
			interpreter.define(new Identifier(sym), var);
		} else{
			System.out.println("Second Item on the Stack was not a symbol");
		}

	}


	public Identifier getName() {
		return new Identifier("def");
	}

}
