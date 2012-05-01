/**
 * Nathaniel Lim
 * Williams College: CSCI 136
 * 3/3/08
 * Lab 3: Dynamic Array
 */

import java.util.Random;

public class Date2 {
	//Num of Days in a given month, used by numDays
	private static final int[] daysPerMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	//Adjustments used in the algorithm implemented in getDayOfWeek;
	private static final int[] adj = {0,1,4,4,0,2,5,0,3,6,1,4,6};
	//0 = Saturday, used in dayOfWeekToString
	private static final String [] dayNames = {"Saturday", "Sunday", "Monday", 
			"Tuesday", "Wednesday", "Thursday", "Friday"};
	//Used in toString()
	private static final String [] monthNames = {"", "January", "February", "March", "April", 
		"May", "June", "July", "August", "September", "October", "November", "December"};
	
	
	private int d;
	private int m;
	private int y;
	
	public Date2(int d, int m, int y){
		this.d = d;
		this.m = m;
		this.y = y;
	}
	
        //Method structure taken from solution to Lab 0 by Morgan McGuire
	public int getDayOfWeek(){
		int relYears = y - 1900;
		int x = adj[m] + d + relYears + relYears/4;
		//The algorithm subtracts one from the adj table
                //if it is a leap year and the month is Jan. or Feb.
                if (isLeapYear(y) && m <= 2){
			x--;
		}
		return x % 7;
	}
	
	
	public static Date2 random(){
		Random r = new Random();
		int randomYear = 1900 + r.nextInt(200);
		int randomMonth = r.nextInt(12) + 1;
		int randomDayLimit = numDays(randomMonth, randomYear);		
		int randomDay = r.nextInt(randomDayLimit) + 1;
		return new Date2 (randomMonth, randomDay, randomYear);
    }    
	
	public static String dayOfWeekToString(int n){
            //n = 0 returns "Saturday".
            return dayNames[n];
	}
	
       //Source taken from solution to Lab 0 by Morgan Mcguire
	private static boolean isLeapYear(int y){
            
		assert y >= 1900;
		/**
		 * Leap years are divisible by 4, except for centuries, 
		 * except for those divisble by 400
		 */
		return 
			((y % 4) == 0) &&
			((y % 100)!=0) ||
			((y % 400) == 0);
	}
	
	
	
	public static int numDays(int m, int y){
		int out = daysPerMonth[m];
		if (m == 2 && isLeapYear(y)){
			out = 29;
		}
		return out;
	}


	public int getDay() {
		return d;
	}
	public int getMonth() {
		return m;
	}
	public int getYear() {
		return y;
	}
	
	public String toString(){
		return monthNames[m] + " " + d + ", " + y;
	}
}
