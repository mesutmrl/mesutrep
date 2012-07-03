package com.boryazilim;


public class FragmentAndUpload {
	
	private static final int CHUNK_SIZE = 1024*500; //Size of Chunks as kb
	private static final int TRADE_SIZE = 1024*10; //First 10Kb Will Be Exchanged
	
	public static void main(String[] args) throws Exception
	{	
		 // SOURCE_PATH will be assumed as known
		 String SOURCE_PATH ="C:\\Users\\User\\Documents\\Youcam\\Capture_20120629_2.wmv";
		 String DEST_PATH ="D:\\result.wmv";
		 
		 /*
		  * This thread looks for the source file exist or not, if split job works with capturing function
		  * this part will be useless beacuse we will know the file is definitely
		  */
		 Thread CFL = new Thread ( new CheckFile ( SOURCE_PATH ) );
		 CFL.start();
		 
		 while ( CFL.isAlive() ) { } // When file is found, thread dies and program flow continues
		 
		
		 SplitAndMerge SAM = new SplitAndMerge ( SOURCE_PATH, CHUNK_SIZE,TRADE_SIZE );
		 Thread splitThread = new Thread ( SAM );
		 splitThread.start ( );
		 
		 while ( splitThread.isAlive()  ) {	} // Thread dies when source video file does not grow anymore
		 
		if ( SAM.isCompleted() ) 
		{	
			SAM.mergeParts( DEST_PATH );
			
			CheckSum resultCheckUp = new CheckSum ( SOURCE_PATH );
			System.out.println ( "SORCE FILE MD5: "+resultCheckUp.getMD5Checksum() );
			
			resultCheckUp = new CheckSum ( DEST_PATH );
			System.out.println ( "RESULT FILE MD5: "+resultCheckUp.getMD5Checksum() );
		}
		
	}

}





















/*
try {
	System.out.println("IN MAIN THREAD : "+new Date());
	System.out.println("FILE COULD NOT FOUND: "+filePath);
	System.out.println("--------------------------------");
	newThread.sleep(1000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
i++; 
filePath = "D://VIDEO00"+i+".3gp";
exists = new File( filePath ).exists();
}

System.out.println("IN MAIN THREAD FILE FOUND: "+filePath);
System.out.println("--------------------------------"); */