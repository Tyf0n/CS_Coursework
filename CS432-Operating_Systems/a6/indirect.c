/*
 * Nathaniel Lim
 * CS432 - Assignment 6
 * Upgrading the WUFS for the use of one indirect block
 * 
 * My inode (see wufs_fs.h) has:
 * a 32 bit file size (to accomodate a max file size of 519k = 0x81c00
 */
#include <linux/buffer_head.h>
#include "wufs.h"

typedef __u16 block_t;	/* 16 bit, host order */

int wufs_get_blk(struct inode * inode, sector_t block, struct buffer_head *bh_result, int create);
void wufs_truncate(struct inode * inode);
unsigned wufs_blocks(loff_t size, struct super_block *sb);

static inline block_t *bptrs(struct inode *inode);

static DEFINE_RWLOCK(pointers_lock);

static inline block_t *bptrs(struct inode *inode) {
	return (block_t *)wufs_i(inode)->ini_data;
}


// Returns the block_t where the indirect is pointed
// and sets up the buffer head for the block

// Pass this function a an empty pointer to a buffer_head.
// To be assigned when a new block is created
static int alloc_indirect_block(struct inode *inode, int ind_add, struct buffer_head *bh) {
	if (!ind_add) return -ENOSPC;	
	//Constructing the new, indirect block
	bh = sb_getblk(inode->i_sb, ind_add);
	set_buffer_new(bh);
	//Get ready to write (zeros) to the buffer head
	lock_buffer(bh);
	memset(bh->b_data, 0, bh->b_size);
	set_buffer_uptodate(bh);
	unlock_buffer(bh);	
	mark_buffer_dirty_inode(bh, inode);
	return 0;
}

/**
 * wufs_get_block: (module-wide utility function)
 * Get the buffer assoicated with a particular block.
 * If create=1, create the block if missing; otherwise return with error
 */
int wufs_get_blk(struct inode * inode, sector_t block, struct buffer_head *bh, int create)
{
  /* get the meta-data associated with the file system superblock */
  struct wufs_sb_info *sbi = wufs_sb(inode->i_sb);
  block_t *bptr, *ptr, *branch_ptr, *ind_block_ptr;
  struct buffer_head *ind_bh = NULL;
  int ind_add;
  int n;
  int is_indirect_access = 0;

  if (block < 0 || block >= sbi->sbi_max_fblks) {
    return -EIO;
  }

  // Get all of the block pointers in the inode
  // 0 - 6 will be pointers directly to blocks
  // 7 will be the indirect block
  bptr = bptrs(inode); 

  if (block  < WUFS_INODE_BPTRS ) {
	//Direct block pointer
  	ptr = bptr+block;
  } else {
	//Must be an indirect block
	is_indirect_access = 1;
	block -= WUFS_INODE_BPTRS;
	branch_ptr = bptr + WUFS_INODE_BPTRS - 1; // Last pointer to a block of 512 block pointers, point 6
start_branch:
	if(!*branch_ptr) {
	//Need to create the indirect block
		if (!create) return -EIO;
		ind_add = wufs_new_block(inode);
		if (!ind_add) return -ENOSPC;
		write_lock(&pointers_lock);
		if(*branch_ptr) {
			//Some thread already allocated the indirect block
			write_unlock(&pointers_lock);
			wufs_free_block(inode, ind_add);
			goto start_branch;
		} else {
			*branch_ptr = ind_add;
			mark_inode_dirty(inode);
			write_unlock(&pointers_lock);
			//whole indirect block needs to be created
			//and pass the empty ind_bh pointer to be assigned
			alloc_indirect_block(inode, ind_add, ind_bh);
		}		
	} else {
	//Indrect block already has been created, so get the 
	//buffer_head assigned to the indirect block.
		ind_bh = sb_bread(inode->i_sb, *branch_ptr);
	}

	//Read the ind_bh, since the logical block goes through
	//the indirect block of addresse, to another block
	ind_block_ptr = (block_t *) ind_bh->b_data;
	ptr = ind_block_ptr + block;	
  }
  
  if(is_indirect_access) {
start_indirect:	
	if (!*ptr) {
		if (!create) return -EIO;
		n = wufs_new_block(inode);
		if (!n) return -ENOSPC;		
		lock_buffer(ind_bh);
		if (*ptr) {
      			/* some other thread has set this! yikes: back out */
      			unlock_buffer(ind_bh);
      			/* return block to the pool */
      			wufs_free_block(inode,n);
      			goto start_indirect; /* above */
    		} else {
      			/* we're good to modify the block pointer */
      			*ptr = n;
      			/* done with critical path */
      			/* update time and flush changes to disk */
      			set_buffer_uptodate(ind_bh);
			unlock_buffer(ind_bh);
			inode->i_mtime = inode->i_ctime = CURRENT_TIME_SEC;
      			mark_inode_dirty(inode);
			mark_buffer_dirty_inode(ind_bh, inode);      			
      			set_buffer_new(bh);
    		}
	}
	map_bh(bh, inode->i_sb, *ptr);
	return 0;	

  } else {
	/* now, ensure there's a block reference at the end of the pointer */
start_direct:
  	if (!*ptr) {
    		/* if we're not allowed to create it, claim an I/O error */
    		if (!create) return -EIO;
    		/* grab a new block */
    		n = wufs_new_block(inode);
    		/* not possible? must have run out of space! */
    		if (!n) return -ENOSPC;
    		/* critical block update section */
    		write_lock(&pointers_lock);
    		if (*ptr) {
      			/* some other thread has set this! yikes: back out */
      			write_unlock(&pointers_lock);
      			/* return block to the pool */
      			wufs_free_block(inode,n);
      			goto start_direct; /* above */
    		} else {
      			/* we're good to modify the block pointer */
      			*ptr = n;
      			/* done with critical path */
      			write_unlock(&pointers_lock);
      			/* update time and flush changes to disk */
      			inode->i_mtime = inode->i_ctime = CURRENT_TIME_SEC;
      			mark_inode_dirty(inode);
      			
      			set_buffer_new(bh);
    		}
	}
	map_bh(bh, inode->i_sb, *ptr);
  	return 0;
  }

  




}

/**
 * wufs_truncate: (module-wide utility function)
 * Set the file allocation to exactly match the size of the file.
 * (wufs_get_block expands file, so only contraction is considered here.)
 */
void wufs_truncate(struct inode *inode) {
  struct buffer_head *ind_bh = NULL;
  block_t *branch_ptr;
  block_t *ind_block_ptr = NULL;
  block_t *blk = bptrs(inode);
  int need_indirect_access = 0;
  int i;
  long bcnt;

  //First, Look at the last pointer in the inode
  //This is the branch pointer, the one pointing to the indirect block
  //If this pointer is set, then we need to do indirect access
  //and free the blocks pointed to from the indirect block
  //We read the buffer_head ind_bh and get the first pointer
  branch_ptr = blk + 6;
  if (*branch_ptr) {
	need_indirect_access = 1;
	ind_bh = sb_bread(inode->i_sb, *branch_ptr);
  	ind_block_ptr = (block_t *) ind_bh->b_data;
	i = 518;
  } else {
	i = 5;
  }
  block_truncate_page(inode->i_mapping, inode->i_size, wufs_get_blk);

   /* compute the number of blocks needed by this file */
  bcnt = (inode->i_size + WUFS_BLOCKSIZE -1) / WUFS_BLOCKSIZE;

  /* set all blocks referenced beyond file size to 0 (null) */

  //We start at the end of the file and truncate going backwards
  //If the file was more than 7k 
  //	we need to start truncating from the indirect block, i= 518, 
  //Else
  //    we start truncating from the second to last pointer in the inode: i = 5
 
  for (; i >= bcnt; i--) {
    if (i > 6) {
	//Start freeing blocks, pointed to from the indirect block
	lock_buffer(ind_bh);
	if (ind_block_ptr[i-6]) {
		wufs_free_block(inode, ind_block_ptr[i-6]);
	}
	ind_block_ptr[i-6] = 0;
	set_buffer_uptodate(ind_bh);
	unlock_buffer(ind_bh);
	mark_buffer_dirty_inode(ind_bh, inode);	
    } else {
	// We need to treat the freeing indirect block separately
	if (need_indirect_access && i == 6) {
		lock_buffer(ind_bh);
		if (ind_block_ptr[i - 6]) {
			wufs_free_block(inode, ind_block_ptr[i-6]);
		}
		ind_block_ptr[i-6] = 0;
		set_buffer_uptodate(ind_bh);
		unlock_buffer(ind_bh);
		mark_buffer_dirty_inode(ind_bh, inode);
		//Return the buffer back to the buffer pool
		//The data in the indirect block is no longer needed.
		brelse(ind_bh);
	}
	//Freeing blocks pointed to directly from the inode
	write_lock(&pointers_lock);
	if (blk[i]) {
		wufs_free_block(inode, blk[i]);
	}
	blk[i] = 0;
	write_unlock(&pointers_lock);	
    }    
  }  
  /* My what a big change we made!  Timestamp and flush it to disk. */
  inode->i_mtime = inode->i_ctime = CURRENT_TIME_SEC;
  mark_inode_dirty(inode);
}
//------------------------------------------------------------------
/**
 * wufs_blocks: (utility function)
 * Compute the number of blocks needed to cover a size "size" file.
 */
unsigned int wufs_blocks(loff_t size, struct super_block *sb)
{
  return (size + sb->s_blocksize - 1) / sb->s_blocksize;
}
