/*

C Implementation for Hash Table
Nathaniel Lim
CS432
2/9/2011

The hashTable struct is a array (declared as a pointer) of a bunch of void pointers.
The hashcodes for the String keys are found from my hash_function, index = hashcode mod buckets
The entries for the keys are void pointers, allowing the storage of arbitrary types of entries.
*/

#include <stdio.h>
#include <stdlib.h>
#include "hash.h"

extern hash ht_alloc(int buckets){ //Allocate a hashTable
	hash h = (struct hashTable *)malloc(sizeof(struct hashTable));
	h->slots = (void **)malloc(buckets*sizeof(void *));
	h->num_slots = buckets;
	return h;
}

extern unsigned long hash_function(char *str) {
	//Hash Function djb2 from http://www.cse.yorku.ca/~oz/hash.html
        unsigned long hashcode = 5381;
        int c;
        while ((c = *str++))
            hashcode = ((hashcode << 5) + hashcode) + c; /* hashcode * 33 + c */
        return hashcode;
}


extern void ht_put(hash h, char *key, void *value){ // add a key/value pair
	unsigned long code = hash_function(key);
	int index = code % h->num_slots;
	(h->slots)[index] = value;
}


extern void *ht_get(hash h, char *key){ // get a value based on key
	unsigned long code = hash_function(key);
	int index = code % h->num_slots;
	return (h->slots)[index];
}

void ht_free(hash h) { // return all space allocated by table	
	free(h->slots);
	free (h);
}              
/*
int main (){
	printf("si: %lu\n", hash_function("si"));
	printf("ii: %lu\n", hash_function("ii"));
}
*/
