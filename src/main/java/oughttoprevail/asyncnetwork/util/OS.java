/*
Copyright 2019 https://github.com/OughtToPrevail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package oughttoprevail.asyncnetwork.util;

public interface OS
{
	static boolean isAndroid0()
	{
		boolean android;
		try
		{
			Class.forName("android.app.Application", false, ClassLoader.getSystemClassLoader());
			android = true;
		} catch(Throwable ignored)
		{
			// Failed to load the class uniquely available in Android.
			android = false;
		}
		return android;
	}
	
	String OS = System.getProperty("os.name").toLowerCase();
	boolean WINDOWS = OS.contains("win");
	boolean LINUX = OS.contains("nux") || OS.contains("nix");
	boolean MAC = OS.contains("mac");
	boolean ANDROID = isAndroid0();
	/**
	 * The model of the system, if the system is 32 bit the model will be "32" and if 64 the model
	 * will be "64".
	 */
	String MODEL = System.getProperty("sun.arch.data.model");
}