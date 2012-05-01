#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#define NUM_THREADS	8
typedef struct {
	int i;
	char * s;
}evaluation;

struct eval_args{
	evaluation e;
	char result_type;
	void*(*f)(void*);
	void *arg;
};

void* evaluate(void* threadarg){
   	struct eval_args *my_data;
   	my_data = (struct eval_args *) threadarg;
   	taskid = my_data->thread_id;
  	 sum = my_data->sum;
   	hello_msg = my_data->message;
   	printf("Thread %d: %s  Sum=%d\n", taskid, hello_msg, sum);
   	pthread_exit(NULL);
}

int main(int argc, char *argv[]) {
	pthread_t threads[NUM_THREADS];
	struct thread_data thread_data_array[NUM_THREADS];
	int rc, t, sum;

	sum=0;


	for(t=0;t<NUM_THREADS;t++) {
	sum = sum + t;
  thread_data_array[t].thread_id = t;
  thread_data_array[t].sum = sum;
  thread_data_array[t].message = messages[t];
  printf("Creating thread %d\n", t);
  rc = pthread_create(&threads[t], NULL, PrintHello, (void *) 
       &thread_data_array[t]);
  if (rc) {
    printf("ERROR; return code from pthread_create() is %d\n", rc);
    exit(-1);
    }
  }
pthread_exit(NULL);
}


