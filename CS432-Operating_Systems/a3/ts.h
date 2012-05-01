/* Nathaniel Lim
 * Header file for use of the tuplespace
 * CS432 - Assigment 3: Database
 */


#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#ifndef TS_H
#define TS_H

typedef union {
	int i;		// a 32-bit integer; size 4
	char *s; 	// a 64-bit pointer; size 8
} field; 

typedef field  *tuple;


typedef struct tuplelist  *tl;
struct tuplelist {
	tuple data;
	tl next;
};

extern void out(char * shape, ...);
extern int  inp(char * form,  ...);
extern int  rdp(char * form,  ...);
extern int  in( char * form,  ...);
extern int  rd( char * form,  ...);

#endif
