/*
 * Nathaniel Lim
 * CS432 - A7
 * Header File for client service to the WACD
 */

#ifndef WAC_H
#define WAC_H

#define SET 0x1
#define GET 0x2
#define MOMENT 0x3
#define GO 0x4
#define STOP 0x5
#define GOTO 0x6
#define SHUT 0x7
#define FIN 0x8
#define ACK 0xE
#define RET_VAL 0xF
#define BUF_SIZE 6
#define MAX_CONNECTIONS 3

extern int wac_init(char *hostname);
extern int wac_set(int secs);
extern int wac_get(void);
extern int wac_moment(int msecs);
extern int wac_go(void);
extern int wac_stop(void);
extern int wac_goto(int time);
extern int wac_finish(void);
extern int wac_shutdown(void);



#endif /* WAC_H */
