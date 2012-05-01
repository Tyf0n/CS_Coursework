#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define NUM_THREADS	8

typedef struct {
	int i;
	char * s;
} evaluation;

struct eval_args{
	evaluation *e;
	char result_type;
	void *(*f)(void*);
	void *arg;
};


//void evaluate(evaluation e, char result_type, void*(*f)(void*), void* arg){
//}

void* evaluate(void* threadarg){
	int (*ret_int)(void*);
	char *(*ret_str)(void*);   	
	struct eval_args *my_data;
   	my_data = (struct eval_args *) threadarg;
   	char result_type = my_data->result_type;
	
	printf("input string: %s\n", (char*) my_data->arg);


	if( result_type == 'i') {
		ret_int = (int (*)(void*)) &my_data->f;
		my_data->e->i = (*ret_int)(my_data->arg);
	} else {
		ret_str = (char* (*)(void*)) &my_data->f;
		my_data->e->s = strcpy(my_data->e->s, (*ret_str)(my_data->arg));
	}	
   	pthread_exit(NULL);
}

int main() {
	int num = 1;
	pthread_t threads[num];
	struct eval_args *eval_args_array;
	eval_args_array = (struct eval_args * ) malloc (sizeof(struct eval_args));
	eval_args_array[0].e = (evaluation *) malloc (sizeof (evaluation));
	void * status;
	char * h = "Hello World\n";
	eval_args_array[0].result_type = 'i';
	eval_args_array[0].f = (void* (*)(void*)) &strlen;
	eval_args_array[0].arg = (void *) h;

	pthread_create(&threads[0], NULL, evaluate, (void *) &eval_args_array[0]); 
	pthread_join( threads[0], &status);
	printf("strlen(%s) = %d\n", h, eval_args_array[0].e->i);
	pthread_exit(NULL);
}


