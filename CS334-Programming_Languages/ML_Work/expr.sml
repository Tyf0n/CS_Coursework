(* Nathaniel Lim
 * Tues March 9, 2010
 * expr.sml
 * cs334
 *)

(* Magic constant to make datatypes print out fully *)
Control.Print.printDepth:= 100;
Control.Print.printLength:= 100;

datatype Expr = 
    VarX
  | VarY
  | Sine     of Expr
  | Cosine   of Expr
  | Average  of Expr * Expr
  | Times    of Expr * Expr
  | Geomean  of Expr * Expr
  | Abs      of Expr
  | Geomean3 of Expr * Expr * Expr;

(* Geomean takes the square root of the product of two expressions *)
(* Abs     takes the absolute value of an expression *)
(* Geomean3 takes the cube root of the product of three expressions *)

(* build functions:
     Use these helper functions to generate elements of the Expr
     datatype rather than using the constructors directly.  This
     provides a little more modularity in the design of your program
*)
fun buildX()              = VarX;
fun buildY()              = VarY;
fun buildSine(e)          = Sine(e);
fun buildCosine(e)        = Cosine(e);
fun buildAverage(e1,e2)   = Average(e1,e2);
fun buildTimes(e1,e2)     = Times(e1,e2);
fun buildGeomean(e1,e2)   = Geomean(e1,e2);
fun buildAbs(e)           = Abs(e);
fun buildGeomean3(e1, e2, e3) = Geomean3(e1, e2, e3);


(* exprToString : Expr -> string
   Complete this function to convert an Expr to a string 
*)


fun exprToString(VarX) = "x"
  | exprToString(VarY) = "y"
  | exprToString(Sine(e))   = "sin(pi*" ^ exprToString(e) ^")"
  | exprToString(Cosine(e)) = "cos(pi*" ^ exprToString(e) ^")"
  | exprToString(Average(e1, e2)) = "(" ^ exprToString(e1) ^ " + " ^ exprToString(e2) ^ ")/2"
  | exprToString(Times(e1, e2))   = exprToString(e1) ^ " * " ^ exprToString(e2)
  | exprToString(Geomean(e1,e2))  = "(" ^ exprToString(e1) ^ " * " ^ exprToString(e2) ^ ")^(1/2)"
  | exprToString(Abs(e))          = "|" ^ exprToString(e)  ^ "|"
  | exprToString(Geomean3(e1, e2, e3)) = "("^exprToString(e1) ^ " * " ^ exprToString(e2) ^" * "^ exprToString(e3)^")^(1/3)";


(* eval : Expr -> real*real -> real
   Evaluator for expressions in x and y
 *)

fun abs x = if x < 0.0 then ~1.0*x else x;

fun eval (VarX) (x, y) = x
  | eval (VarY) (x, y) = y
  | eval (Sine(e)) (x, y) = Math.sin( Math.pi*(eval e (x,y)) )
  | eval (Cosine(e)) (x, y) = Math.cos( Math.pi*(eval e (x,y)) )
  | eval (Average(e1, e2)) (x, y) = ((eval e1 (x, y)) + (eval e2 (x, y))  )/2.0
  | eval (Times(e1, e2))  (x, y) = (eval e1 (x, y)) * (eval e2 (x, y))
  | eval (Geomean(e1,e2)) (x, y) = Math.sqrt( abs( (eval e1 (x, y)) * (eval e2 (x, y))) )
  | eval (Abs(e)) (x, y) = abs(   (eval e (x,y))  )
  | eval (Geomean3(e1, e2, e3)) (x,y) = Math.pow ((eval e1 (x, y))*(eval e2 (x, y))*(eval e3 (x, y)) , 1.0/3.0);
	 
(************** Add Testing Code Here ***************)
val sampleExpr =
      buildCosine(buildSine(buildTimes(buildCosine(buildAverage(buildCosine(
      buildX()),buildTimes(buildCosine (buildCosine (buildAverage
      (buildTimes (buildY(),buildY()),buildCosine (buildX())))),
      buildCosine (buildTimes (buildSine (buildCosine
      (buildY())),buildAverage (buildSine (buildX()), buildTimes
      (buildX(),buildX()))))))),buildY())))
val sampleE2 = buildAverage(buildTimes(buildCosine(buildX()), buildSine(buildY())), 
	buildTimes(buildSine(buildX()), buildCosine(buildY())));
val sampleE3 = buildTimes(buildCosine(buildX()), buildSine(buildY()));

exprToString(sampleExpr);
exprToString(sampleE2);
exprToString(sampleE3);

eval sampleExpr (0.1, 0.1);
eval sampleE2 (0.25, 0.25);
eval sampleE3 (0.0, 0.5);


