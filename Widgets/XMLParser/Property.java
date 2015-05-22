package XMLParser;
/* Property
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

/**
 * Encapsulates an attribute of a {@link Node} in a DOM.
 * <p>
 * Each property consists of two Strings; one for the
 * property name and one for the property value.
 * 
 * @author Nius Atreides (Nicholas Harrell)
 */
public class Property
{
	/** The name of this Property. */
	public final String NAME;
	
	/** The value of this property. */
	public final String VALUE;
	
	/**
	 * Returns a new Property object with the specified
	 * name and value.
	 * 
	 * @param name	The name of this property.
	 * @param value	The value of this property.
	 */
	public Property(String name, String value)
	{
		NAME = name;
		VALUE = value;
	}
}
