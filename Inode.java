/**
 * Created by Michael on 7/23/2015.
 * Modified by Fengjuan Qiu 08/07/2016
 *
 * Represents an inode in memory, maintaining pointers to all the blocks
 * that hold the file's data.
 */

public class Inode
{
    private final static int inodeSize = 32;  // fix to 32 bytes
    private final static int directSize = 11; // # direct pointers

    public int length;                        // file size in bytes
    public short count;                       // # file-table entries pointing to this
    public short flag;                        // 0 = unused, 1 = used
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                     // a indirect pointer


    public final static short UNUSED = 0;
    public final static short USED = 1;
    public final static short READ = 2;
    public final static short WRITE = 3;
    public final static short DELETE = 4;

    private final static int indirectSize = Disk.blockSize / 2;

    //constructor that initilizes all data
    Inode()
    {
        length = 0;
        count = 0;
        flag = USED;
        for (int i = 0; i < directSize; i++)
        {
            direct[i] = -1;
            indirect = -1;
        }
    }

    //Methods

    //---------------------------Inode(short iNumber)--------------------------

    //retrieve iNode from disk
    Inode(short iNumber)
    {
        int blkNumber = (iNumber / 16) + 1;

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread( blkNumber, data);

        int offset = (iNumber % 16) * inodeSize;

        System.out.println("offset is: " + offset);

        length = SysLib.bytes2int( data, offset);
        offset += 4;

        System.out.println("length is:" + length);
        count = SysLib.bytes2short(data, offset);

        System.out.println("count is: " + count);
        offset += 2;
        flag = SysLib.bytes2short(data, offset);
        offset += 2;
        for(int i = 0; i < directSize; i++)
        {
            direct[i] = SysLib.bytes2short(data, offset);
            offset += 2;
        }
        indirect = SysLib.bytes2short(data, offset);
    }


    //-----------------------------toDisk(short Inumber)-----------------------

    //save to disk as the i-th inode
    void toDisk(short iNumber)
    {
        int blkNumber = (iNumber / 16) + 1;
        //read the whole block from the disk
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blkNumber, data);
        int offset = 0;
        SysLib.int2bytes(length, data, iNumber * inodeSize + offset);
        offset += 4;
        SysLib.short2bytes(count, data, iNumber * inodeSize + offset);
        offset += 2;
        SysLib.short2bytes(flag, data, iNumber * inodeSize + offset);
        offset += 2;

        for(int i = 0; i < directSize; i++, offset += 2)
        {
            SysLib.short2bytes(direct[i], data, iNumber * inodeSize + offset);
        }
        SysLib.short2bytes(indirect, data, iNumber *inodeSize + offset);
        SysLib.rawwrite(blkNumber, data);

    }

    public boolean setNextBlockNumber(short blockNumber) {
        //Check direct first
        for (int i = 0; i < directSize; i++) {
            if (direct[i] <= 0) {
                direct[i] = blockNumber;
                return true;
            }
        }
        //Check indirect
        byte[] indirect_block = readIndirectBlock();
        for(short offset_in_indirect = 0; offset_in_indirect < indirectSize *2;
            offset_in_indirect += 2) {
            //The next free indirect will be -1
            if (SysLib.bytes2short(indirect_block, offset_in_indirect) <= 0) {
                //write the block number to the byte array
                SysLib.short2bytes(blockNumber, indirect_block,
                        offset_in_indirect);
                //write the block back to disk return success condition on disk
                return SysLib.rawwrite(indirect, indirect_block) != -1;
            }
        }
        return false;
    }

    private byte[] readIndirectBlock() {
        byte[] indirect_block = new byte[Disk.blockSize];
        //read the entire indirect block
        SysLib.rawread(indirect, indirect_block);
        return indirect_block;
    }
}

