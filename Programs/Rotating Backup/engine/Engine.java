/* RotatingBackup
 * Nicholas Harrell, 26 April 2015
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

package engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;

import managers.EntryManager;
import managers.EntryManager.*;
import Managers.ConsoleManager;
import Widgets.Clock;
import Widgets.Clock.ClockEvent;
import Widgets.Clock.ClockListener;
import Widgets.Copier.TreeCopier;
import Widgets.FileLoader;
import XMLParser.*;

/**
 * Manages all functionality of the
 * RotatingBackup program.
 * <p>
 * The RotatingBackup is currently implemented only to place
 * copies of the specified files and directories in ~CWD/Backups/. 
 * The program will be later extended to allow specification of a destination
 * directory and/or specification to place backups sibling to the originals.
 * 
 * TODO: Implement specified-directory backup placement.
 * TODO: Implement sibling-of-original backup placement.
 * 
 * @author Nicholas Harrell
 */
public class Engine implements ClockListener
{	
	/** The console manager handles all console IO. */
	public final ConsoleManager CM = new ConsoleManager();
	
	/** The manager of all directories and files to back up. */
	protected final EntryManager EM = new EntryManager();
	
	/**
	 * The clock which regulates backup execution:
	 * A backup is commenced on each clock tick.
	 */
	protected Clock CLOCK;
	
	/**
	 * The depth of the rotating backup.
	 * A backup with a depth of 5 will create five new backups
	 * before cycling back and overwritng the oldest existing backup.
	 * Those implementing this should take care to consider
	 * the size of the files they are backing up.
	 * There is no upper bound, but the effective minimum is one.
	 * Any value less than one will result in a single backup being
	 * overwritten on each tick.
	 */
	protected int DEPTH;
	
	/** The current index which traverses circularly through {@link #DEPTH} **/
	protected int lvl = 1;
	
	/**
	 * A blank constructor. All loading and initialization procedures
	 * are delegated to separate functions in order to allow loading control
	 * to be handled by an external initializer.
	 */
	public Engine(){};
	
	/**
	 * Reads the config file and renders the necessary constructs
	 * for maintaining a rotating backup. After this method completes,
	 * simply starting the {@link #CLOCK} will render the program
	 * fully operational.
	 */
	public void load()
	{
		// Say hello.
		CM.printl("INIT",0,"Console initialized.");
		CM.printl("INIT",1,CM.getDate());
		CM.printl("INIT",1,CM.getCWD());
		
		// Try to create a log file.
		try
		{
			CM.beginLogging(CM.getCWD() + "/logs/");
		}
		catch(IOException e)
		{
			CM.printl("ERROR",0,"Failed to create log file.");
			CM.printl("",1,e);
			CM.printl("",0,"Aborting...");
			System.exit(0);
		}
		
		// Try to read from config file.
		XMLParser FILE;
		FILE = new XMLParser();
		
		try
		{
			FILE = new XMLParser(FileLoader.getCWD() + "/Config.xml", false);
		}
		catch(XMLParserException e)
		{
			if(e.getCause() instanceof FileNotFoundException)
			{
				CM.printl("ERROR",0,"Failed to load config file:");
				CM.printl("",1,FileLoader.getCWD() + "/Config.xml");
				CM.printl("",1,"File should be in XML format and have these data:");
				CM.printl("",2,"<PARAM FREQUENCY=int DEPTH=int />");
				CM.printl("",2,"<ENTRY PATH=string />");
				CM.printl("",2,"<ENTRY PATH=string />");
				System.exit(0);
			}
			else
			{
				CM.printl("",0,e);
				System.exit(0);
			}
		}
		
		final Node ROOT = FILE.getRootNode();		
		Node PARAM = ROOT.getChildOfType("param");
		if(PARAM == null)
		{
			CM.printl("LOAD",0,"Config error: PARAM tag not found.");
			CM.printl("",1,"<PARAM FREQUENCY=int DEPTH=int />");
			System.exit(0);
		}
		
		if(PARAM.getProperty("frequency") == null || PARAM.getProperty("depth") == null)
		{
			CM.printl("ERROR",0,"Config error: PARAM tag is missing FREQUENCY or DEPTH attribute.");
			System.exit(0);
		}
		
		int freq = Integer.parseInt(PARAM.getProperty("frequency").VALUE);
		CLOCK = new Clock(freq);
		CLOCK.addClockListener(this);
		DEPTH = Integer.parseInt(PARAM.getProperty("depth").VALUE);
		
		for(Node n : ROOT.getChildrenOfType("Directory"))
			if(n.getProperty("path") == null || n.getProperty("path").VALUE.length() < 1)
			{
				CM.printl("LOAD",0,"Config error: DIRECTORY tag is missing its PATH.");
				System.exit(0);
			} else
				try
				{
					EM.addDirectory(n.getProperty("path").VALUE);
				} catch (EntryManagerException e)
				{
					CM.printl("ERROR",1,e);
				}
		
		for(Node n : ROOT.getChildrenOfType("File"))
			if(n.getProperty("path") == null || n.getProperty("path").VALUE.length() < 1)
			{
				CM.printl("LOAD",0,"Config error: FILE tag is missing its PATH.");
				System.exit(0);
			} else
				try
				{
					EM.addFile(n.getProperty("path").VALUE);
				} catch (EntryManagerException e)
				{
					CM.printl("ERROR",1,e);
				}
		
		CM.printl("LOAD",0,"Load complete.");
		CM.printl("",0,"=======================================");
	}
	
	/** Begin maintaining the rotating backup. **/
	public void startClock()
	{
		CLOCK.startClock();
	}

	@Override
	public synchronized void clockTicked(ClockEvent e)
	{
		CM.printl("TICK",0,"Commencing backup...");
		
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		for(Entry subj : EM.getEntries())
		{
			if(!Files.exists(Paths.get(subj.PATH)))
			{
				CM.printl("ERROR",1,"File does not exist: " + subj.PATH);
				continue;
			}
			
			CM.printl("COPY",1,"Copying " + subj.PATH);
			CM.printl("",2,"To " + CM.getCWD() + "/Backups/" + subj.NAME + ".BAK." + lvl);
			TreeCopier tc = new TreeCopier(
					Paths.get(subj.PATH),
					Paths.get(CM.getCWD() + "/Backups/" + subj.NAME + ".BAK." + lvl),
					false,
					true);
            try
			{
				Files.walkFileTree(Paths.get(subj.PATH), opts, Integer.MAX_VALUE, tc);
			} catch (IOException e1)
			{
				CM.printl("ERROR",2,"Error copying file:");
				CM.printl("",2,e1);
				e1.printStackTrace();
			}
		}
		
		lvl ++;
		if(lvl > DEPTH)
			lvl = 1;
	}
	
	/**
	 * Copy a file from one path to another using NIO FileChannels.
	 * 
	 * @param subj	The subject file to be copied.
	 * @param dest	The destination file to which to copy.
	 */
	protected void copyFile(File subj, File dest)
	{
		FileChannel srcChan = null;
		FileChannel destChan = null;
		try
		{
			srcChan = new FileInputStream(subj).getChannel();
			destChan = new FileOutputStream(dest).getChannel();
			destChan.transferFrom(srcChan,0,srcChan.size());
		}
		catch(IOException e)
		{
			CM.printl("ERROR",0,"Failed to execute copy for " + subj.toPath());
			CM.printl("",1,e);
		}
		finally
		{
			try
			{
				srcChan.close();
				destChan.close();
			}
			catch(IOException e)
			{
				CM.printl("ERROR",0,"Failed to close stream.");
				CM.printl("",1,e);
				CM.printl("",0,"Aborting...");
				System.exit(0);
			}
		}
	}
}
