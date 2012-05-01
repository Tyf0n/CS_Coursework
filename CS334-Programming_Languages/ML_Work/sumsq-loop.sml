
fun sumSquares 0 = 0
  | sumSquares n = n*n + sumSquares(n-1);

sumSquares 10;

fun sumSquareLoop n =
  let val i = ref 0;
      val total = ref 0
  in
      while (!i <= n) do (
        total := !total + !i * !i;
        i := !i + 1
      );
      !total
  end;

sumSquareLoop 10;