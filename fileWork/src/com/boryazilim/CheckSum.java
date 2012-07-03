package com.boryazilim;

import java.io.*;
import java.security.MessageDigest;

public class CheckSum {
   
	private static String FileName;
	
	public CheckSum ( String Filename )
	{
		FileName = Filename;
	}
	
   private byte[] createChecksum( ) throws Exception {
       
	   File file = new File(FileName);
	   int FILE_LENGTH = (int) file.length();	
	   
	   InputStream fis =  new FileInputStream(FileName);

       byte[] buffer = new byte[FILE_LENGTH]; //Byte Array Length Could Be Changed
       MessageDigest complete = MessageDigest.getInstance("MD5");
       int numRead;

       do {
           numRead = fis.read(buffer);
           if (numRead > 0) { // If buffer is not empty, it is obvious that some data was read
               complete.update(buffer, 0, numRead); // Updates MessageDigest Object
           }
       } while (numRead != -1); // Until the end of file reached

       fis.close();
       return complete.digest();
   }
   
   public String getMD5Checksum ( ) throws Exception {
	   
	   byte[] byteArray = createChecksum  ();
	   String result="";
	   
	   for ( int i=0; i < byteArray.length ; i++ )
	   {
		   //Converts Byte Array's i.element to integer
		   result+= Integer.toString( (byteArray[i] & 0xff) + 0x100, 16 ).substring(1);
	   }
	   return result;
   }
}
