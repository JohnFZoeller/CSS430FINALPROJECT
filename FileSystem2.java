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

    //CHECK
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
    //CHECK
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
    //CHECK
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
        FileTableEntry result = filestructuretable.falloc(fileName, mode);

        if(result == null)
            return null;
        else if(result != null && mode.equals("w")){
            if(dealloAllBlocks(result) == false)
                return null;
        }
        else
            return result;

        return result; //just for compliing
    }
/*    public FileTableEntry open( String fileName, String mode)
    {
        boolean new_file = directory.namei(fileName) == -1;
        FileTableEntry result = filestructuretable.falloc(fileName, mode);        short flag;
        if (mode.equals("a")) {
            seek(result, 0, SEEK_END);
            flag = Inode.WRITE;
        } else if (mode.equals("w")) {    //truncate the file to 0
            dealloAllBlocks(result);
            new_file = true;
            flag = Inode.WRITE;
        } else if (mode.equals("w+")) { //w+ sets the seek at beginning of file
            flag = Inode.WRITE;
        } else {             //mode is read, sets seek to beginning of the file
            if (new_file)
                return null;
            flag = Inode.READ;
        }
        //We only want to set the flag if we are the first one in
        if (result.count == 1) {
            result.inode.flag = flag;
        }        if (new_file) {
            //assign a direct block to it
            short direct_block = (short)superblock.getFreeBlock();
            if (direct_block == -1) {
                return null; //Not enough space for a direct block
            }
            result.inode.setNextBlockNumber(direct_block);
            result.inode.toDisk(result.iNumber);
        }//        if(result == null)
//            return null;
//        else if(result != null && mode.equals("w")){
//            if(dealloAllBlocks(result) == false)
//                return null;
//        }
//        else
//            return result;        
return result; //just for compliing
    }*/

    //close the file corresponding to fd by removing the corresponding FileTableEntry
    boolean close( FileTableEntry ftEnt)
    {
        if(ftEnt == null)
            return false;

        synchronized(ftEnt){
                ftEnt.count--;

                if(ftEnt.count >= 1)
                    return true;

            }

            return filestructuretable.ffree(ftEnt);
    }

    //return the size in bytes of the file indicated by fd
    int fsize( FileTableEntry ftEnt)
    {
        if(ftEnt != null)
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

        if(ftEnt == null || !ftEnt.mode.equals("r")){
            SysLib.cout("Error in first if \n");
            return -1;
        }

        synchronized(ftEnt){

            bufferLength = buffer.length;
            seekPoint = ftEnt.seekPtr;
            size = fsize(ftEnt);
            readArr = new byte[Disk.blockSize];
            i = 0;

            //used to be while(bufferLength > 0 && size > seekPoint){

            while(bufferLength > i){

                readBlock = getId(ftEnt);
                cur = seekPoint % Disk.blockSize;
                total = Disk.blockSize - cur;
                left = (total < (bufferLength - i)) ? total : (bufferLength - i);

                if(readBlock == -1){
                    SysLib.cout("error in second if \n");
                    return -1;
                }

                SysLib.rawread(readBlock, readArr);
                System.arraycopy(readArr, cur, buffer, i, left);

                ftEnt.seekPtr += left;
                bufferLength -= left;
                i += left; 

            }
        }

        SysLib.cout("the value of i = " + i + "\n");
        return i;
    }

    //write data from the file specified by the FileTableEntry into the buffer, additional blocks
    // are allocated to the inode as need
    int write( FileTableEntry ftEnt, byte[] buffer)
    {
        byte[] writeArr;
        int bufferLength, writeBlock, cur, i, total, left, newWriteBlock, switcher, writeBlock2, alloTest;
        boolean block;

        if(ftEnt == null || ftEnt.mode.equals("r"))
            return -1;

        synchronized(ftEnt){
            i = 0;
            bufferLength = buffer.length;
            writeArr = new byte[Disk.blockSize];

            while(0 < bufferLength){
                writeBlock = getId(ftEnt);                                             //getId == findTargetBlock

                if(writeBlock == -1){
                    newWriteBlock =  superblock.getFreeBlock();
                    switcher = ftEnt.inode.alloBlock(ftEnt.seekPtr, (short)newWriteBlock);      //alloBlock = registerTargetBlock

                    switch(switcher){
                        case 0: break;                                                                                    //good to go
                        case 1: return -1;                                                                              //error case
                        case 2:                                                                                                //special case
                            writeBlock2= superblock.getFreeBlock();
                            block = ftEnt.inode.alloBlock2(writeBlock2);

                            if(block == false)
                                return -1;

                            alloTest = ftEnt.inode.alloBlock(ftEnt.seekPtr, (short)newWriteBlock);

                            if(alloTest != 0)
                                return -1;
                            break;
                    }
                    writeBlock = newWriteBlock;
                }

                SysLib.rawread(writeBlock, writeArr);
                cur = ftEnt.seekPtr % Disk.blockSize;
                total = Disk.blockSize - cur;

                left = (total > bufferLength) ? bufferLength : total;  

                System.arraycopy(buffer, i, writeArr, cur, left);
                SysLib.rawwrite(writeBlock, writeArr);

                ftEnt.seekPtr += left;
                bufferLength -= left;
 
                if(total > bufferLength)
                    i = 0;
                else
                    i -= left;
            }
            if(ftEnt.seekPtr > ftEnt.inode.length)
                ftEnt.inode.length = ftEnt.seekPtr;

            ftEnt.inode.toDisk(ftEnt.iNumber);
            return i;
        }
    }

    //stuff here
    private boolean dealloAllBlocks( FileTableEntry ftEnt)
    {
        byte[] blocks;
        short returning;

        if(ftEnt == null)
            return false;

        //direct first
        for(int i = 0; i < 11; i++){
            if(ftEnt.inode.direct[i] != -1){
                superblock.returnBlock(ftEnt.inode.direct[i]);
                ftEnt.inode.direct[i] = -1;
            }
        }

        //now indirect
        if((blocks = clear(ftEnt)) != null){
            while((returning = SysLib.bytes2short(blocks, 0)) != -1)
                superblock.returnBlock(returning);
        }

        ftEnt.inode.toDisk(ftEnt.iNumber);
        return true;
    }

    //update the seek pointer corresponding to fd as SEEK_SET, SEEK_CUR, SEEK_END defined above
    int seek( FileTableEntry ftEnt, int offset, int whence)
    {
        if(ftEnt == null)
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
            default:
                return ftEnt.seekPtr;
        }
    }

    //pointer2 will either be -1, 
    int getId(FileTableEntry ftEnt)
    {
        int off, newOff;
        byte[] finder;

        off = ftEnt.seekPtr / Disk.blockSize;

        if(off < 11)
            return ftEnt.inode.direct[off];

        if(ftEnt.inode.indirect == -1)
            return -1;

        finder = new byte[Disk.blockSize];
        SysLib.rawread(ftEnt.inode.indirect, finder);
        newOff = (off - 11) * 2;
        return SysLib.bytes2short(finder, newOff);
    }

    byte[] clear(FileTableEntry ftEnt){
        byte[] clearing;

        if(ftEnt.inode.indirect == -1)
            return null;

        clearing = new byte[Disk.blockSize];
        SysLib.rawread(ftEnt.inode.indirect, clearing);
        ftEnt.inode.indirect = -1;

        return clearing;
    }

}










