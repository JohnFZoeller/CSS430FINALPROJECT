/**
 * CSS 430 Operation System
 * Summer 2016
 * Final Project
 *
 * Superblock.java
 * reads the physical SuperBlock from disk, validates the health of the disk
 * and identifying free blocks, adding blocks to the free list,
 * and writing back to disk the contents of SuperBlock.
 * If validation fails, it will format the disk and write a new SuperBlock to the disk
 */

public class Superblock {


    public int totalBlocks;  //number of disk blocks
    public int totalInodes;  //the number of inodes
    public int freeList;  //the block number of the free list's head

    //read the superblock from disk
    public Superblock( int diskSize ) {
        // read superblock data from disk
        byte[] superBlock = new byte[Disk.blockSize];
        SysLib.rawread(0, superBlock);

        totalBlocks = SysLib.bytes2int(superBlock, 0);
        totalInodes = SysLib.bytes2int(superBlock, 4);

        freeList = SysLib.bytes2int(superBlock, 8);

        // check contents of disk for problems, if found format disk.
        if (this.totalBlocks != diskSize) {
            SysLib.cerr("Error initializing superblock, formatting disk!");
            this.totalBlocks = diskSize;
            // default number of blocks to create
            format();
        }
    }

    // format disk with default number of inodes
    public synchronized void format() {
        format(64);
    }

    // format disk with specified number of inodes
    public synchronized void format(int inodes) {
        byte[] superBlock = new byte[Disk.blockSize];
        this.totalBlocks = 1000;
        this.totalInodes = inodes; // default number of inodes in superblock

        // go to first free block
        this.freeList = ((this.totalInodes * 32) / Disk.blockSize) + 2;

        // put superblock info in byte array
        SysLib.int2bytes(this.totalBlocks, superBlock, 0);
        SysLib.int2bytes(this.totalInodes, superBlock, 4);
        SysLib.int2bytes(this.freeList, superBlock, 8);

        // write the superblock contents to the disk
        SysLib.rawwrite(0, superBlock);

        // clear out rest of disk blocks
        byte[] data = new byte[Disk.blockSize];
        for (short i = (short)this.freeList; i < this.totalBlocks; i++)
        {
            // get rid of data
            for (int a = 0; a < Disk.blockSize; a++)
            {
                data[a] = (byte)0;
            }

            // get next block, if at end return 0
            short next_block;
            if (i == totalBlocks - 1) {
                next_block = (short)0;
            } else {
                next_block = (short)(i + 1);
            }
            SysLib.short2bytes(next_block, data, 0);
            //Save back to the disk
            SysLib.rawwrite(i, data);
        }
    }

    // pull a free block off the freelist
    public int getFreeBlock() {
        int returnVal = this.freeList;
        // if valid free block
        if (this.freeList != -1) {
            // read data from free block
            byte[] data = new byte[Disk.blockSize];
            // clear out block
            for( int i = 0; i < Disk.blockSize; i++) {
                data[i] = 0;
            }
            SysLib.rawread(this.freeList, data);

            // new block to use
            int nextFree = SysLib.bytes2int(data, 0);

            // clear out block
            for( int i = 0; i < Disk.blockSize; i++) {
                data[i] = 0;
            }
            SysLib.rawwrite(this.freeList, data);

            // set to new block
            this.freeList = nextFree;
        }
        return returnVal;
    }

    // put a free block put onto the freelist
    public void returnBlock(short blockNumber) {
        byte[] data = new byte[Disk.blockSize];
        for (int a = 0; a < Disk.blockSize; a++)
        {
            data[a] = (byte)0;
        }
        SysLib.int2bytes(this.freeList, data, 0);
        SysLib.rawwrite(blockNumber, data);

        this.freeList = blockNumber;
    }

    void sync()
    {
        //create buffer
        byte tempData[] = new byte[Disk.blockSize];

        //save properties
        SysLib.int2bytes(totalBlocks, tempData, 0);
        SysLib.int2bytes(totalInodes, tempData, 4);
        SysLib.int2bytes(freeList, tempData, 8);

        //write to disk
        SysLib.rawwrite(0, tempData);
    }


}
