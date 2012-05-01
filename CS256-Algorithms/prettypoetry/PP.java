/* (c) Nathaniel Lim
   Williams College
   CS256, HW5, #3c
   3/20/09
   A Dynamic Programming Solution to the "Pretty Poetry" Problem
   using recursive memoization with HashMaps.
   
   Word List: words
   Pair: represents a continuous subsection of the world list: words[i, j]
   
   
   Compile: javac PP.java
   Run:		java PP {Line Length} {input.txt}
*/

import java.lang.Integer;
import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;


public class PP{

    public static HashMap<Pair, Integer> minSlackTable = new HashMap<Pair, Integer>(); //Subsection -> minSlack^2 Value
    public static HashMap<Pair, Integer> kValues = new HashMap<Pair, Integer>(); // Subsection -> Optimal spot to split on 
    public static ArrayList<String> words = new ArrayList<String>(); //A List of all the words in the input file
    public static int L; //The Desired Number of Lines Line Length

    public static void main (String [] args) throws java.io.FileNotFoundException {
		if (args.length !=2){
			System.out.println("Please Enter: {Line Length} {filename.txt}");
		} else {
			L = Integer.parseInt(args[0]);
			File f = new File(args[1]);
			Scanner sc = new Scanner (f);
			sc.useDelimiter("\\s");
			while (sc.hasNext()){
				words.add(sc.next());
			}
			int size = words.size();
			Integer answer = minSlack2(0, size-1);
			//Now the minSlackTable and the KValues are are filled in
	
			ArrayList<Integer> partitions = getPartitions(0, size-1);
			//Partitions is the list of ks that split lines	    
	
			Collections.sort(partitions);
			//Make sure that p is in order for printing
		
			// Print out the lines, go to next line once you hit a partitions(s)
			int k;
			int i = 0;
			for (int s = 0; s < partitions.size(); s++){
				k = partitions.get(s);
				while ( i <= k){
					if (i < k){
						System.out.print(words.get(i) + " ");
					} else {
						System.out.println(words.get(i));
					}
					i++;
				}
			}
	
		}
    }


    //Recursively finds the k values that are used in the optimal solution
    public static ArrayList<Integer> getPartitions(int i, int j){
		ArrayList<Integer> p = new ArrayList<Integer>();
		Pair section = new Pair(i, j);
		if (kValues.containsKey(section)){
			//Look up the best place to split
			int bestK = kValues.get(section).intValue();
			if (bestK >= j){
				//If it past the section, just split at the end
				p.add(j);
			} else {
				//Otherwise concat list of best splits of left to best splits of right 
				p.addAll(getPartitions(i, bestK));
				p.addAll(getPartitions(bestK+1, j));
			}
			return p;
		} else {
			return null;
		}
    }
	    

    //This is the dynamic programming algorithm that finds the minSlack^2 for an interval
    //Keeps minSlackTable values memoized, and keep track of the optimal k choices
    //in a table called kValues

    public static Integer minSlack2(int i, int j){
		Pair section = new Pair (i, j);
		if (minSlackTable.containsKey(section)){
			//Already has been memoized
			return minSlackTable.get(section);
		} else {
			if (j < i){
				//null interval
				minSlackTable.put(section, 0);
				return 0;
			} else if (i == j) {
				//Just one word
				int r1 = slack2(i, j);
				minSlackTable.put(section, r1);
				kValues.put(section, j+1);
				return r1;
			} else if (lineLength(i, j) <= L){
				//If the words stay within a line, it is 
				// the optimal solution.
				int r2 = slack2(i, j);
				minSlackTable.put(section, r2);
				kValues.put(section, j+1);
				return r2;
			} else {
				//Recursive Case: the lineLength(i,j) > L
				//So find the best subdivision of the section
				//Find the minimum sum of squares of slacks
				//by iterating through all the possiblities and 
				//choosing the minimum one.
		
				int minValue = Integer.MAX_VALUE;
				int k;
				int bestK = -1;
				for (k = i; k < j; k++){
					int test_slack = minSlack2(i, k) + minSlack2(k+1, j);
					if (test_slack < minValue){
						minValue = test_slack;
						bestK = k;
					}
				}
				// Update Tables, For this sections we have found 
				// The best k value to split up the line
				// And the  minSlack^2 value for that k
				minSlackTable.put (section, minValue);
				kValues.put (section, bestK);
				return minValue;
			}
			
		}
    }

    // Computes the length of a line from word i to word j inclusive
    public static int lineLength(int i, int j){
		if (i==j){
			return words.get(i).length();
		}       
		int output = 0;
		for (int a = i; a <= j; a++){
			output += words.get(a).length() + 1;
			if ( a == j){
				output--;
			}
		}
		return output;
	}
	
	// Computes the Squre of the Slack of the Line
	public static int slack2(int i, int j){
		int temp = L - lineLength(i, j);
		return temp*temp;
    }
}


// A Helper class that is used as the key for the minSlackTable and kValues HashMaps
// It represents a continuous subsection of the words
class Pair{
    int i, j;
    public Pair (){
    }
    public Pair(int i, int j){
		this.i = i;
		this.j = j;
    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(Object o){
		if (o instanceof Pair){
			Pair p = (Pair)o;
			return this.i == p.i && this.j == p.j;
		} else {
			return false;
		}
    }

    public int hashCode(){
		Integer oi = new Integer(i);
		Integer oj = new Integer(j);
		return oi.hashCode()+ 4*oj.hashCode(); 
    }
    
    public String toString(){
		return "(" + i + ", " + j + ")";
    }

}
