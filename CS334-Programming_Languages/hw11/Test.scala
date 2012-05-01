object Test {
	def main(args : Array[String]): Unit = {
		
		println("Trying Call-By-Name");
		firstValueByName (1, loop);		
		println("Call-By-Name Terminated");
		
		println("Trying Call-By-Value");
		firstValue (1, loop);		
		println("Call-By-Value Terminated");

	}


	def loop: Int = loop
	def firstValue (x: Int, y: Int) = x
	def firstValueByName (x: => Int, y: => Int) = x

	def square(x: Int): Int = {
		x*x;
	}

	def printcubes (x: Int, y: Int): Unit = {
		x <= x*x*x;
		y <= y*y*y;
		print(x + ", ");
		println(y);
	}
	
	def printcubes2 (x: => Int, y: => Int): Unit = {
		x <= x*x*x;
		y <= y*y*y;
		print(x + ", ");
		println(y);
	}
}


