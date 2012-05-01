/**
A C Header File for Hash Table

Nathaniel Lim
CS432
2/9/2011

Since my hashTable does not utilize the Circular Queue Structure in this directory,
I compiled and ran the type program as follows:

gcc -Wall -o type type.c hash.c
./type


*/

#ifndef HASH_H
#define HASH_H
typedef struct hashTable *hash;          // hashTable pointer

struct hashTable {
	void ** slots;
	int num_slots;
};

extern hash ht_alloc(int buckets);       // allocate a hash table
extern void ht_put(hash h, char *key, void *value);        // add a key/value pair
extern void *ht_get(hash h, char *key); // get a value based on key
extern void ht_free(hash h);               // return all space allocated by table
#endif
