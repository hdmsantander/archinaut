package mx.uam.archinaut.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cell of the Design Structure Matrix
 * 
 * @author humbertocervantes
 *
 */
public class MatrixDependencyGroup {
	
	// The logger
	private static final Logger logger = LoggerFactory.getLogger(MatrixDependencyGroup.class);
	
	// number that identifies the source of the dependency
	private MatrixElement source;
	
	// number that identifies the destination of the dependency
	private MatrixElement destination;
		
	// A hashmap that contains dependencies the string is the type the integer is the number of occurrences
	protected Map <String, Integer> dependencies = new HashMap <>();
	
	private int coChanges = 0;
		
	/**
	 * Consctructor
	 * 
	 * @param source the source element
	 * @param destination the destination element
	 */
	public MatrixDependencyGroup(MatrixElement source, MatrixElement destination) {
		
		this.source = source;
		this.destination = destination;
	}
	
	/**
	 * Add a new dependency. In case this type already existed, the occurrences are incremented
	 * 
	 * @param type A string that describes the type of dependency
	 * @param value An integer that represents the number of occurrences
	 */
	public void addDependency(String type, int occurrences) {
		
		if(dependencies.containsKey(type)) {
			logger.warn("MatrixDependecyGroup.addDependency: dependency already contains type = "+type);
		}

		dependencies.put(type, occurrences);

	}
	
	/**
	 * Returns the index of the source of the dependency
	 * 
	 * @return an integer with the index
	 */
	public MatrixElement getSource() {
		return source;
	}

	/**
	 * Returns the index of the destination of the dependency
	 * 
	 * @return an integer with the index
	 */
	public MatrixElement getDestination() {
		return destination;
	}
	
	/**
	 * Returns the total number of dependencies
	 * 
	 * @return an integer with the total number of dependencies
	 */
	public int getTotalDependencies() {
		int total = 0;
				
		for (int a:dependencies.values()) {
			total += a;
		}
		return total;
	}
	
	/**
	 * Returns the map of dependencies <type, occurrences>
	 * 
	 * @return the map of dependencies
	 */
	public Map <String,Integer> getDependencies() {
		return dependencies;
	}
	
	/**
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	public static MatrixDependencyGroup getDependencies(MatrixElement source, MatrixElement destination) {
		ArrayList <MatrixElement> tempArray = new ArrayList <> ();
		tempArray.add(destination);
		List <MatrixDependencyGroup> groups = getDependencies(source,tempArray);
		if(groups.size()>1) {
			logger.warn("MatrixDependencyGroup.getDependencies: warning more than 1 result");
		} else {
			logger.trace("MatrixDependencyGroup.getDependencies: found "+groups.size()+" between "+source+" and "+destination);
		}
		if(groups.size()==1) 
			return groups.get(0);
		return null;

	}

	
	/**
	 * Get dependencies from a source element with respect to an array of possible targets
	 * the targets may be groups, in that case, the MatrixDependencyGroups that are returned
	 * aggegate dependencies of dependencies between the source element and children of the target groups
	 * 
	 * @param source
	 * @param possibleTargets
	 * @return
	 */
	public static List <MatrixDependencyGroup> getDependencies(MatrixElement source, List <MatrixElement> possibleTargets) {
		 
		// We need a map as a destination may occur in more than one dependency, to aggregate numbers
		Map <MatrixElement, MatrixDependencyGroup> depsMap = new HashMap <>();
		
		if(!(source instanceof MatrixElementGroup)) {
			
			for(MatrixDependencyGroup currentDependencyGroup:source.getDependencies()) {
				
				// Since the source is a MatrixElement, the destination must be another
				// MatrixElement
				MatrixElement newDestination = currentDependencyGroup.getDestination();
				
				// We first check if the destination is present in the list of possible targets
				if (possibleTargets.contains(newDestination)) {
					
					// In this case we do not need to aggregate values, since this is
					// a direct dependency between MatrixElements
					depsMap.put(newDestination, currentDependencyGroup);

				} else {
					// The dependency points to an element which is not in the target list
					// we need to recalculate the destination 
					newDestination = recalculateDestination(currentDependencyGroup.getDestination().getGroup(), possibleTargets);
					
					// newDestination can be null when we are checking for violations
					if(newDestination != null) {
					
						// Check if there was already a dependency to this destination in the cache
						AggregateMatrixDependencyGroup newDependencies = (AggregateMatrixDependencyGroup) depsMap.get(newDestination);
						
						if(newDependencies == null) {
							// Create the dependency and put it in the cache
							newDependencies = new AggregateMatrixDependencyGroup(source, newDestination);

							depsMap.put(newDestination, newDependencies);
						} 
						
						// We now must aggregate all of the dependencies that are represented by this
						// new MatrixDependencyGroup. This is done in AddDependency which does not 
						// replace existing occurrences if the type has been added previously, it
						// rather adds them
						newDependencies.addDependencyGroup(currentDependencyGroup);
					}
				} 
			}
			
		} else { 
			
			// The element is a group
			MatrixElementGroup group = (MatrixElementGroup) source;
			
			// All the children of this group will be grouped under the same source, which is the group
			// So we have to go through each one of the children
			for(MatrixElement child:group.getChildren()) {
							
				List <MatrixDependencyGroup> elementDependencies = getDependencies(child, possibleTargets);
				
				for(MatrixDependencyGroup currentDependencyGroup:elementDependencies) {
					
					// This is the destination, but we have to see if it is currently in the
					// list of targets
					MatrixElement newDestination = currentDependencyGroup.getDestination();
					
					if(!possibleTargets.contains(newDestination)) {
							newDestination = recalculateDestination(currentDependencyGroup.getDestination().getGroup(), possibleTargets);
					}
					
					// Add to the map cache
					MatrixDependencyGroup newDependencies = depsMap.get(newDestination);
					if(newDependencies == null) {
						newDependencies = new AggregateMatrixDependencyGroup(group, newDestination);

						depsMap.put(newDestination, newDependencies);
					} 
					
					// Copy the original dependencies or increment the existing dependencies
					// addDependency does not replace existing occurrences if the type has
					// been added previously
					((AggregateMatrixDependencyGroup)newDependencies).addDependencyGroup(currentDependencyGroup);
				}
			}
		}
		
		return new ArrayList <>(depsMap.values());

	}
	
	/**
	 * Recalculate a destination for an element which is not in the matrix
	 * 
	 * @param element original element
	 * @return new destination
	 */
	private static MatrixElement recalculateDestination(MatrixElement element, List <MatrixElement> possibleTargets) {
		
		// Check first if the element already exists in the list of targets
		if(possibleTargets.contains(element)) {
			return element;
		} else if(element.getGroup() != null) // it is not, check in the parent
		{
			return recalculateDestination(element.getGroup(), possibleTargets);
		}
		
		return null;
	}
	

	public int getCoChanges() {
		return coChanges;
	}

	public void setCoChanges(int coChanges) {
		this.coChanges = coChanges;
	}

	public String toString() {
		return "MatrixDependencyGroup from source:"+source+" destination:"+destination;
	}

}
