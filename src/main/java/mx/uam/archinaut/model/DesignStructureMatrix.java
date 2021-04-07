package mx.uam.archinaut.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.DesignStructureMatrixLoader;
import mx.uam.archinaut.model.MatrixElement.ElementStatus;


/**
 * 
 * Class that represents a DesignStructureMatrix
 * 
 * @author humbertocervantes
 *
 */
@Slf4j
public class DesignStructureMatrix implements Serializable {
	
	private static final long serialVersionUID = -7697462458971200457L;

	// Name of this matrix
	private String name;
		
	// The root group
	private MatrixElementGroup root;
	
	// The element names
	private List <MatrixElement> elements = new ArrayList<>();

	// Element constraints
	private ArrayList <ElementConstraint> elementConstraints = new ArrayList<>();

	// Dependency constraints
	private ArrayList <DependencyConstraint> dependencyConstraints = new ArrayList<>();
	
	// Map of full names to elements
	private Map <String, MatrixElement> fullNameElementsMap = new HashMap <> ();

	// Map of full names to elements
	private static Map <String, List<String>> renameMap = new HashMap <> ();

	// Diff
	private ArchitecturalDiff diff;

	// Maximums for dependency values
	private int [] maximumDependencyValues = new int [DependencyMetric.values().length];

	// Maximums for element values
	private int [] maximumElementValues = new int [ElementMetric.values().length];

	// Maximums for element values
	private int [] minimumElementValues = new int [ElementMetric.values().length];
		
	private String fileName;
	
	private String elementNamesPrefix;
	
	private String metricsNamesPrefix;
	
	private String logNamesPrefix;
	
	private String filesExtension;
	
	// Exclusion strings
	private List <String> exclusionStrings = new ArrayList<>();

	/**
	 * 
	 * Create a DesignStructureMatrix with an array of element names and an array of 
	 * DesignStructureMatrixCells which contain information about the dependencies
	 * 
	 * @param elements an array with the names of the elements
	 * @param dependencies a square array with DesignStructureMatrixCells which contain the details of the dependencies
	 */
	public DesignStructureMatrix(String name, String fileName, List <MatrixElement> elements) {
		
		this.name = name;
		this.fileName = fileName;
		this.elements = elements;
		
		if(elements.size()>0) {
			StringTokenizer tokenizer = new StringTokenizer(elements.get(0).getFullName(),"_");
			while(tokenizer.hasMoreTokens()) {
				filesExtension = tokenizer.nextToken();
			}
			//logger.info("Inferred file extension:"+filesExtension);
		}
	}
	
	public String getFilesExtension() {
		return filesExtension;
	}
	
	/**
	 * 
	 * @return
	 */
	public Iterable<MatrixElement> getElements() {
		return elements;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Method so that the DesignStructureMatrixModel can create a model with the elements from the matrix
	 * 
	 * @return
	 */
	List <MatrixElement> getElementsAsList() {
		return elements;
	}
		
	/**
	 * Returns the number of elements in the matrix
	 * 
	 * @return an integer with the number of elements
	 */
	public int getElementsCount() {
		return elements.size();
	}
	
	
		
	/**
	 * 
	 * @param element
	 * @return
	 */
	public int getIndexOfElement(MatrixElement element) {
		return elements.indexOf(element);
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public MatrixElement getElement(int index) {
		return elements.get(index);
	}

	/**
	 * 
	 * @param root
	 */
	public void setRootGroup(MatrixElementGroup root) {
		this.root = root;	
		
		// This is done in two passes otherwise there is a problem with the 
		// lookup mechanism in 
		populateMap(root);
		
	}
	
	/**
	 * Populate the map that associates a full name with an element
	 * 
	 * @param element
	 */
	private void populateMap(MatrixElement element) {
				
		fullNameElementsMap.put(element.getFullName(), element);
		
		if(element instanceof MatrixElementGroup) {
			for(MatrixElement current:((MatrixElementGroup)element).getChildren()) {
				populateMap(current);
			}
		}
		
	}
	
	public static void setRenameMap(Map <String, List<String>> newRenameMap) {
		renameMap = newRenameMap;
	}
	
	/**
	 * Retrieve an element given its full name
	 * 
	 * @param name
	 * @return
	 */
	public MatrixElement getElementByFullname(String name) {
		
		
		name = name.replaceFirst("src\\.", "");
		                                               
		
		MatrixElement element = fullNameElementsMap.get(name);

		// If element is null, it may be a renamed element
		if(element==null) {
			// We get altenative names in the rename map
			List <String> newNames = renameMap.get(name);
			
			// We now check if any of these alternative names are present in this matrix
			if(newNames != null) {
				
				log.info("Found "+newNames.size()+" alternative names for element "+name+". Total number of renames:"+renameMap.size());
				
				
				for(String candidateName:newNames) {
					element = fullNameElementsMap.get(candidateName);
					if(element != null) {
						return element;
					} 
				} 

				log.info("Element "+name+" not found");

			} 
		}
		
		return element;
	}
	
	/**
	 * Returns the status of the element in this matrix.
	 * RENAMED: The element exists in this matrix under a different name
	 * PRESENT: The element is present in this matrix
	 * NOTPRESENT: The element is not present
	 * 
	 * @param name
	 * @return
	 */
	public ElementStatus getElementStatus(String name) {
		MatrixElement element = fullNameElementsMap.get(name);
		if(element==null) {
			element = getElementByFullname(name);
			if(element != null) {
				return ElementStatus.RENAMED;
			}
		} else {
			return ElementStatus.PRESENT;
		}
		return ElementStatus.NOTPRESENT;		
	}
	
	/**
	 * 
	 * @return
	 */
	public MatrixElementGroup getRootGroup() {
		return root;
	}
	
	/**
	 * Add a new constraint
	 * 
	 * @param constraint
	 * @return true if added successfully
	 */
	public boolean addDependencyConstraint(DependencyConstraint constraint) {
		log.info("Added dependency constrant "+constraint );
		return dependencyConstraints.add(constraint);
	}
	
	/**
	 * Remove a constraint
	 * 
	 * @param constraint
	 * @return true if removed successfully
	 */
	public boolean removeDependencyConstraint(DependencyConstraint constraint) {
		return dependencyConstraints.remove(constraint);
	}
	
	/**
	 * Add a new constraint
	 * 
	 * @param constraint
	 * @return true if added successfully
	 */
	public boolean addElementConstraint(ElementConstraint constraint) {
		return elementConstraints.add(constraint);
	}
	
	/**
	 * Remove a constraint
	 * 
	 * @param constraint
	 * @return true if removed successfully
	 */
	public boolean removeElementConstraint(ElementConstraint constraint) {
		return elementConstraints.remove(constraint);
	}
	/**
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	public DependencyConstraint findDependencyConstraint(MatrixElement source, MatrixElement destination) {
		for(DependencyConstraint constraint:dependencyConstraints) {
			if(constraint.getSource()==source && constraint.getDestination()==destination) {
					return constraint;
			}
		}
		return null;
	}
	
	/**
	 * Get the dependency constraints as an iterable
	 * 
	 * @return
	 */
	public Iterable <DependencyConstraint> getDependencyConstraints() {
		return dependencyConstraints;
	}
	
	/**
	 * Get the element constraints as an iterable
	 * 
	 * @return
	 */
	public Iterable <ElementConstraint> getElementConstraints() {
		return elementConstraints;
	}
	
	/**
	 * Get total for a metric
	 * 
	 * @param type
	 * @param index
	 * @return
	 */
	public int getTotalForMetric(ElementMetric type) {
		
		int total = 0;
		for(MatrixElement element:this.getElements()) {
			total += element.getMetricValue(type);
		}
		return total;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}

	public boolean setMaximumDependencyValue(DependencyMetric type, int value) {
		maximumDependencyValues[type.getIndex()] = value;
		return true;
	}

	public int getMaximumDependencyValue(DependencyMetric type) {
		return maximumDependencyValues[type.getIndex()];
	}

	
	public boolean setMaximumElementValue(ElementMetric type, int value) {
		maximumElementValues[type.getIndex()] = value;
		return true;
	}

	public int getMaximumElementValue(ElementMetric type) {
		return maximumElementValues[type.getIndex()];
	}
	
	public boolean setMinumElementValue(ElementMetric type, int value) {
		minimumElementValues[type.getIndex()] = value;
		return true;
	}

	public int getMinimumElementValue(ElementMetric type) {
		return minimumElementValues[type.getIndex()];
	}

	
	public ArchitecturalDiff getDiff() {
		return diff;
	}

	public void setDiff(ArchitecturalDiff diff) {
		this.diff = diff;
	}
	
	public void setElementNamesPrefix(String elementNamesPrefix) {
		this.elementNamesPrefix = elementNamesPrefix;
		
	}

	public String getElementNamesPrefix() {
		return elementNamesPrefix;
	}
	
	public void setMetricsNamesPrefix(String metricsNamesPrefix) {
		this.metricsNamesPrefix = metricsNamesPrefix;
	}

	public String getMetricsNamesPrefix() {
		return metricsNamesPrefix;
	}

	public void setLogNamesPrefix(String logNamesPrefix) {
		this.logNamesPrefix = logNamesPrefix;
	}

	public String getLogNamesPrefix() {
		return logNamesPrefix;
	}
	
	/**
	 * Add a new exclusion string
	 * 
	 * @param exclusionString
	 */
	public void addExclusionString(String exclusionString) {
		exclusionStrings.add(exclusionString);
	}
	
	public void removeExclusionString(String exclusionString) {
		exclusionStrings.remove(exclusionString);
	}
	
	public List <String> getExclusionStrings() {
		return exclusionStrings;
	}


	
}
