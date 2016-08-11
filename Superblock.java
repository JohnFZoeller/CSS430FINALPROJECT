/** 
 * CSS 430 Operation System
 * Summer 2016
 * Final Project
 *
 * SuperBlock.java
 *
 * It reads SuperBlock from disk, validates the state of the disk.
 * identify free blocks, add blocks to the free list, writing back to disk
 * the contents of SuperBlock.
 * If validation fails, it will format the disk and write a new SuperBlock to the disk
 */

public class Superblock
{
    public int totalBlocks;			// the number of disk blocks
    public int totalInodes;			// the number of inodes
    public int freeList;			// the block number of the free list's head
    
    
    //----------------------- Superblock(int diskSize) ---------------------
    // constructor of the Superblock
    // initialize number of disk blocks, freeList, and totalInodes.
    // also call format method to initialize default number for inode Blocks
    public Superblock(int diskSize)
    {
        
        //read the superblock from disk
        byte[] superBData = new byte[Disk.blockSize];
        SysLib.rawread(0, superBData);
        
        totalBlocks = SysLib.bytes2int(superBData, 0);
        totalInodes = SysLib.bytes2int(superBData, 4);
        freeList = SysLib.bytes2int(superBData, 8);
        
        // check if contents of disk are valid, if so format disk
        if (totalBlocks != diskSize && totalInodes < 0 && freeList < 2)
        {
            // need to format disk
            totalBlocks = diskSize;
            SysLib.cout("Error: initialize to default format");
            format();
        }
    }
    
    
    //--------------------------- format(int files)--------------------------
    // it sets up disk block with number of inodes and sets up the freelist
    // to take the first 2 bytes for each remaining disk blocks
    public synchronized void format(int numInodes)
    {
        byte[] superBData = new byte[Disk.blockSize];
        this.totalBlocks = 1000;
        this.totalInodes = numInodes;
        
        
        this.freeList = ((this.totalInodes * 32) / Disk.blockSize) + 2;
        
        // put info of superblock to the byte array
        SysLib.int2bytes(this.totalBlocks, superBData, 0);
        SysLib.int2bytes(this.totalInodes, superBData, 4);
        SysLib.int2bytes(this.freeList, superBData, 8);
        
        // write the superblock contents to the disk
        SysLib.rawwrite(0, superBData);
        
        
        // clean up rest of disk blocks
        byte[] data = new byte[512];
        for (short i = (short)freeList; i < totalBlocks; i++)
        {
            //fill the rest of the block with 0
            for (int j = 0; j < Disk.blockSize; j++)
            {
                data[j] = (byte)0;
            }
            
            // get nest block
            // if at the end, the next block in the free list return 0
            short next_block;
            if (i == totalBlocks - 1) {
                next_block = (short)0;
            } else {
                next_block = (short)(i + 1);
            }
            SysLib.short2bytes(next_block, data, 0);
            
            //Save to disk
            SysLib.rawwrite(i, data);
        }
    }
    
    //---------------------------- format() ---------------------------------
    //it format dist with default inodes number
    public synchronized void format(){
        format(64);
    }
    
    //------------------------------ sync() ----------------------------------
    // Write totalBlocks, inodeBlocks, and freeList back to disk.
    public void sync()
    {
        //create new buffer
        byte[] superBData = new byte[Disk.blockSize];
        
        SysLib.rawread(0, superBData);
        
        SysLib.int2bytes(totalBlocks, superBData, 0);
        SysLib.int2bytes(totalInodes, superBData, 4);
        SysLib.int2bytes(freeList, superBData, 8);
        
        SysLib.rawwrite(0, superBData);
    }
    
    //---------------------------- getFreeBlock() ----------------------------
    // take the top block from the freelist
    public short getFreeBlock() {
        
        // save a copy of the current head of the list
        short temp = (short)freeList;
        
        // read the first free block
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(freeList, data);
        // get the next free block from the one to be removed
        freeList = (int)SysLib.bytes2short(data, 0);
        
        //overwrite first 2 bytes in the block
        SysLib.rawread(temp, data);
        SysLib.short2bytes((short)0, data, 0);
        //write back to disk
        SysLib.rawwrite(temp, data);
        
        return temp;
    }
    
    //--------------------- returnBlock(short blockNumber) ----------------------
    //put a free block to the end of the free list
    public void returnBlock(short blockNumber) {
        
        short last_free = (short)freeList;
        short next_free = 0;
        byte[] current_end = null;
        byte[] new_end = null;
        
        SysLib.rawread(blockNumber, new_end);
        SysLib.short2bytes((short)0, new_end, 0);
        SysLib.rawwrite(blockNumber, new_end);
        
        while (last_free < totalBlocks){
            // read next block
            SysLib.rawread(last_free, current_end);
            next_free = SysLib.bytes2short(current_end, 0);
            if (next_free == 0) {
                //set next free block to the given lockNumber
                SysLib.short2bytes(blockNumber, current_end, 0);
                SysLib.rawwrite(last_free, current_end);
                return;
            }
            // keep traversing
            last_free = next_free;
        }
    }
}
