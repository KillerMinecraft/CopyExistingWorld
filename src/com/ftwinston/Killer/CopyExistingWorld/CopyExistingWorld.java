package com.ftwinston.Killer.CopyExistingWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.World.Environment;

import com.ftwinston.Killer.Option;
import com.ftwinston.Killer.WorldConfig;

public class CopyExistingWorld extends com.ftwinston.Killer.WorldOption
{
	@Override
	public Option[] setupOptions()
	{
		Option[] options = {
			new Option("No custom worlds specified", true)
		};
		
		return options;
	}
	
	@Override
	public void setupWorld(final WorldConfig world, final Runnable runWhenDone)
	{
		if ( world.getEnvironment() != Environment.NORMAL )
		{
			createWorld(world, runWhenDone);
			return;
		}
		
		final String name = world.getName();
		getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(getPlugin(), new Runnable() {
			
			@Override
			public void run()
			{
				File sourceDir = new File(getPlugin().getServer().getWorldContainer() + File.separator + name);
				File targetDir = new File(getPlugin().getServer().getWorldContainer() + File.separator + name);
				
				try
				{
					copyFolder(sourceDir, targetDir);
				}
				catch (IOException ex)
				{
					//getPlugin().log.warning("An error occurred copying the " + name + " world");
				}
				
				sourceDir = new File(getPlugin().getServer().getWorldContainer() + File.separator + name + "_nether");
				targetDir = new File(getPlugin().getServer().getWorldContainer() + File.separator + name + "_nether");
				
				if ( sourceDir.exists() && sourceDir.isDirectory() )
					try
					{
						copyFolder(sourceDir, targetDir);
					}
					catch (IOException ex)
					{
						//getPlugin().log.warning("An error occurred copying the " + name + "_nether world");
					}
				
				// now run a synchronous delayed task, to actually load/create worlds for these folders
				getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
					@Override
					public void run()
					{
						createWorld(world, runWhenDone);
					}
				});
			}
		});
	}
	
	private void copyFolder(File source, File dest) throws IOException
	{
		if(source.isDirectory())
		{	 
    		//if directory doesn't exist, create it
    		if(!dest.exists())
    		   dest.mkdir();
 
    		//list all the directory contents
    		String files[] = source.list();
    		if ( files == null )
    			return;
    		
    		//recursively copy everything in the directory
    		for (String file : files)
    		{
    		   File srcFile = new File(source, file);
    		   File destFile = new File(dest, file);
    		   
    		   copyFolder(srcFile,destFile);
    		}
    	}
		else
		{
			InputStream in = null;
			OutputStream out = null;
			try
			{
	    		in = new FileInputStream(source);
		        out = new FileOutputStream(dest); 
				byte[] buffer = new byte[1024];
				
				int length;
				while ((length = in.read(buffer)) > 0)
				{
					out.write(buffer, 0, length);
				}
			}
			finally
			{
				if ( in != null )
					in.close();
				if ( out != null )
					out.close();
			}
    	}
	}
}