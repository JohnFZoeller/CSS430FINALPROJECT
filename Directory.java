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
    		SysLib.cerr("Exception with accessing byte array in Directory.java. \n");
    		e.printStackTrace();
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
    	int intBytes = fsize.length * 4;
    	int charBytes = fnames.length * maxChars * 2;
        byte[] temp = new byte[intBytes + charBytes];
        
        // Populating byte array with file size int data
        int offset = 0;
        int currentInt = 0;
        for(int i = 0; i < fsize.length; i++)
        {
        	currentInt = fsize[i];
        	SysLib.int2bytes(currentInt, temp, offset);
        	offset = offset + 4; // 4 Bytes per int
        }
        
        // Now populating byte array with fname char data
        // Outer loop will traverse each name
        for(int i = 0; i < fnames.length; i ++)
        {
        	// Retrieving current fname and trimming non chars
        	String currStr = new String(fnames[i], 0, fsize[i]);
        	// Making byte array of current string
        	byte currentByteArr[] = currStr.getBytes();
        	// Now storing bytes into overall byte array
        	System.arraycopy(currentByteArr, 0, temp, offset, currentByteArr.length);
        	// Could have stored 30 chars * 2Bytes/char = 60 bytes
        	offset = offset + (maxChars * 2);
        }
        return temp;
    }

    // ------------- ialloc(String filename) -------------
    
    // Filename is the one of a file to be created.
    // allocates a new inode number for this filename.
    public short ialloc( String filename )
    {
    	short result = (short) -1;
    	int nameLength = filename.length();
    	if(nameLength == 0)
    	{
    		SysLib.cerr("Error with ialloc(String filename) in Directory: filename.length() = 0 \n");
    		return result;
    	}
    	
    	// Will need to see if there's a slot that's not used
    	for(int i = 1; i < fsize.length; i++)
    	{
    		// Checking if not used
    		if(fsize[i] == 0)
    		{
    			result = (short) i;
    			// Checking if filename is too long
    			if(nameLength > maxChars)
    			{
    				fsize[i] = maxChars;
    			}
    			else
    			{
    				fsize[i] = nameLength;
    			}
    			
    			// Now storing filename in fnames[][] array
    			filename.getChars( 0, fsize[i], fnames[i], 0 ); 
    		}
    	}
        return result;
    }

    // ------------- ifree(short iNumber) --------------
    
    // deallocates this inumber (inode number)
    // the corresponding file will be deleted.
    public boolean ifree( short iNumber )
    {
    	short lowest = (short) 1; // Cannot deallocate root directory
    	short greatest = (short) fsize.length;
    	// Checking if out of bounds
    	if(iNumber < lowest || iNumber >= greatest)
    	{
    		SysLib.cerr("Error in ifree(short iNumber) in Directory: iNumber out of bounds \n");
    		return false;
    	}
    	
    	// Clearing the filename, replacing chars with
    	// white space.
        for(int i = 0; i < fsize[iNumber]; i++)
        {
            fnames[iNumber][i] = ' ';
        }
        fsize[iNumber] = 0;
        
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