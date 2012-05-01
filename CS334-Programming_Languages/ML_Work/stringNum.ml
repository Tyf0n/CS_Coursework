fun charToNum c = ord c - ord #"0";

fun calcList (nil, n) = n
  | calcList (fst::rest, n) = calcList(rest, 10*n + charToNum fst);

fun stringToNum s = calcList(explode s, 0);

stringToNum "3a05";
