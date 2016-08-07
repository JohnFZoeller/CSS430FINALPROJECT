/**
 * Created by Michael on 7/23/2015.
 */

/**
 * 
 * Modified by Oscar Garcia-Telles on 8 August 2016
 *
 */

// the “/” root directory maintains each file 
// in an different directory entry that contains 
// its file name
public class Directory
{
	// -----------------------------------------------
	// --------------- Private Fields ----------------
	// -----------------------------------------------
	// max characters of each file name
    private static int maxChars = 30; 

    // Directory entries
    private int fsize[];     // each element stores a different file size.
    private char fnames[][]; // each element stores a different file name.

    // -------------------------------------------
    // -------------- Constructor ----------------
    // -------------------------------------------
    public Directory( int maxInumber )
    {
        // directory constructor
        fsize = new int[maxInumber];  // maxInumber = max files
        for ( int i = 0; i < maxInumber; i++ )
        {
        	fsize[i] = 0; // all file size initialized to 0
        }
        
        // Next line initializes our directory filenames
        // 2D array.There will be at most maxInumber
        // files (a file per row in fnames) and each 
        // name is at most maxChars in length.
        fnames = new char[maxInumber][maxChars];
        String root = "/";                // entry(inode) 0 is "/"
        fsize[0] = root.length( );        // fsize[0] is the size of "/".
        root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
    }

    // --------------------------------------------
    // -------------- Methods ---------------------
    // --------------------------------------------

    // ------------- bytes2Directory(byte data[] --------
    // Initializes the Directory instance with this data[]
    // which is retrieved from the disk.
    public int bytes2directory( byte data[] )
    {
    	int offset = 0; // Starting index to read from
    	// data[] may be empty, so will use try/catch
    	try
    	{
    		// Initializing int fsize[]
        	for(int i = 0; i < fsize.length; i++)
        	{
        		// Initializing our Directory's fsize array.
        		// bytes2int(data, offset) reads 4 bytes
        		// starting at index offset, so offset needs
        		// to be incremented by 4 later on.
        		fsize[i] = SysLib.bytes2int(data, offset);
        		// Incrementing offset
        		offset = offset + 4;
        	}
        	
        	// Initializing char fnames[][]
        	for(int k = 0; k < fnames.length; k++)
        	{
        		// Following constructor creates a String from a byte array
        		// and starts reading from index offset. The last parameter
        		// is the size of the String IN BYTES. Each char is 
        		// 2 Bytes, hence maxChars * 2.
        		String fileName = new String(data, offset, maxChars * 2);
        		fileName.getChars(0, fsize[k], fnames[k], 0);
        	}
    	}
    	catch(ArrayIndexOutOfBoundsException e)
    	{
    		return -1;
    	}
    	
        return 0;	// Returns 0 if successful
    }

    // --------------- directory2bytes() -----------
    
    // converts and return Directory information into a plain byte array
    // this byte array will be written back to disk.
    // note: only meaningfull directory information should be converted
    // into bytes.
    public byte[] directory2bytes( )
    {
        byte[] temp = new byte[20];
        return temp;
    }

    // ------------- ialloc(String filename) -------------
    
    // filename is the one of a file to be created.
    // allocates a new inode number for this filename
    public short ialloc( String filename )
    {
        return 0;
    }

    // ------------- ifree(short iNumber) --------------
    
    // deallocates this inumber (inode number)
    // the corresponding file will be deleted.
    public boolean ifree( short iNumber )
    {

        /*for(int i = 0; i < fsize[iNumber]; i++)
        {
            set fnames[iNumber][i] to 0;
        }
        set fsize[iNumber] to 0;
        */
        return true;
    }

    // -------------- namei(String filename) --------------
    
    // returns the inumber corresponding to this filename
    public short namei( String filename )
    {
        /* use for loop to search the filename in the fnames,
        return the index if found it, else return -1
         */
        return 0;
    }
}