/*
 *
 * the file system maintains the file(structure) table shared among all user threads
 * It represents the set of file table entries.
*/
import java.util.*;

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
			if(filename.equals("/"))
			{
				iNumber = 0;
			}
			else
			{
				iNumber = dir.namei(filename);
			}

			if (iNumber < 0)
			{
				//the file is not exists because the iNumber is negative
				iNumber = dir.ialloc(filename);
				inode = new Inode(iNumber);
				inode.flag = inode.WRITE;
				break;
			}
			else //the iNumber is positive file exists
			{
				inode = new Inode(iNumber);
				if (mode.equals("r"))
				{
					if (inode.flag == inode.UNUSED || inode.flag == inode.USED || inode.flag == inode.READ)
					{
						inode.flag = inode.READ; //set the flag to r mode
						break;
					}
					else if (inode.flag == inode.WRITE)
					{
//						try
//						{
//							wait();
//						}
//						catch (InterruptedException e) {}
						break;
					} else if (inode.flag == inode.DELETE)
					{
						iNumber = -1;
						return null;
					}
				}
				else
				{
					if (inode.flag == inode.UNUSED || inode.flag == inode.USED)
					{
						inode.flag = inode.WRITE;
						break;
					}
					else if (inode.flag == inode.WRITE || inode.flag == inode.READ)
					{
//						try
//						{
//							wait();
//						}
//						catch (InterruptedException e) {}
						break;
					}
					else if (inode.flag == inode.DELETE)
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
	// free this file table entry.
	// return true if this file table entry found in my table
	public synchronized boolean ffree( FileTableEntry e )
	{
//		if(table.removeElement(e))
//		{
//			//if remove successfully, count --
//			e.inode.count--;
//			if (e.inode.flag == Inode.READ || e.inode.flag == Inode.WRITE)
//			{
//				notify();  //wake up other waiting threads
//			}
//			e.inode.toDisk(e.iNumber); // save inode into the disk
//			return true; //return true when the file table entry is found
//		}
//		return false;

		Inode inode = new Inode(e.iNumber);
		// try to remove the given FileTableEntry, if it is in the table,
		// the remove methods will return true
		if (table.remove(e)) {
			if (inode.flag == inode.READ) {
				// if there is only one reader, set the flag to used (no more
				// users read that file) and wake up one thread(user)
				if (inode.count == 1) {
					notify();
					inode.flag = inode.USED;
				}

			} else if (inode.flag == inode.WRITE) {
				// set the flag to used
				inode.flag = inode.USED;
				// wake up all threads (users) waiting for that file since
				// there might be threads waiting for a reading which can
				// execute in concurent mode.
				notifyAll();
			}

			// decrease the number of users of that file about one
			inode.count--;
			inode.toDisk(e.iNumber);
			return true;
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

}