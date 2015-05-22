package XMLParser;
/* XMLParser
 * Nicholas Harrell, 10 March 2014
 * For general use and modification, provided
 * 	that the name and date here remain attached.
 */

//	Lines preceded with /* DEBUG */// have been left in this file
//	because they may be useful in modifying this code.

import java.util.ArrayList;

/**
 * The XMLParser reads an XML file and constructs a heirarchy
 * of objects representing the DOM therein.
 * <p>
 * When instantiated (with a path), the XMLParser will parse
 * an XML file at that path. The parser will construct a
 * heirarchy of Node objects representing the DOM tree,
 * which is subsequently accessible via {@link #getRootNode()}.
 * <p>
 * The entire XML file is parsed upon instantiation of the
 * XMLParser object. Thus, every XMLParser object comes
 * complete with all of the object instantiations and
 * links it requires in order to do its job.
 * Parsing an XML file is now a one-line task.
 * <p>
 * The parser will throw an {@link XMLParserException} if it
 * encounters a problem; that exception contains a brief summary
 * of the issue, and the string literal in which the problem occurred.
 * Line numbers are not included in error messages because the
 * parser does not retain the original line structure of the XML
 * file while parsing.
 * <p>
 * XMLParser will ignore block comments denoted by <code>&lt;!-- --&gt;</code>
 * and any line begining with a hash ('<code>#</code>').
 * 
 * @author	Nius Atreides (Nicholas Harrell)
 */
public class XMLParser
{	
	//
	//	GLOBAL VARIABLES
	//
	
	/** The absolute, unmodified contents as read line-by-line of the XML file. */
	protected final String[] SOURCE;
	
	/** A modified version of SOURCE, treated to make parsing easier.
		These treatments are detailed in void buildNodeTree(). */
	protected ArrayList<String> CLEAN = new ArrayList<String>();
	
	/** The root node, of which all nodes are children, and by
		which the tree constructed herein is accessed. */
	protected final Node ROOT = new Node("~ROOT",null);
	
	//
	//	METHODS
	//
	
	/**
	 * Returns an XMLParser object.
	 * <br>
	 * Instantiating in this way will automatically parse the XML
	 * document at the specified path and construct an object
	 * heirarchy representing the DOM. This heirarchy can be
	 * accessed via {@link #getRootNode()}.
	 * <br>
	 * A permanent, unaltered copy of the contents of the file
	 * read by the parser is kept and can be accessed by means
	 * of {@link #getSource()}.
	 * 
	 * @param	path	A string containing the complete filepath of the XML document to parse.
	 * @param	verbose	<code>true</code> prints the source file before processing, and the node
	 * 					tree afterwards.
	 * 					<code>false</code> prints nothing.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	public XMLParser(String path,boolean verbose) throws XMLParserException
	{
		FileLoader FL;
		try
		{
			 FL = new FileLoader(path);
			 SOURCE = FL.getLines();
			 buildNodeTree(verbose);
		}
		catch(Exception e)
		{
			throw new XMLParserException("Failed to load the specified file.",e);
		}
	}
	
	/**
	 * Returns an XMLParser object.
	 * <br>
	 * Instantiating in this way will automatically parse the XML
	 * document at the specified path and construct an object
	 * heirarchy representing the DOM. This heirarchy can be
	 * accessed via {@link #getRootNode()}.
	 * <br>
	 * A permanent, unaltered copy of the contents of the file
	 * read by the parser is kept and can be accessed by means
	 * of {@link #getSource()}.
	 * 
	 * @param	path	A string containing the complete filepath of the XML document to parse.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	public XMLParser(String path) throws XMLParserException
	{
		this(path,false);
	}
	
	/**
	 * Returns a blank, nonfunctional XMLParser object.
	 * <br>
	 * This blank constructor exists only so that implementors can
	 * satisfy "object may not have been initialized" issues regarding
	 * try/catch.
	 * <br>
	 * Attempting to use a blank XMLParser for anything will have
	 * undesirable effects.
	 */
	public XMLParser(){SOURCE = null;}
	
	/**
	 * Returns the root {@link Node} of the DOM heirarchy.
	 * <p>
	 * Every Node (except the root Node) possesses a reference to its
	 * parent, and references to its children.
	 * <p>
	 * Further access to the DOM is facilitated by the {@link Node} class.
	 * 
	 * @return	A Node whose {@link Node#TYPE} is "~ROOT" and whose {@link Node#PARENT} is null.
	 */
	public Node getRootNode()
	{
		return ROOT;
	}
	
	/**
	 * Returns a complete, unaltered copy of the exact contents of the file at the
	 * path specified to the XMLParser.
	 * 
	 * @return 	An array of <i>n</i> Strings, where n is the number of
	 * 			lines in the file. Each string contains exactly one line
	 * 			of the original file.
	 */
	public String[] getSource()
	{
		return SOURCE.clone();
	}
	
	/**
	 * Executes construction of the object heirarchy representing the DOM.
	 * 
	 * @param	verbose	<code>true</code> prints the source file before processing, and the node
	 * 					tree afterwards.
	 * 					<code>false</code> prints nothing.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	protected void buildNodeTree(boolean verbose) throws XMLParserException
	{		
		//
		//	VERBOSITY
		//
		
		if(verbose)
		{
			System.out.println("XMLParser: begining parsing of the following source:");
			for(String i : SOURCE)
				System.out.println(i);
			System.out.println("\n### END FILE");
			
			System.out.println("\nDEBUG INFO:");
			System.out.println("SOURCE = String[] length=" + SOURCE.length + ".");
			System.out.println("SOURCE element lengths:");
			for(int i = 0; i < SOURCE.length; i ++)
				System.out.print(SOURCE[i].length() + " ");
			System.out.println("\n");
		}
			
		//
		//	SOURCE PREPARATION
		//
		
		boolean BCmode = false;		// Whether the parser is inside a block comment
		
		//Iterate through each line...
		for(int i = 0; i < SOURCE.length; i ++)
		{
			//Skip blank lines
			if(SOURCE[i].length() == 0)
				continue;
			
			//Strip leading whitespaces
			while(SOURCE[i].charAt(0) == '\t' || SOURCE[i].charAt(0) == ' ')
				SOURCE[i] = SOURCE[i].substring(1);
			
			//Ignore blank lines
			// (lines containing only whitespace were not
			//	caught on the first blank line skip.)
			if(SOURCE[i].length() < 1)
				continue;
			
			//Ignore single-line comments
			if(SOURCE[i].charAt(0) == '#')
			{
				SOURCE[i] = "";
				continue;
			}
			
			//Strip this line of block-comment sections.
			//Any part of this line which is part of a block
			//	comment will be stricken. This includes block
			//	comments which are entirely contained in the
			//	middle of this line.
			SB BCSline = stripBC(SOURCE[i],BCmode);
			SOURCE[i] = BCSline.VAL;
			BCmode = BCSline.TF;
			
			//Ignore blank lines
			if(SOURCE[i].length() < 1)
				continue;
			
			//Strip leading whitespaces
			while(SOURCE[i].charAt(0) == '\t' || SOURCE[i].charAt(0) == ' ')
				SOURCE[i] = SOURCE[i].substring(1);
		}
		
		//Clean the source. Any line which ends in the middle of a
		//	quote or a tag is merged with the following line.
		for(String i : SOURCE){CLEAN.add(i);}
		for(int i = 0; i < CLEAN.size(); i ++)
		{
			boolean DQmode = false;
			boolean SQmode = false;
			boolean Tmode = false;
			for(int c = 0; c < CLEAN.get(i).length(); c ++)
			{
				if(CLEAN.get(i).charAt(c) == '"')
					if(!SQmode && !isEscaped(CLEAN.get(i),c))
						DQmode = !DQmode;
				
				if(CLEAN.get(i).charAt(c) == '\'')
					if(!DQmode && !isEscaped(CLEAN.get(i),c))
						SQmode = !SQmode;
				
				if(CLEAN.get(i).charAt(c) == '<')
					if(!DQmode && !SQmode)
					{
						if(Tmode)
							throw new XMLParserException("XML Parse Error: Misplaced opening caret within \"" + CLEAN.get(i) + "\".");
						else
							Tmode = true;
					}
				
				if(CLEAN.get(i).charAt(c) == '>')
					if(!DQmode && !SQmode)
					{
						if(!Tmode)
							throw new XMLParserException("XML Parse Error: Misplaced closing caret within \"" + CLEAN.get(i) + "\".");
						else
							Tmode = false;
					}
			}
			
			if(DQmode || SQmode || Tmode)
			{
				CLEAN.set(i,CLEAN.get(i) + " " + CLEAN.remove(i + 1));
				i --;
			}
		}
		
		//
		//	DOM CONSTRUCTION
		//
		
		//Begin construction of the DOM by parsing the internal
		//	contents of the ROOT object -- which is the whole
		//	file.
		parseInner(ROOT);
		
		//
		//	VERBOSITY
		//
		
		if(verbose)
		{
			System.out.println("XMLParser: Node tree construction complete: ");
			String[] output = ROOT.toTreeStringArray();
			for(String i : output)
				System.out.println(i);
			System.out.println("\n\n### END NODE TREE");
		}
		
	}
	
	/** Used to keep track of the current
		position of the parser in the contents of the file. In this
		way {@link #parseInner(Node)} can freely recurse, descending
		and backwards-ascending the DOM heirarchy freely as it reads
		the XML linearly. */
	protected int lineIndex = 0;
	/**
	 * Continues parsing the remaining file contents, using the specified
	 * node as the working parent of any newly encountered nodes or loose
	 * text.
	 * 
	 * @param	parent	A Node object of which new nodes will be children.
	 * @throws	XMLParserException	If the parser cannot find the closing
	 * 								carret for a given object, or if it
	 * 								finds a closing tag which does not
	 * 								match the immediately preceding opening
	 * 								tag, it will throw an exception.
	 */
	public void parseInner(Node parent) throws XMLParserException
	{		
		//Search through all lines...
		for(; lineIndex < CLEAN.size(); lineIndex ++)
		{
			/* DEBUG *///System.out.println("Processing line \"" + CLEAN.get(lineIndex) + "\".");
			
			//Get the position (if any) of the opening tag for an object on this line.
			int opos = getQSensitiveIndexOf('<',CLEAN.get(lineIndex),true);
			
			//If this line has no object tags, append the entire line to
			//	the loose-inner string of the current parent object.
			if(opos == -1)
			{
				parent.appendLooseInner(CLEAN.get(lineIndex));
				continue;
			}
			
			//If this line has an object tag, extract the entire object declaration.
			int epos = getQSensitiveIndexOf('>',CLEAN.get(lineIndex),true);
			if(epos == -1)
				throw new XMLParserException("XML Parse Error: Could not find closing caret within \"" + CLEAN.get(lineIndex) + "\".");			
			
			NB result = constructNode(CLEAN.get(lineIndex).substring(opos + 1,epos),parent);
			
			//Insert preceding text on this line as object loose-inner text.
			parent.appendLooseInner(CLEAN.get(lineIndex).substring(0,opos));
			
			//Insert remaining text on this line as the next line in the CLEAN array.
			CLEAN.add(lineIndex + 1,CLEAN.get(lineIndex).substring(epos + 1));
			
			//Detect whether this is a closing tag.
			if(result.NODE.TYPE.charAt(0) == '/')
				if(result.NODE.TYPE.equalsIgnoreCase("/" + parent.TYPE))
				//The parent is being closed, so Return, stepping up one level in the DOM.
					return;
				else
				//Some random tag, not the parent, is being closed.
					throw new XMLParserException("XML Parse Error: Bad closing tag \"" + result.NODE.TYPE + "\" in \"" + CLEAN.get(lineIndex) + "\".");
			
			//Add the new object to the parent node.
			parent.addChild(result.NODE);
			
			//If the new object is self-terminating, process
			//	the remainder under the same (current) parent.
			if(result.TF)
				continue;
			
			//If the new object is NOT self-terminating, process
			//	the remainder under the new node.
			lineIndex ++;
			parseInner(result.NODE);
		}
	}
	
	/**
	 * Builds a {@link Node} object from a {@link String} containing an XML
	 * object tag. The literal should meet these requirements:
	 * <ul>
	 * 	<li>The literal contains only the contents of the opening object tag.
	 * 	<li>The literal does not contain the carets of the object tag.
	 * 	<li>The literal may contain a closing slash if necessary.
	 * </ul>
	 * <p>
	 * The following are examples of valid source literals:
	 * <ul>
	 * <li><code>Essay type="term" author='George Winston'</code>
	 * <li><code>Piano color="black" /</code><br>
	 * <li><code>Toaster</code>
	 * <li><code>Blender /</code>
	 * <li><code>Dishwasher speed='64E25565 RPM' /</code>
	 * </ul>
	 * 
	 * @param source	The literal to parse as a Node and its properties.
	 * @param parent	The Node to which to attach this new Node as a child,
	 * 					and to which this Node will refer as its parent.
	 * @return	An {@link NB} which contains the new Node and a boolean representing
	 * 			whether the new node is self-terminating.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	public NB constructNode(String source,Node parent) throws XMLParserException
	{
		/* DEBUG *///System.out.println("Building node from source string: `" + source + "`.");
		
		ArrayList<String> tokens = new ArrayList<String>();
		
		//Move the parser caret to the first non-space character
		//	in the source string.
		int FNS = getNextNonSpace(source,0);
		
		/* DEBUG *///System.out.println("First non-space of source: " + FNS);
		
		for(int i = FNS; i < source.length();)
		{
			/* DEBUG *///System.out.println("\ti = " + i);
			
			//If the parser is just now begining with the new object,
			//	do not expect an equals sign in the first token.
			//Else, read to the first non-quote space after the
			//	first block of text after the equals sign.
			int nextSpace = (i == 0) ? getNextSpace(source,0) : getNextSpace(source,getNextNonSpace(source,getQSensitiveIndexOf('=',source,i,false) + 1));
			
			/* DEBUG *///System.out.println("\tNextSpace = " + nextSpace);
			
			//If the next space could not be determined, either because
			//	a bad property was read (with no equals sign) or because
			//	there are no remaining properties, read to the end of the
			//	source string for the next property.
			if(nextSpace == -1)
				nextSpace = source.length();
			
			/* DEBUG *///System.out.println("\t(NextSpace = " + nextSpace + ")");
			
			//Add this token to the list of properties to be parsed.
			tokens.add(source.substring(i,nextSpace));
			
			/* DEBUG *///System.out.println("\tAdded `" + source.substring(i,nextSpace) + "`");
			
			//Move the parser caret to the next non-space after the token
			//	it just parsed.
			i = getNextNonSpace(source,nextSpace);
			
			/* DEBUG *///System.out.println("\ti = " + i);
			/* DEBUG *///System.out.println("================================");
		}
		
		//Create the new node.
		Node newNode = new Node(tokens.get(0),parent);
		
		//Parse each property.
		for(int i = 1; i < tokens.size(); i ++)
			if(!tokens.get(i).equals("/"))
				newNode.addProperty(constructProperty(tokens.get(i)));
		
		//Return the new node, and whether it is self-terminating.
		return new NB(newNode,source.charAt(source.length() - 1) == '/');
	}
	
	/**
	 * Builds a {@link Property} object from a {@link String} containing valid
	 * syntax for specifying XML object attributes.
	 * <p>
	 * The literal may contain leading and trailing whitespaces, as well
	 * as whitespaces between the equals sign and the attribute's name and
	 * value.
	 * 
	 * TODO: Add support for self-evident attributes, such as &lt;SomeElement hidden&gt;.
	 * 
	 * @param	source	The literal to parse as a Property.
	 * @return	A Property fully endowed with name and data-value.
	 * @throws	XMLParserException	If the parser encounters a property which has
	 * 								bad quotation marks or no equals sign, it will
	 * 								throw an exception.
	 */
	public Property constructProperty(String source) throws XMLParserException
	{
		if(source.indexOf("=") == -1)
			throw new XMLParserException("XML Parse Error: malformed property in \"" + source + "\".");
		
		//Divide the property by the first equals sign.
		String[] tokens = source.split("=",2);
		
		//Strip leading and trailing whitespaces from both tokens.
		tokens[0] = tokens[0].trim();
		tokens[1] = tokens[1].trim();
		
		//Strip quotes from the property value.
		if(tokens[1].charAt(0) == '"' || tokens[1].charAt(0) == '\'')
			if(tokens[1].charAt(tokens[1].length() - 1) == tokens[1].charAt(0))
				tokens[1] = tokens[1].substring(1,tokens[1].length() - 1);
			else
				throw new XMLParserException("XML Parse Error: malformed property value in \"" + source + "\".");
		
		//Remove escaping forward-slashes from the property value.
		for(int i = 0; i < tokens[1].length(); i ++)
			if(tokens[1].charAt(i) == '\\' && !isEscaped(tokens[1],i))
				tokens[1] = tokens[1].substring(0,i) + tokens[1].substring(i + 1);
		
		//Build and return the new property.
		return new Property(tokens[0],tokens[1]);
	}
	
	//
	//	UTILITY
	//
	
	/**
	 * Returns whether the character at the specified index
	 * in the given string is escaped.
	 * <p>
	 * The parser will first read the character at the given
	 * index. That character is escaped if the immediately
	 * preceding character is a forward slash AND the character
	 * immediately before that is not a forward slash.
	 * 
	 * @param	subject	The string in which to check the character
	 * 					at the given index.
	 * @param	index	The index of the character to check.
	 * @return	<code>true</code> if the character at the given
	 * 			index is preceded by exactly one forward slash.
	 * 			<code>false</code> if the character at the given
	 * 			index is not preceded by exactly one forward slash.
	 */
	protected boolean isEscaped(String subject,int index)
	{
		try
		{
			if(subject.charAt(index - 1) == '\\')
			{
				try
				{
					if(subject.charAt(index - 2) == '\\')
						return false;
					return true;
				}
				catch(IndexOutOfBoundsException e)
				{
					return true;
				}
			}
		}
		catch(IndexOutOfBoundsException e)
		{
			return false;
		}
		return false;
	}
	
	/**
	 * Returns the position of the next white-space in the provided string
	 * after index <i>rel</i>. If the character at index <i>rel</i> is a
	 * white-space character, then <i>rel</i> is returned.
	 * <p>
	 * If no white-space could be found after index <i>rel</i> then
	 * <code>-1</code> is returned.
	 * <p>
	 * Searching for whitespaces is done according to {@link #getQSensitiveIndexOf(char, String, boolean)}
	 * and is therefore quote-sensitive. Whitespaces inside a quotation will not be matched.
	 * 
	 * @param	haystack	The literal in which to scan for white-spaces.
	 * @param	rel			The minimum index at which a white-space will be
	 * 						recognized.
	 * @return	An <code>int</code> specifying the position of the next
	 * 			white-space character after index <i>rel</i>.
	 * 			<p>
	 * 			If no white-space could be found after index <i>rel</i> then
	 * 			<code>-1</code> is returned.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	protected int getNextSpace(String haystack,int rel) throws XMLParserException
	{
		/* DEBUG *///System.out.println("\t\tSearching for next space in `" + source + "` begining at rel = " + rel);
		/* DEBUG *///System.out.println("\t\tQSI of ` `  in `" + source.substring(rel) + "` = " + getQSensitiveIndexOf(' ',source.substring(rel),false));
		/* DEBUG *///System.out.println("\t\tQSI of `\\t` in `" + source.substring(rel) + "` = " + getQSensitiveIndexOf('\t',source.substring(rel),false));
		/* DEBUG *///System.out.println("\t\tQSI of `\\n` in `" + source.substring(rel) + "` = " + getQSensitiveIndexOf('\n',source.substring(rel),false));
		/* DEBUG *///System.out.println("\t\tQSI of `\\r` in `" + source.substring(rel) + "` = " + getQSensitiveIndexOf('\r',source.substring(rel),false));
		/* DEBUG *///System.out.println("\t\tQSI of `\\f` in `" + source.substring(rel) + "` = " + getQSensitiveIndexOf('\f',source.substring(rel),false));
		
		int nextSpace = getMinPositive( new int[]{	getQSensitiveIndexOf(' ', haystack.substring(rel), false),
													getQSensitiveIndexOf('\t', haystack.substring(rel), false),
													getQSensitiveIndexOf('\n', haystack.substring(rel), false),
													getQSensitiveIndexOf('\r', haystack.substring(rel), false),
													getQSensitiveIndexOf('\f', haystack.substring(rel), false)		});
		
		/* DEBUG *///System.out.println("\t\t\tSelected " + nextSpace);
		
		return (nextSpace == -1) ? -1 : nextSpace + rel;
	}
	
	/**
	 * Returns the position of the next non white-space in the provided string
	 * after index <i>rel</i>. If the character at index <i>rel</i> is not a
	 * white-space character, then <i>rel</i> is returned.
	 * <p>
	 * If no non white-space could be found after index <i>rel</i> then
	 * <code>-1</code> is returned.
	 * <p>
	 * Searching for whitespaces is done according to {@link #getQSensitiveIndexOf(char, String, boolean)}
	 * and is therefore quote-sensitive. Whitespaces inside a quotation will not be matched.
	 * 
	 * @param haystack	The literal in which to scan for non white-spaces.
	 * @param rel		The minimum index at which a non white-space will be
	 * 					recognized.
	 * @return	An <code>int</code> specifying the position of the next
	 * 			non white-space character after index <i>rel</i>.
	 * 			<p>
	 * 			If no non white-space could be found after index <i>rel</i> then
	 * 			<code>-1</code> is returned.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	protected int getNextNonSpace(String haystack, int rel) throws XMLParserException
	{
		/* DEBUG *///System.out.println("\t\tSearching for NNS in `" + source + "` begining at rel = " + rel);
		
		int nextSpace = getNextSpace(haystack,rel);
		
		/* DEBUG *///System.out.println("\t\t\tNext Space = " + nextSpace);
		
		if(nextSpace == rel)
		{
			/* DEBUG *///System.out.println("\t\t\tTrying NNS search again...");
			return getNextNonSpace(haystack, rel + 1);
		}
		else
		{
			/* DEBUG *///System.out.println("\t\t\tNNS = " + rel);
			return rel;
		}
	}
	
	/**
	 * Returns the value of the smallest positive integer in the provided
	 * integer array.
	 * <p>
	 * If no positive integers are present in the array, then <code>-1</code>
	 * is returned.
	 * 
	 * @param list	An array of integers.
	 * @return	The smallest positive integer in the <i>list</i> array of
	 * 			integers, or <code>-1</code> if no positive integers are
	 * 			present.
	 */
	protected int getMinPositive(int[] list)
	{
		int ret = -1;
		for(int i = 0; i < list.length; i ++)
			if(list[i] >= 0 && (list[i] < ret || ret == -1))
				ret = list[i];
		return ret;
	}
	
	/**
	 * A simple construct for passing a {@link Node} and a related boolean
	 * in a single object reference.
	 */
	protected class NB
	{
		/**
		 * A simple construct for passing a {@link Node} and a related boolean
		 * in a single object reference.
		 * 
		 * @author	Nius Atreides
		 */
		
		/** The Node carried by this NB. */
		public final Node NODE;
		
		/** The boolean carried by this NB. */
		public final boolean TF;
		
		/**
		 * Returns a new NB object referring to the specified {@link Node} and
		 * containing the specified boolean value.
		 * 
		 * @param node	The Node to which to refer.
		 * @param bool	The boolean value to carry with the Node.
		 */
		public NB(Node node, boolean bool)
		{
			NODE = node;	TF = bool;
		}
	}
	
	/**
	 * Splits a string into an ArrayList, by the specified splitter, except for
	 * instances of that splitter which occur inside of quotes.
	 * 
	 * @param target	The String to split.
	 * @param splitter	The char by which to split the string.
	 * @return	An ArrayList of Strings, of lenght <i>n + 1</i> where <i>n</i> is
	 * 			the number of occurrences of <i>splitter</i> in <i>target</i>.
	 * @throws	XMLParserException	If the parser encounters trouble parsing the XML document
	 * 								then it will throw an exception containing a brief
	 * 								description of the problem, and the string literal in
	 * 								which the problem occurred.
	 */
	protected ArrayList<String> QSensitiveSplit(String target, char splitter) throws XMLParserException
	{
		ArrayList<String> tokens = new ArrayList<String>();
		
		int pos = getQSensitiveIndexOf(splitter, target, false);
		if(pos == -1)
		{
			tokens.add(target);
			return tokens;
		}
		
		tokens.add(target.substring(0,pos));
		tokens.addAll(QSensitiveSplit(target.substring(pos + 1),splitter));
		return tokens;
	}
	
	/**
	 * Searches the String <i>haystack</i> for the first occurrence of the
	 * <code>char</code> <i>needle</i> at or after index <i>min</i>. Matches
	 * which are inside quotes (single or double) are ignored.
	 * 
	 * @param	needle		The character for which to search.
	 * @param	haystack	The String in which to search for <code>char</code>
	 * 						<i>needle</i>.
	 * @param 	min			The minimum index at which a match can be made.
	 * @param	checkCarets If <i>checkCarets</i> is true, then the <i>haystack</i>
	 * 						will be considered an XML literal, and an
	 * 						{@link XMLParserException} will be thrown if a misplaced
	 * 						caret is encountered.
	 * @return	The first non-quoted occurrence of the <code>char</code> <i>needle</i>
	 * 			in <i>haystack</i> after index <i>min</i>.
	 * @throws	XMLParserException	If the parser encounters an opening caret while
	 * 								reading an object declaration, or a closing caret
	 * 								while not reading an object declaration, an
	 * 								XMLParserException will be thrown.
	 * 								<p>
	 * 								Some examples of exceptional code:
	 * 								<ul>
	 * 								<li>&lt;Dog name="doge" type="derp" /&gt;&gt;
	 * 								<li>SomeLooseText.... &lt; Blah Blah &lt;Marker /&gt;
	 * 								</ul>
	 */
	protected int getQSensitiveIndexOf(char needle, String haystack, int min, boolean checkCarets) throws XMLParserException
	{
		return min + getQSensitiveIndexOf(needle,haystack.substring(min),checkCarets);
	}
	
	/**
	 * Searches the String <i>haystack</i> for the first occurrence of the
	 * <code>char</code> <i>needle</i>. Matches which are inside quotes
	 * (single or double) are ignored.
	 * 
	 * @param	needle		The character for which to search.
	 * @param	haystack	The String in which to search for <code>char</code>
	 * 						<i>needle</i>.
	 * @param	checkCarets	If <i>checkCarets</i> is true, then the <i>haystack</i>
	 * 						will be considered an XML literal, and an
	 * 						{@link XMLParserException} will be thrown if a misplaced
	 * 						caret is encountered.
	 * @return	The first non-quoted occurrence of the <code>char</code> <i>needle</i>
	 * 			in <i>haystack</i>.
	 * @throws	XMLParserException	If the parser encounters an opening caret while
	 * 								reading an object declaration, or a closing caret
	 * 								while not reading an object declaration, an
	 * 								XMLParserException will be thrown.
	 * 								<p>
	 * 								Some examples of exceptional code:
	 * 								<ul>
	 * 								<li>&lt;Dog name="doge" type="derp" /&gt;&gt;
	 * 								<li>SomeLooseText.... &lt; Blah Blah &lt;Marker /&gt;
	 * 								</ul>
	 */
	protected int getQSensitiveIndexOf(char needle, String haystack, boolean checkCarets) throws XMLParserException
	{
		boolean DQmode = false;
		boolean SQmode = false;
		boolean Tmode = false;
		for(int c = 0; c < haystack.length(); c ++)
		{
			if(haystack.charAt(c) == '"')
				if(!SQmode && !isEscaped(haystack,c))
				{
					if(needle == '"')
						return c;
					DQmode = !DQmode;
				}
			
			if(haystack.charAt(c) == '\'')
				if(!DQmode && !isEscaped(haystack,c))
				{
					if(needle == '\'')
						return c;
					SQmode = !SQmode;
				}
			
			if(haystack.charAt(c) == '<')
				if(!DQmode && !SQmode)
				{
					if(Tmode && checkCarets)
						throw new XMLParserException("XML Parse Error: Misplaced opening caret within \"" + haystack + "\".");
					else
						Tmode = true;
				}
			
			if(haystack.charAt(c) == '>')
				if(!DQmode && !SQmode)
				{
					if(!Tmode && checkCarets)
						throw new XMLParserException("XML Parse Error: Misplaced closing caret within \"" + haystack + "\".");
					else
						Tmode = false;
				}
			
			if(!SQmode && !DQmode && haystack.charAt(c) == needle)
				return c;
		}
		
		return -1;
	}
	
	/**
	 * A simple construct for passing a {@link Node} and a related boolean
	 * in a single object reference. 
	 */
	protected class SB
	{
		/**
		 * A simple construct for passing a {@link Node} and a related boolean
		 * in a single object reference.
		 * 
		 * @author	Nius Atreides
		 */
		
		/** The String carried by this SB. */
		public final String VAL;
		
		/** The boolean carried by this SB. */
		public final boolean TF;
		
		/**
		 * Returns a new SB object referring to the specified {@link String} and
		 * containing the specified boolean value.
		 * 
		 * @param value	The literal String value to carry.
		 * @param bool	The boolean value to carry with the string.
		 */
		public SB(String value, boolean bool)
		{
			VAL = value;	TF = bool;
		}
	}
	
	/**
	 * Recursively strips a {@link String} of block-comments as denoted by
	 * <code><!-- --></code>.
	 * 
	 * @param	feed	The String to strip of block comments.
	 * @param	BCmode	Whether this particular call occurs in the middle
	 * 					of a block-comment.
	 * 					<p>
	 * 					This should be <code>false</code> on the initial call.
	 * @return	An {@link SB} which contains the stripped String and a boolean
	 * 			representing the final state of BCmode upon method completion.
	 */
	protected SB stripBC(String feed, boolean BCmode)
	{
		if(BCmode)
		//This string is in block comment mode...
		{
			int pos = feed.indexOf("-->");
			if(pos == -1)
			//If this string has no end-comment tag, return nothing
				return new SB("",true);
			
			//If this string has an end-comment tag, return
			//	the remainder of this string (after the tag)
			//	stripped of block comments.
			return stripBC(feed.substring(pos + 3),false);
		}
		
		//This string is not in block comment mode...
		int pos = feed.indexOf("<!--");
		if(pos == -1)
			//If this string has no begin-comment tag, return
			//	the entire string.
			return new SB(feed,false);
		
		//If this string has a begin-comment tag, return
		//	the begining of this string, with a BC-stripped
		//	version of the remainder of the string appended.
		SB result = stripBC(feed.substring(pos + 4),true);
		return new SB(feed.substring(0,pos) + result.VAL,result.TF);
	}
	
}