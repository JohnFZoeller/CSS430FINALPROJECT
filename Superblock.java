/**
 * Created by Michael on 7/23/2015.
 */

//manages the list of free blocks
public class Superblock
{
    public int totalBlocks; //the number of disk blcoks
    public int totalInodes; //the number of inodes
    public int freeList; // the block number of the free list's head

    //read the superblock from disk
    public Superblock(int diskSize)
    {
    		// Place holder values just to compile!
		totalBlocks = 0;
		totalInodes = 10;
		freeList = 0;
    }

}
