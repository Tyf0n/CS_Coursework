(*
  ML Examples from Lecture.
  Stephen Freund
  cs334
*)


(* Always include these at the top of your SML files so 
   large datatype values and lists will print fully *)
Control.Print.printDepth := 100;
Control.Print.printLength := 100;

(******************************************************)

(* A simple fibonacci function to demonstrate patterns *)
fun fib 0 = 1
  | fib 1 = 1
  | fib n = fib(n-1) + fib(n-2);

(* Functions used below *)
fun square x = x * x;
fun inc x = x + 1;

(******************************************************)

(* 
   length: 'a list -> int
   Polymorphic functino to compute the length of a list. 
*)
fun length nil = 0 
  | length (x::xs) = 1 + length(xs);

length [1,2,3,4];
length nil;
length ["A"];

(******************************************************)

(* 
   flip: 'a * 'b list -> 'b * 'a list
   Flip the order of pairs of values.
   example: flip [(1,"A"),(2,"B")]  ==>  [("A",1), ("B",2)]
*)
fun flip nil = nil
  | flip ((x,y)::rest) = (y,x)::(flip(rest));

flip [(1,"A"),(2,"B")];
flip [(3.3,true),(1.2,false)];

(******************************************************)

(* 
   compose: ('a -> 'b) -> ('b -> 'c) -> 'a -> 'c
   Compose the functions f and g.
*)
fun compose f g = (fn x => g(f(x)));

(compose inc square) 4;

(* 
   example of creating a new function that will take the
   size of a string and then square it. 
*)
val sqSize = compose size square;

sqSize "moo";

(******************************************************)

(* 
   contains: ''a * ''a list -> ''a list
   A function to test whether an element is in a list.
   Note that this requires an equality type, as indicated
   by the '' on the type variable ''a. 
*)
fun contains (x,nil) = false 
  | contains (x,(y::ys)) = (x = y) orelse contains(x,ys);

contains (3, [1,2,3,4]);
contains ("A", ["B", "C"]);

(******************************************************)

(* 
  Create a simple abbreviation for the tupe type int * int 
*)
type Point = int * int;

val x : Point = (3,4);

(******************************************************)

(* 
   A datatype declaration that creates a new type
   to represent the compass directions. 
*)
datatype Direction = North | South | East | West;

(* 
   move: (int * int) * Direction -> (int * int)
   A function to move a point in the specified direction
   on the Cartesian plane.
*)
fun move((x,y),North) = (x,y+1) 
  | move((x,y),South) = (x,y-1) 
  | move((x,y),East) = (x+1,y) 
  | move((x,y),West) = (x-1,y);

val dir = West;
move ((1,2), dir);
move ((0,0), North);

(******************************************************)

(*
   A datatype to represent two different payment schemes.
*)
datatype Payment = Cash of real
                 | Check of string * int * real;

(*
   amount : Payment -> double
   Return the amount of either kind of payment
*)
fun amount (Cash(x)) = x 
  | amount (Check(bank,num,x)) = x;
 
(*
   isCheck : Payment -> boolean
   Return whether a payment is a check.
*)
fun isCheck (Cash(_)) = false 
  | isCheck (Check(_,_,_)) = true;

(*
   tally : Payment list -> real
   Sum the amounts of all payments in a list
*)
fun tally nil = 0.0
  | tally (p::ps) = amount(p) + tally(ps);


val p1 = Cash(100.00);
val p2 = Check("BankNorth", 1001, 55.55);

amount(p1);
amount(p2);
isCheck(p1);
isCheck(p2);
tally [p1,p2];

(******************************************************)

(*
   A recursive datatype to capture simple parse trees
   for expressions.
*)
datatype Expr = Num of int
              | Plus of Expr * Expr
              | Mult of Expr * Expr
              ;

(* 
   eval : Expr -> int
   Compute the value of an expression represented as 
   an Expr.
*)
fun eval(Num(n)) = n 
  | eval(Plus(e1,e2)) = eval(e1) + eval(e2)
  | eval(Mult(e1,e2)) = eval(e1) * eval(e2)
  ;

val e1 = Plus(Num(1), Mult(Num(2),Num(3)));
eval e1;


(******************************************************)

(*
   A polymorphic recursive datatype to represent trees
   containing any type of value at the leaves.
*)
datatype 'a Tree = Leaf of 'a
                 | Node of 'a Tree * 'a Tree;

(* create an int Tree and a string Tree *)
val iTree = Node(Leaf(1), Leaf(2));
val sTree = Node(Leaf("A"), Node(Leaf("B"), Leaf("C")));

(*
   collapse: 'a Tree -> 'a list
   Return a list of all values stored in a tree.
*)
fun collapse (Leaf(x)) = [x] 
  | collapse (Node(l,r)) = collapse(l) @ collapse(r);

collapse iTree;
collapse sTree;
