/* Nathaniel Lim
 * CS334 - HW 10
 */

public class Sieve extends Thread {

    private Buffer<Integer> in;
    
    public Sieve (Buffer<Integer> in){
	this.in = in;
    }

    public static void main(String args[]) {
	if (args.length == 1){
		String input = args[0];
		int n = Integer.parseInt(input);
		System.out.println("Sieve starting from 2 to " + n); 
		Buffer<Integer> buffer = new Buffer<Integer>(5);
		Sieve s = new Sieve(buffer);
		s.start();
		try {		
			for (int i = 2; i <= n; i++){
				buffer.insert(new Integer(i));
			}
			buffer.insert(new Integer(-1));			
		} catch(InterruptedException e){
			System.out.println("Something was interrupted!");
		}
	} else {
		System.out.println("Just give the number n, denoting the upper limit to the Sieve");
	} 
    }

    public void run() {
	try {
		Integer first = in.delete();		
		int firstNum = first.intValue();

		if (firstNum < 0){
			return;
		} else {
			System.out.println(first.intValue());
			Buffer<Integer> out = new Buffer<Integer>(5);
			Sieve nextSieve = new Sieve(out);
			nextSieve.start();
			Integer next = in.delete();
			while (next.intValue() >= 0){
				if (next.intValue() % firstNum != 0){
					out.insert(next);
				}
				next = in.delete();
			}
			
			//Insert the negative number
			out.insert(next);
			return;
		}
	} catch (InterruptedException e){
		System.out.println("Something was interrupted!");	
	}	
    	
    }
}
