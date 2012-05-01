/*
 * Make Williams Unix File System utility, mk.wufs.
 *
 * Usage: mkwufs [-c] [-l file] filesystem
 *    -c    check disk blocks, possibly creating bad block list
 *    -l    read bad block list from file
 *    filesystem  the device to be used
 */

/*
 * This utility writes an empty WUFS (Williams Utilitiarian File System) image
 * on into an associated file or block device.
 */
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/fs.h>
#include <unistd.h>
#include <fcntl.h>
#include <mntent.h>
#include <string.h>
#include <time.h>

#include "wufs_fs.h"
#include "wufs.h"

static char *ProgName = 0;	/* name of this utility */
static int Verbose = 0;		/* talk and talk about what's up */
static char *Device = 0;	/* the target device */
static int Disk = -1;		/* descriptor of device */
static int Check = 0;		/* check blocks on device */
static char *BBFile = 0;	/* file containing bad block numbers */
static int BadBlocks = 0;	/* count of bad blocks found */

/* internal representation of file system fields */
static struct wufs_super_block *SB = 0; /* the super block */
static bitmap IMap;		       /* the inode map */
static bitmap BMap;		       /* the block map */
static struct wufs_inode *Inode = 0;  /* the inode structures */
static struct wufs_dirent *RootDir = 0; /* root directory block */
static struct wufs_indirect_block *IDB = 0;  /*All of the indirect blocks */
static int num_idb_allocated;

inline int maximum(int a, int b) { return (a>b)?a:b; }
inline int minimum(int a, int b) { return (a<b)?a:b; }

/*
 * Forward declaration of local routines.
 */
static void Usage();
static void parseArgs(int,char**);
static int mounted(char *);
static int probe(int,long);
static long determineSize(char*);
static int allocBlock(void);
static int allocInode(void);
/*
 * Print the current usage.  Assumes ProgName is set.  Does not return.
 */
void Usage()
{
  fprintf(stderr,"Usage: %s [-ch] [-l file] device\n",ProgName);
  fprintf(stderr,"\t-c\tcheck for bad blocks [currently %s]\n",Check?"on":"off");
  fprintf(stderr,"\t-h\tprint this usage\n");
  fprintf(stderr,"\t-l\tbad block file [currently %s]\n",BBFile?BBFile:"not used");
  fprintf(stderr,"\t-v\tbe verbose%s\n",Verbose?" [like, I already am]":"");
  fprintf(stderr,"\tdevice\tdevice targeted for file system [currently %s]\n",Device?Device:"not specified");
  exit(1);
}

/*
 * Parse the argument list.  Will not return on error.
 */
void parseArgs(int argc, char **argv)
{
  ProgName = argv[0];
  while (--argc) {
    char *arg = *++argv;
    if (arg[0] == '-') {
      while (*++arg) {
	switch (*arg) {
	case 'h':
	  Usage();
	  break;
	case 'c':
	  Check = 1;
	  break;
	case 'v':
	  Verbose = 1;
	  break;
	case 'l':
	  BBFile = *++argv;
	  argc--;
	  break;
	default:
	  fprintf(stderr,"Unrecognized option: -%c\n",*arg);
	  Usage();
	  break;
	}
      }
    } else {
      if (Device) Usage();
      else Device = arg;
    }
  }
  if (!Device) Usage();
}

/*
 * This routine checks to see if the device is mounted.
 * It does this by searching through the /etc/mtab file for a file system
 * with the same name.  The interface is described in getmntent(3).
 */
int mounted(char *device)
{
  FILE *mtab = setmntent("/etc/mtab","r");
  struct mntent *fs;
  while ((fs = getmntent(mtab)) && strcmp(fs->mnt_fsname,device));
  endmntent(mtab);
  return fs != 0;
}

/*
 * probe to see if there's a readable value at a file's location.
 */
int probe(int f, long offset)
{
  char c;
  return (lseek(f,offset,SEEK_SET)>=0) && read(f,&c,1);
}

/*
 * Determine the size of the device, in bytes.
 * We do this by performing a binary search on legal seek values.
 */
static long determineSize(char *device)
{
  long low = 0, high = 1, mid;
  int size;
  int f = open(device,O_RDONLY);
  if (f < 0) { perror(device); exit(1); }

  /* assume this is a block device, and ask for the number of blocks */
  if (ioctl(f, BLKGETSIZE, &size) >= 0) {
    /* success */
    size *= 512;
  } else {
    /*
     * failure: probably a character device
     * first: seek until you get an error
     */
    for (;;) {
      if (!probe(f,high)) break;
      low = high;
      high *= 2;
    }
    
    /* now hone the bounds */
    do {
      /* invariant: low is valid, high is not */
      mid = (low + high)/2;	/* rounds down to low */
      if (probe(f,mid)) {
	low = mid;
      } else {
	high = mid; 
      }
    } while (high-low-1);
    size = low+1;
  }
  close(f);
  return size;
}

/*
 * We populate the fields of the on-disk superblock.
 * Many of these calculations depend on the size of the specific disk, 
 * and illegal geometries are not identified until later in the process.
 */
void buildSuperBlock(void)
{
  /* we allocate a block to hold the superblock */
  SB = (struct wufs_super_block*)malloc(WUFS_BLOCKSIZE);
  if (!SB) {
    fprintf(stderr,"Could not allocate superblock memory.\n");
    exit(1);
  }
  /* clear the memory */
  memset(SB,0,WUFS_BLOCKSIZE);

  /* identify this as a WUFS */
  SB->sb_magic = WUFS_MAGIC;

  /* figure out how many bytes there are on the device */
  int size = determineSize(Device);
  if (size == 0) {
    fprintf(stderr,"Could not determine the size of device %s\n",Device);
    exit(1);
  }

  /* compute the number of blocks that fit on this device (rounds down) */
  SB->sb_blocks = minimum(size/WUFS_BLOCKSIZE,0xffff);

  /* compute the number of inodes needed (fills out block) */
  int ipb = WUFS_INODES_PER_BLOCK;
  SB->sb_inodes = SB->sb_blocks/3;
  /* ...round up to fill out block */
  SB->sb_inodes = (SB->sb_inodes+(ipb-1))/ipb*ipb;

  /* compute the number of blocks needed for inode map (rounds up) */
  SB->sb_imap_bcnt = fullChunks(SB->sb_inodes,WUFS_BLOCKSIZE*8);

  /* compute the number of blocks occupied by the block map
   * (this is overkill -- the first few blocks are always used, but
   *  the approach is simple and has the advantage that smaller maps
   *  can be written into the same space at a later date, if needed)
   */
  SB->sb_bmap_bcnt = fullChunks(SB->sb_blocks,WUFS_BLOCKSIZE*8);

  /* compute the index of the first data block */
  SB->sb_first_block = 2                 /* boot + super */ + 
                       SB->sb_imap_bcnt  /* for inode map */ +
                       SB->sb_inodes/ipb /* blocks of inodes */ +
                       SB->sb_bmap_bcnt  /* for block map */;

  /* do a sanity check: is there enough space to hold even the headers? */
  if (SB->sb_first_block >= SB->sb_blocks) {
    fprintf(stderr,"Device is too small to support file system.\n");
    exit(1);
  }

  /* compute the maximum file size (this file system...is a dog) */
  SB->sb_max_fsize = (__u32)(WUFS_INODE_BPTRS * WUFS_BLOCKSIZE + WUFS_INODE_INDIRECT_BPTRS *(WUFS_BLOCKSIZE/WUFS_BPTRSIZE)*WUFS_BLOCKSIZE);
  //SB->sb_max_fsize = (__u16) (WUFS_INODE_BPTRS * WUFS_BLOCKSIZE);
	printf("Max File size: %x\n", SB->sb_max_fsize);
  /* set the state appropriately */
  SB->sb_state = WUFS_VALID_FS;

  /* report on what we've done */
  if (Verbose) {
    fprintf(stderr,"block 0 reserved to hold boot sector\n");
    fprintf(stderr,"block 1 holds super block, Size: %d Bytes\n", (int)sizeof(struct wufs_super_block));
    fprintf(stderr,"%d data blocks available\n",SB->sb_blocks);
    fprintf(stderr,"   %d block(s) reserved for block map\n",SB->sb_bmap_bcnt);
    fprintf(stderr,"%d inodes allocated\n",SB->sb_inodes);
    fprintf(stderr,"   inodes contain %d pointers\n",WUFS_INODE_BPTRS);
    fprintf(stderr,"   %d block(s) reserved for inode map\n",SB->sb_imap_bcnt);
    fprintf(stderr,"   %d block(s) reserved for %d byte inodes\n",SB->sb_inodes/ipb,
	    (int)sizeof(struct wufs_inode));
    fprintf(stderr,"first data block is block %d\n",SB->sb_first_block);
    fprintf(stderr,"maximum file size is %d Bytes\n",SB->sb_max_fsize);
    fprintf(stderr,"size of directory entry: %d\n",(int)sizeof(struct wufs_dirent));
  }
  
}

void buildBitMaps(void)
{
  /* allocate and initialize the inode bitmap */
  IMap = (__u8*)malloc(SB->sb_imap_bcnt*WUFS_BLOCKSIZE);
  if (!IMap) {
    fprintf(stderr,"Could not allocate memory for inode bitmap.\n");
    exit(1);
  }
  /* we set the bits so that no "non-inodes" are ever available */
  memset(IMap,0xff,SB->sb_imap_bcnt*WUFS_BLOCKSIZE);
  int inode;
  /* remember: inodes start counting at 1, thus <= and inode-1 */
  for (inode = 1; inode <= SB->sb_inodes; inode++) {
    clearBit(IMap,inode-1);
  }

  /* allocate and initialize the blockmap bitmap */
  BMap = (__u8*)malloc(SB->sb_bmap_bcnt*WUFS_BLOCKSIZE);
  if (!BMap) {
    fprintf(stderr,"Could not allocate memory for the data block bitmap.\n");
    exit(1);
  }
  memset(BMap,0xff,SB->sb_bmap_bcnt*WUFS_BLOCKSIZE);
  int block;
  for (block = SB->sb_first_block; block < SB->sb_blocks; block++) {
    clearBit(BMap,block);
  }
}

/*
 * Check to see if a block is "bad".  We simply demand that 
 * we can reach the appropriate block address and read an entire
 * block from that point onward.  No checks for writability.
 */
int bad(int block)
{
  char buffer[WUFS_BLOCKSIZE];	/* holds a block */
  /* we seek to the location on the drive and attempt a read */
  long blockOffset = block*WUFS_BLOCKSIZE;
  return (blockOffset != lseek(Disk,blockOffset,SEEK_SET)) ||
         (WUFS_BLOCKSIZE != read(Disk,buffer,WUFS_BLOCKSIZE));
}

/*
 * Attempt to read all (usable) blocks on the device.
 * Bad blocks are immediately reserved and (later) will be gathered
 * together into one or more bad-block files.
 */
void checkBlocks(void)
{
  int block;
  if (Verbose) {
    fprintf(stderr,"Checking disk blocks...");
    fflush(stderr);
  }
  for (block = 0; block < SB->sb_blocks; block++) {
    if (bad(block)) {
      if (!getBit(BMap,block)) BadBlocks++;
      setBit(BMap,block);
      if (Verbose) {
	fprintf(stderr,"Block %d is bad.\n",block);
      }
      if (block < SB->sb_first_block) {
	fprintf(stderr,"Early bad block (%d) makes file system impossible.\n",
		block);
	exit(1);
      }
    }
  }
  if (Verbose) {
    fprintf(stderr,"done.\n");
  }
}

/*
 * Read a file of bad blocks numbers, and incorporate 
 * those into the bad block list.
 */
void loadBadBlockFile(char *fname)
{
  /* open the file of bad blocks */
  FILE *bbf = fopen(fname,"r");
  if (bbf == 0) {
    fprintf(stderr,"Could not open the bad block file %s.\n",fname);
    exit(1);
  }
  if (Verbose) {
    fprintf(stderr,"Bad blocks read from %s:\n",fname);
  }
  
  /* now, read through the blocks */
  int block;
  while (1 == fscanf(bbf,"%d",&block)) {
    if (block < SB->sb_first_block) {
      fprintf(stderr,"Early bad block (%d) makes file system impossible.\n",
	      block);
      exit(1);
    } else if (block >= SB->sb_blocks) {
      fprintf(stderr,"Warning: bad block %d is beyond end of device.\n",
	      block);
      fprintf(stderr,"(Are you sure this bad block list goes with this device?)\n");
      continue;
    }
    if (Verbose) { fprintf(stderr," %d",block); fflush(stderr); }
    if (!getBit(BMap,block)) {
      setBit(BMap,block);
      BadBlocks++;
    } else if (Verbose) {
      fprintf(stderr," (known)"); fflush(stderr);
    }
  }
  if (Verbose) { fprintf(stderr,"\n"); }
  fclose(bbf);
}

/*
 * Build the initial inodes.
 * inode 1 is always the root directory (identifiable by the fact that . == ..)
 * inode 2 (and possibly others) are files to hold the identifiable bad blocks.
 */
void buildInodes(void)
{
  Inode = (struct wufs_inode *)calloc(SB->sb_inodes,sizeof(struct wufs_inode));
  if (!Inode) {
    fprintf(stderr,"Could not allocated memory for the inodes.\n");
    exit(1);
  }
  
  /* create root directory file: */

  /*  1. allocate the inode */
  int rootInode = allocInode(); /* allocate a directory */
  struct wufs_inode *rino = &Inode[rootInode-1];
  rino->in_mode = S_IFDIR + 0755; /* mark as directory */

  /*  2. allocate space for the directory file */
  int rootDir = allocBlock();	/* allocate directory store */
  RootDir = (struct wufs_dirent *)calloc(WUFS_BLOCKSIZE,1);
  if (!RootDir) {
    fprintf(stderr,"Could not allocate memory to hold root directory image.\n");
    exit(1);
  }
  rino->in_block[0] = rootDir;

  /* tell me something I don't know */
  if (Verbose) {
    fprintf(stderr,"Root directory is at inode %d, using block %d.\n",
	    rootInode, rootDir);
  }

  /*  3. populate the directory file: */
  struct wufs_dirent *dp = RootDir;
  /* ... add "." directory */
  dp->de_ino = rootInode;
  strcpy(dp->de_name,".");
  rino->in_size += WUFS_DIRENTSIZE;
  rino->in_nlinks++;
  dp++;

  /* ... add ".." directory */
  dp->de_ino = rootInode;
  strcpy(dp->de_name,"..");
  rino->in_size += WUFS_DIRENTSIZE;
  rino->in_nlinks++;
  dp++;

  /* We now collect all the bad blocks, bundled together into one or more "files" */
  /* keep count of remaining bad blocks to be bundled */
  int bbc = BadBlocks;
  int fileNumber = 0;
  int bblock = SB->sb_first_block; /* first possible bad block */
    
  while (bbc) {
    /* ... add a ".bad-0", ".bad-1", etc. */
    char fileName[WUFS_NAMELEN+1];
    sprintf(fileName,".bad-%x",fileNumber);
    if (Verbose) {
      fprintf(stderr,"Placing the following bad blocks in /%s:\n", fileName);
    }
    /* 
     * first, we check to see if we can add another bad block file without
     * extending the root directory to a second block.  If so, we give up.
     * (Woof.  This system is a dog.)
     */
    if ((dp - RootDir) >= WUFS_DIRENTS_PER_BLOCK) {
      fprintf(stderr,"Too many bad blocks to store in root directory.\n");
      exit(1);
    }

    /* allocate inode to hold some bad block pointers */
    int bbinonum = allocInode();
    struct wufs_inode *bbino = &Inode[bbinonum-1];
    int n = 0;

    /* place several bad blocks into this file */
    while (bbc && (n < WUFS_INODE_BPTRS)) {
       	bblock = findNextSet(BMap,bblock,SB->sb_blocks);
      	/* sanity check: shouldn't run out of bad blocks before bbc hits zero */
      	if (bblock == -1) {
		fprintf(stderr,"Internal error: lost bad block while building root directory.\n");
		exit(1);
      	}
      	/* take care: step over root directory block (only one allocated) */
      	if (bblock != rootDir) {
		if (Verbose) {
		  fprintf(stderr," %d",bblock); fflush(stderr);
		}
		bbino->in_block[n++] = bblock;
		bbino->in_size += WUFS_BLOCKSIZE;
		/* one more bad block, taken care of */
		bbc--;
      	}
      
      bblock++;
    }

  //Set up and write the indirect block containing bad block addresses
   if (bbc) {
	int ind_block = findNextClear(BMap, 0, SB->sb_blocks);
	setBit(BMap, ind_block);
	if (ind_block == -1) {
		fprintf(stderr,"Internal error: lost bad block while building root directory.\n");
		exit(1);
      	}
	IDB = (struct wufs_indirect_block *) malloc (5 * sizeof(struct wufs_indirect_block));
	IDB[num_idb_allocated].block_number = ind_block;	
	bbino->in_block[n++] = ind_block;
	int i = 0;		
	while (bbc) {
		bblock = findNextSet(BMap,bblock,SB->sb_blocks);
		while (bblock == rootDir) {bblock = findNextSet(BMap,bblock,SB->sb_blocks);}	

		IDB[num_idb_allocated].block_addresses[i++] = bblock;
		if (Verbose) {
		  fprintf(stderr," %d",bblock); fflush(stderr);
		}
		bbino->in_size += WUFS_BLOCKSIZE;
		bbc--;
		bblock++;

	}

	num_idb_allocated++;
    }



    if (Verbose) {
      	fprintf(stderr,"\n");
    }
   

    /* add the file to the root directory */
   dp->de_ino = bbinonum;
   strncpy(dp->de_name,fileName,WUFS_NAMELEN);
   rino->in_size += WUFS_DIRENTSIZE;
   bbino->in_nlinks++;
   dp++;
    
   /* get read for the next file: */
   fileNumber++;
  }
}

/*
 * Find (and allocate) an inode.  The fields of the inode are
 * initialized to support an empty file, created now.
 * Failure is not an option.
 */
int allocInode(void)
{
  struct wufs_inode *node;

  /* look at the map, and find a free inode (1 origin) */
  int ino = findNextClear(IMap, 0, SB->sb_inodes);
  //printf("IMap = %x\n", *IMap);
  //printf("SB->sb_inodes = %d\n", SB->sb_inodes);
  if (ino == -1) {
    fprintf(stderr,"Could not find a free inode (necessary).\n");
    exit(1);
  }

  /* mark it as allocated */
  setBit(IMap,ino);

  /* make the inode number 1-origin */
  ino++;

  /* now, populate the fields for an empty file, created now */
  node = &Inode[ino-1];
  node->in_mode = S_IFREG + 0644; /* readable regular file (see stat(2)) */
  node->in_nlinks = 0;		/* must be updated to nonzero value! */
  node->in_uid = getuid();	/* owner of file is me */
  node->in_gid = node->in_uid ? getgid():0; /* trad. user root is group root */
  node->in_size = 0;		/* size is initially empty */
  node->in_time = time(0);	/* now is the (creation) time! */
  return ino;
}

/*
 * Find (add allocate) a free data block.
 * The approach simply involves a scan of the block map for a zero.
 * It's fatal, at this point, if none is found.
 */
int allocBlock(void)
{
  int block = findNextClear(BMap, SB->sb_first_block, SB->sb_blocks);
  if (block == -1) {
    fprintf(stderr,"Could not allocate free data block (necessary).\n");
    exit(1);
  }

  /* mark it as allocated */
  setBit(BMap,block);

  return block;
}

/*
 * Write one or more blocks.
 */
void writeBlocks(int disk, int blockNumber, void *data, int blockCount)
{
  long offset = blockNumber * WUFS_BLOCKSIZE;
  int byteCount = blockCount * WUFS_BLOCKSIZE;
  if (offset != lseek(disk, offset, SEEK_SET)) {
    fprintf(stderr,"Error seeking to valid block during write.\n");
    exit(1);
  }
  if (byteCount != write(disk, data, byteCount)) {
    fprintf(stderr,"Error during write to seemingly valid block.\n");
    exit(1);
  }
}

/*
 * Write out file system.
 * Clears boot block, writes superblock, bitmaps, inodes, and root directory.
 */
void writeFS(void)
{
  /* zero out the boot sector (and associated block) */
  __u8 blk[WUFS_BLOCKSIZE];
  memset(blk,0x00,WUFS_BLOCKSIZE);
  writeBlocks(Disk, 0, blk, 1);

  /* write the superblock */
  writeBlocks(Disk, 1, SB, 1);

  /* write the inode bitmap */
  writeBlocks(Disk, 2, IMap, SB->sb_imap_bcnt);

  /* write the data block bit map */
  int lba = 2 + SB->sb_imap_bcnt;
  writeBlocks(Disk, lba, BMap, SB->sb_bmap_bcnt);
  
  /* write the inodes */
  lba += SB->sb_bmap_bcnt;
  writeBlocks(Disk, lba, Inode, SB->sb_inodes/WUFS_INODES_PER_BLOCK);

  /*write the indirect blocks*/
  int i;
  for(i = 0; i < num_idb_allocated; i++) {
	writeBlocks(Disk, IDB[i].block_number, IDB[i].block_addresses, 1);
  }

  /* write the root directory */
  int rootDirLBA = Inode[0].in_block[0];
  writeBlocks(Disk, rootDirLBA, RootDir, 1);

  /* free at last, free at last */
  if (Verbose) {
    fprintf(stderr,"file system written.\n");
  }
}

int main(int argc, char **argv)
{
  parseArgs(argc,argv);
  if (mounted(Device)) {
    fprintf(stderr,"Won't make file system on mounted device.\n");
    exit(1);
  }
  buildSuperBlock();
  buildBitMaps();
  Disk = open(Device,O_RDWR);
  if (Check) checkBlocks();
  if (BBFile) loadBadBlockFile(BBFile);
  if (Verbose) {
    fprintf(stderr,"%d bad blocks found.\n",BadBlocks);
  }
  buildInodes();
  fprintf(stderr,"%d indirect blocks allocated at blocks: ", num_idb_allocated);
  int i;
  for (i = 0; i < num_idb_allocated; i++) {
     fprintf(stderr,"%d ", IDB[i].block_number);
  }
  fprintf(stderr, "\n");

  writeFS();
  close(Disk);
  return 0;
}
