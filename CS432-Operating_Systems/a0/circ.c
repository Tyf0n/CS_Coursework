/**
A C implementation of a Circular Queue
Nathaniel Lim
CS432
2/9/2011

I left the main method in this program to show that it works for a simple case.
Since my hash.c does not rely on the this Circular Queue data structure, I wanted
to turn this program in as a stand-alone file. 

However if you want to run your own test code using the Circular Queue, you should
comment out the main method in this file.
*/

#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include "circ.h"


extern cirq cq_alloc(){ // allocate a queue
	struct element *tail;
	cirq q;	
	q = (struct element **)malloc(sizeof(struct element*));
	tail = (struct element *)malloc(sizeof(struct element));
	tail = NULL;	
	*q = tail;
	return q;
}
    


/**
	Returns the size of the circular queue.
	Because the cirq is just a pointer to a pointer to an element, so we can't store a member
	for the size, and since it is a linked list structure, we must traverse to calculate the size.

	Assigning each element a position in the queue would make cq_size constant, except it forces
	either cq_enq or cq_deq to have to traverse the structure to update the positions.

	Since I believe that the user of cirq would be enqueuing and dequeuing much more often than 
	requesting the size, that this is a fair solution.
*/
extern int cq_size(cirq q){ 
	int output = 0;

	struct element *tail, *current, *next_node;
	if (q != NULL) {
		tail = *q;
		if(tail != NULL){
			++output;	
			current = tail->next;
			while (current != tail){
				next_node = current->next;
				++output;
				current = next_node;
			}
		}
	}
	return output;
}


extern void cq_enq (cirq q, void *value){ 
	struct element *tail, *ein, *head;
	
	ein =  (struct element *)malloc(sizeof(struct element));
	ein->data = value;
	tail = *q;
	if (tail == NULL) { //First Element Enqueued.
		ein->next = ein;
		tail = ein;
	} else { //Manipulate Pointers to Add the new Element to the tail
		ein->next = tail->next;
		tail->next = ein;
		tail = ein;
	}
	*q = tail;	
}

extern void *cq_deq(cirq q) { // dequeue and return a queue value
	struct element *tail, *head;
	
	tail = *q;	

	if(tail == NULL) {
		return NULL;
	} else {
		head = tail->next;
		void * output = head->data;
		if (head == tail){
			tail = NULL;
			*q = tail;			
		} else {
			tail->next = head->next;		
		}
		return output;
	}
}


extern void *cq_peek(cirq q){ // return the value at the queue head
	struct element *tail, *head;
	
	tail = *q;	

	if(tail == NULL) {
		return NULL;
	} else {
		head = tail->next;
		if (head == NULL) {
			return NULL;
		}
		return head->data;
	}
}
extern void cq_rot(cirq q) { // requeue the head element at the tail
	void *v;	
	v = cq_deq(q);
	cq_enq(q,v); 
}

extern void cq_free(cirq q){ // return all space allocated to queue
	struct element *tail, *current, *next_node;
	if (q != NULL) {
		tail = *q;
		if(tail != NULL){	
			current = tail->next;
			while (current != tail){
				next_node = current->next;
				free (current);
				current = next_node;
			}

			free(tail);
		}
		free(q);
	}			
}    

int main(){
	int x, y, z; 
	int *px, *py, *pz;
	int *peekedVal, *poppedVal;

	x = 3;
	y = 4;
	z = 5;
	px = &x;
	py = &y;
	pz = &z;
	cirq mycq = cq_alloc();	
	printf("Just allocated cq,  Size is now: %d\n", cq_size(mycq));
	cq_enq(mycq, px);
	printf("Just pushed %d.     Size is now: %d\n", *px, cq_size(mycq));
	cq_enq(mycq, py);
	printf("Just pushed %d.     Size is now: %d\n", *py, cq_size(mycq));
	cq_enq(mycq, pz);
	printf("Just pushed %d.     Size is now: %d\n", *pz, cq_size(mycq));
	cq_rot(mycq);
	printf("Just rotated,      Size is now: %d\n", cq_size(mycq));
	poppedVal = (int *)cq_deq(mycq);		
	printf("Just popped off the value: %d, Size is now: %d \n", *poppedVal, cq_size(mycq));
	poppedVal = (int *)cq_deq(mycq);		
	printf("Just popped off the value: %d, Size is now: %d \n", *poppedVal, cq_size(mycq));
	poppedVal = (int *)cq_deq(mycq);		
	printf("Just popped off the value: %d, Size is now: %d \n", *poppedVal, cq_size(mycq));
	peekedVal = (int *)cq_peek(mycq);	
	if (peekedVal != NULL) {
		printf("Peeked and saw: %d\n", *peekedVal);
	} else {
		printf("Peeked and saw nothing\n");
	}

	cq_free(mycq);
	return 0;
}
