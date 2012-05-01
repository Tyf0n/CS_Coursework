#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "ts.h"
int main()
{
  char * shape = "si";
  char word[512], *fp, *tp;
  int count;
  printf("Starting the scanning\n");
  // scan words, sans punctuation
  while (1 == scanf("%s",word)) {
    for (fp = tp = word; (*tp = *fp); fp++) isalpha(*tp) && (*tp++ |= 32);
    count = 0;    
    inp("s?i",word,&count);	// read in tuple (if none, count untouched)
    count++;
    out("si",word,count);	// write out tuple
  }
  count = 0;
  printf("Done reading in\n");
  //printTuples(shape);
  //strcpy(word, "Nonsense");
  while (inp("?si",word,1)) count++;
  printf("%d words appear once\n",count);
  count = 0;
  while (inp("?si",word,2)) count++;
  printf("%d words appear twice\n",count);
  count = 0;
  while (inp("?si",word,3)) count++;
  printf("%d words appear 3 times\n",count);
  count = 0;
  while (inp("?si",word,4)) count++;
  printf("%d words appear 4 times\n",count);
  int times = 0;
  rdp("s?i", "the", &times);
  printf("'the' appears %d times\n", times);
  times = 0;
  rdp("s?i", "and", &times);
  printf("'and' appears %d times\n", times);
  //printTuples("si");  
  return 0;

}
