;val x = 5;
;fun f(y) = (x+y)-2;
;fun g(h) = let val x = 7 in h(x) end;
;let val x = 10 in g(f) end;

val x = 5;
fun f(y) = 
	let val z = [1, 2, 3]
		fun g(w) = w + x + y
	in
		g
	end;
val h = let val x = 7 in f(3) end;
h(2);
