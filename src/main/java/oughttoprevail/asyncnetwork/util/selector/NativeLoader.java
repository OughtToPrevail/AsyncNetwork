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
package oughttoprevail.asyncnetwork.util.selector;

import java.io.InputStream;
import java.net.URL;

import oughttoprevail.asyncnetwork.exceptions.LoadException;
import oughttoprevail.asyncnetwork.util.OS;

public interface NativeLoader
{
	/**
	 * Loads the specified file + extension.
	 *
	 * @param file the file name
	 * @param extension the file extension
	 * @return whether it file was successfully loaded
	 */
	static boolean load(String file, String extension)
	{
		//Append "/lib" and the model of this machine
		file = file + OS.MODEL;
		String resource = "/lib" + file + extension;
		try
		{
			URL url = NativeLoader.class.getResource(resource);
			if(url == null)
			{
				System.err.println("Failed to find " + resource + "!");
				return false;
			}
			try
			{
				System.load(url.getPath());
				return true;
			} catch(Throwable e)
			{
				try(InputStream in = url.openStream())
				{
					String path;
					//remove "/"
					resource = resource.substring(1);
					if(OS.ANDROID)
					{
						path = AndroidNativeLoader.copy(file, extension, resource, in);
					} else
					{
						path = DesktopNativeLoader.copy(file, extension, resource, in);
					}
					
					System.load(path);
					return true;
				} catch(Throwable e1)
				{
					e.printStackTrace();
					e1.printStackTrace();
				}
			}
		} catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	static void exception(String selector) throws LoadException
	{
		throw new LoadException("Cannot load oughttoprevail.oughttoprevail.asyncnetwork.util.selector." + selector + ", Selector not implemented for " + OS.OS + "!");
	}
}