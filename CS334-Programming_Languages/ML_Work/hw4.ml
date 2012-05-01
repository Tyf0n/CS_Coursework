(* Nathaniel Lim
   CS334 - HW4 - 3/9/10 *)

(* Always include these at the top of your SML files so 
   large datatype values and lists will print fully *)

Control.Print.printDepth := 100;
Control.Print.printLength := 100;

(*    *)

datatype 'a tree = LEAF of 'a | NODE of ('a tree * 'a tree);

fun maptree (f, LEAF(x)) = LEAF(f(x))
  | maptree (f, NODE(left, right)) = NODE(maptree(f, left), maptree(f, right));

(* #4 Type inference and Bugs *)

fun append (nil, l)  = l
  | append (x::l, m) = x::append(l, m);


(* Currying.  Higher order ML functions *)
fun power (exp, base) = 
	if exp = 0 then 1
	else base * power(exp-1, base);

fun cpower exp base = 
	if exp = 0 then 1
	else base * cpower (exp-1) base;

fun curry f a b = f(a, b);
 
fun uncurry f (a, b) = f a b;


uncurry(curry(power));
curry(uncurry(cpower));

(*Disjoint Unions stuff*)

datatype IntString = tag_int of int | tag_str of string;
val x = if 1 < 2 then tag_int(3) else tag_str("here fido");
let val tag_int (m) = x in m + 5 end;

(* Random Art Testing Functions *)

fun for (low:int, high:int, f:(int -> unit)) =
  let val i = ref low;
  in
      while (!i <= high) do (
        f(!i);
        i := !i + 1
      );
      ()
  end;

for (2, 5, (fn x => (print ((Int.toString(x))^"\n"))));

