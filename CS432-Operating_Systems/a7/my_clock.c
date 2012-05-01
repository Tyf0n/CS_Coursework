/*
 * Nathaniel Lim
 * CS432 - Assignment 7
 * 
 * My Clock:
 * 
 * Tick the clock at the normal rate until it reaches the the zero
 * Change the ticking to 3x normal rate (20s to do one revolution)
 * 
 * Move the second hand at 3x the rate, pausing at 0s, 15s, 30s, 45s
 * 
 * Since the movement of the second hand takes 5s.
 * The sleep required to keep the clock in time is 10s.
 */


#include <stdio.h>
#include <time.h>
#include "wac.h"

void t(int *now)
{
  *now = time(0) - 4*60*60;
  *now = *now % (12*60*60);
}

int main(int argc, char**argv)
{
  int now,i;
  wac_init(argc>1?argv[1]:"localhost");
  wac_stop();
  wac_moment(1000);
  t(&now);
  wac_set(now);
  //Go to the start of the next minute
  wac_goto( now + (60 - (now % 60)));
  wac_moment(333);
  sleep(2);
  for (i = 0; i < 5 ; i++) {
    sleep(10);
    t(&now);
    wac_goto(now);
  }
  wac_finish();
}
