package com.boryazilim;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class SplitAndMerge implements Runnable {
	
	private static String SOURCE_PATH;
	private static long FILE_SIZE,PREV_SIZE,POINTER,CHUNK_SIZE;
	private static int TRADE_SIZE;
	
	private static int NUMBER_OF_CHUNKS = 0;
	private static int TIME_INTERVAL = 2000; // Default time interval, in every 2 seconds file size is checked
	
	private volatile boolean COMPLETED = false;
	private volatile boolean FILE_LENGTH_STABLE = false;
	private volatile boolean IS_READABLE = false;
	
	private File willBeRead;
	private ArrayList<String> nameList = new ArrayList<String> ();
	private InputStream inStream = null;
	
	
	public SplitAndMerge ( String sourceFileName, int Chunk_Size, int trade_size ) throws FileNotFoundException
	{
		SOURCE_PATH = sourceFileName;
		CHUNK_SIZE = Chunk_Size;
		TRADE_SIZE = trade_size;
		FILE_SIZE = 0;
		PREV_SIZE = 1;
		POINTER = 0;
		willBeRead = new File ( SOURCE_PATH );
		inStream = new BufferedInputStream ( new FileInputStream( willBeRead ));
	}
	
	@Override
	public void run() 
	{
		isFileStable ( ); // At first look file will be always unstable
		while ( (!isCompleted()) )
		{
			try {
				Thread.sleep(TIME_INTERVAL);
				if ( !FILE_LENGTH_STABLE)
					isFileStable ( );
				else 
					TIME_INTERVAL = 100; // When file becomes stable, time interval could be smaller
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			try {
				IS_READABLE=isReadAble ( );
				if ( IS_READABLE )
				{
					/*
					 * Checking state of file, growth is compared with chunk size
					 * IS_READABLE holds the answer, and organizes processes
					 */
					readAndFragment ( );
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			//Refreshing video informations and closing inputstream
			replaceFirstPart ( ); //Replace first x kb data with the original one
			inStream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/*
	 *  This Function Checks The Size of File whether size is stable or not.
	 *  if it is stable, FILE_LENGTH_STABLE variable will be set to true
	 */
	private void isFileStable ( )
	{
		PREV_SIZE=FILE_SIZE;
		FILE_SIZE = willBeRead.length();
		
		if ( PREV_SIZE == FILE_SIZE && (!FILE_LENGTH_STABLE) )
		{
			FILE_LENGTH_STABLE = true;
		}
		
	}
	
	private boolean isReadAble ( )
	{
		if ( (FILE_LENGTH_STABLE) && POINTER==FILE_SIZE )
		{
			setCompleted(true);
			return false;
		}
		
		if ( (FILE_SIZE >= (CHUNK_SIZE + POINTER)) && (!FILE_LENGTH_STABLE) && (CHUNK_SIZE!=0)  )
		{
			/*
			 * File keeps on growing and  buffer already includes enough bytes to read ( >= CHUNK_SIZE )
			 * Read operation could be performed
			 */
			return true;
		}
		else if ( (FILE_SIZE >= (CHUNK_SIZE + POINTER)) && (FILE_LENGTH_STABLE) && (CHUNK_SIZE!=0)  )
		{
			/*
			 * FILE DOES NOT GROW ANYMORE, STABLE AND THERE ARE ENOUGH BYTES TO READ ( >= CHUNK_SIZE )
			 * Read operation could be performed
			 */
			return true;
		}
		else if ( (FILE_SIZE < (CHUNK_SIZE + POINTER)) && (!FILE_LENGTH_STABLE) && (CHUNK_SIZE!=0)  )
		{
			/*
			 * Not enough data is available, we have to wait fot the buffer to fill up
			 * Read operation could not be performed, returns false
			 */
			return false; 
		}
		else if ( (FILE_SIZE < (CHUNK_SIZE + POINTER)) && (FILE_LENGTH_STABLE) && (CHUNK_SIZE!=0)  )
		{
			/*
			 * We have to wait to get enough bytes for reading or writing 
			 * CHUNK_SIZE is being revaluated
			 */
			CHUNK_SIZE = (int) (FILE_SIZE - POINTER); 
			return true;
		}
		return false;
	}
	
	private void readAndFragment ( ) throws IOException
	{
		
		byte[] temporary = null;
		
		if ( CHUNK_SIZE > 0 )
		{
			
			String PART_NAME ="PART"+NUMBER_OF_CHUNKS+".bin";
			temporary = new byte[(int) CHUNK_SIZE]; // Temporary Byte Array
			int bytesRead = inStream.read(temporary, 0, (int) CHUNK_SIZE);
			POINTER+=CHUNK_SIZE;
			
			if ( bytesRead > 0) // If bytesRead array is not empty
			{
				NUMBER_OF_CHUNKS++;
			}
						
			write(temporary, "D://"+PART_NAME);
			nameList.add("D://"+PART_NAME); // Part name is being appended to parts
		}
		else {
			setCompleted(true);
		}
		
	}
	
	private void write(byte[] DataByteArray, String DestinationFileName)
	{
	    try {
	      OutputStream output = null;
	      try {
	        output = new BufferedOutputStream(new FileOutputStream(DestinationFileName));
	        output.write( DataByteArray );
	      }
	      finally {
	        output.close();
	      }
	    }
	    catch(FileNotFoundException ex){
	    	ex.printStackTrace();
	    }
	    catch(IOException ex){
	    	ex.printStackTrace();
	    }
	}
	
	public void mergeParts ( String DESTINATION_PATH )
	{
		File[] file = new File[nameList.size()];
		byte AllFilesContent[] = null;
		
		int TOTAL_SIZE = 0;
		int FILE_NUMBER = nameList.size();
		int FILE_LENGTH = 0;
		int CURRENT_LENGTH=0;
		
		for ( int i=0; i<FILE_NUMBER; i++)
		{
			file[i] = new File (nameList.get(i));
			TOTAL_SIZE+=file[i].length();
		}
		
		try {
			AllFilesContent= new byte[TOTAL_SIZE]; // Length of file
			InputStream inStream = null;
			
			for ( int j=0; j<FILE_NUMBER; j++)
			{
				inStream = new BufferedInputStream ( new FileInputStream( file[j] ));
				FILE_LENGTH = (int) file[j].length();
				inStream.read(AllFilesContent, CURRENT_LENGTH, FILE_LENGTH);
				CURRENT_LENGTH+=FILE_LENGTH;
				inStream.close();
			}
			
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found " + e);
		}
		catch (IOException ioe)
		{
			 System.out.println("Exception while reading the file " + ioe);
		}
		finally 
		{
			write (AllFilesContent,DESTINATION_PATH);
		}
		
		System.out.println("SUCCESSFUL MERGE");
	}

	private void replaceFirstPart ( ) throws IOException
	{
		byte [] missingPart = new byte[TRADE_SIZE]; //First 10kb of data is exchanged
		
		RandomAccessFile rafIn = new RandomAccessFile( willBeRead,"r" );
		rafIn.read(missingPart); // Same as fileInputStream
			
		File file = new File (nameList.get(0));
		RandomAccessFile rafOut = new RandomAccessFile( file,"rw" );
		rafOut.seek(0);
		rafOut.write(missingPart);
		
		rafOut.close();
	}

	public boolean isCompleted() {
		return COMPLETED;
	}

	public void setCompleted(boolean cOMPLETED) {
		COMPLETED = cOMPLETED;
	}

}


