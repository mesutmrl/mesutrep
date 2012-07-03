package com.boryazilim;

import java.io.File;

public class CheckFile implements Runnable {

	private static String filePath;
	private volatile boolean isRunning;
	
	public CheckFile ( String PATH )
	{
		filePath = PATH;
		isRunning = true;
	}
	
	public void run ( )
	{
		isRunning = new File( filePath ).exists();
		while ( !isRunning )
		{
		 try {
				Thread.sleep(1000);
			} catch ( InterruptedException ex )
			{
				ex.printStackTrace();
			}
		 isRunning = new File( filePath ).exists();
		}
	}
}
