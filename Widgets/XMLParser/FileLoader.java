package XMLParser;
/* FileLoader
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Provides quick and easy functionality for loading the contents
 * of a file into a usable data structure.
 * <p>
 * The FileLoader reads from the file at a given path immediately
 * upon instantiation. As such, FileLoaders and loaded files
 * are to possess an exact one-to-one relationship.
 * 
 * @author	Nius Atreides (Nicholas Harrell)
 */
public class FileLoader
{
	/** The path of the file which was read. */
	public final String PATH;
	
	/** A list of Strings, containing the complete and unmodified
	 	contents of the file. Each line in the file is placed into
	 	its own string.												*/
	protected ArrayList<String> LINES = new ArrayList<String>();
	
	/**
	 * Returns a new FileLoader object which has already loaded the
	 * contents of the file at the specified <i>path</i>.
	 * 
	 * @param path	The location of the file which this particular
	 * 				FileLoader will read.
	 * @throws IOException	An Exception is thrown if the file at
	 * 						the specified {@link #PATH} failed to
	 * 						load.
	 */
	public FileLoader(String path) throws IOException
	{
		PATH = path;
		readFromFile();
		
		//Debug...
		/*
		System.out.println("FileLoader: Read file...");
		for(String i : LINES)
			System.out.println(i);
		System.out.println("===============================================================================");
		*/
	}
	
	/**
	 * Execute the reading of the file at the {@link #PATH}.
	 * Any problems with loading the file will be caught and reported
	 * to the standard output.
	 * 
	 * @throws IOException	An exception is thrown if an error occurrs
	 * 						while attempting to read the file at the
	 * 						specified {@link #PATH}.
	 */
	protected void readFromFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(PATH));
		String line;
		while((line = br.readLine()) != null)
			LINES.add(line);
		br.close();
	}
	
	/**
	 * Get a copy of the list of lines read from the file at {@link #PATH}.
	 * 
	 * @return	A copied list of strings containing the complete, unmodified
	 * 			contents of the file read at {@link #PATH}.
	 */
	public String[] getLines()
	{
		String[] lines = new String[LINES.size()];
		for(int i = 0; i < LINES.size(); i ++)
			lines[i] = LINES.get(i);
		return lines;
	}
}
