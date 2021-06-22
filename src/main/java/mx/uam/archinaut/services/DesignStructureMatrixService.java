package mx.uam.archinaut.services;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.stream.JsonReader;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.DesignStructureMatrixLoader;
import mx.uam.archinaut.data.loader.YamlLoader;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.data.nameprocessing.PrefixRemovalNameProcessor;
import mx.uam.archinaut.model.DependencyMetric;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.DesignStructureMatrixModel;
import mx.uam.archinaut.model.MatrixDependencyGroup;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.MatrixElementGroup;
import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;

/**
 * 
 * Service that handles business logic associated with DSMs
 * 
 * @author humbertocervantes
 *
 */

@Slf4j
@Service
public class DesignStructureMatrixService {
	
	private static final Logger log = LoggerFactory.getLogger(DesignStructureMatrixService.class);
	
	public static final String CONFIGURATION_FILENAME = "configuration.yml";
	
	@Autowired
	private DesignStructureMatrixLoader loader;
	
	@Autowired
	private MetricsService metricsService;
	
	@Autowired
	private GitLogService gitService;
	
	@Autowired
	private YamlLoader yamlLoader;

	/**
	 * Loads a matrix from a JSON file
	 * 
	 * @param filename
	 * @return
	 */
	public DesignStructureMatrix loadMatrixFromJSON(String name, String fileName, String elementPrefix, List <String> exclusions) {
		
		NameProcessor processor = new PrefixRemovalNameProcessor(elementPrefix);
		
		DesignStructureMatrix matrix = null;
		
		DesignStructureMatrix initialMatrix = loader.loadFromJSON(name, fileName, processor);
		
		if(initialMatrix == null) {
			return null;
		}
		
		if(exclusions != null && exclusions.size()>0 && initialMatrix.getFilesExtension().contentEquals("java")) {
			// Remove test files
			
			int removedTests = 0;
			int removedDependencies = 0;
			
			List <MatrixElement> elementsWithoutTests = new ArrayList <>();
			List <MatrixElement> elementsForRemoval = new ArrayList <>();
			
			// First we mark all the elements for removal
			for(MatrixElement element:initialMatrix.getElements()) {
				boolean remove = false;
				
				for(String exclusion:exclusions) {
					if(element.getFullName().contains(exclusion)) {
						remove = true;
						break;
					}
				}
				
				if(!remove) {
					elementsWithoutTests.add(element);
				} else {
					elementsForRemoval.add(element);
					removedTests ++;
				}
			}
			
			// Next we go through the existing elements and remove the removed elements from their dependencies
			
			for(MatrixElement element:elementsWithoutTests) {	

				List <MatrixDependencyGroup> dependenciesForRemoval = new ArrayList <>();

				for(MatrixDependencyGroup dependencies : element.getDependencies()) {
										
					if(elementsForRemoval.contains(dependencies.getDestination())) {
						dependenciesForRemoval.add(dependencies);
					}
				}
				
				for(MatrixDependencyGroup dependencies : dependenciesForRemoval) {
					element.removeDependency(dependencies);
					//logger.info("loadMatrixFromJSON: Removed a dependency from "+dependencies.getSource()+" to "+dependencies.getDestination());
					removedDependencies ++;
				}
				
			}
			
			
			matrix = new DesignStructureMatrix(initialMatrix.getName(),initialMatrix.getFileName(),elementsWithoutTests);
			
			for(String exclusion:exclusions) {
				matrix.addExclusionString(exclusion);
			}
			
			log.info("loadMatrixFromJSON: Removed {} elements and {} dependencies, exclusions are: {}",removedTests, removedDependencies, Arrays.toString(exclusions.toArray()));
			
		
		} else {
			matrix = initialMatrix;
			log.info("loadMatrixFromJSON: Did not remove any elements");

		}

		int maxDependencies = 0;
		
		for(MatrixElement element:matrix.getElements()) {	
			for(MatrixDependencyGroup dependencies : element.getDependencies()) {
				int currentDependencies = dependencies.getTotalDependencies();
				if(currentDependencies > maxDependencies) {
					maxDependencies = currentDependencies;
				}
			}
		}
			
		matrix.setMaximumDependencyValue(DependencyMetric.DEPENDENCIES,maxDependencies);
		matrix.setElementNamesPrefix(elementPrefix);
		
		return matrix;

		
	}
		
	/**
	 * Create a DSM that groups the elements hierarchically by namespace
	 * 
	 * @param matrix the original DSM
	 * @param separator the character that separates names
	 * @param concatenate true if groupings with only one child should be concatenated
	 * @return the root group
	 */
	public MatrixElementGroup createNamespaceGrouping(DesignStructureMatrix matrix, char separator) {
		
		ArrayList <MatrixElement> elements = new ArrayList <>();
		

		for(MatrixElement element: matrix.getElements()) {
			
			// Creates a tokenizer that will separate based on package name
			StringTokenizer tokenizer = new StringTokenizer(element.getFullName(),Character.toString(separator));
			
			
			if(tokenizer.hasMoreTokens()) {
				String namespace = tokenizer.nextToken();

				boolean found = false;
				
				// Check if a DesignStructureMatrixElementGroup has been created previously for this namespace
				for(MatrixElement currentElement:elements) {					
					if(currentElement.getName().equals(namespace)) {
						found = true;
						addSubElement(tokenizer, (MatrixElementGroup) currentElement, element, namespace);
					}
				}
				
				if(!found) {
					// Check if there are more tokens. In case there aren't this is a leaf element
					if(tokenizer.hasMoreTokens()) {
						MatrixElementGroup newElement = new MatrixElementGroup(namespace);
						elements.add(newElement);
						addSubElement(tokenizer,newElement, element, namespace);
					} else {	
						// Leaf element, we just copy it
						element.setName(namespace);
						elements.add(element);

					}
				}
			}
		}
		
		
		MatrixElementGroup rootElement = new MatrixElementGroup(matrix.getName());

		
		for(MatrixElement e:elements) {
			rootElement.addChild(e);
		}
		
		 matrix.setRootGroup(rootElement);
		 
		 return rootElement;
	}
	
	

	/**
	 * Recursive function to create the hierarchy of DesignStructureMatrixElementGroup
	 * 
	 * @param tokenizer the tokenizer that is parsing the name
	 * @param parent the parent DesignStructureMatrixElementGroup
	 * @param element the element to add
	 */
	private void addSubElement(StringTokenizer tokenizer, MatrixElementGroup parent, MatrixElement element, String fullNamespace) {

		if(tokenizer.hasMoreTokens()) {
			String namespace = tokenizer.nextToken();
			fullNamespace = fullNamespace + "." + namespace;
			
			// Check if the parent already has a node for this token
			for(MatrixElement child:parent.getChildren()) {
				if(child.getName().equals(namespace)) {
					addSubElement(tokenizer, (MatrixElementGroup) child, element, fullNamespace);
					return;
				}
			}
			// The parent does not have a node for this token, check if there are more
			// tokens. If not, this is a leaf element
			if(tokenizer.hasMoreTokens()) {
				MatrixElementGroup newChild = new MatrixElementGroup(fullNamespace);
				newChild.setName(namespace);
				
				parent.addChild(newChild);
				addSubElement(tokenizer,newChild, element, fullNamespace);	
			} else {
				// Leaf element, we copy the original element into the new matrix
				element.setName(namespace);
				parent.addChild(element);
			}
		}	
	}
	
	/**
	 * Change the hotspot status of an element
	 * 
	 * @param element
	 * @param status
	 * @return
	 */
	public boolean setHotspotStatus(MatrixElement element, boolean status) {
		return element.setHotspot(status);
	}

	/**
	 * Create a matrixModel that only contains the dependents
	 * 
	 * @param matrix
	 * @param element
	 * @return
	 */
	public DesignStructureMatrixModel getDependentsModel(DesignStructureMatrix matrix, MatrixElement element) {
		
		DesignStructureMatrixModel model = DesignStructureMatrixModel.getModelFromElementsInMatrix(matrix);
		List <MatrixElement> dependents = model.getListOfDependents(element);
		List <MatrixElement> dependees = model.getListOfDependees(element);
		ArrayList <MatrixElement> listWithDependents = new ArrayList <> ();
		
		listWithDependents.addAll(dependents);
		listWithDependents.add(element);
		listWithDependents.addAll(dependees);
		
		Map <MatrixElement, MatrixElement> tempMap = new HashMap <> ();

		// First we make a copy of all the elements
		for(MatrixElement dependentElement:listWithDependents) {
			MatrixElement newElement = new MatrixElement(dependentElement.getFullName());
			if(dependentElement == element) {
				newElement.setHotspot(true);
			}
			tempMap.put(dependentElement,newElement);
		}
		
		// Now we copy the dependencies
		for(MatrixElement dependentElement:listWithDependents) {
			for(MatrixDependencyGroup group:dependentElement.getDependencies()) {
				
				// We only copy the dependency if the target is there (it should be!)
				if(listWithDependents.contains(group.getDestination())) {
					MatrixDependencyGroup newGroup = new MatrixDependencyGroup(tempMap.get(dependentElement),tempMap.get(group.getDestination()));
					Map <String,Integer> dependencyMap = group.getDependencies();
					for(String currentKey:dependencyMap.keySet()) {
						newGroup.addDependency(currentKey, dependencyMap.get(currentKey));
					}
					newGroup.setCoChanges(group.getCoChanges());
					tempMap.get(dependentElement).addDependency(newGroup);					
				}
			}
		}
		
		ArrayList <MatrixElement > elementsInMatrix = new ArrayList<MatrixElement> (tempMap.values());
		
		DesignStructureMatrix tempMatrix = new DesignStructureMatrix(element.getFullName(),"",elementsInMatrix);

		/*
		MatrixElementGroup rootElement = new MatrixElementGroup(element.getFullName());

		
		for(MatrixElement e:elementsInMatrix) {
			rootElement.addChild(e);
		}
		
		matrix.setRootGroup(rootElement);
		*/
		
		createNamespaceGrouping(tempMatrix,'$');

		
		DesignStructureMatrixModel finalModel = new DesignStructureMatrixModel(tempMatrix,elementsInMatrix);
		return finalModel;
	}
	
	/**
	 * Creates an initial model for the matrix
	 * 
	 * @param matrix
	 * @return
	 */
	public DesignStructureMatrixModel createInitialModel(DesignStructureMatrix matrix) {
		MatrixElementGroup root = createNamespaceGrouping(matrix,'.');
		
		ArrayList <MatrixElement> elements = new ArrayList <> ();

		for(MatrixElement element:root.getChildren()) {
			elements.add(element);
		}
		
		return new DesignStructureMatrixModel(matrix, elements);

	}
	
	public DesignStructureMatrix loadDataBasedOnConfigurationFile() throws IOException, CsvValidationException {
		
		// Get the configuration associated with the depends JSON file
		YamlConfigurationEntry dependsConfiguration = yamlLoader.getDependsConfigurationEntry();
		
		// Get the configuration associated with the rest of the entries in the configuration file
		List<YamlConfigurationEntry> configurationEntries = yamlLoader.getNonDependsConfigurationEntries();
		
		// Load the initial matrix using the depends configuration
		DesignStructureMatrix matrix = loadMatrixFromJSON(dependsConfiguration);
		
		// Create the grouping of the elements based on the package level
		createNamespaceGrouping(matrix,'.');
		
		// Load all metrics defined in the configuration file, but depends as that was already loaded.
		metricsService.loadMetrics(configurationEntries, matrix);		
		
		return matrix;
		
	}
	
	public DesignStructureMatrix loadMatrixFromJSON(YamlConfigurationEntry dependsConfiguration) {
				
		NameProcessor processor = new PrefixRemovalNameProcessor(dependsConfiguration.getRenaming().getPrefix());
		
		return loader.loadFromJSON("archinaut", dependsConfiguration.getFile(), processor);
				
	}

}
