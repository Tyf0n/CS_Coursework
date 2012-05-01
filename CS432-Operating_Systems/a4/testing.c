#include <string.h>
#include <pthread.h>
#include "ts.h"

void* addSomething(void * arg){
	char * team = (char*) arg;
	out("si", team, 2);
	pthread_exit(NULL);
}


int slow_strlen(char * string) {
	sleep(1);
	return strlen(string);
}



void * wait_for_num_keep(void * arg){
	char * cool_word = (char *) malloc (100* sizeof(char));	
	int* vp = (int*) arg;
	printf("Using rd to look for the first word to occur %d times\n", *vp);
	rd("?si", cool_word, *vp);
	printf("%s was the first word to occur %d times, keep it! \n", cool_word, *vp);
	free(cool_word);
}

void * wait_for_num_consume(void * arg){
	char * cool_word = (char *) malloc (100* sizeof(char));	
	int* vp = (int*) arg;
	printf("Using in to look for the first word to occur %d times\n", *vp);
	in("?si", cool_word, *vp);
	printf("%s was the first word to occur %d times, consume it! \n", cool_word, *vp);
	free(cool_word);
}


int inc (void * p) {
	return 1 + (int)*(double*)p;
}

int main() {
	//A simple test of concurrency
	printf("\n");
	printf("A Simple Test of Concurrency (calling my slow_strlen function):\n");
	time_t start = time(NULL);
	eval("!i!i", slow_strlen, "Hello", slow_strlen, "World!");
	time_t end = time(NULL);
	double diff = difftime(end, start);
	printTuples("ii");
	printf("Time spent using eval: %f\n", diff);
	start = time(NULL);
	int x, y;
	x = slow_strlen("Hello!");
	y = slow_strlen("World");
	out("ii", x, y);
	end = time(NULL);
	diff = difftime(end, start);
	printTuples("ii");
	printf("Time spent pre-computing (serially) and using out:  %f\n", diff);	
	printf("\n");
	printf("Another of the eval method, with two different evaluations\n");	
	double oldvalue = 3.1;
	eval("s!is!i", "Length", strlen, "Hello World", "Other Length", inc, &oldvalue);
	printTuples("sisi");
	


	printf("\n");
	printf("A Test of rwlocks with conditional signal:\n");
	printf("I want the current thread to read, while two in's fight over the lock\n"); 
	void* status;
	int four = 4;
	int two = 2;
	pthread_t subroutine;
	pthread_t subroutine2;
	pthread_create(&subroutine,  NULL, wait_for_num_consume, (void*)(&two ));
	pthread_create(&subroutine2, NULL, wait_for_num_consume, (void*)(&four));	
  	char word[512], *fp, *tp;
 	int count;
  	printf("Starting the scanning\n");
  	while (1 == scanf("%s",word)) {
 		for (fp = tp = word; (*tp = *fp); fp++) isalpha(*tp) && (*tp++ |= 32);
    		count = 0;    
    		inp("s?i",word,&count);	// read in tuple (if none, count untouched)
    		count++;
    		out("si",word,count);	// write out tuple
  	}
  	count = 0;
	strcpy(word, "Blank");
	printf("Word was: %s\n", word);
	rdp("?si", word, 4);	  	
	printf("Done reading in\n");
	sleep(2);	
	printf("We still read %s occurring, 4 times\n", word);
	printTuples("si");
	printf("\n");
}
