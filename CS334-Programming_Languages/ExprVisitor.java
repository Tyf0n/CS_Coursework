/*
 * Nathaniel Lim, CS334, HW 6
 */

import java.lang.Integer;

/** Abstract class for all expressions */
abstract class Expr {
    abstract <T> T accept(Visitor<T> v);
}

class Number extends Expr {
    protected int n;

    public Number(int n) { this.n = n; }

    public <T> T accept(Visitor<T> v) {
	return v.visitNumber(this.n);
    }
}

class Sum extends Expr {
    protected Expr left, right;

    public Sum(Expr left, Expr right) {
	this.left = left;
	this.right = right;
    }

    public <T> T accept(Visitor<T> v) {
	return v.visitSum(left.accept(v), right.accept(v));
    }
}

class Div extends Expr {
    protected Expr left, right;

    public Div(Expr left, Expr right) {
	this.left = left;
	this.right = right;
    }

    public <T> T accept(Visitor<T> v) {
	return v.visitDiv(left.accept(v), right.accept(v));
    }
}


class Subtract extends Expr {
    protected Expr left, right;

    public Subtract(Expr left, Expr right) {
	this.left = left;
	this.right = right;
    }

    public <T> T accept(Visitor<T> v) {
	return v.visitSubtract(left.accept(v), right.accept(v));
    }
}

class Times extends Expr {
    protected Expr left, right;

    public Times(Expr left, Expr right) {
	this.left = left;
	this.right = right;
    }

    public <T> T accept(Visitor<T> v) {
	return v.visitTimes(left.accept(v), right.accept(v));
    }
}

/** Abstract class for all visitors */
abstract class Visitor<T> {
    abstract T visitNumber(int n);
    abstract T visitSum(T left, T right);
    abstract T visitSubtract(T left, T right);
    abstract T visitTimes(T left, T right); 
    abstract T visitDiv(T leftm, T right);
}

/** Example visitor to convert an Expr to a String */
class ToString extends Visitor<String> {
    public String visitNumber(int n) { 
	return "" + n;
    }
    public String visitSum(String left, String right) {
	return "(" + left + " + " + right + ")";
    }

    public String visitSubtract (String left, String right) {
	return "(" + left + " - " + right + ")";
    }

    public String visitTimes (String left, String right) {
	return "(" + left + " * " + right + ")";
    }

    public String visitDiv (String left, String right) {
	return "(" + left + " / " + right + ")";
    }	
}

class Eval extends Visitor<Integer> {
     public Integer visitNumber(int n){
	return new Integer(n);
     }
     public Integer visitSum(Integer left, Integer right) {
	return new Integer(left.intValue() + right.intValue() );
     }

     public Integer visitSubtract(Integer left, Integer right) {
	return new Integer(left.intValue() - right.intValue() );
     }

     public Integer visitTimes(Integer left, Integer right) {
	return new Integer(left.intValue() * right.intValue() );
     }

     public Integer visitDiv(Integer left, Integer right) {
	return new Integer(left.intValue() / right.intValue() );
     }
}

class Compile extends Visitor<String>{
     public String visitNumber(int n){
	return "PUSH(" + n + ")";
     }
     public String visitSum(String left, String right) {
	return left + " " + right + " ADD";
     }

     public String visitSubtract(String left, String right) {
	return left + " " + right + " SUB";
     }

     public String visitTimes(String left, String right) {
	return left + " " + right + " MULT";
     }

     public String visitDiv(String left, String right) {
	return left + " " + right + " DIV";
     }
}



public class ExprVisitor { 
    public static void main(String args[]) { 
	Expr e = new Sum(new Number(3), new Number(2)); 
	Expr e2 = new Sum(new Subtract(new Number (4), new Number (2)), new Times(new Number(3), new Number(4)));
	ToString printer = new ToString();
	String stringRep = e.accept(printer); 
	System.out.println(stringRep);
	System.out.println(e.accept(new Eval()));
	System.out.println(e.accept(new Compile()));
	System.out.println(e2.accept(new ToString()));
	System.out.println(e2.accept(new Eval()));
	System.out.println(e2.accept(new Compile()));
	Expr e3 = new Times(new Number (3), new Subtract(new Number (1), new Number (2)));
	System.out.println(e3.accept(new Compile()));
    }

}

