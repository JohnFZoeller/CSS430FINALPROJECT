/**
 * Created by Michael on 7/23/2015.
 */

//the “/” root directory maintains each file in an different directory entry that contains its file name
public class Directory
{
    private static int maxChars = 30; // max characters of each file name

    // Directory entries
    private int fsize[];        // each element stores a different file size.
    private char fnames[][];    // each element stores a different file name.

    public Directory( int maxInumber )
    {
        // directory constructor
        fsize = new int[maxInumber];     // maxInumber = max files
        for ( int i = 0; i < maxInumber; i++ )
            fsize[i] = 0;                 // all file size initialized to 0
        fnames = new char[maxInumber][maxChars];
        String root = "/";                // entry(inode) 0 is "/"
        fsize[0] = root.length( );        // fsize[0] is the size of "/".
        root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
    }


    // initializes the Directory instance with this data[]
    public int bytes2directory( byte data[] )
    {
        return 0;
    }

    // converts and return Directory information into a plain byte array
    // this byte array will be written back to disk
    // note: only meaningfull directory information should be converted
    // into bytes.
    public byte[] directory2bytes( )
    {
        byte[] temp = new byte[20];
        return temp;
    }

    // filename is the one of a file to be created.
    // allocates a new inode number for this filename
    public short ialloc( String filename )
    {
        return 0;
    }

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

    // returns the inumber corresponding to this filename
    public short namei( String filename )
    {
        /* use for loop to search the filename in the fnames,
        return the index if found it, else return -1
         */
        return 0;
    }
}