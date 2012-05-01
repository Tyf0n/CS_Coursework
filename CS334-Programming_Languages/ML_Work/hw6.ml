(* Nathaniel Lim
   CS334 - Apr 13, 2010
   HW6
*)


exception DigitException; 
exception DotProd;
exception Excpt of int;
fun twice(f, x) = f(f(x)) handle Excpt(x) => x;
fun pred(x) = if x = 0 then raise Excpt(x) else x-1;
fun dumb(x) = raise Excpt(x);
fun smart(x) = (1 + pred(x)) handle Excpt(x) => 1;

twice(pred, 1);
twice(dumb, 1);
twice(smart, 0);


(* 1st Implementation (a) *)

fun charToNum c = if (ord c < ord#"0" orelse ord c > ord#"9") then raise DigitException else ord c - ord #"0";

fun calcList (nil, n) = n
  | calcList (fst::rest, n) = calcList(rest, 10*n + charToNum fst);

fun stringToNum s = calcList(explode s, 0) handle DigitException => ~1;

stringToNum "3405";
stringToNum "3a05";


(* 2nd Implementation (b) *)

fun charToNum2 c = if (ord c < ord#"0" orelse ord c > ord#"9") then ~1 else ord c - ord #"0";

fun calcList2 (nil, n) = n
  | calcList2 (fst::rest, n) = 
	if charToNum2 fst = ~1 then ~1 else calcList2(rest, 10*n + charToNum2 fst);

fun stringToNum2 s = calcList2(explode s, 0);

stringToNum2 "3405";
stringToNum2 "34a5";


fun dotprodtail nil nil acc = acc
  | dotprodtail nil y acc = acc
  | dotprodtail x nil acc = acc
  | dotprodtail (h1::rest1) (h2::rest2) acc = dotprodtail rest1 rest2 (acc+h1*h2);

fun dotprod x y = 
	if length x = length y then dotprodtail x y 0 else raise DotProd;

dotprod [1, 2, 3] [~1, 5, 3];
(*dotprod [1, 2, 3] [4, 5];*)


fun fib_helper(0, x, _) = x
  | fib_helper(n, x, y) = fib_helper(n-1, y, x+y);

fun fast_fib n = fib_helper(n, 0, 1);

fast_fib 5;
fast_fib 10;



