/**
 * Created by Fengjuan on 7/11/16.
 */

//The main file system class that ties everything together and is how you declare a FileSystem
public class FileSystem
{
    private Superblock superblock;
    private Directory directory;
    private FileStructureTable filestructuretable;

    public FileSystem(int diskBlocks)
    {
        //create superblock, and format disk with 64 inodes in default
        superblock = new Superblock( diskBlocks );

        //create direcotry, and register "/" in directory entry 0
        directory = new Directory( superblock.totalInodes);

        //file table is created, and store direcotry in the file table
        filestructuretable = new FileStructureTable( directory );

        //direcotry reconstruction
        FileTableEntry dirEnt = open( "/" , "r");
        int dirSize = fsize( dirEnt);
        if( dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirData );
            directory.directory2bytes(dirData) ;

        }
        close( dirEnt);
    }

    //write the disk's metadata to the superblock on the disk
    void sync()
    {

    }

    //format the disk. the files specifies the maximum number of the files can be created
    //in the file system.
    //Return 0 on success, otherwise, -1.
    boolean format( int files )
    {

    }

    //Opens the file specified by the fileName in the given mode. FileStrucureTable allocates
    //a new file descriptor, and return it as FileTableEntry
    FileTableEntry open( String fileName, String mode)
    {

    }

    //close the file corresponding to fd by removing the corresponding FileTableEntry
    boolean close( FileTableEntry ftEnt)
    {

    }

    //return the size in bytes of the file indicated by fd
    int fsize( FileTableEntry ftEnt)
    {

    }

    //read data from the file specified by the FileTableEntry into the buffer
    int read( FileTableEntry ftEnt, byte[] buffer)
    {

    }

    //write data from the file specified by the FileTableEntry into the buffer, additional blocks
    // are allocated to the inode as need
    int write( FileTableEntry ftEnt, byte[] buffer)
    {

    }
    //
    private  boolean dealloAllBlocks( FileTableEntry ftEnt)
    {

    }
    //destroys the file specified by filename, return true when it's removed successfully
    boolean delete( String  filename)
    {

    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    //update the seek pointer corresponding to fd as SEEK_SET, SEEK_CUR, SEEK_END defined above
    int seek( FileTableEntry ftEnt, int offset, int whence)
    {

    }

}

