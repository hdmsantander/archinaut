/*
MIT License

Copyright (c) 2018-2019 Humberto Cervantes

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package mx.uam.archinaut.data.nameprocessing;

import java.util.ArrayList;
import java.util.List;


/**
 * Interface form modules that perform processing on the names of MatrixElements
 * 
 * @author humbertocervantes
 *
 */
public abstract class NameProcessor {
	private NameProcessor next = null;
	
	public NameProcessor() {
	}
	
	/**
	 * Successor for the name processor, to create chains of processing
	 * 
	 * @param next
	 */
	public NameProcessor(NameProcessor next) {
		this.next = next;
	}
	
	/**
	 * 
	 * @param next
	 */
	public void addSuccessor(NameProcessor next) {
		this.next = next;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public String processName(String name) {
		
		List <String> tempArray = new ArrayList<>() ;
		tempArray.add(name);
		tempArray = processNames(tempArray);
		return tempArray.get(0);
	}
	
	/**
	 * Process the names
	 * 
	 * @param names the elements of the matrix
	 * @return an array with the elements with their names processed
	 */
	public List <String> processNames(Iterable <String> names) {
		List <String> processedNames = performProcessing(names);
		if(next != null) {
			processedNames = next.processNames(processedNames);
		}
		
		return processedNames;
		
	}
	
	/**
	 * This abstract method is where the processing takes place. It must be implemented by
	 * specific processors
	 * 
	 * @param names  elements the elements of the matrix
	 * @return
	 */
	protected abstract List <String> performProcessing(Iterable <String> names);

}
