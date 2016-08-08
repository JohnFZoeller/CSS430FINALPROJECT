import java.util.*;
/*
 * Modified by Fengjuan Qiu 08/07/2016
 * the file system maintains the file(structure) table shared among all user threads
 * It represents the set of file table entries.
*/
public class FileStructureTable
{

	// Private fields
	private Vector<FileTableEntry> table; // the actual entity of this file table
	private Directory dir;        // the root directory

	//------------------------FileTable(Directory directory)-------------------

	// instantiate a file (structure) table receive a reference to the Director
	// from the file system
	public FileStructureTable( Directory directory )
	{
		table = new Vector<FileTableEntry>( );
		dir = directory;
	}

	// major public methods

	//----------------falloc(String filename, String mode)---------------------

	// allocate a new file (structure) table entry for this file name
	// allocate/retrieve and register the corresponding inode using dir
	// increment this inode's count
	// immediately write back this inode to the disk
	// return a reference to this file (structure) table entry
	public synchronized FileTableEntry falloc( String filename, String mode )
	{
		short iNumber = -1;
		Inode inode = null; //initilzes a inode to null
		while(true)
		{
			iNumber = (filename.equals("/") ? 0 : dir.namei(filename));
			if (iNumber < 0)
			{
				//the file is not exists because the iNumber is negative
				inode = new Inode();
				iNumber = dir.ialloc(filename);
				break;
			} else //the iNumber is positive file exits
			{
				inode = new Inode(iNumber);
				if (mode.equals("r"))
				{
					if (inode.flag == 0 || inode.flag == 1 || inode.flag == 2)
					{
						inode.flag = 2;
						break;
					} else if (inode.flag == 3)
					{
						try
						{
							wait();
						}
						catch (InterruptedException e) {}
						break;
					} else if (inode.flag == 4)
					{
						iNumber = -1;
						return null;
					}
				} else
				{
					if (inode.flag == 0 || inode.flag == 1)
					{
						inode.flag = 3;
						break;
					}
					else if (inode.flag == 3 || inode.flag == 2)
					{
						try
						{
							wait();
						}
						catch (InterruptedException e) {}
						break;
					} else if (inode.flag == 4)
					{
						iNumber = -1;
						return null;
					}
				}
			}
		}
		inode.count++;  //increment inode's count
		inode.toDisk(iNumber); //write this inode back to the disk
		FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
		table.addElement(e); //add this FileTableEntry into the vector
		// which contains a set of FileTableEntrys
		return e; // return a refernece to this file table entry
	}

	//-------------------------ffree(FileTableEntry e)-------------------------

	// receive a file table entry reference
	// save the corresponding inode to the disk
	// free this file table entry.
	// return true if this file table entry found in my table
	public synchronized boolean ffree( FileTableEntry e )
	{
		if(table.removeElement(e))
		{
			//if remove successfully, count --
			e.inode.count--;
			if (e.inode.flag == 2 || e.inode.flag == 3)
			{
				notify();
			}
			e.inode.toDisk(e.iNumber); // save inode into the disk
			return true; //return true when the file table entry is found
		}
		return false;
	}

	//--------------------------------fempty()---------------------------------

	// return true if table is empty
	// should be called before starting a format
	public synchronized boolean fempty( )
	{
		return table.isEmpty( ); //return true when the table is empty
	}

} // End of FileStructureTable class}