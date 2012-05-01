/*
 * Wsh - Williams Shell Program
 * Nathaniel Lim (c)
 * CS432 - Spring 2011
 * Williams College
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <errno.h>
#include <fcntl.h>
#define MAXLINE 1024
#define MAX_PIPE_FILES 20
#define MAXARGS	 128
#define MAX_COMMANDS 20
#define EXITED 1
#define READ 0
#define WRITE 1

/* Defining the Process and Job structs */
typedef struct process {            
	struct process *next;       
	char *argv[MAXARGS];
	int prev_pipe[2];
	int next_pipe[2];
	int requires_pipe_out;
	int requires_pipe_in;   
	pid_t pid;                  
	char completed;             
	char stopped;              
	int status;                
} process;

typedef struct job  {             
	struct job *next;           
	char command[MAXLINE];      
	process *first_process;    
	int jid;                  
	pid_t pgid;                
	char foreground;            
	char notified;              
	int stdin, stdout, stderr;  
} job;

/* For Signal Handling */
typedef void handler_t(int);


/* Helper Functions (at the bottom of this source file) */
void parse_input(const char *cmdline);
int execBuiltin(char **argv);
void bgfg(char **argv);
void launch_job(job *j, int foreground);
void put_job_in_foreground(job *j, int cont);
void put_job_in_background(job *j, int cont);
int  pstatus_update(process *p, int status);
void wait_for_job(job *j);
void job_notify(void);
void mark_job_as_running(job *j);
void continue_job(job *j, int foreground);
handler_t *Signal(int signum, handler_t *handler);
void sigtstp_handler(int sig);
void sigint_handler(int sig);
job *find_job_by_pgid(pid_t pgid);
job *find_job_by_jid(int jid);
int job_is_stopped(job *j);
int job_is_completed(job *j);
job *new_job(void);
process *new_process(job *j);
void free_job(job *j);
void add_job(job *j);
void listjobs(void);
int fgpgid(void);
int maxjid(void);
void print_error(char *err_message);


/* Global Variables */
job *first_job = NULL;      
char *prompt = "wsh> ";     
int verbose = 0;
int nextjid = 1;
char *currentDirectory;
char *home;
char *dir;

int main(int argc, char **argv) {
	char c;
	char cmdline[MAXLINE];
	int emit_prompt = 1; /* emit prompt (default) */
	home = (char *) malloc (1024 * sizeof(char));
	dir = (char *) malloc (1024* sizeof(char));
	home = getenv("HOME");

	/* Signal Handlers*/
	/* These are C Preprocessor Macros */
	/* These Signals interupt and kill the foreground process */

	Signal(SIGINT,  sigint_handler);  /* ctrl-c */
	Signal(SIGTSTP, sigtstp_handler); /* ctrl-z */
	
	/* Execute the shell's read/eval loop */
	while (1) {		
		printf("%s", prompt);
		fflush(stdout);

		/* Get the commandline */
		if ((fgets(cmdline, MAXLINE, stdin) == NULL) && ferror(stdin)) {
			printf("fgets error\n");
			exit(1);
		}

		/* End of file Character: Ctrl-d */
		if (feof(stdin)) { 
			fflush(stdout);
			exit(0);
		}

		/* Prepreprocess (yes I know its a hack)*/
		char * hash = strchr(cmdline, '#');
		if (hash) {
			*hash = '\0';
		}


		/* Preprocess the cmdline, running commands separated
		   ';' characters as if they were typed on separate lines */
		int num_commands = 0;
		char *temp;
		char* commands[MAX_COMMANDS];
		temp = strtok(cmdline, ";");
		commands[num_commands] = temp;
		num_commands++;
	
		while (   (temp = strtok(NULL, ";")) != NULL) {
			commands[num_commands] = temp;
		 	num_commands++;
		}
		int i;
		/* Evaluate the command line, separated by ; chars */
		for(i = 0; i < num_commands; i++) {
			
			job_notify();
			parse_input(commands[i]);
			fflush(stdout);
			fflush(stdout);
		}
		
	}
	exit(EXIT_SUCCESS);
}



/* 
 * Parse the command line and build the argv array.
 * and launch off jobs as you read.
 */
void parse_input(const char *cmdline) {
	char *argv[MAXARGS];          /* argv for exec */
	static char array[MAXLINE];   /* holds local copy of command line */
	char *buf = array;            /* ptr that traverses command line */
	char *delim;                  /* points to first space delimiter */
	int argc;                     /* number of args */
	int bg = 0;                      /* background job? */
	int k;
	job *j = new_job();
	process *p = new_process(j);
	//strcpy(j->command, buf);      /* associate command with new job */

	strcpy(buf, cmdline);
	buf[strlen(buf)-1] = ' ';     /* replace trailing '\n' with space */



	/* Build the argv list */
	while (*buf && (*buf == ' ')) /* ignore leading spaces */
		buf++;
	argc = 0;
	
	delim = strchr(buf, ' ');
	while (delim) {		
		while (*buf && (*buf == ' ')) /* ignore spaces */
		   buf++;		
		
		if(*buf == '&'){
			bg =1;
			*buf = '\0';
			argv[argc] = NULL;		
			memcpy(p->argv, argv, MAXARGS * sizeof(char *)); /* end of pipeline */			
			strcpy(j->command, argv[0]);
			add_job(j);
			launch_job(j, !bg);

			buf++;
			bg = 0;
			argc = 0;
			j = new_job();
			p = new_process(j);
			while (*buf && (*buf == ' ')) 
			   buf++;		
			
		} else if (*buf == '>') { /*handle redirection, output to file*/
			char * outputfile;
			/* Pass this char and keep on reading to get the output file name */
			buf++;
			/* Ignore more spaces */
			while (*buf && (*buf == ' ')) 
			   buf++;
			delim = strchr(buf, ' ');
			*delim = '\0';
			outputfile = buf;
			int outfd = open(outputfile, O_CREAT|O_WRONLY, 0666);
			if (outfd < 0) {
				fprintf(stderr, "Error opening output file\n");
			}			
			//change stdout for job			
			j->stdout = outfd;	
			buf = delim +1;				
		} else 	if (*buf == '<') { /*handle redirection input from file*/
			char * inputfile;
			/* Pass this char and keep on reading to get the input file name */
			buf++;
			/* Ignore more spaces */
			while (*buf && (*buf == ' ')) 
			   buf++;
			delim = strchr(buf, ' ');
			*delim = '\0';
			inputfile = buf;
			int infd = open(inputfile, O_CREAT|O_RDONLY, 0666);
			if (infd < 0) {
				fprintf(stderr, "Error opening input file\n");
			}			
			//change stdin for job			
			j->stdin = infd;	
			buf = delim +1;				
		} else if (*buf == '|') {            /* handle pipes */
			buf++;
			argv[argc] = NULL;        /* end of this process's argv */
			memcpy(p->argv, argv, MAXARGS * sizeof(char *));
			p = new_process(j);       /* prepare for post-pipe process */
			argc = 0;
			while (*buf && (*buf == ' ')) /* ignore more spaces */
			   buf++;
		}
		if (delim = strchr(buf, ' ')){
			argv[argc++] = buf;
			*delim = '\0';
			buf = delim + 1;
		}               
		
	}
	argv[argc] = NULL;
	
	if (argc == 0) { /* ignore blank line */
		free_job(j);
		return;
	}	
	if(non_forking_builtins(argv)) {
		free_job(j);
		return;
	}

	memcpy(p->argv, argv, MAXARGS * sizeof(char *)); /* end of pipeline */
	strcpy(j->command, argv[0]);	
	add_job(j);
	launch_job(j, !bg);
	return;
}

/* These builtin functions are not to be used in piping*/
/* cd, exit, quit, kill */

int non_forking_builtins (char ** argv) {
	
	if (strcmp(argv[0], "cd") == 0) {
        	if (argv[1] == NULL) {
              		chdir(home);  
			return 1;
       		} else {
			/* Handle the '~' character for cd'ing */
			memset(dir, '\0', MAXLINE);
			if(*argv[1] == '~'){
				dir = strcpy(dir, home);
				if (strlen(argv[1]) > 1) {
					argv[1]++;
					dir = strcat(dir, argv[1]);
				}
			} else {
				dir = strcpy(dir, argv[1]);
			}	
        	      	if (chdir(dir) == -1) {                                  
        	               	fprintf(stderr, " %s: no such directory\n", argv[1]);
			}
			return 1;
        	}
	}
	
	if (strcmp(argv[0], "exit") == 0) {
		int exit_status;		
		if (argv[1] != NULL) {
			exit_status = atoi(argv[1]);
        	} else {
			exit_status = 0;
		}
		exit(exit_status);
		return 1;
	}

	if (strcmp(argv[0],"quit") == 0) {
		exit(0);
		return 1;
    	}

	if (strcmp(argv[0], "kill") == 0) {
		int pid_to_kill;
		//This number will be coded by its sign, 
  		//0, 

		if (argv[1] != NULL) {
			pid_to_kill = atoi(argv[1]);
			kill(pid_to_kill, SIGINT);
        	} else {
			fprintf(stderr, "Please give an argument to kill\n");
		}		


               return 1;
        }    	
	return 0;
}

/* 
 * execBuiltin
 * These commands can be used in piping
 * jobs, cd, help
 */
int execBuiltin(char **argv) {
	
	if (strcmp(argv[0],"jobs") == 0) {
   		listjobs();
        	return 1;
    	}

    	if (strcmp(argv[0],"bg") == 0 || strcmp(argv[0],"fg") == 0) {
        	bgfg(argv);
        	return 1;
    	}    
   	   	
       	if (strcmp(argv[0], "cd") == 0) {
        	if (argv[1] == NULL) {
              		chdir(home);  
			return 1;
       		} else {
			/* Handle the '~' character for cd'ing */
			memset(dir, '\0', MAXLINE);
			if(*argv[1] == '~'){
				dir = strcpy(dir, home);
				if (strlen(argv[1]) > 1) {
					argv[1]++;
					dir = strcat(dir, argv[1]);
				}
			} else {
				dir = strcpy(dir, argv[1]);
			}	
        	      	if (chdir(dir) == -1) {                                  
        	               	fprintf(stderr, " %s: no such directory\n", argv[1]);
			}
			return 1;
        	}
	}	
	if (strcmp(argv[0], "help") == 0) {
		//Implement as a job
       	        printf(	"exit [n]\t\tpwd\ncd [dir]\t\thelp\nfg\t\t\tjobs\nkill\n");
       	        return 1;
        }	
        	
    	return 0;     /* The command was not builtin, return 0 */
}	

/* Execute the builtin bg and fg commands. */
void bgfg(char **argv) {
	char* cmd = argv[0];
	int jid = atoi(argv[1]);
	printf("Running %s on job %d\n", cmd, jid);
	job *j = find_job_by_jid(jid);
	if(j){
	    if(strcmp(cmd, "fg") == 0)
	        put_job_in_foreground(j, 1);
	    else
	        put_job_in_background(j, 1);
	} else {
	    print_error("Invalid jid specified for bg or fg");
	}
}

void put_job_in_foreground(job *j, int cont) {
    if (cont) {
        kill(-j->pgid, SIGCONT);
    }    
    process* p = j->first_process;
    while (p) {
        p->stopped = 0;
        p->status = 0;
        p = p->next;
    }    
    j->foreground = 1; 
    wait_for_job(j);
    return;    
}

void put_job_in_background(job *j, int cont) {
    if (cont) {
        kill(j->pgid, SIGCONT);
    }
    j->foreground = 0;
    return;
}


/* Jobs that have iterative forking processes in a pipeline must keep track
   of what fids they have opened.  This helper fn closes_pipes closes all of the given fids*/
void close_pipes(int pipefiles[], int numpipefiles) {
	int index;	
	for (index = 0; index < numpipefiles; index++) {
		close(pipefiles[index]);
	}
}	


/*  launch_process is called when when launch job forks */
void launch_process(process *p, pid_t pgid, int in, int out, int err, int pipefiles[], int numpipefiles){
    if(pgid == 0) {
        pgid = getpid();
    }
    //Maintain a process group id
    setpgid(p->pid,pgid);
    

    // Dup into STDIN if we want to pipe in
    // or we want redirection
    if (p->requires_pipe_in) {
	dup2(p->prev_pipe[READ], STDIN_FILENO);
    } else if (in != STDIN_FILENO) {
	dup2(in, STDIN_FILENO);
    }


    // Dup into STDOUT if we want to pipe out
    // or we want redirection
    if (p->requires_pipe_out) {
	dup2(p->next_pipe[WRITE], STDOUT_FILENO);
    } else if (out != STDOUT_FILENO){
          dup2(out, STDOUT_FILENO);
    }

    close_pipes(pipefiles, numpipefiles);  
    char* filename = p->argv[0];

    /* Try to execute one fo the builtlin instructions
       and exit if you do */
    if(execBuiltin(p->argv)) {
	exit(0);	
    }

    /* Or else run some executable */

    if (execvp(filename, p->argv)) {
	    printf("command: %s\n", filename);
    	    print_error("execvp can't find it: \n");
    }
}


/*  Iteratively launches the processes it contains
    in a pipeline */
void launch_job(job *j, int foreground) {
    int index;
    process *current_process = j->first_process;
    int pipefiles[MAX_PIPE_FILES];
    int numpipefiles = 0;
    while(current_process){
        int in =  j->stdin;
	int out = j->stdout;

	//If there is another process we must pipe to it.
        if(current_process->next){
	    current_process->requires_pipe_out = 1;
	    current_process->next->requires_pipe_in  = 1;
            pipe(current_process->next_pipe);
	    current_process->next->prev_pipe[READ]  =  current_process->next_pipe[READ];
	    current_process->next->prev_pipe[WRITE] = current_process->next_pipe[WRITE];
	    pipefiles[numpipefiles] = current_process->next_pipe[READ];
	    numpipefiles++;
	    pipefiles[numpipefiles] = current_process->next_pipe[WRITE];
	    numpipefiles++;
        }

	/* Fork */
	int pid = fork();

	//Child Process:
        if(pid == 0){		
		launch_process(current_process, j->pgid, in, out, j->stderr, pipefiles, numpipefiles);
        } else if (pid < 0){
		fprintf(stderr, "Problem forking\n");
	} else {
	//Parent continues going through the rest of the processes		
            	if(j->pgid == 0){
                	j->pgid = pid;
		}
            	current_process->pid = pid;
        }
        
        current_process = current_process->next;	
    }

    close_pipes(pipefiles, numpipefiles);
    
    if (foreground)
        put_job_in_foreground(j, 0);
    else {
        put_job_in_background(j, 0);
        printf("[%d] (%d) %s\n", j->jid, j->pgid, j->command);
    }
    return;
}

/* Update the status of a process */
int pstatus_update(process *p, int status){
    if(WIFEXITED(status)) {
        p->completed = 1;
    }

    if(WIFSTOPPED(status)) {
        p->stopped = 1;
    }

    p->status = status;
    return 0;
}

/*
 *  Wait for the processes statuses to be updated
 */
void wait_for_job(job *j) {
    int status;    
    // Waiting for the Job, requires waiting for all processes 
    int num_running = 0;
    process* p;
    for (p = j->first_process; p != NULL; p = p->next) {
        int status;
        pid_t wpid = waitpid(p->pid, &status, WUNTRACED);  
        pstatus_update(p,status);
    }
    
    return;
}

/*
 *    Notify the user (print to STDOUT)about stopped or terminated
 *    jobs; delete terminated jobs from the active job list.
 */
void job_notify(void) {
    job *j = first_job;
    job *prev = NULL;    
    while (j) {        
        // Update job's status        
        process* p = j->first_process;
        while (p) {
            int status;
            pid_t wpid = waitpid(p->pid, &status, WUNTRACED | WNOHANG);
         
            if (wpid != -1)
                pstatus_update(p, status);
            p = p->next;
        }       

        if (!j->notified && job_is_stopped(j) && !j->foreground && j->jid != 0) {
            printf("Stopped: [%d] (%d) %s\n", j->jid, j->pgid, j->command);
            j->notified = 1;
        }

	// If the Job is completed, remove it and free its memory
        if(job_is_completed(j) && j->jid != 0) {
            if(!prev) { // We're on the first job
                first_job = j->next;
            } else {
                prev->next = j->next;
            }
	    free_job(j);
        }
	prev = j;
	j = j->next;
    }
}

/*
 * Mark job as back and running again.
 */
void mark_job_as_running(job *j) {
	process *p;
	for (p = j->first_process; p; p = p->next)
		p->stopped = 0;
	j->notified = 0;
}

/*
 * Make the job continue
 */
void continue_job(job *j, int foreground) {
	mark_job_as_running(j);
	if (foreground)
		put_job_in_foreground(j, 1);
	else
		put_job_in_background(j, 1);
}

/*
 * Signal:  A Wrapper around the function sigaction
 */
handler_t* Signal(int signum, handler_t *handler)  {
	struct sigaction action, old_action;
	action.sa_handler = handler;  
	sigemptyset(&action.sa_mask);
	action.sa_flags = SA_RESTART;
	if (sigaction(signum, &action, &old_action) < 0)
		print_error("Signal error");
	return (old_action.sa_handler);
}

/* Handler for SIGINT 

	This prevents the wsh from being killed
	Catch the signal, and forward it to the foreground job
*/
void sigint_handler(int sig) {
    int pid = fgpgid();
    if(!pid) {
        return;
    } else {
    	kill(-pid, SIGINT);
    	wait_for_job(find_job_by_pgid(pid));
    	job_notify();
    }
}


/* Handler for SIGSTP
	This prevents the wsh from being suspended
	Catch the signal, and forward it to the foreground job
*/
void sigtstp_handler(int sig) {
    int pid = fgpgid();
    printf("Job [%d] stopped\n", find_job_by_pgid(pid)->jid);
    kill(-pid, SIGTSTP);
    job_notify();
}



/*  Job related Helper functions to:

 Find jobs by there various ids:
    process group id
    job id 

  Poll the status of jobs

*/ 
job* find_job_by_pgid(pid_t pgid) {
	job *j;
  
	for (j = first_job; j; j = j->next)
		if (j->pgid == pgid)
			return j;
	return NULL;
}

job* find_job_by_jid(int jid) {
	job *j;
  
	for (j = first_job; j; j = j->next)
		if (j->jid == jid)
			return j;
	return NULL;
}

int job_is_stopped(job *j) {
	process *p;
  
	for (p = j->first_process; p; p = p->next)
		if (!p->completed && !p->stopped)
			return 0;
	return 1;
}


int job_is_completed(job *j) {
	process *p;
  
	for (p = j->first_process; p; p = p->next)
		if (!p->completed)
			return 0;
	return 1;
}


/*Add a job to the linked list */
void add_job(job *j) {
	job *last = first_job;
	j->jid = nextjid++;
	if(last) {
		while(last->next)
			last = last->next;
		last->next = j;
	} else {
		first_job = j;
	}
}

/*Print a list of the jobs */
void listjobs(void) {
    job *j = first_job;
    
    while(j) {
		printf("[%d] (%d) ", j->jid, j->pgid);
		if (job_is_stopped(j))
			printf("Stopped ");
		else if (!job_is_completed(j))
			printf("Running ");
		printf("%s\n", j->command);
		j = j->next;
	}
}

//Get the processor group id of the job that is in the foreground
int fgpgid(void) {
	job *j = first_job;

	while(j) {
		if(j->foreground)
			return j->pgid;
		j = j->next;
	}
	return 0;
}

//Get the job id that is the largest in the list of jobs
int maxjid(void)  {
	job *j = first_job;
	int max = 0;

	while(j){
		if (j->jid > max) {
		    max = j->jid;
		}
		j = j->next;
	}
    return max;
}


/* Constructors and Free-ers */
job* new_job(void) {
	job *j = malloc(sizeof(job));
	if(j == NULL)
		print_error("malloc error");
	j->next = NULL;
	j->first_process = NULL;
	j->jid = 0;
	j->pgid = 0;
	j->foreground = 0;
	j->notified = 0;
	j->stdin = STDIN_FILENO;
	j->stdout = STDOUT_FILENO;
	j->stderr = STDERR_FILENO;
	return j;
}

process* new_process(job *j) {
	process *p = malloc(sizeof(process)), *old = j->first_process;
	if(p == NULL)
		print_error("malloc error");

	p->prev_pipe[0] = -1;
	p->prev_pipe[1] = -1;
	p->next_pipe[0] = -1;
	p->next_pipe[1] = -1;
	p->requires_pipe_out = 0;
	p->requires_pipe_in = 0;
	p->next = NULL;
	p->pid = 0;
	p->completed = 0;
	p->stopped = 0;
	p->status = 0;
	if(old) {
		while(old->next)
			old = old->next;
		old->next = p;
	} else
		j->first_process = p;
	return p;
}

void free_job(job *j) {
	process *p = j->first_process, *next;
	while (p) {
		next = p->next;
		free(p);
		p = next;
	}
	free(j);
	nextjid = maxjid() + 1;
}

/* Error Printer */
void print_error(char *msg) {
	fprintf(stderr, "%s: %s\n", msg, strerror(errno));
	exit(1);
}
