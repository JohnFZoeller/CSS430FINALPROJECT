/*
*Author:        John Zoeller
*Date:            8/7/16
*Title:            FileSystem.java
*/
public class FileSystem{

	private SuperBlock superblock;
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
		superblock = new SuperBlock( diskBlocks );

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
		boolean newFile;
		newFile = (directory.namei(fileName) == -1);

		FileTableEntry result = filestructuretable.falloc(fileName, mode);

		short flag;
		if (mode.equals("a"))
		{
			seek(result, 0, SEEK_END);
			flag = Inode.WRITE;
		}
		else if (mode.equals("w"))
		{	//truncate the file to 0
			dealloAllBlocks(result);
			newFile = true;
			flag = Inode.WRITE;
		} else if (mode.equals("w+"))
		{ //w+ sets the seek at beginning of file
			flag = Inode.WRITE;
		} else
		{	//mode is read, sets seek to beginning of the file
			if (newFile)
				return null;
			flag = Inode.READ;
		}
		//We only want to set the flag if we are the first one in
		if (result.count == 1)
		{
			result.inode.flag = flag;
		}

		//if it's a new file, assign a direct block to it
		if (newFile)
		{
			//assign a direct block to it
			short directBlock = superblock.getFreeBlock();
			if (directBlock == -1)
			{
				return null; //Not enough space for a direct block
			}
			result.inode.setNextBlockNumber(directBlock);
			result.inode.toDisk(result.iNumber);
		}
		return result;
	}

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

			while(bufferLength > i)
			{

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
		int bufferLength, seekPoint, size, writeBlock, cur, i, total, left, newWriteBlock;

		if(ftEnt == null || ftEnt.mode.equals("r"))
			return -1;

		synchronized(ftEnt){
			bufferLength = buffer.length;
			size = fsize(ftEnt);
			seekPoint = ftEnt.seekPtr;
			i = 0;
			writeArr = new byte[Disk.blockSize];

			while(i < bufferLength){
				writeBlock = getId(ftEnt);
				cur = seekPoint % Disk.blockSize;
				total = Disk.blockSize - cur;
				left = (total < bufferLength) ? total : bufferLength;  // not sure about this line


				if(writeBlock == -1){
					newWriteBlock =  superblock.getFreeBlock();
					//should be contruct here to make sure the block is good.
					writeBlock = newWriteBlock;
				}

				SysLib.rawread(writeBlock, writeArr);
				System.arraycopy(buffer, i, writeArr, cur, left);
				SysLib.rawwrite(writeBlock, writeArr);

				ftEnt.seekPtr += left;
				bufferLength -= left;
				i += left;
			}

		}
		ftEnt.inode.toDisk(ftEnt.iNumber);
		return i;
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
		}
		return -1; //just for compilation
	}

	//pointer2 will either be -1,
	int getId(FileTableEntry ftEnt)
	{
		int pointer, pointer2, pointer3;
		byte[] finder;

		pointer = ftEnt.seekPtr / Disk.blockSize;

		if(pointer < 11) {
			pointer2 = ftEnt.inode.direct[pointer];

		}
		else if(ftEnt.inode.indirect == -1)
			pointer2 = -1;
		else if(ftEnt.inode.indirect != -1)
		{
			finder = new byte[Disk.blockSize];
			SysLib.rawread(ftEnt.inode.indirect, finder);
			pointer3 = 2 * (pointer - 11);
			pointer2 = SysLib.bytes2short(finder, pointer3);
		}
		else
			pointer2 = -1;
		return pointer2;
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
