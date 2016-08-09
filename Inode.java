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

    //constructor that initilizes all data
    Inode()
    {
        length = 0;
        count = 0;
        flag = 1;
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
        int blkNumber = iNumber / 16 + 1;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread( blkNumber, data);

        int offset = (iNumber % 16) * inodeSize;
        length = SysLib.bytes2int( data, offset);
        offset += 4;
        count = SysLib.bytes2short(data, offset);
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
        int blkNumber = iNumber / 16 + 1;
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
}

