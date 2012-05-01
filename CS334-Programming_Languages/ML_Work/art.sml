(* Nathaniel Lim
 * Tues March 9, 2010
 * art.sml
 * cs334 - HW 4
 *)

use "expr.sml";


(*
   A type abbreviation for a function that takes a
   two integers lo and hi, and returns a random number
   in the range [lo,hi].
 *)
type RandomGenerator = (int * int) -> int;


(******************* Functions you need to write **********)
    
(* for: int * int * (int -> unit) -> unit
   Applies the function f to all the integers between low and high
   inclusive; the results get thrown away.
 *)

fun for (low:int, high:int, f:(int -> unit)) =
  let val i = ref low;
  in
      while (!i <= high) do (
        f(!i);
        i := !i + 1
      );
      ()
  end;


(* build: int * RandomGenerator -> Expr 
   Build an expression tree.
   I have done this recursively.
   I chose to not terminate the building ever 
   on a call that had a non-zero depth, so this forces
   build to make really complicated expressions.

 *)
fun build (depth, rand: RandomGenerator) =
	let val a = rand(1, 7);
	    val b = rand(1, 2)
	in 
	    if depth = 0 then 
		if b = 1 then buildX()
		else          buildY()
	    else
		if      a = 1 then buildSine  (build(depth-1, rand))
		else if a = 2 then buildCosine(build(depth-1, rand))
		else if a = 3 then buildAverage( build(depth-1, rand), build(depth-1, rand) )
		else if a = 4 then buildTimes  ( build(depth-1, rand), build(depth-1, rand) )
		else if a = 5 then buildGeomean ( build(depth-1, rand), build(depth-1, rand) )
		else if a = 6 then buildAbs( build(depth-1, rand) )
	 	else 		   buildGeomean3(build(depth-1, rand) , build(depth-1, rand), build(depth-1, rand) )
   	end;
		


(* makeRand int * int -> (int * int -> int)
   Returns a function that, given a low and a high, returns
   a random int between the limits.  seed1 and seed2 are the
   random number seeds.  Pass the result of this function
   to build 

   Example:
      let val rand = makeRand(10,39) in 
        let x = rand(1,4) in 
          (* x is 1,2,3, or 4 *)
        end
      end;
 *)
fun makeRand(seed1, seed2) : RandomGenerator =
    let val generator = Random.rand(seed1,seed2)
    in
        fn (x,y) => Random.randRange(x,y)(generator)
    end ;


(********************* Bitmap creation code ***************)

(* 
   You should not have to modify the remaining functions.

   Add testing code to the bottom of the file.

 *)
  
(* 
 Converts an integer i from the range [-N,N] 
 into a real in [-1,1] 
 *)
fun toReal(i,N) = (Real.fromInt i) / (Real.fromInt N);
    
(* 
 Converts real in [-1,1] to an integer in the range [0,255] 
 *)
fun toIntensity(z) = Real.round(127.5 + (127.5 * z));
    
    
(* emitGrayscale :  ((real * real) -> real) * int -> unit
 emitGrayscale(f, N) emits the values of the expression
 f (converted to intensity) to the file art.pgm for an 
 2N+1 by 2N+1 grid of points taken from [-1,1] x [-1,1].
 
 Look up "portable pixmap" in Wikipedia for a full 
 description of the file format, but it's essentially 
 a one-line header followed by one byte (representing gray
 value 0..255) per pixel.
 *)
fun emitGrayscale (f,N) =
    let
        (* Open the output file and write the header *)
        val stream = TextIO.openOut "art.pgm"
        val N2P1 = N*2+1    (* Picture will be 2*N+1 pixels on a side *)
        val _ = TextIO.output(stream,"P5 " ^ (Int.toString N2P1) ^ " " ^
                              (Int.toString N2P1) ^ " 255\n")
            
        val _ = 
            for (~N, N, 
                 fn ix =>
                 for (~N, N, 
                      fn iy =>
                      let
                          (* Convert grid locations to [-1,1] *)
                          val x = toReal(ix,N)
                          val y = toReal(iy,N)
                              
                          (* Apply the given random function *)
                          val z = f(x,y)
                              
                          (* Convert the result to a grayscale value *)
                          val iz = toIntensity(z)
                      in
                          (* Emit one byte for this pixel *)
                          TextIO.output1(stream, Char.chr iz)
                      end))
            
        val _ = TextIO.closeOut stream
    in
        ()
    end


(* doRandomGray : int * int * int -> unit
 Given a depth and two seeds for the random number generator,
 create a single random expression and convert it to a
 grayscale picture with the name "art.pgm"
 *)
fun doRandomGray (depth,seed1,seed2) =
    let
        (* Initialize random-number generator g *)
        val g = makeRand(seed1,seed2)
            
         (* Generate a random expression, and turn it into an ML function *)
        val e = build(depth,g)
        val f = eval e
            
        (* 301 x 301 pixels *)
        val N = 150
            
            
    in
        print (exprToString e);
        (* Emit the picture *)
        emitGrayscale(f,N)
    end

(* emitColor : (real*real->real) * (real*real->real) *
               (real*real->real) * int -> unit
 emitColor(f1, f2, f3, N) emits the values of the expressions
 f1, f2, and f3 (converted to RGB intensities) to the output
 file art.ppm for an 2N+1 by 2N+1 grid of points taken 
 from [-1,1] x [-1,1].
 
 Look up "portable pixmap" in Wikipediax for a full 
 description of the file format, but it's essentially a 
 one-line header followed by three bytes (representing 
 red, green, and blue values in the range 0..255) per pixel.
 *)	
fun emitColor (f1,f2,f3,N) =
    let
        val stream = TextIO.openOut "art.ppm"
            
        val N2P1 = N*2+1
        val _ = TextIO.output(stream,"P6 " ^ (Int.toString N2P1) ^ " " ^
                              (Int.toString N2P1) ^ " 255\n")
            
        val _ = 
            for (~N, N, 
                 fn ix =>
                 for (~N, N, 
                      fn iy =>
                      let
                          (* Map grid locations into [-1,1] *)
                          val x = toReal(ix,N)
                          val y = toReal(iy,N)
                              
                          (* Apply the given random function *)
                          val z1 = f1(x,y)
                          val z2 = f2(x,y)
                          val z3 = f3(x,y)
                              
                          (* Convert the result to R, G, and B values *)
                          val iz1 = toIntensity(z1)
                          val iz2 = toIntensity(z2)
                          val iz3 = toIntensity(z3)
                      in
                          (* Emit three byte for this pixel *)
                          TextIO.output1(stream, Char.chr iz1);
                          TextIO.output1(stream, Char.chr iz2);
                          TextIO.output1(stream, Char.chr iz3)
                      end))
            
        val _ = TextIO.closeOut stream
    in
        ()
    end

(* doColor : int * int * int -> unit
 Given a depth and two seeds for the random number generator,
 create a single random expression and convert it to a
 color picture with the name "art.ppm"  (note the different
 extension from toGray) 
 *)
fun doRandomColor (depth,seed1,seed2) =
    let
        (* Initialize random-number generator g *)
        val g = makeRand(seed1,seed2)
            
        (* Generate a random expressions, and turn them into ML functions *)
        val e1 = build(depth,g)
        val e2 = build(depth,g)
        val e3 = build(depth,g)
        val f1 = eval e1
        val f2 = eval e2
        val f3 = eval e3
            
        val _ = (print "red   = "; print (exprToString e1); print "\n")
        val _ = (print "green = "; print (exprToString e2); print "\n")
        val _ = (print "blue  = "; print (exprToString e3); print "\n")
            
        (* Open the output file and write the header *)
        val N = 150
            
    in
        (* Emit the picture *)
        emitColor(f1,f2,f3,N)
    end;


(*************** Insert Testing Code Here ******************)


(* build (2, makeRand(10,11)); 
val sampleExpr =
      buildCosine(buildSine(buildTimes(buildCosine(buildAverage(buildCosine(
      buildX()),buildTimes(buildCosine (buildCosine (buildAverage
      (buildTimes (buildY(),buildY()),buildCosine (buildX())))),
      buildCosine (buildTimes (buildSine (buildCosine
      (buildY())),buildAverage (buildSine (buildX()), buildTimes
      (buildX(),buildX()))))))),buildY())));

doRandomGray(5, 33, 89);
doRandomColor(5, 33, 89);
val f = eval sampleExpr;
emitGrayscale(f, 150)

val ex1 = build(3, makeRand(1212, 4932));
exprToString(ex1); *)

doRandomColor(5, 33, 89);





        
