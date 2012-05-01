/* Nathaniel Lim
 * Operating Systems
 * Assignment 5
 * 
 * Implementing the BitMap functions.
 * 
 * Utility routines that are useful to supporting Williams Unix File System
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "wufs_fs.h"
#include "wufs.h"

/*
 * Compute (rounding up) chunks of size chunkSize needed to hold size items.
 * Might be used to find out how many blocks we need for i inodes:
 *    fullChunks(i,WUFS_BLOCKSIZE/WUFS_INODESIZE);
 */
int fullChunks(int size, int chunkSize)
{
  return (size + (chunkSize-1))/chunkSize;
}

/*
 * Some bitfield manipulation functions
 * Accidentally deleted (*sigh*) by Duane.
 */

/*
 * Set the ith bit (zero origin) in bitfield f
 */
void setBit(__u8 *f, int i)
{
  if (i < 0) return;
  *(f+i/8) = *(f+i/8) | (1 << (i%8));
}

/*
 * Clear the ith bit (zero origin) in bitfield f
 */
void clearBit(__u8 *f, int i)
{
  if (i < 0) return;
  *(f+i/8) = *(f+i/8) & (~(1 << (i%8)));
}

/*
 * Return the ith bit (zero origin) in bitfield field: 0 or 1
 */
int getBit(__u8 *field, int i)
{
  if (i < 0) return -1;
  return (int)((*(field+i/8) & (1 << (i%8))) >> (i%8));
}

/*
 * Find the next bit (starting with i or, if -1, 0) set in field of n.
 * If none, return -1.
 */
int findNextSet(__u8 *f, int i, int n)
{
  if (i < 0) return -1;
  if (i == -1) i = 0;
  int upper_lim = i+n;
  for(; i < upper_lim; i++) {
	if((1<<(i%8))&(*(f+i/8))){
		return i;
	}
  }
  return -1;
}

/*
 * Find the next bit (starting with i or, if -1, 0) clear in field of n.
 * If none, return -1.
 */
int findNextClear(__u8 *f, int i, int n)
{
  if (i < 0) return -1;
  if (i == -1) i = 0;
  int upper_lim = i+n;
  for(; i < upper_lim; i++) {
	if(!((1<<(i%8))&(*(f+i/8)))){
		return i;
	}
  }
  return -1;
}

/*

//Test Code for the bitmap operations
void printbin(__u8 *d) {
	int j;
	for(j = 0; j < 8; j++){
		int i;
		for(i = 7; i>=0; i--) {
	        	if((1<<i)&(*(d+j))){
				printf("1");
	        	} else {
				printf("0");
			}
	    	}
		printf(" ");
	}
	printf("\n");
}

int main () {
	bitmap f = (__u8*) malloc (sizeof(__u8)*8);
	*f = 127;
	*(f+1)= 64;
	printbin(f);
	setBit(f, 7);
	printbin(f);
	clearBit(f, 6);
	printbin(f);
	int b = 7;
	int i;
	for (i = 0; i < 16; i++){
		printf("getBit(f, %d) = %d\n", i, getBit(f, i));
	}
	printf("findNextClear(f, 5, 3) = %d\n", findNextClear(f, 5, 3));
	printf("findNextClear(f, 0, 4) = %d\n", findNextClear(f, 0, 4));
	printf("findNextClear(f, 7, 1) = %d\n", findNextClear(f, 7, 1));
	printf("---------\n");
	*f = 64;
	printbin(f);
	for (i = 0; i <= 7; i++){
		printf("getBit(f, %d) = %d\n", i, getBit(f, i));
	}
	printf("findNextSet(f, 5, 3) = %d\n", findNextSet(f, 5, 3));
	printf("findNextSet(f, 0, 8) = %d\n", findNextSet(f, 0, 8));
	printf("findNextSet(f, 7, 1) = %d\n", findNextSet(f, 7, 1));
	free(f);
	return 0;
}
*/

