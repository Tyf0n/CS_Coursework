/*
 * Nathaniel Lim
 * CS432 - Assignment 7
 *
 * The WACD:  Williams analog clock daemon
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/fcntl.h>
#include <time.h>
#include "wac.h"
#define TICK 0
#define TOCK 1
#define IDLE 2
#define PORT_BYTE_OFFSET 0x378

/*
	The sending of packets between the client and the server
	Is the writing and reading to and from a buffer in the folliwng format:
	5 Byte Packet:

		Byte 0:  
			Contains a value providing a description of the type of message
			WAC  client provides: SET, GET, MOMENT, GO, STOP, GOTO, SHUT, FIN
			WACD server responds to these messages, changes state accordingly sends back packet with ACK, RET_VAL
		Byte 1-4:
			This contains the unsigned integer
			As the argument sent to the WACD server for a procedure call 
			Or the return value sent back to WAC client
*/

static int port_writing_mode = 0;
static int current_time = 0;
static int moment = 1000;
static int conn_left = MAX_CONNECTIONS;
static int net;
static int client;
static int client_open = 0;
static unsigned char buffer[BUF_SIZE];
static int is_going_to = 0;
static int go_to_time = 0;
static int clock_started = 0;
static int last_port_write;
static struct sockaddr_in saddr;
static struct sockaddr_in caddr;


int abs (int x) {
	if (x < 0) {
		return -x;
	} else {
		return x;
	}
}

int start_daemon(int port) {
  socklen_t clilen;
  struct sockaddr_in saddr, caddr;
  /* net ~ Service Socket */
  net = socket(AF_INET, SOCK_STREAM, 0);
  if (net < 0) { perror("opening socket"); exit(1); }
  int sl = sizeof(struct sockaddr_in);
  saddr.sin_family = AF_INET;
  saddr.sin_addr.s_addr = INADDR_ANY;
  saddr.sin_port = htons(port);
  /* non-blocking accepts (see fcntl(2)) */
  if (bind(net, (struct sockaddr*)&saddr, sizeof(saddr)) < 0) {
    perror("bind");
    exit(1);
  }
}

int main(int argc, char *argv[]){
	int n, port, i;	
	port =  (argc > 1) ? atoi(argv[1]):10010;
	char bytes[3];
	bytes[TICK] = 0x01;
	bytes[TOCK] = 0x02;
	bytes[IDLE] = 0x03;
	char inputline[10];
	int dev_port;
	printf("Type y or n for port writing mode:\n");
	fgets(inputline, sizeof(inputline), stdin);
	if (inputline[0] == 'y' || inputline[0] == 'Y') {
		 printf("Prepare for writing to /dev/port\n");
		 port_writing_mode = 1;
		 last_port_write = TOCK; // So that we start off ticking!
		 dev_port = open("/dev/port", O_WRONLY);
		 if (dev_port == -1) {
			printf("Can't access file for write!\n");
			port_writing_mode = 0;
		 } 	
		 
	} else {
		 printf("Safely NOT writing to /dev/port\n");
	}
	start_daemon(port);
  	listen(net,5);
	char * response = (char*) malloc(50 * sizeof(char));
	int function; 
	int argument;
  	while (conn_left > 0){
  		int cl = sizeof(caddr);
		/* client ~ Session Socket */
  		client = accept(net, (struct sockaddr*)&caddr, &cl);
		client_open = 1;
		/* Make the Session Socket Non-blocking */
		int x;
  		x = fcntl(client ,F_GETFL,0);
  		fcntl(client, F_SETFL, x | O_NONBLOCK);
  		if (client < 0) { perror("accept"); exit(1); }
		while(client_open){
			/* read message from client: */
			usleep(1000*moment);			
			if(clock_started) {
				current_time++;
				
				if (port_writing_mode) {
					if (last_port_write == TICK) {
						printf("I'm writing 0x%x to byte 0x%x on /dev/port\n", bytes[TOCK], PORT_BYTE_OFFSET);
						printf("Seeking to Byte 0x%x in /dev/port\n", PORT_BYTE_OFFSET);
						lseek(dev_port, PORT_BYTE_OFFSET, SEEK_SET);
						write(dev_port, bytes+TOCK, 1);
						last_port_write = TOCK;
					} else if (last_port_write == TOCK) {
						printf("I'm writing 0x%x to byte 0x%x on /dev/port\n", bytes[TICK], PORT_BYTE_OFFSET);
						printf("Seeking to Byte 0x%x in /dev/port\n", PORT_BYTE_OFFSET);
						lseek(dev_port, PORT_BYTE_OFFSET, SEEK_SET);
						write(dev_port, bytes + TICK, 1);						
						last_port_write = TICK;
					}
				}
				printf("Time: %02d:%02d:%02d\n", current_time/(60*60), (current_time % (60*60))/60, current_time % 60);				
			}
  			n = read(client,buffer,255);  			
			if (n != -1 && !is_going_to) {
				function = (int) buffer[0];
				bzero(&argument, sizeof(int));
				memcpy(&argument, buffer+1, 4);
				switch( function ) {
    					case SET: 
						current_time = argument;
						printf("Time was set to: %02d:%02d:%02d\n", current_time/(60*60), (current_time % (60*60))/60, current_time % 60);
						bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = ACK;
    						/* writing Ack back */
    						n = write(client,buffer, BUF_SIZE);
    						break;
    					case GET:
        					bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = RET_VAL;
						memcpy(buffer+1, &current_time, 4);
    						/* Returning the time */
						n = write(client,buffer, BUF_SIZE);
    						break;
					case MOMENT:
						printf("Setting the moment to %d ms\n", argument);
						moment = argument;
						bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = ACK;
    						n = write(client,buffer, BUF_SIZE);
						break;
					case GO:
						printf("Starting clock\n");
						clock_started = 1;
						bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = ACK;
    						/* writing Ack back */
    						n = write(client,buffer, BUF_SIZE);
						break;
					case STOP:
						printf("Stopping the clock\n", argument);
						clock_started = 0;
						if  (port_writing_mode) {
							printf("I'm writing 0x%x to byte 0x%x on /dev/port\n", bytes[IDLE], PORT_BYTE_OFFSET);
							printf("Seeking to Byte 0x%x in /dev/port\n", PORT_BYTE_OFFSET);
							lseek(dev_port, PORT_BYTE_OFFSET, SEEK_SET);
							write(dev_port, bytes + IDLE, 1);
						}
						bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = ACK;
    						/* writing Ack back */
    						n = write(client,buffer, BUF_SIZE);
						break;
					case GOTO:
						if (argument < current_time || abs(current_time - argument) > 3600) {					
							printf("Bad Go To Time: %02d:%02d:%02d\n", argument/(60*60), (argument % (60*60))/60, argument % 60);
							bzero(buffer, BUF_SIZE*sizeof(char));								
							buffer[0] = RET_VAL;
							int fail = 0;
							memcpy(buffer+1, &fail, 4);
    							/* Returning the time */
							n = write(client,buffer, BUF_SIZE);
						} else {
							clock_started = 1;
							printf("Going to Time: %02d:%02d:%02d\n", argument/(60*60), (argument % (60*60))/60, argument % 60);
							go_to_time = argument;
							is_going_to = 1;
						}
						break;
						

					case FIN:
						if  (port_writing_mode) {
							printf("I'm writing 0x%x to byte 0x%x on /dev/port\n", bytes[IDLE], PORT_BYTE_OFFSET);
							printf("Seeking to Byte 0x%x in /dev/port\n", PORT_BYTE_OFFSET);
							lseek(dev_port, PORT_BYTE_OFFSET, SEEK_SET);
							write(dev_port, bytes + IDLE, 1);
						}												
						client_open = 0;
						bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = ACK;
    						/* writing Ack back */
    						n = write(client,buffer, BUF_SIZE);						
						break;
					case SHUT:
						if  (port_writing_mode) {
							printf("I'm writing 0x%x to byte 0x%x on /dev/port\n", bytes[IDLE], PORT_BYTE_OFFSET);
							printf("Seeking to Byte 0x%x in /dev/port\n", PORT_BYTE_OFFSET);
							lseek(dev_port, PORT_BYTE_OFFSET, SEEK_SET);
							write(dev_port, bytes + IDLE, 1);
						}
						client_open = 0;
						conn_left = 0;
						bzero(buffer, BUF_SIZE*sizeof(char));	
						buffer[0] = ACK;
    						/* writing Ack back */
    						n = write(client,buffer, BUF_SIZE);
						break;						
    					default :
        					client_open = 0;
				}								
				
			}
			if (is_going_to) {
				if(current_time >= go_to_time) {
					/* Give Reponse to Client */
					printf("Reached Go To Time: %02d:%02d:%02d\n", go_to_time/(60*60), (go_to_time % (60*60))/60, go_to_time % 60);
					bzero(buffer, BUF_SIZE*sizeof(char));											
					buffer[0] = RET_VAL;
					int success = 1;
					memcpy(buffer+1, &success, 4);    					
					n = write(client,buffer, BUF_SIZE);
					/* No Longer performing GOTO, stop clock */					
					is_going_to = 0;
					clock_started = 0;
				}
			}
						
		}
  		close(client);
		conn_left--;
  	}
	close(dev_port);
 	close(net);
	return 0; 
}
 
