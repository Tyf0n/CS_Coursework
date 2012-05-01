typically:	mkfs.wufs

mkfs.wufs:	mkfs.wufs.c wufs.c wufs.h wufs_fs.h 
	gcc -g -Wall -o mkfs.wufs mkfs.wufs.c wufs.c

install:
	@echo '(Install in /sbin to use with mkfs -t wufs.)'

clean:	
	@rm -f mkfs.wufs *.o *~ \#*
	@echo Clean.
