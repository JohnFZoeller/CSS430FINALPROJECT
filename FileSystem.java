/*
*Author:        John Zoeller
*Date:            8/7/16
*Title:            FileSystem.java
*/
public class FileSystem{

    private Superblock superblock;
    private Directory directory;
    private FileStructureTable filestructuretable;
    
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    public FileSystem(String fileName, String mode){
    }

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
          directory.bytes2directory(dirData);
       }
       close( dirEnt);
    }

    //write the disk's metadata to the superblock on the disk
    void sync()
    {
        FileTableEntry dirEnt;
        byte[] dirData;

        dirEnt = open("/", "w");
        dirData = directory.directory2bytes();
        write(dirEnt, dirData);
        close(dirEnt);
        superblock.sync();
    }

    //format the disk. the files specifies the maximum number of the files can be created
    //in the file system.
    //Return 0 on success, otherwise, -1.
    boolean format( int files )
    {
        if(files > 0){
            superblock.format(files);
            directory = new Directory(superblock.totalInodes);
            filestructuretable = new FileStructureTable(directory);
            return true;
        } else return false;
    }

    //Opens the file specified by the fileName in the given mode. FileStrucureTable allocates
    //a new file descriptor, and return it as FileTableEntry
    public FileTableEntry open( String fileName, String mode)
    {
        FileTableEntry result = FileStructureTable.falloc(fileName, mode);

        if(result == NULL)
            return NULL;
        else if(result != NULL && mode.equals("w")){
            if(dealloAllBlocks(result) == false)
                return NULL;
        }
        else return result;
    }

    //close the file corresponding to fd by removing the corresponding FileTableEntry
    boolean close( FileTableEntry ftEnt)
    {
        if(ftEnt != NULL){
            synchronized(ftEnt){
                ftEnt.count--;

                if(ftEnt.count >= 1)
                    return true;
            }
        } else return false;
    }

    //return the size in bytes of the file indicated by fd
    int fsize( FileTableEntry ftEnt)
    {
        if(ftEnt != NULL)
            return ftEnt.inode.length; 
        else return -1;
    }

    //destroys the file specified by filename, return true when it's removed successfully
    boolean delete( String filename)
    {
        short i = directory.namei(filename);

        if(i == -1){
            return false;
        } else return directory.ifree(i);
    }

    //read data from the file specified by the FileTableEntry into the buffer
    int read( FileTableEntry ftEnt, byte[] buffer)
    {
        byte[] readArr;
        int bufferLength, seekPoint, size, readBlock, cur, i, total, left;

        if(ftEnt == NULL || !ftEnt.mode.equals("r"))
            return NULL;

        synchronized(ftEnt){

            bufferLength = buffer.length;
            seekPoint = ftEnt.seekPtr;
            size = fsize(ftEnt);
            readArr = new byte[Disk.blockSize];
            i = 0;

            while(bufferLength > 0 && size > seekPoint){

                readBlock = getId(ftEnt);
                cur = seekPoint % Disk.blockSize;
                total = Disk.blockSize - cur;
                left = (total < (bufferLength - i)) ? total : (bufferLength - i);

                if(readBlock == -1)
                    return -1;

                SysLib.rawread(readBlock, readArr);
                System.arraycopy(readArr, cur, buffer, i, left);

                ftEnt.seekPtr += left;
                i += left; 
                bufferLength -= left;

            }
        }
        return i;
    }

    //write data from the file specified by the FileTableEntry into the buffer, additional blocks
    // are allocated to the inode as need
    int write( FileTableEntry ftEnt, byte[] buffer)
    {
        return -1;
    }

    //stuff here
    private boolean dealloAllBlocks( FileTableEntry ftEnt)
    {
        byte[] blocks;

        if(ftEnt != NULL){


        } else return false;
    }

    //update the seek pointer corresponding to fd as SEEK_SET, SEEK_CUR, SEEK_END defined above
    int seek( FileTableEntry ftEnt, int offset, int whence)
    {
        if(ftEnt == NULL)
            return -1;

        switch(whence){
            case SEEK_SET:
                ftEnt.seekPtr = offset;

                if(ftEnt.seekPtr < 0)
                    ftEnt.seekPtr = 0;
                if(ftEnt.seekPtr > fsize(ftEnt))
                    ftEnt.seekPtr = fsize(ftEnt);
                return ftEnt.seekPtr;
            case SEEK_CUR:
                ftEnt.seekPtr += offset;

                if(ftEnt.seekPtr < 0)
                    ftEnt.seekPtr = 0;
                if(ftEnt.seekPtr > fsize(ftEnt))
                    ftEnt.seekPtr = fsize(ftEnt);
                return ftEnt.seekPtr;
            case SEEK_END:
                ftEnt.seekPtr = offset + fsize(ftEnt);

                if(ftEnt.seekPtr < 0)
                    ftEnt.seekPtr = 0;
                if(ftEnt.seekPtr > fsize(ftEnt))
                    ftEnt.seekPtr = fsize(ftEnt);
                return ftEnt.seekPtr;
        }
    }

    int getId(FileTableEntry ftEnt)
    {
        int pointer, pointer2, pointer3;
        byte[] finder;

        pointer = ftEnt.seekPtr / Disk.blockSize;

        if(pointer < 11)
            pointer2 = ftEnt.direct[pointer];
        else if(ftEnt.inode.indirect == -1)
            pointer2 = -1;
        else if(ftEnt.inode.indirect != -1)
        {
            finder = new byte[Disk.blockSize];
            SysLib.rawread(ftEnt.inode.indirect, finder);
            pointer3 = 2 * (pointer - 11);
            pointer2 = SysLib.bytes2short(finder, pointer3);
        }

        return pointer2;
    }

}










