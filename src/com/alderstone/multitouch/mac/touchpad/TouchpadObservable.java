//  Copyright 2009 Wayne Keenan
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//  TouchpadObservable.java
//
//  Created by Wayne Keenan on 27/05/2009.
//
package com.alderstone.multitouch.mac.touchpad;

import icy.system.SystemUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;

public class TouchpadObservable extends Observable
{
	
	private static volatile Object			initGuard;
	private static volatile boolean			loaded		= false;
	private static final TouchpadObservable	INSTANCE	= new TouchpadObservable();
	
	// diasable client construction
	private TouchpadObservable()
	{
	}
	
	/**
	 * Gets the current running instance of the multi-touch provider.
	 * 
	 * @return
	 * @throws UnsupportedOperationException
	 *             if multi-touch support is not available on the client OS
	 */
	public static TouchpadObservable getInstance() throws UnsupportedOperationException
	{
		startupNative();
		return INSTANCE;
	}
	
	static
	{
		initGuard = new Object();
		
		// BEGIN - Changes made by Alexandre Dufour on 19 sept. 2011
		
		// Goal: load the native library from a local resource file
		// Problem: the resource cannot be loaded directly from within a .jar file
		// Solution: copy the resource into a temporary file and load it instead
		
		// System.loadLibrary("GlulogicMT");
		
		if (!SystemUtil.isMac())
			throw new UnsupportedOperationException("Warning: multi-touch is currently available for Mac OS X (Intel 64) only");
		
		try
		{
			InputStream is = TouchpadObservable.class.getResourceAsStream("libGlulogicMT.jnilib");
			
			File libFile = File.createTempFile("libGlulogicMT", ".jnilib");
			FileOutputStream fos = new FileOutputStream(libFile);
			
			int b;
			while ((b = is.read()) != -1)
				fos.write(b);
			fos.close();
			
			// load the temporary file
			System.load(libFile.getAbsolutePath());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// END - Changes made by Alexandre Dufour on 19 sept. 2011
	}
	
	private static void startupNative()
	{
		synchronized (initGuard)
		{
			if (!loaded)
			{
				loaded = true;
				registerListener();
				ShutdownHook shutdownHook = new ShutdownHook();
				Runtime.getRuntime().addShutdownHook(shutdownHook);
			}
		}
	}
	
	private static void shutdownNative()
	{
		deregisterListener();
	}
	
	public static void mtcallback(int frame, double timestamp, int id, int state, float size, float x, float y, float dx, float dy, float angle, float majorAxis, float minorAxis)
	{
		INSTANCE.update(frame, timestamp, id, state, size, x, y, dx, dy, angle, majorAxis, minorAxis);
	}
	
	// native methods
	
	native static int registerListener();
	
	native static int deregisterListener();
	
	// shutdown hook
	static class ShutdownHook extends Thread
	{
		public void run()
		{
			shutdownNative();
		}
	}
	
	// Observer interface code
	
	public void update(int frame, double timestamp, int id, int state, float size, float x, float y, float dx, float dy, float angle, float majorAxis, float minorAxis)
	{
		setChanged();
		notifyObservers(new Finger(frame, timestamp, id, state, size, x, y, dx, dy, angle, majorAxis, minorAxis));
	}
	
}
