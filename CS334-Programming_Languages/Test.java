import java.util.*;
import java.util.List;
import java.lang.Integer;
import java.lang.Number;

public class Test{

	public static void main (String [] args){
	
		List<Number> listofNum = new ArrayList<Number>();
		List<Integer> listofInt = new ArrayList<Integer>();
		
		Integer f = new Integer(3);

		listofInt.add(f);
		System.out.println(addAll(listofInt, listofNum));

	}
/*
	public void addAll_NG(List src, List dest){
		for (T o : src){
			dest.add(o);
		}
	}
*/

	public static <T> void addAll0(List<T> src, List<T> dest) {
		for (T o : src){
			dest.add(o);
		}
	}

	public static <T> void addAll1(List<? extends T> src, List<T> dest) {
		for (T o : src){
			dest.add(o);
		}
	}

	public static <T> void addAll2(List<T> src, List<? super T> dest) {
		for (T o : src){
			dest.add(o);
		}
	}
	public static <T> T addAll(List<T> src, List<? super T> dest) {
		T last = null;
		for (T o : src){
			dest.add(o);
			last = o;
		}
		return last;
	}



}
