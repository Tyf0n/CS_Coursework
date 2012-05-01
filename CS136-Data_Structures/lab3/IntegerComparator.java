/*
 * Nathaniel Lim
 * Williams College: CSCI 136
 * 3/3/08
 * Lab 3: Dynamic Array
 */

import java.util.Comparator;



public class IntegerComparator implements Comparator<Integer> {
    /*
     * Implementation of Comparator specifically for Integers
     */
	public int compare(Integer arg0, Integer arg1) {
		return arg0.intValue() - arg1.intValue();
	}

	

}
