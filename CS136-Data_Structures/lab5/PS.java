/*
 * Nathaniel Lim
 * CS 136 Williams College
 * Lab 6: Postscript
 * Mar 31, 2008
 * njl2@williams.edu
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class PS {
    /*
     * This PS class is an interpreter for reading postscript commands.
     * It keeps track of the operands using java.util.Stack,
     * and the variables usings java.util.Map, mapping Identifier objects
     * to general objects.
     * The various operators are stored in a Map, mapping operator string to the
     * specific Operator object.  The operator strings are stored in a set so that 
     * operatorMap.keySet() does not have to be called everytime the interpreter wants to
     * check whether an Identifier is the name of an operator.  
     * 
     * If the object pushed is an Operator, the apply method is 
     * performed.  Many implementations of Operator perform casts assuming that 
     * the Objects on the stack are of a certain type.  If they are not the expected
     * type,  ClassCastException will be thrown at runtime (failing fast).  
     */
	private Map<Identifier, Object> vars = new HashMap<Identifier, Object>();
	private Stack<Object> operands = new Stack<Object>();
	private Map<String, Operator> operatorMap = new HashMap<String, Operator>();
	private Set<String> operators;
	
	public PS (){
		
		operatorMap.put("def", new DefOperator());
		operatorMap.put("exec", new ExecOperator());
		operatorMap.put("add", new AddOperator());
		operatorMap.put("mul", new MulOperator());
		operatorMap.put("sub", new SubOperator());
		operatorMap.put("div", new DivOperator());
		operatorMap.put("dup", new DupOperator());
		operatorMap.put("ifelse", new IfElseOperator());
		operatorMap.put("if", new IfOperator());
		operatorMap.put("exch", new ExchOperator());
		operatorMap.put("pop", new PopOperator());
		operatorMap.put("eq", new EqOperator());
		operatorMap.put("ne", new NeOperator());
		operatorMap.put("gt", new GtOperator());
		operatorMap.put("lt", new LtOperator());
		operatorMap.put("clear", new ClearOperator());
		operatorMap.put("pstack", new PstackOperator());
		operatorMap.put("quit", new QuitOperator());
		
		operators = operatorMap.keySet();	
	}
	
	public static void main (String [] args){
		PS interpreter = new PS();		
		StreamIterator stream = new StreamIterator(System.in);
		for(Object val: stream){
			interpreter.push(val);
                }		
	}
	
	public Object pop(){
		assert operands.peek() != null : "Stack is empty";
		return operands.pop();
	}
	
	public void push(Object obj){
		if (obj instanceof Identifier){
			//If the obj is a defined variable 
			//execute its value
			if (vars.containsKey(obj)){
				obj = vars.get(obj);
				exec(obj);
			} else if (   operators.contains(((Identifier)obj).getValue())){ 
				//If the Identifier is a name of an operator
				String opName = ((Identifier)obj).getValue();
				Operator o = operatorMap.get(opName);
				o.apply(this);
			}
		} else {
			operands.push(obj);
		}
	}
	
	public void exec (Object obj){
		if (obj instanceof Procedure){
			//Push all the steps of the procedure on the stack
			Procedure p = (Procedure)obj;
			for (Object o: p){
				push(o);
			}			
		} else {
			operands.push(obj);
		}	
	}

	/*
	 * Clears all the values on the stack and 
	 * and clears all the defined variables.
	 */
	public void clear(){
		operands = new Stack<Object>();
		vars = new HashMap<Identifier, Object>();
	}
	
	
	/*
	 * Associate an Identifier to the Object value in a Map
	 * in order to define the variable
	 */
	public void define(Identifier name, Object val){
		vars.put(name, val);
	}
	
	/*
	 * Prints out the elements that are on the stack,
	 * so that the first line is the first item to be
	 * popped of the stack.
	 */
	public void pstack() {
		for (int i = operands.size() -1; i >= 0; i--){
			System.out.println(operands.get(i));
		}		
        }

        /*
         * Prints out a table of the defined variable names and their values.
         */
	public void ptable () {
		for (Identifier x : vars.keySet()){
			System.out.println(x + ": " + vars.get(x));
		}
	}
	
	public void quit() {
		System.exit(0);
	}
		
}
