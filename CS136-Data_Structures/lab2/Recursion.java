/**
 * Nathaniel Lim
 * Williams College, CS136 Spring 2008
 * LAB 2: Recursion
 * February 25, 2008
 * njl2@williams.edu
 */
import java.util.ArrayList;

public class Recursion {
    private static final String delimiter  = ", ";

    public static void main (String [] args) {    	
    }
    /**
     * static test methods of each recursive method.
     */
    public static void testSumDigits(){
        System.out.println(sumDigits(1234));
    }
    public static void testCountcannonballs(){
        System.out.println(countCannonballs(4));
    }
    public static void testIsPalindrome(){
            System.out.println("hannah   " + isPalindrome("hannah") );
            System.out.println("computer  " + isPalindrome("computer") );
    }
    public static void testIsBalanced(){
            System.out.println("[{}()]  " +  isBalanced("[{}()]"));
            System.out.println("{{}{}())(]]    " + isBalanced("{{}{}())(]]"));
    }
    public static void testSubstring(){
            ArrayList<String> result = new ArrayList<String>();
            generateSubstrings("ABCD", result);
            printArray(result);
    }
    public static void testPerm(){
        ArrayList<String> result = new ArrayList<String>();
        generatePermutations("ABCD", result);
        printArray(result);
    }
    public static void testToBinary(){
        for (int i = 0; i < 33; i++) {
            System.out.println("i" + ":  " + toBinary(i) );
        }
    }
    public static void testSubsetSum(){
        int [] set = {1, 2, 3, 4, 5};
        System.out.println("100   " + subsetSum(set, 100, new ArrayList<Integer>()));
        System.out.println("9     " + subsetSum(set, 9,   new ArrayList<Integer>()));
    }
    // 1.A
    /** This method recursively add the last digit 
     *  to the sum of the rest of the digits.
     */
    public static int sumDigits(int n) {
        assert n >= 0;
        if (n==0){
            return 0;
        }else {
            return (n%10)+ sumDigits(n/10);
        }
    }
    //1.B
    /** Recursively sums up the bottom layer of the
     *  cannonball pyramid
     */
    public static int countCannonballs(int height){
	assert height >= 0;
	if ( height == 0 ){
	    return 0;
	}else {
	    return height*height + countCannonballs(height -1);
	}
    }
    
    // 1.C
    /** Recursively checks is the first and last
     *  characters are equal, removes them, and
     *  checks the resultant string in the same way.
     *  The 'lazy' && operator insures that if the 
     *  first and last characters are not equal,
     *  the recursion stops, and the method returns false.
     */
    public static boolean isPalindrome(String s){
    	assert s!=null;
    	int l = s.length();
	if (l == 0 || l == 1) {
	    return true;
	} else {
            return (s.charAt(0)==s.charAt(l -1)) &&
		isPalindrome(s.substring(1,l-1));
	}
    }
    
    // 1.D

    /** If the string contains: (), {}, [], remove them
     *  and then figure out if this resultant string
     *  is balanced, or else return false.
     */
    public static boolean isBalanced(String s){    	
    	assert (s != null);
    	if (s.length() == 0){
    		return true;
    	} else {  
            if (! (s.contains("()") ||
                   s.contains("{}") || 
    		   s.contains("[]"))  ){    			
                return false;
            } else {
                s = s.replaceAll("\\(\\)", "");
                s = s.replaceAll("\\{\\}", "");    	
                s = s.replaceAll("\\[\\]", "");    	
                return isBalanced(s);
            }    		
    	}    	
    }
    
    
    //2.A    
    /** To generate all the substrings, split the
     *  string into a one character long one, and the rest
     *  then create all the substrings concatenating the 'head' with 
     *  the substrings of the 'rest' String, and add them to the 
     *  accumulation of substrings ( 'result' ).
     */
    public static void generateSubstrings(String s, ArrayList<String> result){
    	assert(s != null && result != null);
    	if (s.length() == 0){
            result.add("");
    	} else {
            String head = s.substring(0,1);
    	    String rest = s.substring(1); 
            generateSubstrings(rest, result);
            ArrayList <String> temp = new ArrayList<String>();
            for (int i = 0; i < result.size(); i++){   			
                temp.add( head +  result.get(i) );
            }
            result.addAll (temp);			
    	}
    }    
    
    
    //2.B

    /** The helper method permuteHelper is needed because
     * as the recursion occurs, string accumulation needs to passed
     * on.  permuteHelper has two strings as parameters that make
     * the distinction between characters chosen, and those not.
     */
    public static void generatePermutations(String str, ArrayList<String> result){
    	assert str != null;
    	permuteHelper("", str, result);;
    }    
    
    /** The algorithm for evaluating the permutations of a string
     * is to have an accumulation variable: 'soFar', and rest,
     * and iterating through rest (characters not chosen yet),
     * accumulate each character in  different cases, and repeat the
     * process on the remaining unchosen characters ( 'rest'), until
     * there are no more characters to be chosen.
     */
    private static void permuteHelper(String soFar, String rest, ArrayList<String> result){
	assert(soFar != null && rest != null && result != null);
    	for (int i = 0; i < rest.length(); i++){
            String newSoFar = new String(soFar);
            String newRest = "";			
            int y = 0;
            for (int j = 0; j < rest.length(); j++){
		if (j != i){
                    newRest+=rest.charAt(j);
			y++;
		}
            }			
            newSoFar += rest.charAt(i);
            if (newRest.length() == 0){
                result.add(newSoFar);				
            } else {				
                permuteHelper(newSoFar, newRest, result);
            }			        
        }
    }
    
    //2.C
    /**
     * The following two methods act in conjunction
     * to convert decimal to binary without trailing 
     * zeros, but still defined for number=0
     */
    public static String toBinary(int number) {
    	assert number >=0; 
    	if (number == 0){
            return "0";
    	} else {
            return toBinaryHelper(number);
    	}    	
    }    
    private static String toBinaryHelper(int number){
    	assert number >=0;
    	String output = "";
    	if (number > 0){
            output = toBinaryHelper(number/2) + number%2;
    	}    	
    	return output;
    }
    
    //2.D   
    /**
     * This method recursively creates all the sums of the subsets
     * of the set, and consistently checks whether these sums
     * equal the target.
     * The sums are created by adding the first integer to all
     * the sums from the rest of the integers, and adding those new
     * sums to the accumulator: 'result'
     */
    public static boolean subsetSum(int[] sets, int targetSum, ArrayList<Integer> result){
    	assert (sets != null && result != null);
    	if (sets.length == 0){
            if (targetSum == 0){
                return true;
            } else {
                return false;
            }
    	} else if (sets.length == 1){
            result.add(sets[0]);
            return targetSum == sets[0];
    	}
    	else {
            int head = sets[0];
            int [] rest = cdr(sets);
            if (subsetSum (rest, targetSum, result)){
                return true;
            } else {
                ArrayList <Integer> temp = new ArrayList<Integer>(); 
                if (head == targetSum){
                    return true;
                }
                temp.add(head);
                for (int i = 0; i < result.size(); i++){        			
                    if (head + result.get(i) == targetSum){
                        return true;
                    }
                    temp.add( head +  result.get(i)  );
                }
                result.addAll (temp); 			
                return false;
            }   		
    	}
    }
    
    /** cdr is the method in LISP meaning: 
     * return the rest of the list
     * excluding the first element*/
    private static int[] cdr (int[] input){
    	assert input != null;
    	if (input.length <=1){
    		return null;
    	} else {
            int [] output = new int[input.length-1];
            for (int i = 0; i < output.length; i++){
    		output[i] = input[i+1];
            }
            return output;
    	}
    }    
    
    private static void printArray(ArrayList x) {
    	System.out.print("[");   	
    	for (int i = 0; i < x.size(); i++){
            System.out.print(x.get(i) + delimiter);
    	}
    	System.out.println("]");		
    }
}
