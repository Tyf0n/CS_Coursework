(* Nathaniel Lim
   CS334 - HW3 - 3/2/10 *)


fun sumSquares 0 = 0
  | sumSquares n = n*n + sumSquares(n-1);

sumSquares(4);
sumSquares(5);

fun listDup (s, 0) = nil
  | listDup (s, n) = s::listDup(s, n-1);

listDup("moo", 4);
listDup(1, 2);
listDup(listDup("cow", 2), 2);

(* The function has the type: 'a * int -> 'a list
   This means that it takes a tuple of an unknown type 'a and an int
   and outputs a list containing elements of type a.  This is appropriate,
   because you can not infer what kind of elements the user wants to make
   a list out of.
 *)


(* I have chosen to simply ignore the elements on the bigger list, and not
   include them in the list of tuples *)

fun zip (nil, nil) = nil
  | zip (nil, second) = nil
  | zip (first, nil) = nil
  | zip (firsth::firstrest, secondh::secondrest) = (firsth, secondh)::zip(firstrest, secondrest);

zip([1, 2, 3, 4], ["a", "b", "c", "de"]);

(* Takes a list of tuples and recreates the two lists, one containing the first 
   elements of the tuple, and the other with the second element of the tuples.*)
fun unzip nil = ([], [])
  | unzip ((x, y)::rest) =
	let val (first, second) = unzip(rest)
	in
	  (x::first, y::second)
	end;

unzip([(1, "a"), (2, "b"), (3, "c"), (4, "d")]);

fun zip3 (nil, nil, nil) = nil
  | zip3 (nil, second, third) = nil
  | zip3 (first, nil, third) = nil
  | zip3 (first, second, nil) = nil
  | zip3 (firsth::firstrest, secondh::secondrest, thirdh::thirdrest) = (firsth, secondh, thirdh)::zip3(firstrest, secondrest, thirdrest);

zip3([1, 2, 3, 4], ["a", "b", "c", "de"], [5, 6, 7, 8]);

(* You can't write a zip_any that will take k lists and create a list of k-tuples because even when you are able to count the number of lists in the input, you can not use that information write the pattern matching for the data.  Pattern-matching relies on knowledge of the specific format of the input.*)

(* find  takes a pair and and a list, and returns the index, ~1 if not there *)
fun find_helper (x, nil, n) = ~1
  | find_helper(x, (y::ys), n) = 
	if x = y then n else find_helper(x, ys, n+1);

fun find (x, l) = find_helper(x, l, 0);
find (3, [1,2,3,4]);
find ("A", ["B", "C"]);

(* Trees *)

datatype IntTree = LEAF of int | NODE of (IntTree * IntTree);
fun sum(LEAF(x)) = x
  | sum(NODE(left, right)) = sum(left) + sum(right);

sum(LEAF 3);
sum(NODE(LEAF 2, LEAF 3));
sum(NODE(LEAF 2, NODE(LEAF 1, LEAF 1)));

fun max(x, y) = if x < y then y else x;

fun abs (x) = if x < 0 then ~1*x else x;

(* Return the maximum height of a tree *)
fun height (LEAF(x)) = 1
  | height (NODE(left, right)) = max(1 + height(right), 1 + height(left));

height(LEAF 3);
height(NODE(LEAF 2, LEAF 3));
height(NODE(LEAF 2, NODE(LEAF 1, LEAF 1)));

(*balanced returns a bool: true if the subtrees are balanced and the heights of the subtrees do not differ by more than 1*) 
fun balanced (LEAF(x)) = true
  | balanced(NODE(left, right)) = balanced(left) andalso balanced(right) andalso abs(height(left) - height(right)) <= 1;

balanced (LEAF 3);
balanced (NODE (LEAF 2, LEAF 3));
balanced (NODE(LEAF 2, NODE(LEAF 3, NODE(LEAF 1, LEAF 1))));

(*This implemenation of balanced is inefficient because it uses height and balanced in its recursive call, so the tree is really traversed twice for one call of balanced.  One way you could make this more efficient is to write a helper function that would return a tuple containing both the true value for balanced, and the current height of the node, that way you only have to traverse the tree once. *)

(* Stack operations *)
datatype OpCode = PUSH of real | ADD | MULT | SUB | DIV | SWAP;
type Stack = real list;
fun eval (nil, first::rest) = first
   | eval (PUSH(n)::ops, stack) = eval(ops, n::stack)
   | eval (ADD::ops, a::b::rest) = eval(ops, (b+a)::rest)
   | eval (MULT::ops,a::b::rest) = eval(ops, (b*a)::rest)
   | eval (SUB::ops, a::b::rest) = eval(ops, (b-a)::rest)
   | eval (DIV::ops, a::b::rest) = eval(ops, (b/a)::rest)
   | eval (SWAP::ops,a::b::rest) = eval(ops, b::a::rest)
   | eval (_, _) = 0.0;

eval([PUSH(2.0), PUSH(1.0), SUB], []);
eval([PUSH(2.0), PUSH(3.0), DIV], []);
