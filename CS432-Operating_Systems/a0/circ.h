/**
A C Header File for Circular Queue

Nathaniel Lim
CS432
2/9/2011

If you do include this header file in your test program, be sure to comment out the main method
in circ.c

*/

#ifndef CIRQ_H
#define CIRQ_H

typedef struct element **cirq;           // cirq handle

struct element {
	void *data;
	struct element *next;
};

extern cirq cq_alloc(void);              // allocate a queue
extern int cq_size(cirq q);              // return the size of a queue
extern void cq_enq(cirq q, void *value); // add a value to the queue
extern void *cq_deq(cirq q);             // dequeue and return a queue value
extern void *cq_peek(cirq q);            // return the value at the queue head
extern void cq_rot(cirq q);              // requeue the head element at the tail
extern void cq_free(cirq q);             // return all space allocated to queue
#endif

