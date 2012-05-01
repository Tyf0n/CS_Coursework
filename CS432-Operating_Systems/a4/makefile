## Nathaniel Lim
## CS432 - Spring 2011
## Assignment 4: 3/19/2011
## A (Really) Simple Make File of my test
## Make with:  'make test'
## Clean with  'make cleantest'

## The test program is designed to run with 'testinput.txt' as input
## Please run ./test < testinxt

## The freq is designed to run any input
## Please run ./freq < 'inputfile'
## or     run ./freq  
## and input words from stdin, Hit: Ctl-D (for EOF)

test :
	gcc -pthread -o test testing.c ts.h ts.c hash.h hash.c

freq :  
	gcc -pthread -o freq freq.c ts.h ts.c hash.h hash.c

cleantest :
	rm test

cleanfreq :
	rm freq
