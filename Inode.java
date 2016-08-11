/**
 *
 * Represents an inode in memory, maintaining pointers to all the blocks
 * that hold the file's data.
 */

public class Inode {
    private final static int inodeSize = 32;  // fix to 32 bytes
    private final static int directSize = 11; // # direct pointers

    public int length;                        // file size in bytes
    public short count;                       // # file-table entries pointing to this
    public short flag;                        // 0 = unused, 1 = used
    public short direct[] = new short[directSize];     // direct pointers
    public short indirect;                     // a indirect pointer


    public final static short UNUSED = 0;
    public final static short USED = 1;
    public final static short READ = 2;
    public final static short WRITE = 3;
    public final static short DELETE = 4;

    private final static int indirectSize = (Disk.blockSize) / 2;

    //constructor that initilizes all data
    Inode() {
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
    Inode(short iNumber) {
        int blkNumber = (iNumber / 16) + 1;  // the block number is calculated by
        // using the format of 16 inodes per superblock and and one to reach next superblock

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blkNumber, data);

        int offset = (iNumber % 16) * inodeSize;
        length = SysLib.bytes2int(data, offset);
        offset += 4; //offset by 4 for int length

        count = SysLib.bytes2short(data, offset);
        offset += 2; //offset by 2 for short count
        flag = SysLib.bytes2short(data, offset);
        offset += 2;  //offset by 2 for short count

        for (int i = 0; i < directSize; i++) {
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

        byte[] data = new byte[Disk.blockSize];

        SysLib.rawread(blkNumber, data);
        int offset = 0;
        SysLib.int2bytes(length, data, iNumber * inodeSize + offset);
        offset += 4;
        SysLib.short2bytes(count, data, iNumber * inodeSize + offset);
        offset += 2;
        SysLib.short2bytes(flag, data, iNumber * inodeSize + offset);
        offset += 2;

        for (int i = 0; i < directSize; i++, offset += 2) {
            SysLib.short2bytes(direct[i], data, iNumber * inodeSize + offset);
        }
        SysLib.short2bytes(indirect, data, iNumber * inodeSize + offset);
        SysLib.rawwrite(blkNumber, data);

    }

    //-----------------------------setNextBlockNumber()----------------------
    //
    public boolean setNextBlockNumber(short blkNumber) {
        //Checking direct blocks
        for (int i = 0; i < directSize; i++) {
            if (direct[i] <= 0) {
                direct[i] = blkNumber;
                return true;
            }
        }
        //Checking indirect blocks
        byte[] indirectBlk = readIndirectBlk();
        for (short offsetIndirect = 0;
             offsetIndirect < indirectSize * 2; offsetIndirect += 2) {

            if (SysLib.bytes2short(indirectBlk, offsetIndirect) <= 0) {
                //write the block number to the byte array
                SysLib.short2bytes(blkNumber, indirectBlk, offsetIndirect);

                //write the block back to disk and return true when it's successfuly
                return SysLib.rawwrite(indirect, indirectBlk) != -1;
            }
        }
        return false;
    }

    //-----------------------------readIndirectBlock()-----------------------
    //read indirect block
    public byte[] readIndirectBlk() {
        byte[] indirectBlk = new byte[Disk.blockSize];

        SysLib.rawread(indirect, indirectBlk);
        return indirectBlk;
    }

}