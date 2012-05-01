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
#include <sys/time.h>
#include "hash.h"
#include "ts.h"
#define NUM_SHAPES 13
#define MAX_TUPLE_SIZE 10
#define MAX_TUPLES 100
#define MAX_SHAPE_SIZE 10

static hash tuplespace;
static pthread_once_t once_control = PTHREAD_ONCE_INIT;

/*
 Since POSIX doesn't support cond_wait on a condition with rwlock 
 I have to write my own solution using one of each of the  following locking mechanisms: 
 mutex, rwlock, and cond
*/

static pthread_rwlock_t lock;
static pthread_mutex_t  signaling_mutex;
static pthread_cond_t tuple_added;

/*
 The tuplespace is a hashTable that maps:  char * shape --> tuplelist
 A tuplelist (defined in ts.h), is a linked list, containing its tuple data
	and a pointer to the next node (another tuplelist) in the tuplelist
 A tl is a pointer to a tuplelist
 A tuple (defined in ts.h) is an array (declared as pointer) of field unions
*/
void printTuples(char * shape){
	tl list = ht_get(tuplespace, shape);
	printf("Tuples with Shape %s:\t", shape);
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

   This is only called by in and inp, which acquire the the appropriate locks
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
	pthread_rwlock_init(&lock, NULL);
	pthread_cond_init(&tuple_added, NULL);
	pthread_mutex_init(&signaling_mutex, NULL);
	tuplespace = ht_alloc(NUM_SHAPES);
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
	
	//get_wr_lock();
	pthread_rwlock_wrlock(&lock);
	pthread_mutex_lock(&signaling_mutex);
	tl currentlist = ht_get(tuplespace, shape);
	if(currentlist == NULL) {
		ht_put(tuplespace, shape, nextlist);
	} else {
		nextlist->next = currentlist;
		ht_put(tuplespace, shape, nextlist);
	}
	pthread_cond_broadcast(&tuple_added);
	pthread_mutex_unlock(&signaling_mutex);
	//release_wr_lock();
	pthread_rwlock_unlock(&lock);
}

/*
 Looks in the tuplespace for a match, and fills the pointer
 with the value that was queried.
 Then deletes that tuple from the tuplelist for that shape.
*/

int inp(char *form, ...){
	pthread_once(&once_control,  init_ts);	
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
	
	//get_wr_lock();
	pthread_rwlock_wrlock(&lock);
	
	tl currentlist = ht_get(tuplespace, shape);
	//If there are not tuples of this shape, then return 0	
	if (currentlist == NULL) {
		free(shape);
		pthread_rwlock_unlock(&lock);		
		//release_wr_lock();
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
	
	pthread_rwlock_unlock(&lock);
	return isMatch;
}

/*
 Looks in the tuplespace for a match, and fills the pointer
 with the value that was queried.
*/
int rdp(char *form, ...){
	pthread_once(&once_control,  init_ts);	
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
	//get_rd_lock();
	pthread_rwlock_rdlock(&lock);
	tl currentlist = (struct tuplelist *) ht_get(tuplespace, shape);
	//If there are not tuples of this shape, then return 0	
	if (currentlist == NULL) {
		free(shape);
		//release_rd_lock();
		pthread_rwlock_unlock(&lock);		
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
	//release_rd_lock();
	pthread_rwlock_unlock(&lock);
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
	//get_wr_lock();
	pthread_rwlock_wrlock(&lock);
	pthread_mutex_lock(&signaling_mutex);	
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
			//release_wr_lock();
			pthread_mutex_unlock(&signaling_mutex);
			pthread_rwlock_unlock(&lock);
			return isMatch;
		} else {
			//Wait for a new tuple to be added:
			//Atomically releases lock before waiting, 
			//Gets signal and atomically acquires lock again before returning
			pthread_rwlock_unlock(&lock);
			//printf("in-ing, No Tuple, Wait for an add\n");
			pthread_cond_wait(&tuple_added, &signaling_mutex);
			//printf("in-ing, Tuple Added\n");
			pthread_rwlock_wrlock(&lock);
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
	//get_rd_lock();
	pthread_rwlock_rdlock(&lock);
	pthread_mutex_lock(&signaling_mutex);
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
			pthread_mutex_unlock(&signaling_mutex);
			pthread_rwlock_unlock(&lock);
			return isMatch;
		} else {
			//Wait for a tuple
			//Atomically releases lock before waiting, 
			//and atomically acquires it again before returning
			pthread_rwlock_unlock(&lock);
			printf("rd-ing, No Tuple, Wait for an add\n");
			pthread_cond_wait(&tuple_added, &signaling_mutex);
			printf("rd-ing, Tuple_Added, try to read again\n");
			pthread_rwlock_rdlock(&lock);
		}
	}	
}


struct eval_args{
	int * int_result;
	char * s_result;
	char return_type;
	void *(*f)(void*);
	void *arg;
};



void * evaluate(void * args){
	struct eval_args *arguments = (struct eval_args *) args;	
	int (*ret_int)(void*);
	char* (*ret_str)(void*);
	if  (arguments->return_type == 'i') {
		ret_int = (int (*)(void*)) (arguments->f);
		*(arguments->int_result) = (*ret_int) ((void*)arguments->arg);
	} else {
		ret_str = (char* (*)(void*)) (arguments->f);
		strcpy(arguments->s_result, (*ret_str) ((void*)arguments->arg));
	}			
	pthread_exit(NULL);
}

/*
	eval is a variadic function that takes a format, which contain 0 or more
	! operator, which denotes which fields in the tuple about to be added 
	need to be evaluated by a worker function.  Pointers to the worker function and
	pointers to the fields are passed to the to the evaluate helper function 
	via the eval_args struct, and the results are written to the fields

	Before writing the tuple, of course, eval joins with all the threads running 
	evaluate
*/
int eval(char* form, ...) {
	//Initialize the tuplespace if needed.
	pthread_once(&once_control,  init_ts);
	//Figure out what shape the tuple is from the form
	char * shape = (char *) malloc (MAX_SHAPE_SIZE * sizeof(char));
	shape = strcpy_exclude(shape, form, '!');
	//Figure out how many threads running evaluate, we need to spawn
	int NUM_EVALS = char_count('!', form);
	pthread_attr_t attr;
	pthread_attr_init(&attr);
  	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	void* status;	
	pthread_t eval_threads[NUM_EVALS];
	struct eval_args eval_args_array[NUM_EVALS];
	va_list ap;
	int j = 0;
	int current_eval = 0;
	int current_field = 0;
	int test_result = 0;
	//Start filling the tuple: nextlist	
	tl nextlist = tl_alloc(shape);
	va_start (ap, form);
	for (j = 0; j < strlen(form); j++){
		if (form[j]== '!') {
			j++;
			if (form[j] == 'i') {
				//The worker function returns an int, set return_type
				//Get the int * to which the result will be written to		
				eval_args_array[current_eval].int_result = &(nextlist->data[current_field].i); 	
				eval_args_array[current_eval].f =   va_arg(ap, void* (*)(void*) );
				eval_args_array[current_eval].arg = va_arg(ap, void* );
				eval_args_array[current_eval].return_type = 'i';
			} else {
				//The worker function returns an char*, set return_type ='i'.					
				//Get the char * to which the result will be written to
				eval_args_array[current_eval].s_result = nextlist->data[current_field].s; 	
				eval_args_array[current_eval].f =   va_arg(ap, void* (*)(void*) );
				eval_args_array[current_eval].arg = va_arg(ap, void* );
				eval_args_array[current_eval].return_type = 's';
			}
			//spawn of the thread and continue
			pthread_create(&eval_threads[current_eval], NULL, evaluate, (void *) &eval_args_array[current_eval]); 
			current_field++;
			current_eval++;	
		//If there is no evaluation to be done, fill in the tuple normally as in out	
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
	int i;
	for(i=0;i<NUM_EVALS;i++) {
  		pthread_join(eval_threads[i], &status);
  	}
	pthread_attr_destroy(&attr);
	//get_wr_lock();
	pthread_rwlock_wrlock(&lock);
	pthread_mutex_lock(&signaling_mutex);
	tl currentlist = (struct tuplelist *) ht_get(tuplespace, shape);
	if(currentlist == NULL) {
		ht_put(tuplespace, shape, nextlist);
	} else {
		nextlist->next = currentlist;
		ht_put(tuplespace, shape, nextlist);
	}
	pthread_cond_broadcast(&tuple_added);
	//release_wr_lock();
	pthread_mutex_unlock(&signaling_mutex);
	pthread_rwlock_unlock(&lock);
	free(shape);
	return 0;
}
