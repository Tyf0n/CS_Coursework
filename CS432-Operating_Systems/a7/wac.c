/*
 * Nathaniel Lim
 * CS432 - Assignment 7
 *
 * wac.c creates a client interface to the 
 * wacd (Williams Analog Clock Daemon) server
 */

#include "wac.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/fcntl.h>
#include <time.h>

static int port = 10010;
static int net;
static unsigned char buffer[BUF_SIZE];
static struct sockaddr_in saddr;  /* an ip(4) socket address */

int wac_init(char *hostname){	
    	struct hostent *server;
    	/* get server address */
    	server = gethostbyname(hostname);
    	if (server == NULL) {
      		perror("gethostbyname");
      		exit(0);
    	}
    	net = socket(AF_INET, SOCK_STREAM, 0);
    	if (net < 0) {
      		perror("socket");
      		exit(1);
    	}
    	memset(&saddr,0,sizeof(saddr));
    	saddr.sin_family = AF_INET;
    	/* copy the host address to the socket */
    	bcopy(server->h_addr, &(saddr.sin_addr.s_addr), server->h_length);
    	saddr.sin_port = htons(port);
    	if (connect(net,(struct sockaddr *)&saddr,sizeof(saddr)) < 0) {
      		perror("connect");
      		exit(1);
    	}
}
int wac_set(int secs) {
	int function, argument, response, return_val, n;	
	// First write
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = SET;
	argument = secs;	
	buffer[0] = function;
	memcpy(buffer + 1, &argument, 4);
	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);		
	/* Read Response */
	bzero(buffer, BUF_SIZE*sizeof(char));    	
	n = read(net, buffer, BUF_SIZE);
    	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == ACK) {
		printf("Set Time: %02d:%02d:%02d\n", secs/(60*60), (secs % (60*60))/60, secs % 60); 
	}
	return 1;	

}
int wac_get(void) {
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = GET;
	buffer[0] = function;
	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */
	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net,buffer,255);
    	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == RET_VAL) {
		printf("Got Time: %02d:%02d:%02d\n", return_val/(60*60), (return_val % (60*60))/60, return_val % 60); 
	}
	return return_val;	
}
int wac_moment(int msecs) {
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = MOMENT;
	argument = msecs;	
	buffer[0] = function;
	memcpy(buffer + 1, &argument, 4);
	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */    	
	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net, buffer, BUF_SIZE);
	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == ACK) {
		printf("WACD moment set to: %d ms\n", msecs);
	}
	return 1;	
}
int wac_go(void) {
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = GO;
	buffer[0] = function;
	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */
    	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net,buffer,BUF_SIZE);
	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == ACK) {
		printf("Started Clock!\n");
	}
	return 1;
}
int wac_stop(void) {
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = STOP;
	buffer[0] = function;
    	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */
	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net, buffer, BUF_SIZE);    	
	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == ACK) {
		printf("Stopped Clock\n");
	}
	return 1;
}
int wac_goto(int time){
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = GOTO;
	argument = time;	
	buffer[0] = function;
	memcpy(buffer + 1, &argument, 4);
    	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */
	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net, buffer, BUF_SIZE);
    	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == RET_VAL) {
		printf("Go To returned: ");
		if (return_val) {
			printf("at Time: %02d:%02d:%02d\n", time/(60*60), (time % (60*60))/60, time % 60); 
		} else {
			printf("prematurely, invalid Go To Time: %02d:%02d:%02d\n", time/(60*60), (time % (60*60))/60, time % 60);
		}
	}
	return return_val;
}
int wac_finish(void) {
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = FIN;
	buffer[0] = function;
    	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */
    	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net, buffer, BUF_SIZE);
    	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == ACK) {
		printf("Closed connection to WACD!\n");
	}
	close(net);
	return 1;
}
int wac_shutdown(void) {
	int function, argument, response, return_val, n;	
	bzero(buffer, BUF_SIZE*sizeof(char));
	function = SHUT;
	buffer[0] = function;
    	/* write it to server */
    	n = write(net,buffer, BUF_SIZE);
	/* Read response */
    	bzero(buffer, BUF_SIZE*sizeof(char));
    	n = read(net, buffer, BUF_SIZE);
    	response = (int) buffer[0];
	bzero(&return_val, sizeof(int));
	memcpy(&return_val, buffer+1, 4);
	if (response == ACK) {
		printf("Shut down WACD!\n");
	}
	close(net);
	return 1;
}

void setPort(int x) {
	port = x;
}
