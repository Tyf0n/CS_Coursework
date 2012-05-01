/*
template <typename T>
T max(T x, T y) 
{
    return x < y ? y : x;
}
*/

#include <iostream>
#include <string>
using namespace std;

template <class T> class Container
{
  public:
    T internal;
    static Container lastInstance;
    Container(T value);
};

template <class T> Container<T>::Container(T value) {  // constructor
    internal = value;
    lastInstance = this;
}

int main ()
{
	Container<char*> str = new Container<char*>("Cow");
	Container<int> myint = new Container<int>(31337);
	cout << "Last Instance: " << str.lastInstance.internal;
}
