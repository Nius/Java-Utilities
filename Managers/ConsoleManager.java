/* DirectiveManager
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

package Managers;

import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Provides a lightweight interface with the system console for user IO.
 * Provides an easy means of formatting log files for console IO.
 * 
 * @author Nicholas Harrell
 */
public class ConsoleManager
{
	/**
	 * The ISR listens constantly on the default {@link System#console()}.
	 * When input is received it is sent to {@link #reportLine(String, boolean)}
	 * and subsequently {@link #fireEvent(String)}.
	 */
	protected final InputStreamReaderThread ISR = new InputStreamReaderThread();
	
	/**
	 * A complete list of all lines transacted - whether input or output - by this
	 * console.
	 */
	protected final ArrayList<Line> LOG = new ArrayList<Line>();
	
	/** Writes to the log file. */
	protected FileWriter writer;
	
	/**
	 * Specification of the date format to use.<br>
	 * yyyy-MM-dd_HH-mm-ss
	 */
	protected static final DateFormat DFORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	
	/**
	 * Whether the ConsoleManager has been instructed to begin logging.
	 * This mechanism allows the console to begin processing IO before it is
	 * ready to write to the log file. 
	 */
	protected boolean BEGANLOG = false;
	
	/** The path for this ConsoleManager's log file. */
	protected String LOGFILE;
	
	/** The width of prefixes for formatted lines. */
	protected static final int PREFIX_WIDTH = 15;
	
	/** The width of a tab. */
	protected static final int TAB_WIDTH = 5;
	
	/**
	 * Whether this ConsoleManager has failed to write to the log file.
	 * If it has failed it will no longer attempt to write to the log,
	 * thus preventing an infinite loop:
	 * If logging fails, the console will generate messages to the output
	 * stream indicating that this has happened. Each of those messages would
	 * then also attempt to log, would fail, and would generate failure
	 * messages. This would continue indefinitely.
	 */
	protected boolean hasFailed = false;
	
	/**
	 * Creates a blank Console Manager, but does not execute any setup.
	 */
	public ConsoleManager(){}
	
	/**
	 * Instructs the ConsoleManager to begin logging in the specified
	 * directory.
	 * The log file name is automatically determined:
	 * {@value #DFORMAT}.log
	 * 
	 * Note that {@link #beginListening()} is required to begin listening
	 * for input on the system console.
	 * 
	 * @param directory		The directory into which to place the log file.
	 * @throws IOException	If the ConsoleManager failed to open a log file for
	 * 						writing it will throw an exception.
	 */
	public void beginLogging(String directory) throws IOException
	{
		// Add a trailing slash to the directory so that a complete
		//	filepath string can be generated.
		if(!(directory.endsWith("/") || directory.endsWith("\\")))
			directory += "/";
		
		LOGFILE = directory + getDate() + ".log";
		
		// This line is not written to log.
		printl("INIT",0,"Log file: " + LOGFILE);
		
		writer = new FileWriter(LOGFILE,true);
		BEGANLOG = true;
		
		// All IO hereafter writes to log (until fail).
	}
	
	/**
	 * Instructs this ConsoleManager to begin listening to the System Console.
	 * Note that {@link #beginLogging(String)} is required to begin writing to
	 * the log file.
	 */
	public void beginListening()
	{
		ISR.start();
	}
	
	/**
	 * Retrieve the current system date, formatted
	 * as per {@link #DFORMAT}.
	 * 
	 * @return	The current system date, formatted
	 * 			as per {@link #DFORMAT}.
	 */
	public String getDate()
	{
		Date curDate = new Date();
		return DFORMAT.format(curDate);
	}
	
	/**
	 * Retrieve the current working directory.
	 * 
	 * @return	A string representation of
	 * 			the current working directory.
	 */
	public String getCWD()
	{
		return new File(".").getAbsolutePath();
	}
	
	/**
	 * Adds a line of text to the internal {@link #LOG} and writes
	 * it to the log file at {@link #LOGFILE}. When writing to the
	 * log file the date and source are prepended.
	 * <p>
	 * If, at any point, the ConsoleListener fails to write to the log
	 * file, it will stop attempting to write to the log, print the
	 * stack trace of the offending Exception, fire a new
	 * {@link ConsoleEvent} with a message containing only (char)7,
	 * and continue handling console transactions.
	 * <p>
	 * All lines handled by the ConsoleManager, whether printed or
	 * read from the system console, pass through this method.
	 * <p>
	 * This method is public so that an external class has liberty
	 * to add something to the log file without printing to console.
	 * 
	 * @param text			The text of the line.
	 * @param isUser		The origin of the line.
	 * 						{@code true} if from user input.
	 * 						{@code false} if from program ouput.
	 */
	public void reportLine(String text, boolean isUser)
	{
		String tag = "[" + getDate() + "]" + ((isUser) ? "[USER]" : "");
		
		LOG.add(new Line(tag,text,isUser));
		
		if(!hasFailed)
			try
			{
				writer.write(tag + text + ((text.endsWith(System.lineSeparator())) ? "" : System.lineSeparator()));
				writer.flush();
			}
			catch (IOException e)
			{
				hasFailed = true;
				print("ERROR",0,e);
				fireEvent("" + (char)7);
			}
		
		//If the user just entered something to the system console,
		//	notify all listeners.
		if(isUser)
			fireEvent(text);
	}
	
	/**
	 * Encapsulates a simple system console reader and reports any and all
	 * read lines to its parent {@link ConsoleManager} immediately by means
	 * of {@link ConsoleManager#reportLine(String, boolean)}.
	 * 
	 * @author Nicholas Harrell
	 */
	protected class InputStreamReaderThread extends Thread
	{
		/**
		 * Creates a blank InputStreamReaderThread, which will run
		 * independently in real-time.
		 */
		public InputStreamReaderThread()
		{
			super();
		}
		
		/** Starts reading from the console. */
		public void run()
		{
			Console CONSOLE = System.console();
			while(true)
			{
				try
				{
					String input = CONSOLE.readLine();
					reportLine(input,true);
				}
				catch(Exception e)
				{
					//e.printStackTrace();	// Scaffold
				}
			}
		}
	}
	
	/**
	 * A simple carrier of a line of text and meta-information such
	 * as an identifying tag and a boolean indicating the line's
	 * origin.
	 * 
	 * @author Nicholas Harrell
	 */
	protected class Line
	{
		/** The literal of the line's content. */
		public final String CONTENT;
		
		/**	An identifying tag to accompany the line. */
		public final String TAG;
		
		/**
		 * The origin of the line.
		 * {@code true} if from user input.
		 * {@code false} if from program ouput.
		 */
		public final boolean ISUSER;
		
		/**
		 * Returns a new Line object.
		 * 
		 * @param content	The literal of the line's content.
		 * @param tag		An identifying tag to accompany the line.
		 * @param isUser	The origin of the line.
		 * 					{@code true} if from user input.
		 * 					{@code false} if from program ouput.
		 */
		public Line(String tag, String content, boolean isUser)
		{
			CONTENT = content;
			TAG = tag;
			ISUSER = isUser;
		}
	}
	
	//
	//	PRINTING
	//
	
	/**
	 * Prints the provided output as-is.
	 * All other print methods end here.
	 * 
	 * @param output	The literal to print to the console.
	 */
	public void print(String output)
	{
		if(BEGANLOG)
			reportLine(output,false);
		System.out.print(output);
	}
	
	/**
	 * Prints the provided output as-is, but jumps to a new
	 * line afterwards.
	 * 
	 * @param output	The literal to print to the console.
	 */
	public void printl(String output)
	{
		print(output + "\n");
	}
	
	/**
	 * Prints a formatted line:
	 * <ol>
	 * <li>	Places the provided <code>prefix</code> in brackets and
	 * 		places that at the left edge of the line. If the prefix
	 * 		is blank, then no prefix or brackets are placed.
	 * <li>	Indents the line to a uniform starting position, regardless
	 * 		of the size (or presence) of the bracketed prefix.
	 * <li>	Indents repeatedly per the <code>tabCount</code> provided.
	 * <li>	Prints the provided <code>output</code> as-is.
	 * </ol>
	 * The {@code output} is always {@value #PREFIX_WIDTH} spaces from the
	 * left edge, regardless of whether a {@code prefix} is specified.
	 * Each indentation is a series of {@value #TAB_WIDTH} spaces.
	 * Outputs which have newlines in them will be formatted on the first
	 * line only. Subsequent lines in an output literal are not formatted.
	 * It is recommended that outputs which span multiple lines be split
	 * and passed one line at a time to this method, or printed via
	 * {@link #printls(String, boolean, int, boolean, String)}.
	 * 
	 * @param prefix	The string to place in brackets at the left edge
	 * 					of the line. Use "" to use no prefix.
	 * @param tabCount	How many times to indent the line of text.
	 * @param output	The literal to print to the console.
	 */
	public void print(String prefix, int tabCount, String output)
	{
		//Create the prefix.
		String complete = (prefix.length() > 0) ? "[" + prefix + "] " : "";
		
		//Append spaces to make the prefix and its margin 15 spaces wide.
		for(int i = complete.length(); i < 15; i ++)
			complete += " ";
		
		//Indent the specified number of times.
		for(int i = 0; i < tabCount; i ++)
			for(int j = 0; j < TAB_WIDTH; j ++)
				complete += " ";
		
		//Append the output literal.
		complete += output;
		
		//Print.
		print(complete);
	}
	
	/**
	 * Prints a formatted line:
	 * <ol>
	 * <li>	Places the provided <code>prefix</code> in brackets and
	 * 		places that at the left edge of the line. If the prefix
	 * 		is blank, then no prefix or brackets are placed.
	 * <li>	Indents the line to a uniform starting position, regardless
	 * 		of the size (or presence) of the bracketed prefix.
	 * <li>	Indents repeatedly per the <code>tabCount</code> provided.
	 * <li>	Prints the provided <code>output</code> as-is.
	 * <li>	Jumps to a new line at the end of the <code>output</code> literal.
	 * </ol>
	 * The {@code output} is always {@value #PREFIX_WIDTH} spaces from the
	 * left edge, regardless of whether a {@code prefix} is specified.
	 * Each indentation is a series of {@value #TAB_WIDTH} spaces.
	 * Outputs which have newlines in them will be formatted on the first
	 * line only. Subsequent lines in an output literal are not formatted.
	 * It is recommended that outputs which span multiple lines be split
	 * and passed one line at a time to this method, or printed via
	 * {@link #printls(String, boolean, int, boolean, String)}.
	 * 
	 * @param prefix	The string to place in brackets at the left edge
	 * 					of the line. Use "" to use no prefix.
	 * @param tabCount	How many times to indent the line of text.
	 * @param output	The literal to print to the console.
	 */
	public void printl(String prefix, int tabCount, String output)
	{
		print(prefix,tabCount,output + "\n");
	}
	
	/**
	 * Splits a String by newlines, separately printing each element
	 * according to {@link #printl(String, int, String)}.
	 * 
	 * @param prefix	The string to place in brackets at the left edge
	 * 					of the line. Use "" to use no prefix.
	 * @param pfAll		Whether to prefix all elements in the String array.
	 * 					<code>true</code> will prefix all lines with <code>prefix</code>.
	 * 					<code>false</code> will prefix only the first line.
	 * @param tabCount	How many times to indent the text.
	 * @param indent	Whether to add one to the <code>tabCount</code> after printing
	 * 					the first element.
	 * 					<code>true</code> will indent all elements after the first.
	 * 					<code>false</code> will leave the <code>tabCount</code> unchanged.
	 * @param output	The literal to split by <code>"\n"</code> and print to the
	 * 					console.
	 */
	public void printls(String prefix, boolean pfAll, int tabCount, boolean indent, String output)
	{
		String[] splat = output.split("\n");
		
		printl(prefix,tabCount,splat[0]);		
		for(int i = 1; i < splat.length; i ++)
			printl(pfAll ? prefix : "", indent ? tabCount + 1 : tabCount, splat[i]);
	}
		
	/**
	 * Prints the stack trace of an {@link Exception}, but
	 * formatted in the same way as in {@link #printl}.
	 * The output will jump to a new line after printing.
	 * 
	 * @param prefix	The string to place in brackets at the left edge
	 * 					of the line. Use "" to use no prefix.
	 * @param tabCount	How many times to indent the lines of text.
	 * @param e			The Exception whose stack trace to print.
	 */
	public void print(String prefix, int tabCount, Throwable e)
	{
		if(e.getCause() != null)
			print(prefix,tabCount + 1,e.getCause());
		
		printl("EXCEPTION",tabCount,e.getMessage());
		StackTraceElement[] trace = e.getStackTrace();
		for(StackTraceElement line : trace)
			printl(prefix,tabCount + 1,line.toString());
	}
	
	/**
	 * An alias for {@link #print(String, int, Throwable)}, functionally
	 * identical.
	 * 
	 * @param prefix	The string to place in brackets at the left edge
	 * 					of the line. Use "" to use no prefix.
	 * @param tabCount	How many times to indent the lines of text.
	 * @param e			The Exception whose stack trace to print.
	 */
	public void printl(String prefix, int tabCount, Throwable e)
	{
		print(prefix,tabCount,e);
	}
	
	//
	//	EVENT HANDLING
	//
	
	/** The list of objects listening for events from this ConsoleManager. */
	protected ArrayList<Object> listeners = new ArrayList<Object>();
	
	/**
	 * Adds the specified object to the list of listeners to this ConsoleManager.
	 * All objects on this list are notified via {@link #fireEvent(String)}
	 * when the {@link InputStreamReaderThread} reads a line.
	 * 
	 * @param listener The object which will begin listening to this ConsoleManager.
	 */
	public synchronized void addConsoleListener(ConsoleListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes the specified object from the list of listeners to this ConsoleManager.
	 * The specified object will no longer be notified when the
	 * {@link InputStreamReaderThread} reads a line.
	 * 
	 * @param listener	The object to remove from the list of listeners to this ConsoleManager.
	 */
	public synchronized void removeConsoleListener(ConsoleListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Notifies all listeners to this ConsoleManager that the
	 * {@link InputStreamReaderThread} has read a line from the system console.
	 * 
	 * @param contents	The line which was read from the system console.
	 */
	private synchronized void fireEvent(String contents)
	{
	    ConsoleEvent event = new ConsoleEvent(contents,this);
	    Iterator<Object> i = listeners.iterator();
	    while(i.hasNext())
	    {
	      ((ConsoleListener) i.next()).userInput(event);
	    }
	}
	
	/**
	 * Structures the listening behavior for objects listening to a
	 * {@link ConsoleManager}.
	 * 
	 * @author Nius Atreides (Nicholas Harrell)
	 */
	public interface ConsoleListener
	{
		/**
		 * This method is called when a {@link ConsoleManager} executes its
		 * {@link ConsoleManager#fireEvent(String)} method.
		 * 
		 * @param e		An event object representing the transaction.
		 */
		public void userInput(ConsoleEvent e);
	}
	
	/**
	 * A simple extension of a basic {@link java.util.EventObject}, facilitating standardized
	 * event data for time-driven inter-object transactions.
	 * 
	 * @author Nius Atreides (Nicholas Harrell)
	 */
	public class ConsoleEvent extends java.util.EventObject
	{
		/** Generic serial version ID */
		private static final long serialVersionUID = 1L;
		
		/** The contents of the system console input relevant to the firing of this Event. */
		public final String CONTENTS;
		
		/**
		 * Creates a new default {@link java.util.EventObject} object extended to
		 * ConsoleManagerEvent, carrying the specified {@link String} as
		 * the contents of the relevant system console input and the specified
		 * object as the caller.
		 * 
		 * @param contents	The contents of the relevant system console input line.
		 * @param source	The object reference to carry as the caller.
		 */
		public ConsoleEvent(String contents,Object source)
		{
			super(source);
			CONTENTS = contents;
		}
		
		/**
		 * Returns the caller of this event as, specifically, a {@link ConsoleManager} object.
		 */
		@Override
		public ConsoleManager getSource()
		{
			return (ConsoleManager)source;
		}
	}
}
