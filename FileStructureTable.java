import java.util.*;

//the file system maintains the file(structure)  table shared among all user threads
public class FileStructureTable
{
	
	// Private fields
	private Vector table;         // the actual entity of this file table
    	private Directory dir;        // the root directory

	// Constructor
	public FileStructureTable( Directory directory )
	{ 
	   	table = new Vector( );     // instantiate a file (structure) table
	   	dir = directory;           // receive a reference to the Director
	}                             // from the file system

	// major public methods
	// allocate a new file (structure) table entry for this file name
	// allocate/retrieve and register the corresponding inode using dir
	// increment this inode's count
	// immediately write back this inode to the disk
	// return a reference to this file (structure) table entry
	public synchronized FileTableEntry falloc( String filename, String mode )
	{
		// Fill in
	}

	// receive a file table entry reference
	// save the corresponding inode to the disk
	// free this file table entry.
	// return true if this file table entry found in my table
	public synchronized boolean ffree( FileTableEntry e )
	{

	}

	// return if table is empty
	// should be called before starting a format
	public synchronized boolean fempty( )
	{
		
	   	return table.isEmpty( );
	}
	
} // End of FileStructureTable class