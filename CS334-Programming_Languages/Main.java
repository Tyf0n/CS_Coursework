class Main {
	public static void main(String [] args) {
		Container<String> str = new Container<String>("Cow");
		Container<Integer> myint = new Container<Integer>(31337);
		System.out.println("Last Instance: " + str.lastInstance.internal);
	}
}

class Container<T> {
	public T internal;
	public static Container lastInstance;
	Container(T value){
		internal = value;
		lastInstance = this;
	}
}
