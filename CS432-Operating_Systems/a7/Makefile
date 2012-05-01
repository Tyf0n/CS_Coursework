# Nathaniel Lim
# CS432 - Assignment 7 - 5/6/2011
# Makefile for the Williams Analog Clock interface

typically:
	@echo Executables: wacd, standard, silly, my_clock, shutdown
wacd:
	gcc -o wacd wacd.c
standard:
	gcc -o standard wac.h wac.c standard.c
silly:
	gcc -o silly wac.h wac.c silly.c
my_clock:
	gcc -o my_clock wac.h wac.c my_clock.c
shutdown:
	gcc -o shutdown wac.h wac.c shutdown.c
clean:	
	@rm -f wacd wac standard silly my_clock shutdown
	@echo Cleaned all Executables.
