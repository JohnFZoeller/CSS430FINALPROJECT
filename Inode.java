/**
 * Created by Michael on 7/23/2015.
 */
//Represents an inode in memory, maintaining pointers to all the blocks
// that hold the file's data.
public class Inode
{
    private final static int inodeSize = 32;  // fix to 32 bytes
    private final static int directSize = 11; // # direct pointers

    public int length;                        // file size in bytes
    public short count;                       // # file-table entries pointing to this
    public short flag;                        // 0 = unused, 1 = used
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                     // a indirect pointer

    Inode()
    {
        length = 0;
        count = 0;
        flag = 1;
        for(int i = 0; i < directSize; i++)
        {
            direct[i] = -1;
            indirect = -1;
        }
    }

    //retrieve iNode from disk
    Inode(short iNumber)
    {

    }

    //save to disk as the i-th inode
    int toDisk(short iNumber)
    {

    }
}
