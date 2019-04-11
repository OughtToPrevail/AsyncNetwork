package oughttoprevail.asyncnetwork.impl.util.selector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import oughttoprevail.asyncnetwork.Channel;

public interface AndroidNativeLoader
{
	/**
	 * Returns the temporary path of the current machine.
	 *
	 * @return the temporary path of the current machine
	 */
	static String getTempPath()
	{
		try
		{
			File tempPath = File.createTempFile("TempPathFinder", "");
			String path = tempPath.getParent();
			tempPath.delete();
			return path;
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		return "/";
	}
	
	/**
	 * The temporary path of the current machine
	 */
	String TEMP_PATH = getTempPath();
	
	/**
	 * Copies the specified in into the specified resource.
	 * This is done by creating a new {@link FileOutputStream} and a buffer
	 * the size of {@link Channel#DEFAULT_BUFFER_SIZE} and copying the amount
	 * read into the specified in.
	 * If a fileName is being used while trying to do this an {@link IOException} is
	 * likely to occur and then this will try again updating the name with the
	 * amount of times tried.
	 * If the amount of times tried is more than 250 then it will stop and print the stacktrace.
	 *
	 * @param fileName of the file to be copied
	 * @param extension of the file to be copied
	 * @param resource is the full file
	 * @param in {@link InputStream} of the file that needs to be copied
	 * @return the new file name
	 */
	static String copy(String fileName, String extension, String resource, InputStream in)
	{
		int tries = 0;
		while(true)
		{
			File fileObject = null;
			try
			{
				fileObject = new File(resource);
				if(!fileObject.exists())
				{
					fileObject.createNewFile();
				}
				FileOutputStream outputStream = new FileOutputStream(fileObject);
				byte[] data = new byte[Channel.DEFAULT_BUFFER_SIZE];
				int read;
				while((read = in.read(data)) > 0)
				{
					outputStream.write(data, 0, read);
				}
				return resource;
			} catch(IOException e)
			{
				//delete fileName object if it was created but failed later
				//probably fail due to the fileName being used
				fileObject.delete();
				if(tries > 250)
				{
					e.printStackTrace();
					return null;
				}
				tries++;
				resource = TEMP_PATH + File.separatorChar + fileName + " (" + tries + ")" + extension;
			}
		}
	}
}
