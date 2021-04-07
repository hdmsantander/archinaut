package mx.uam.archinaut.data.nameprocessing;

import java.util.ArrayList;

/**
 * Removes a prefix from an arraylist of element names
 * 
 * @author humbertocervantes
 *
 */
public class PrefixRemovalNameProcessor extends NameProcessor {
	
	String prefix;
	
	/**
	 * Constructor that receives the prefix to remove
	 * 
	 * @param prefix
	 */
	public PrefixRemovalNameProcessor(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Removes the prefixes
	 * 
	 */
	protected ArrayList<String> performProcessing(Iterable<String> name) {
		ArrayList <String> results = new ArrayList <> ();	
		
		
		
		for(String element:name) {
			
			if(element.startsWith(prefix)) {
				results.add(element.substring(prefix.length()));
			} else {
				results.add(element);
			}
		}		
		return results;
		
	}

}
