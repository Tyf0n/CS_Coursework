

/** Adds the last two values on the stack */
public class AddOperator implements Operator {
    public void apply(PS interpreter) {
    	
        double b = (Double)interpreter.pop();
        double a = (Double)interpreter.pop();

        interpreter.push(new Double(a + b));
    }

    public Identifier getName() {
        return new Identifier("add");
    }
}
