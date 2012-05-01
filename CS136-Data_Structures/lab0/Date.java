/*
 * Nathaniel Lim
 * Williams College CSCI136
 * February 8, 2008
 * 
 * This class is a Date object, that can determine the day of the week
 * given the month, day, and year using a simple algorithm.
 */
import java.util.Random;
import java.util.Scanner;
import structure5.*;

public class Date {
    /**
     * The following are the instance variables of a date, month, day number, and year
     * as well as some constant arrays that will help with the associations for numbers
     * in the computation, and output. Also included is a constructor for the Date class.
     */
    private int month, dayNum,year;    
    private final int[] adj = {0,1,4,4,0,2,5,0,3,6,1,4,6};
    private static final int[] daysPerMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private final String [] dayNames = {"Saturday", "Sunday", "Monday",
	"Tuesday", "Wednesday", "Thursday", "Friday"};
    
    public Date(int month, int dayNum, int year) {
        this.month = month;
        this.dayNum = dayNum;
        this.year = year;
    }
    
    /**
     * Implements the algorithm for finding the day of the week for a given date 
     */
    public String getDay(){
        Assert.pre(dayNames.length == 7, "dayNames Array is bad");
	Assert.pre(adj.length == 13, "adjustments Array is bad");
	int day = (adj[month] +dayNum +(year-1900) + (year-1900)/4) % 7;
	//If it is a leap year and the month is either a January(1) or a February(2), subtract 1 
        if (year % 4 == 0 && (month == 1 || month == 2)   ){    
        	day--;
        }
	Assert.post(  (day <=6) && (day >= 0), "Day out of bounds"  );
	
        return dayNames[day];
    }
    public String toString(){
        return month + "/" + dayNum + "/" + year; 
    }    
    /**
     * Generates a random date.
     * The cases of varying month lengths, with and without
     * leap years are accounted for.
     * 1.  Finds a random year and month.
     * 2.  Checks for whether the year is a leap year
     * 3.  Sets month length for February (special case) and other months
     */
    public static Date genRandomDate(){
	Assert.pre(daysPerMonth.length == 13, "daysPerMonth array is bad"); 
	Random r = new Random();
	int randomYear = 1900 + r.nextInt(200);
	int randomMonth = r.nextInt(12) + 1;
	int randomDayLimit;
	if (randomMonth != 2) { //If the random month is not a February
	    randomDayLimit = daysPerMonth[randomMonth];
	} else {
	    if (randomYear % 4 == 0) {// If the random year is a leap year
	    	randomDayLimit = 29;
	    }else {
	    	randomDayLimit = 28;
	    }
	}
	int randomDay = r.nextInt(randomDayLimit) + 1;
	Assert.post( (randomMonth >= 0) && (randomMonth <= 12), "Month out of bounds"  );
	Assert.post( (randomDay >= 0) && (randomDay <= randomDayLimit), "Day out of bounds"  );
	Assert.post ((randomYear >= 1900)&&(randomYear <= 2099),"Year out of bounds");
	return new Date (randomMonth, randomDay, randomYear);
    }    
    /**
     * The main method allows the user to make guesses for the 
     * days of the week for random dates.  The method records and
     * prints out the time (in seconds) for 10 correct guesses 
     */
    public static void main (String [] args) {
	    int numCorrect = 0;
	    long start = System.currentTimeMillis();		
	    while ( numCorrect < 10){
		 //Generating the random date, and printing it
		 Date randomDate = genRandomDate(); 			
		 System.out.println(randomDate);		    
		 //Prompting the user to make a guess about what day it is.
		 System.out.println("Prediction?");
		 Scanner s = new Scanner(System.in);
		 String guess = "";
		 if (s.hasNext()){
		     guess = s.next();
		 }
		 //Figuring out the correct day and 
		 //checking whether the guess is right or wrong.
		 String actualDay = randomDate.getDay();
		 if (actualDay.toUpperCase().equals(guess.toUpperCase())){
		     numCorrect++;
		     System.out.println("Correct: " + guess);
		 }else {
		     System.out.println("Incorrect, it is: " + actualDay);
		 }	  
	    }
	    //Printing out the time taken (in seconds) to make 10 correct guesses
	    long end = System.currentTimeMillis();
	    System.out.println("10 correct took: " + (end-start)/1000 + " seconds");
    }
}
