/*
 * Nathaniel Lim
 * CS432 - Assignment 7
 * 
 * This is a utility to run that simply connects to 
 * wacd on port 10010 and shuts it down
 * Good to run after you have performed a test lock which
 * has finished, but WACD is still fielding client connections
 */
#include <stdio.h>
#include <time.h>
#include "wac.h"

void t(int *now)
{
  *now = time(0) - 4*60*60;
  *now = *now % (12*60*60);
}

int main(int argc, char**argv) {
  wac_init(argc>1?argv[1]:"localhost");
  wac_shutdown();
}
