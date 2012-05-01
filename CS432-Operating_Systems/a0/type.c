// Verifier for the bash "type" command: print full path of executables.
// Makes use of hash and, possibly indirectly, cirq types.

// On linux to use strdup, you need the following:
#define _BSD_SOURCE 1
// see man 3 readdir and man 7 feature_test_macros
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <dirent.h>
#include "hash.h"


int main()
{
  // allocate a hash table to translate executable -> full path specification
  hash h = ht_alloc(997);
  // get path:
  char *path = getenv("PATH");	/* getenv(3) */
  char cmd[256], name[1024], basename[256];
  char *dirname;
  DIR *dir;
  // split path members
  while ((dirname = strsep(&path,":"))) { /* strsep(3) */
    // open directory file
    dir = opendir(dirname);		  /* opendir(3) */
    if (dir) {
      struct dirent *de;	/* dirent(5) */
      while ((de = readdir(dir))) { /* readdir(3) */
	int type = de->d_type;
	// check regular files....
	if (type & DT_REG) {
	  strcpy(name,dirname);	/* strcpy(3) */
	  strcat(name,"/");	/* strcat(3) */
	  strcpy(basename,de->d_name);
	  strcat(name,basename);
	  // ...that are executable
	  if (0 == access(name,X_OK)) { /* access(2) */
	    // add to database if they've not been encountered before
	    if (!ht_get(h,basename)) {
	      // enter into table, but:
	      // make copies of key and value to void poisoning
	      ht_put(h,strdup(basename),strdup(name)); /* strdup(3) */
	    }
	  }
	}
      }
    }
  }
  printf("For each executable entered, its path will be printed.\n");
  while (1 == scanf("%s",cmd)) {
    char *e = ht_get(h,cmd);
    if (e) { printf("%s\n",e); }
    else { printf("Not found as executable.\n");}
  }
  return 0;
}
