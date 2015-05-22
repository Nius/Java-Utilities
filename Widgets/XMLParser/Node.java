package XMLParser;
/* Node
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */



import java.util.ArrayList;

/**
 * Encapsulates a node in a DOM.
 * <p>
 * Eaach node is fully equipped with a reference to its parent and
 * references to its children. Each node comes complete with a range
 * of methods for accessing, parsing, and otherwise handling the DOM
 * tree around, above, and below it.
 * 
 * @author Nius Atreides (Nicholas Harrell)
 */
public class Node
{
	/** A simple literal denoting the type, or name, of this node.
		This is not intended to be a unique identifier.				*/
	public final String TYPE;
	
	/** The list of {@link Property} objects representing this Node's
		attributes, or properties. */
	protected ArrayList<Property> PROPERTIES = new ArrayList<Property>();
	
	/** The list of {@link Node} objects representing this Node's
		children in the DOM. */
	protected ArrayList<Node> CHILDREN = new ArrayList<Node>();
	
	/** A reference to this Node's parent Node in the DOM. */
	public final Node PARENT;
	
	/** A literal containing all non-node inner text of this Node. */
	protected String looseInner = "";
	
	/**
	 * Returns a Node object having the specified type and parent.
	 * 
	 * @param type		The name or type of this Node.
	 * 					This is not intended to be a unique identifier.
	 * @param parent	The Node which is the parent of this one.
	 */
	public Node(String type, Node parent)
	{
		TYPE = type;
		PARENT = parent;
	}
	
	//
	//	STATIC
	//
	
	/**
	 * Print all children of the specified Node.
	 * 
	 * @param subject	The Node for which to print the child tree.
	 * @param prefix	Every line printed here will be prefixed with
	 * 					the <i>prefix</i> literal.
	 */
	public static void printChildren(Node subject, String prefix)
	{		
		for(Property i : subject.getProperties())
			System.out.println(prefix + "PROPERTY: " + i.NAME + " = " + i.VALUE);
		
		System.out.println(prefix + "INNER: " + subject.getLooseInner());
		
		for(Node i : subject.getChildren())
		{
			System.out.println(prefix + "NODE: " + i.TYPE);
			printChildren(i,prefix + "\t");
		}
	}
	
	//
	//	SELF
	//
	
	/**
	 * Returns all of the non-node inner text of this Node.
	 * 
	 * @return	A String containing all of the non-node inner text of this Node.
	 */
	public String getLooseInner()
	{
		return looseInner;
	}
	
	/**
	 * Appends the provided text to the inner text of this Node.
	 * Note that newly appended text will NOT be parsed as XML.
	 * 
	 * @param inner	The text to append to the inner text of this Node.
	 */
	public void appendLooseInner(String inner)
	{
		looseInner += inner;
	}
	
	/**
	 * Determines whether this Node is of a given type. Type names are
	 * not case-sensitive.
	 * 
	 * @param type	The name of the Node type to check for.
	 * @return		<code>true</code> if this Node's type name is
	 * 				case-insensitively identical to the provided type name.
	 * 				<br>
	 * 				<code>false</code> if this Node's type name does not
	 * 				case-insensitively match the provided type name.
	 */
	public boolean isType(String type)
	{
		return type.equalsIgnoreCase(TYPE);
	}
	
	/**
	 * Appends a new {@link Property} object to the list of Properties
	 * belonging to this node. The <i>name</i> and <i>value</i> arguments
	 * are not processed in any way and are directly passed to a new
	 * Property object.
	 * 
	 * @param name	The name of the new property.
	 * @param value	The value of the new property.
	 */
	public void addProperty(String name, String value)
	{
		PROPERTIES.add(new Property(name,value));
	}
	
	/**
	 * Appends a given {@link Property} object to the list of Properties
	 * belonging to this Node.
	 * 
	 * @param property	The Property object to add to the list.
	 */
	public void addProperty(Property property)
	{
		PROPERTIES.add(property);
	}
	
	/**
	 * Returns the first {@link Property} belonging to this Node by the
	 * specified name. The name is case-insensitive.
	 * Returns <code>null</code> if no such property exists.
	 * 
	 * @param name	The name of the Property to seek.
	 * @return	The first Property object with the specified name, or
	 * 			<code>null</code> if no such Property exists.
	 */
	public Property getProperty(String name)
	{
		for(Property i : PROPERTIES)
			if(i.NAME.equalsIgnoreCase(name))
				return i;
		
		return null;
	}
	
	/**
	 * Retrieves all of this Node's Properties.
	 * 
	 * @return	A copy of this Node's array of {@link Property} objects.
	 */
	public Property[] getProperties()
	{
		Property[] properties = new Property[PROPERTIES.size()];
		for(int i = 0; i < PROPERTIES.size(); i ++)
			properties[i] = PROPERTIES.get(i);
		return properties;
	}
	
	//
	//	DOM
	//
	
	/**
	 * Appends the specified Node to the end of this Node's list of child
	 * Nodes.
	 * 
	 * @param	childNode	The Node to append to this Node's list of child
	 * 						Nodes.
	 */
	public void addChild(Node childNode)
	{
		CHILDREN.add(childNode);
	}
	
	/**
	 * Retrieves all of this Node's child Nodes.
	 * 
	 * @return	A copy of this Node's array of child Nodes.
	 */
	public Node[] getChildren()
	{
		Node[] children = new Node[CHILDREN.size()];
		for(int i = 0; i < CHILDREN.size(); i ++)
			children[i] = CHILDREN.get(i);
		return children;
	}
	
	/**
	 * Retrieves all of this Node's child Nodes of the specified
	 * type.
	 * 
	 * @param type	The type of Node to return.
	 * @return	A list of this Node's child Nodes of the specified
	 * 			type.
	 */
	public Node[] getChildrenOfType(String type)
	{
		ArrayList<Node> children = new ArrayList<Node>();
		for(Node i : getChildren())
			if(i.TYPE.equalsIgnoreCase(type))
				children.add(i);
		
		Node[] ret = new Node[children.size()];
		for(int i = 0; i < ret.length; i ++)
			ret[i] = children.get(i);
		return ret;
	}
	
	/**
	 * Retrieves the first child Node of this Node of the specified
	 * type.
	 * 
	 * @param	type	The type of Node to return.
	 * @return	A single Node, the first child of this Node whose
	 * 			type matches the specified type. Returns
	 * 			<code>null</code> if no such Node is found.
	 */
	public Node getChildOfType(String type)
	{
		for(Node i : CHILDREN)
			if(i.TYPE.equalsIgnoreCase(type))
				return i;
		return null;
	}
	
	//
	//	UTILITY
	//
	
	@Override
	public String toString()
	{
		String ret = "<" + TYPE + " ";
		for(Property i : PROPERTIES)
			ret += i.NAME + "=\"" + i.VALUE + "\" ";
		ret += ">" + looseInner + "</" + TYPE + ">";
		
		return ret;
	}
	
	/**
	 * Build an array of {@link String} objects providing a printable representation
	 * of the Node tree. Each child node is cumulatively indented by 4 spaces.
	 * 
	 * @return	An array of {@link String} objects providing a printable representation
	 * 			of the Node tree.
	 */
	public String[] toTreeStringArray()
	{
		ArrayList<String> lines = new ArrayList<String>();
		buildTreeStringArray(lines,"");
		
		String[] ret = new String[lines.size()];
		for(int i = 0; i < lines.size(); i ++)
			ret[i] = lines.get(i);
		
		return ret;
	}
	
	/**
	 * Construct the printable Node tree returned in {@link #toTreeStringArray()} by adding
	 * <code>this</code>.{@link #toString()} to the array and instructing each child
	 * to do the same but with 4 spaces prefixed.
	 * 
	 * @param lines		The array into which to build printable lines.
	 * @param prefix	The cumulating prefix of white spaces, serving as indentation.
	 */
	protected void buildTreeStringArray(ArrayList<String> lines,String prefix)
	{
		lines.add(prefix + toString());
		for(Node i : CHILDREN)
			i.buildTreeStringArray(lines,"    ");
	}
}
