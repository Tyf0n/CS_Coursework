#include <iostream>
using namespace std;

class Vehicle { 
public: 
  int x; 
  virtual void f() {
    cout << "Vehicle.f" << endl;
  }
  void g() {
    cout << "Vehicle.g" << endl;
  }
}; 

class Airplane : public Vehicle { 
public: 
  int y; 
  virtual void f() {
    cout << "Airplane.f" << endl;
  }
  virtual void h() {
    cout << "Airplane.h" << endl;
  }
}; 


void inHeap() { 
  Vehicle *b1 = new Vehicle(); // Allocate object on the heap
  Airplane *d1 = new Airplane(); // Allocate object on the heap
  b1->x = 1;
  d1->x = 2;
  d1->y = 3;

  cout << "b1->f() "; b1->f();
  cout << "d1->f() "; d1->f();

  b1 = d1;  // Assign derived class object to base class pointer

  cout << "b1->f() "; b1->f();
  cout << "d1->f() "; d1->f();
} 

void onStack() { 
  Vehicle b2; // Local object on the stack
  Airplane d2;  // Local object on the stack
  b2.x = 4;
  d2.x = 5;
  d2.y = 6;

  cout << "b2.f() "; b2.f();
  cout << "b2.f() "; d2.f();

  b2 = d2;  // Assign derived class object to base class variable

  cout << "b2.f() "; b2.f();
  cout << "d2.f() "; d2.f();
} 

int main() { 
  inHeap();
  onStack();
} 

