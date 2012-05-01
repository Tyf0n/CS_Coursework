/* Nathaniel Lim
 * CS432 - Assignment 4
 * Database, Multithreaded
 */
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <ctype.h>
#include "hash.h"
#include "ts.h"
#define NUM_SHAPES 10
#define MAX_TUPLE_SIZE 10
#define MAX_TUPLES 100
#define MAX_SHAPE_SIZE 10


static hash tuplespace;
static int firstCalled = 1;
static pthread_mutex_t tuplespace_mutex;
static pthread_cond_t tuple_added;
static pthread_once_t once_control = PTHREAD_ONCE_INIT;
/*
 The tuplespace is a hashTable that maps:  char * shape --> tuplelist
 A tuplelist (defined in ts.h), is a linked list, containing its tuple data
	and a pointer to the next node (another tuplelist) in the tuplelist
 A tl is a pointer to a tuplelist
 A tuple (defined in ts.h) is an array (declared as pointer) of field unions
*/

void printTuples(char * shape){
	tl list = ht_get(tuplespace, shape);
	printf("Tuples with Shape: %s\n", shape);
	while(list != NULL) {
		printf("(");
		int i;
		for (i = 0; i < strlen(shape); i++) {
			if (shape[i] == 'i') {
				printf("%d, ", list->data[i].i);
			} else {
				printf("%s, ", list->data[i].s);
			}
		}
		printf(") --> ");
		list = list->next;
	}
	printf("\n");
}

/*Copy characters from source to sink, excluding a certain character
  For converting form to shape
*/
char* strcpy_exclude(char * sink, char *source, char c) {
	int i = 0;
	int j = 0;	
	while (source[i]!='\0') {
		if(source[i] != c) {
			sink[j] = source[i];
			j++;
		} 		
		i++;
	}
	return sink;
}

/* Count the number of chars in the string */
int char_count(char c, char * string) {
	int count = 0;
	if (string == NULL) return count;
	int i = 0;
	while(string[i] != '\0') {
		if(string[i] == c) count++;
		i++;
	}
	return count;
}


/* Remove a node (list) from the tuplelist linked list at the 
   hash entry keyed by shape.  This is called by inp and in
   where we keep track of the previous node.  Manipulate
   the linked list according to whether the previous list was null
   
   I'm having errors when I free the tuple's memory, I've been trying to 
   debug this for a while but to no avail. I'm writing the threading
   and may get back to this. 
 */

void remove_tl(tl prev, tl list, char * shape ){
	tl temp = list;
	tl nextlist = temp->next;	
	 /* First node, then */
	if (prev == NULL) {
		ht_put(tuplespace, shape, nextlist);
	} else {                               
       		prev->next = nextlist;
	}
	//free(temp->data);
}

/* Allocate a tuplelist, of a given shape.
   We know that the array of fields is as long as the 
   shape string.
*/
tl tl_alloc(char * shape) {
	tl output = (struct tuplelist *) malloc ( sizeof(struct tuplelist));
	output->data = (field * )   malloc ( strlen(shape) * sizeof(field));
	output->next = NULL;
	return output;
}

/* Upon the first calls to the functions: out, inp, and rdp
   We want to make sure the tuplespace (hashTable) has been 
   initialized
*/
void init_ts(void) {
	if (firstCalled) {
		pthread_mutex_init(&tuplespace_mutex, NULL);
		pthread_cond_init (&tuple_added, NULL);
		firstCalled = 0;
		tuplespace = ht_alloc(NUM_SHAPES);
	}
}


/*
 * Output a tuple to the tuplespace.
 */
void out(char *shape, ...){
	pthread_once(&once_control,  init_ts);
	va_list ap;
	int j;
	tl nextlist = tl_alloc(shape);
	//Add all of the fields to the tuplelist: nextlist;
	va_start (ap, shape);
	for (j = 0; j < strlen(shape); j++){
		if (shape[j] == 's') {
			char * tempstring = va_arg(ap, char*);
			nextlist->data[j].s = strdup(tempstring);
		} else if (shape[j] == 'i') {
			nextlist->data[j].i =  va_arg(ap, int);
		}
	}
	va_end (ap);
	pthread_mutex_lock(&tuplespace_mutex);
	tl currentlist = ht_get(tuplespace, shape);
	if(currentlist == NULL) {
		ht_put(tuplespace, shape, nextlist);
	} else {
		nextlist->next = currentlist;
		ht_put(tuplespace, shape, nextlist);
	}
	//printf("Added a tuple and sending a broadcast\n");
	pthread_cond_broadcast(&tuple_added);
	pthread_mutex_unlock(&tuplespace_mutex);
	//printf("Unlocked\n");
	//pthread_exit(NULL);
}

/*
 Looks in the tuplespace for a match, and fills the pointer
 with the value that was queried.
 Then deletes that tuple from the tuplelist for that shape.
*/

int inp(char *form, ...){
	pthread_once(&once_control,  init_ts);	
	pthread_mutex_lock(&tuplespace_mutex);
	char * shape = (char *) malloc (MAX_SHAPE_SIZE * sizeof(char));
	shape = strcpy_exclude(shape, form, '?');
	int query_pos;
	if (strchr(form, '?') == NULL) {
		fprintf(stderr, "Invalid form, must include a ?\n");
	} else {
		query_pos = strchr(form, '?') - form;
	}
	//This 4 variables are temporary storage
	//in case we come upone a match.
	int * intPointer;
	char* charPointer;
	int outputint;
	char* outputstring;
	tl currentlist = ht_get(tuplespace, shape);
	//If there are not tuples of this shape, then return 0	
	if (currentlist == NULL) {
		free(shape);
		pthread_mutex_unlock(&tuplespace_mutex);
		return 0;
	}
	tl prevlist = NULL;
	int j, isMatch;
	va_list ap;
	int current_tuple = 0;	
	//Iterate over the tuples in this tuplelist, and go through
	//the arguments of the query to see if a tuple matches
	while (currentlist != NULL) {
		isMatch = 1;
		va_start(ap, form);
		for (j = 0; j < strlen(shape); j++) {	
			if (j == query_pos) {
				if (shape[j] == 'i') {
					intPointer = va_arg(ap, int*);
					outputint = currentlist->data[j].i;
				} else {
					charPointer = va_arg(ap, char *);
					outputstring = currentlist->data[j].s;
				}
			} else {
				if (shape[j] == 'i') {
					int tempint = va_arg(ap, int);
					if(tempint!= currentlist->data[j].i) { isMatch = 0;}	
				} else {
					char* tempstring = va_arg(ap, char*);
					if( strcmp(tempstring, currentlist->data[j].s) !=0){ isMatch = 0;}
				}
			}				
		}
		if (isMatch) {
			//If it is a match: Fill the pointers
			if (shape[query_pos] == 'i') {
				*intPointer = outputint;
			} else {
				charPointer = strcpy(charPointer, outputstring);
			}
			remove_tl(prevlist, currentlist, shape);			
			break;
		} else {
		}
		prevlist = currentlist;
		currentlist = currentlist->next;
		current_tuple++;	
	}
	
	free(shape);
	va_end (ap);
	pthread_mutex_unlock(&tuplespace_mutex);
	return isMatch;
}

/*
 Looks in the tuplespace for a match, and fills the pointer
 with the value that was queried.
*/
int rdp(char *form, ...){
	pthread_once(&once_control,  init_ts);	
	pthread_mutex_lock(&tuplespace_mutex);	
	char * shape = (char *) malloc (MAX_SHAPE_SIZE * sizeof(char));
	shape = strcpy_exclude(shape, form, '?');
	int query_pos;
	if (strchr(form, '?') == NULL) {
		fprintf(stderr, "Invalid form, must include a ?\n");
	} else {
		query_pos = strchr(form, '?') - form;
	}
	//This 4 variables are temporary storage
	//in case we come upone a match.
	int * intPointer;
	char* charPointer;
	int outputint;
	char* outputstring;
	tl currentlist = (struct tuplelist *) ht_get(tuplespace, shape);
	//If there are not tuples of this shape, then return 0	
	if (currentlist == NULL) {
		free(shape);
		pthread_mutex_unlock(&tuplespace_mutex);
		return 0;
	}
	tl prevlist = NULL;
	int j, isMatch;
	va_list ap;
	int current_tuple = 0;	
	//Iterate over the tuples in this tuplelist, and go through
	//the arguments of the query to see if a tuple matches
	while (currentlist != NULL) {
		isMatch = 1;
		va_start(ap, form);
		for (j = 0; j < strlen(shape); j++) {	
			if (j == query_pos) {
				if (shape[j] == 'i') {
					intPointer = va_arg(ap, int*);
					outputint = currentlist->data[j].i;
				} else {
					charPointer = va_arg(ap, char *);
					outputstring = currentlist->data[j].s;
				}
			} else {
				if (shape[j] == 'i') {
					int tempint = va_arg(ap, int);
					if(tempint!= currentlist->data[j].i) { isMatch = 0;}	
				} else {
					char* tempstring = va_arg(ap, char*);
					if( strcmp(tempstring, currentlist->data[j].s) !=0){ isMatch = 0;}
				}
			}				
		}
		if (isMatch) {
			//If it is a match: Fill the pointers
			if (shape[query_pos] == 'i') {
				*intPointer = outputint;
			} else {
				strcpy(charPointer, outputstring);
			}
			break;
		} else {
		}
		prevlist = currentlist;
		currentlist = currentlist->next;
		current_tuple++;	
	}
	free(shape);
	va_end (ap);
	pthread_mutex_unlock(&tuplespace_mutex);
        return isMatch;
}

/* Another variadic function, in waits around
   until it gets a matching tuple, fills the 
   pointer with the queried value, and consumes it
   from the tuplespace.

   If there is not match on a traversal of the tuplelist
   then the current process gets blocked until a tuple gets 
   added, and searches again.
*/
int in(char* form, ...) {	
	pthread_once(&once_control,  init_ts);
	int isMatch = 1;	
	pthread_mutex_lock(&tuplespace_mutex);
	while(1) {		
		char * shape = (char *) malloc (MAX_SHAPE_SIZE * sizeof(char));
		shape = strcpy_exclude(shape, form, '?');
		int query_pos;
		if (strchr(form, '?') == NULL) {
			fprintf(stderr, "Invalid form, must include a ?\n");
		} else {
			query_pos = strchr(form, '?') - form;
		}
		//This 4 variables are temporary storage
		//in case we come upone a match.
		int * intPointer;
		char* charPointer;
		int outputint;
		char* outputstring;
		tl currentlist = ht_get(tuplespace, shape);
		//If there are not tuples of this shape, then return 0	
		if (currentlist == NULL) {
			isMatch = 0;
		}
		tl prevlist = NULL;
		int j;
		va_list ap;
		int current_tuple = 0;	
		//Iterate over the tuples in this tuplelist, and go through
		//the arguments of the query to see if a tuple matches
		while (currentlist != NULL) {
			isMatch = 1;
			va_start(ap, form);
			for (j = 0; j < strlen(shape); j++) {	
				if (j == query_pos) {
					if (shape[j] == 'i') {
						intPointer = va_arg(ap, int*);
						outputint = currentlist->data[j].i;
					} else {
						charPointer = va_arg(ap, char *);
						outputstring = currentlist->data[j].s;
					}
				} else {
					if (shape[j] == 'i') {
						int tempint = va_arg(ap, int);
						if(tempint!= currentlist->data[j].i) { isMatch = 0;}	
					} else {
						char* tempstring = va_arg(ap, char*);
						if( strcmp(tempstring, currentlist->data[j].s) !=0){ isMatch = 0;}
					}
				}				
			}
			if (isMatch) {
				//If it is a match: Fill the pointers
				if (shape[query_pos] == 'i') {
					*intPointer = outputint;
				} else {
					charPointer = strcpy(charPointer, outputstring);
				}
				//Consume the tuple
				remove_tl(prevlist, currentlist, shape);			
				break;
			} else {
			}
			prevlist = currentlist;
			currentlist = currentlist->next;
			current_tuple++;	
		}	
		free(shape);
		va_end (ap);
		if(isMatch) {
			pthread_mutex_unlock(&tuplespace_mutex);
			return isMatch;
		} else {
			//Wait for a new tuple to be added:
			//Atomically releases lock before waiting, 
			//Gets signal and atomically acquires lock again before returning
			pthread_cond_wait(&tuple_added, &tuplespace_mutex);
		}
	}	
}

/* Another variadic function, in waits around
   until it gets a matching tuple, fills the 
   pointer with the queried value.

   If there is not match on a traversal of the tuplelist
   then the current process gets blocked until a tuple gets 
   added, and searches again.
*/
int rd(char* form, ...) {
	pthread_once(&once_control,  init_ts);
	int isMatch = 1;
	pthread_mutex_lock(&tuplespace_mutex);
	while(1) {		
		char * shape = (char *) malloc (MAX_SHAPE_SIZE * sizeof(char));
		shape = strcpy_exclude(shape, form, '?');
		int query_pos;
		if (strchr(form, '?') == NULL) {
			fprintf(stderr, "Invalid form, must include a ?\n");
		} else {
			query_pos = strchr(form, '?') - form;
		}
		//This 4 variables are temporary storage
		//in case we come upone a match.
		int * intPointer;
		char* charPointer;
		int outputint;
		char* outputstring;
		tl currentlist = ht_get(tuplespace, shape);
		//If there are not tuples of this shape, then return 0	
		if (currentlist == NULL) {
			isMatch = 0;
		}
		tl prevlist = NULL;
		int j;
		va_list ap;
		int current_tuple = 0;	
		//Iterate over the tuples in this tuplelist, and go through
		//the arguments of the query to see if a tuple matches
		while (currentlist != NULL) {
			isMatch = 1;
			va_start(ap, form);
			for (j = 0; j < strlen(shape); j++) {	
				if (j == query_pos) {
					if (shape[j] == 'i') {
						intPointer = va_arg(ap, int*);
						outputint = currentlist->data[j].i;
					} else {
						charPointer = va_arg(ap, char *);
						outputstring = currentlist->data[j].s;
					}
				} else {
					if (shape[j] == 'i') {
						int tempint = va_arg(ap, int);
						if(tempint!= currentlist->data[j].i) { isMatch = 0;}	
					} else {
						char* tempstring = va_arg(ap, char*);
						if( strcmp(tempstring, currentlist->data[j].s) !=0){ isMatch = 0;}
					}
				}				
			}
			if (isMatch) {
				//If it is a match: Fill the pointers
				if (shape[query_pos] == 'i') {
					*intPointer = outputint;
				} else {
					charPointer = strcpy(charPointer, outputstring);
				}
							
				break;
			} else {
			}
			prevlist = currentlist;
			currentlist = currentlist->next;
			current_tuple++;	
		}
	
		free(shape);
		va_end (ap);
		if(isMatch) {
			pthread_mutex_unlock(&tuplespace_mutex);
			return isMatch;
		} else {
			//Wait for a tuple
			//Atomically releases lock before waiting, 
			//and atomically acquires it again before returning
			pthread_cond_wait(&tuple_added, &tuplespace_mutex);
		}
	}	
}

typedef struct {
	int i;
	char * s;
} evaluation;

void evaluate(evaluation e, char result_type, void*(*f)(void*), void* arg){


}

int eval(char* form, ...) {
	char * shape = (char *) malloc (MAX_SHAPE_SIZE * sizeof(char));
	int NUM_EVALS = char_count('!', form);
	pthread_attr_t attr;
	pthread_attr_init(&attr);
  	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	void* status;	
	pthread_t  eval_threads[NUM_EVALS];
	evaluation eval_results[NUM_EVALS];
	pthread_mutex_lock(&tuplespace_mutex);
	va_list ap;
	int j = 0;
	int current_eval = 0;
	int current_field = 0;
	tl nextlist = tl_alloc(shape);
	va_start (ap, form);
	for (j = 0; j < strlen(form); j++){
		if (form[j]== '!') {
			void * subroutine;
			void * arg;
			subroutine = va_arg(ap, void*);
			j++;
			arg = va_arg(ap, void*);

			current_field++;
		
		} else if (form[j] == 's') {
			char * tempstring = va_arg(ap, char*);
			nextlist->data[current_field].s = strdup(tempstring);
			current_field++;
		} else if (form[j] == 'i') {
			nextlist->data[current_field].i = va_arg(ap, int);
			current_field++;
		}
	}
	va_end (ap);
	// Wait on the other threads
	int i;
	for(i=0;i<NUM_EVALS;i++) {
  		pthread_join(eval_threads[i], &status);
  	}	

	pthread_attr_destroy(&attr);
	pthread_mutex_lock(&tuplespace_mutex);
	tl currentlist = (struct tuplelist *) ht_get(tuplespace, shape);
	if(currentlist == NULL) {
		ht_put(tuplespace, shape, nextlist);
	} else {
		nextlist->next = currentlist;
		ht_put(tuplespace, shape, nextlist);
	}
	pthread_cond_broadcast(&tuple_added);
	pthread_mutex_unlock(&tuplespace_mutex);
	free(shape);	
}

void* addSomething(void * arg){
	char * team = (char*) arg;
	out("si", team, 2);
	pthread_exit(NULL);
}

void * inSomething(void * arg){
	char * team = (char*) arg;
	int value = 0;
	in("s?i", team, &value);
	printf("Got the value: %d\n", value);
	pthread_exit(NULL);
}

void * rdSomething(void * arg) {
	char * team = (char*) arg;
	int value = 0;
	rd("s?i", team, &value);
	printf("Got the value: %d\n", value);
	pthread_exit(NULL);
}

void * wait_for_num(void * arg){
	char * cool_word = (char *) malloc (100* sizeof(char));	
	int* vp = (int*) arg;
	printf("Looking  for the first word to occur %d times\n", *vp);
	in("?si", cool_word, *vp);
	printf("%s was the first word to occur %d times \n", cool_word, *vp);
	free(cool_word);
}

int inc (void * p) {
	return 1 + *((int*)p);
}


int main () {
	
	/*
	pthread_mutex_init(&tuplespace_mutex, NULL);
	pthread_cond_init (&tuple_added, NULL);	
 	pthread_attr_t attr;
	pthread_attr_init(&attr);
  	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	void* status;	
	int NUM_THREADS = 1;
	pthread_t threads[NUM_THREADS];
	int eight = 8;
	char * shape = "si";
  	char word[512], *fp, *tp;
  	int count;
	pthread_create(&threads[0], &attr, wait_for_num, (void*)&eight);
  	printf("Starting the scanning\n");
 	// scan words, sans punctuation
  	while (1 == scanf("%s",word)) {
    	    for (fp = tp = word; (*tp = *fp); fp++) isalpha(*tp) && (*tp++ |= 32);
	    count = 0;    
	    inp("s?i",word,&count);	// read in tuple (if none, count untouched)
	    count++;
	    out("si",word,count);	// write out tuple
	}
	count = 0;
	printf("Done reading in\n");
	
	
	pthread_attr_destroy(&attr);
	// Wait on the other threads
	int i;
	for(i=0;i<NUM_THREADS;i++) {
  		pthread_join(threads[i], &status);
  	}
	int times = 0;
	rdp("s?i", "and", &times);
	printf("'and' appears %d times\n", times);	
	// After joining, Print out the tuples
	//printTuples(shape);
  	pthread_mutex_destroy(&tuplespace_mutex);
  	pthread_cond_destroy(&tuple_added);
  	pthread_exit(NULL);
	*/

}


