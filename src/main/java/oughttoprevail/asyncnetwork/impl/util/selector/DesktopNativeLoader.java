package oughttoprevail.asyncnetwork.impl.util.selector;

import sun.security.action.GetPropertyAction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;

public interface DesktopNativeLoader
{
	/**
	 * The temporary folder path of the operating system.
	 */
	Path TEMP_PATH = Paths.get(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
	
	static String copy(String file, String extension, String resource, InputStream in)
	{
		Path path = TEMP_PATH.resolve(resource);
		int tries = 0;
		while(true)
		{
			try
			{
				Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
				return path.toString();
			} catch(AccessDeniedException accessDenied)
			{
				if(tries > 250)
				{
					System.err.println("Failed to load selector (tried 250 times).");
					accessDenied.printStackTrace();
					return null;
				}
				tries++;
				path = TEMP_PATH.resolve(file + " (" + tries + ")" + extension);
			} catch(IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}