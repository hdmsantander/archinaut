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
package mx.uam.archinaut.data.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.MatrixDependencyGroup;
import mx.uam.archinaut.model.MatrixElement;

/**
 * 
 * Loader for JSON files produced by depends (version 0.9.4)
 * https://github.com/multilang-depends
 * 
 * @author humbertocervantes
 *
 */
@Slf4j
@Component
public class DesignStructureMatrixLoader {
		
	/**
	 * This method loads a file produced by depends using the following option
	 * java -jar depends.jar -s -p dot -d outputData java originalData/myproject/src dependency
	 * and returns a DesignStructureMatrix
	 * 
	 * @param filename the path of the file
	 * @return a DesignStructureMatrix
	 */
	public DesignStructureMatrix loadFromJSON (String name, String filename, NameProcessor processor) {
		
		ArrayList <MatrixElement> elements = new ArrayList<>();
		
        try  {
        	
    		log.info("Loading JSON depends output from: ",filename);
    		
    		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        	
        	Reader reader = new InputStreamReader(is);
        	
            // Convert JSON File to Java Object			
        	JsonReader jsonReader = new JsonReader(reader);
        	
            jsonReader.beginObject();
            
            // consume "schemaVersion" : "1.0" 
            jsonReader.nextName();
            jsonReader.nextString();

            // consume "name" : "dependency-sdsm" 
            jsonReader.nextName();
            jsonReader.nextString();

            jsonReader.nextName(); // consume "variables"
            jsonReader.beginArray();

            // filenames
            JsonToken token = jsonReader.peek();

            while(token!=JsonToken.END_ARRAY) {
                String value = jsonReader.nextString();
                value = processor.processName(value);
                
                elements.add(new MatrixElement(value)); // consume file name
                token = jsonReader.peek();
            }                     
            
            jsonReader.endArray();
            
            jsonReader.nextName(); // consume "cells"
            
            jsonReader.beginArray();

            token = jsonReader.peek();
            
            while(token!=JsonToken.END_ARRAY) {
            	jsonReader.beginObject();
            	
                jsonReader.nextName(); // consume "src"
                int row = jsonReader.nextInt();

                jsonReader.nextName(); // consume "dest"
                int column = jsonReader.nextInt();

                jsonReader.nextName(); // consume "values"
            	jsonReader.beginObject();

    			MatrixElement source = elements.get(row);
    			MatrixElement destination = elements.get(column);
    			MatrixDependencyGroup dependencies = new MatrixDependencyGroup(source,destination);
            	source.addDependency(dependencies);
            	
            	while(token!=JsonToken.END_OBJECT) {
            		            		
                    String dependencyType = jsonReader.nextName(); // consume dependency type
                    int intValue = jsonReader.nextInt(); // consume value

                    dependencies.addDependency(dependencyType, intValue);
                                	
            		token = jsonReader.peek();
            	}
            	
            	jsonReader.endObject();

            	jsonReader.endObject();
            	token = jsonReader.peek();
            }
            
            jsonReader.endArray();
            jsonReader.endObject();
            
            jsonReader.close();

            
        } catch (IOException ex) {
        	
            log.error("DesignStructureMatrixLoader.loadFromJSON: ",ex);
            return null;
        }
        
        DesignStructureMatrix matrix = new DesignStructureMatrix(name, filename, elements);
        
        return matrix;        
	}

	
}
