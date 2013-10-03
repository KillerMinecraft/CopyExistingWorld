package com.ftwinston.KillerMinecraft.Modules.CopyExistingWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.World.Environment;

import com.ftwinston.KillerMinecraft.Option;
import com.ftwinston.KillerMinecraft.WorldConfig;
import com.ftwinston.KillerMinecraft.WorldGenerator;
import com.ftwinston.KillerMinecraft.Configuration.ToggleOption;

public class CopyExistingWorld extends WorldGenerator
{
	ToggleOption[] options;

	@Override
	public Option[] setupOptions()
	{
		int num = Plugin.getNumOptions();
		if ( num < 1 )
			options = new ToggleOption[] { new ToggleOption("No custom worlds defined", true) };
		else
		{
			options = new ToggleOption[num];

			for ( int i=0; i<num; i++ )
				options[i] = new ToggleOption(Plugin.getOptionName(i), i==0);
			
			ToggleOption.ensureOnlyOneEnabled(options);
		}
		
		return options;
	}
	
	@Override
	public void setupWorld(final WorldConfig world, final Runnable runWhenDone)
	{
		String optionName = null;
		for ( int i=0; i<options.length; i++ )
			if ( options[i].isEnabled() )
			{
				optionName = Plugin.getOptionName(i);
				world.setSeed(Plugin.getOptionSeed(i));
				break;
			}
		
		if ( world.getEnvironment() == Environment.NETHER )
			optionName += "_nether";
		else if ( world.getEnvironment() == Environment.THE_END )
			optionName += "_the_end";
		
		final String targetName = world.getName();
		final String sourceWorldName = optionName;
		
		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			
			@Override
			public void run()
			{
				File sourceDir = new File(getPlugin().getServer().getWorldContainer() + File.separator + sourceWorldName);
				File targetDir = new File(getPlugin().getServer().getWorldContainer() + File.separator + targetName);
				
				try
				{
					copyFolder(sourceDir, targetDir);
				}
				catch (IOException ex)
				{
				}
				
				// now run a synchronous delayed task, to actually load/create the world for this folder
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
		if ( !source.exists() )
			return;
		
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
