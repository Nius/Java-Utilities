/* Initializer
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

package engine;

/**
 * The entry point of the RotatingBackup program.
 * Sets up an {@link Engine} object which handles
 * all functionality operations.
 * <p>
 * Handles all meta-initialization operations.
 * 
 * @author Nius Atreides (Nicholas Harrell)
 */
public class Initializer
{
	/**
	 * Begins program execution.
	 * <p>
	 * Primary Objective: Set up an {@link Engine} object,
	 * which will handle all program functionality.
	 * 
	 * @param args System execution arguments. None are used.
	 */
	public static void main(String[] args)
	{
		System.out.println("Initializer started.");
		Engine ENG = new Engine();
		ENG.load();
		ENG.CM.beginListening();		
		ENG.startClock();
	}

}