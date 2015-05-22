package XMLParser;
/* XMLParserException
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

/**
 * A simple renaming of a standard {@link Exception}.
 * 
 * @author	Nius Atreides (Nicholas Harrell)
 */
@SuppressWarnings("serial")
public class XMLParserException extends Exception
{
	/**	An Exception of some other kind to which this Exception refers. */
	protected Throwable E;
	
	/**
	 * Returns a new XMLParserException with the specified message.
	 * 
	 * @param message	The message which this {@link Exception} will carry.
	 */
	public XMLParserException(String message)
	{
		super(message);
	}
	
	/**
	 * Returns a new XMLParserException with the specified message,
	 * and the specified Exception as the cause of this one.
	 * 
	 * @param message	The message which this {@link Exception} will carry.
	 * @param e			The Exception which caused this one.
	 */
	public XMLParserException(String message,Exception e)
	{
		super(message,e);
	}
}
