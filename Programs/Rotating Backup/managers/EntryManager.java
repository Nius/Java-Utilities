package managers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages directory and file entries for
 * the RotatingBackup program.
 * 
 * @author Nius
 */
public class EntryManager
{
	/** The list of entries of files and directories to back up **/
	protected final ArrayList<Entry> ENTRIES = new ArrayList<Entry>();
	
	/** The number of Files in {@link #ENTRIES}, as opposed to directories. **/
	protected int files;
	
	/** The number of Directories in {@link #ENTRIES}, as opposed to files. **/
	protected int directories;
	
	/** Returns a new EntryManager, ready to receive new Entries. **/
	public EntryManager(){}
	
	/**
	 * Specify a new file to be managed by this EntryManager.
	 * 
	 * @param path	The complete path to the newly specified file.
	 * @throws EntryManagerException	An exception will be thrown
	 * 									if the provided <code>path</code>
	 * 									cannot be parsed for a filename.
	 */
	public void addFile(String path) throws EntryManagerException
	{
		files ++;
		ENTRIES.add(new File(path));
	}
	
	/**
	 * Specify a new directory to be managed by this EntryManager.
	 * 
	 * @param path	The complete path to the newly specified directory.
	 * @throws EntryManagerException	An exception will be thrown
	 * 									if the provided <code>path</code>
	 * 									cannot be parsed for a filename.
	 */
	public void addDirectory(String path) throws EntryManagerException
	{
		directories ++;
		ENTRIES.add(new Directory(path));
	}
	
	/**
	 * Retrieve a complete list of Entries kept by this manager.
	 * 
	 * @return	A (cloned) list of Entries kept by this manager.
	 */
	public Entry[] getEntries()
	{
		Entry[] clone = new Entry[ENTRIES.size()];
		int i = 0;
		for(Entry e : ENTRIES)
			clone[i++] = e;
		return clone;
	}
	
	/**
	 * Retrieve a complete list of Files kept by this manager.
	 * 
	 * @return	A (cloned) list of Files kept by this manager.
	 */
	public File[] getFiles()
	{
		File[] clone = new File[files];
		int i = 0;
		for(Entry e : ENTRIES)
			if(e instanceof File)
				clone[i++] = (File)e;
		return clone;
	}
	
	/**
	 * Retrieve a complete list of Directories kept by this manager.
	 * 
	 * @return	A (cloned) list of Directories kept by this manager.
	 */
	public Directory[] getDirectories()
	{
		Directory[] clone = new Directory[files];
		int i = 0;
		for(Entry e : ENTRIES)
			if(e instanceof Directory)
				clone[i++] = (Directory)e;
		return clone;
	}
	
	/**
	 * A unifying abstraction for directories and files.
	 * 
	 * @author Nius
	 */
	public abstract class Entry
	{
		/** The path of this entry **/
		public final String PATH;
		
		/** The filename of this File without the directory path **/
		public final String NAME;
		
		/**
		 * Default constructor. All Entries have one path.
		 * @param path	The complete filepath to the directory or file
		 * 				being backed up.
		 * @throws EntryManagerException	An exception will be thrown
		 * 									if the provided <code>path</code>
		 * 									cannot be parsed for a filename. 
		 */
		public Entry(String path) throws EntryManagerException
		{
			PATH = path;
			Pattern regex = Pattern.compile("^.*[/\\\\]([A-Za-z0-9_\\-\\.]+)$");
			Matcher m = regex.matcher(path);
			if(m.matches())
				NAME = m.group(1);
			else
				throw new EntryManagerException("Could not parse path \"" + path + "\" for file or directory name.");
		}
	}
	
	/**
	 * An object representation of a Directory.
	 * 
	 * @author Nius
	 */
	public class Directory extends Entry
	{
		/**
		 * Default constructor. All Directories have one path.
		 * @param path	The complete filepath to the directory or file
		 * 				being backed up.
		 * @throws EntryManagerException	An exception will be thrown
		 * 									if the provided <code>path</code>
		 * 									cannot be parsed for a filename.
		 */
		public Directory(String path) throws EntryManagerException {super(path);}
	}
	
	/**
	 * An object representation of a File.
	 * 
	 * @author Nius
	 */
	public class File extends Entry
	{		
		/**
		 * Default constructor. All Files have one path.
		 * @param path	The complete filepath to the directory or file
		 * 				being backed up.
		 * @throws EntryManagerException	An exception will be thrown
		 * 									if the provided <code>path</code>
		 * 									cannot be parsed for a filename. 
		 */
		public File(String path) throws EntryManagerException {super(path);}
	}
	
	/**
	 * A simple renaming of a standard {@link Exception}.
	 * 
	 * @author	Nius Atreides (Nicholas Harrell)
	 */
	@SuppressWarnings("serial")
	public class EntryManagerException extends Exception
	{
		/**	An Exception of some other kind to which this Exception refers. */
		protected Throwable E;
		
		/**
		 * Returns a new EntryManagerException with the specified message.
		 * 
		 * @param message	The message which this {@link Exception} will carry.
		 */
		public EntryManagerException(String message)
		{
			super(message);
		}
		
		/**
		 * Returns a new EntryManagerException with the specified message,
		 * and the specified Exception as the cause of this one.
		 * 
		 * @param message	The message which this {@link Exception} will carry.
		 * @param e			The Exception which caused this one.
		 */
		public EntryManagerException(String message,Exception e)
		{
			super(message,e);
		}
	}
}
