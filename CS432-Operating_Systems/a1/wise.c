#define _GNU_SOURCE
#include <unistd.h>
#include <stdio.h>
#include <sys/syscall.h>
#include <sys/types.h>
/*
 *
 * Nathaniel Lim - CS432 - Assignment 1: Test Program
 * Create a System Call, Found in a1.diff
 *
 * The System Call Fills a buffer with a snippet of wisdom
 *
 * This is a program in User Land that runs the system call.
 */

int main(int argc, char *argv[]){
	char buffer[100];
      	if (0 == syscall(299, buffer)){
		printf("%s\n", buffer);
	} else {
		perror("wisdom call");
	}
}

