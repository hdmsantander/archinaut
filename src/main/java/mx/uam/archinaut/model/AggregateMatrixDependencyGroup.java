package mx.uam.archinaut.model;

import java.util.ArrayList;

public class AggregateMatrixDependencyGroup extends MatrixDependencyGroup {
	
	private ArrayList <MatrixDependencyGroup> subDependencyGroups = new ArrayList <>();
	
	// Maximum number of coChanges in this dependency group
	private int maxCoChanges = 0;

	private int maxDependencies = 0;
	
	public AggregateMatrixDependencyGroup(MatrixElement source, MatrixElement destination) {
		super(source, destination);
	}

	/**
	 * Add a new dependency. In case this type already existed, the occurrences are incremented
	 * 
	 * @param type A string that describes the type of dependency
	 * @param value An integer that represents the number of occurrences
	 */
	public void addDependencyGroup(MatrixDependencyGroup group) {
		
		if(group.getCoChanges()>0) {
			setCoChanges(getCoChanges()+group.getCoChanges());
			
			// Update the maximum number of cochanges in this group
			int coChanges = group.getCoChanges();
			if(group instanceof AggregateMatrixDependencyGroup) {
				coChanges = ((AggregateMatrixDependencyGroup)group).getMaxCoChanges();
			}
			if(coChanges > maxCoChanges) {
				maxCoChanges = coChanges;
			}
			
		}
		
		if(group.getTotalDependencies()>0) {
			// Update the maximum number of dependencies in this group
			int depsTotal = group.getTotalDependencies();
			if(group instanceof AggregateMatrixDependencyGroup) {
				depsTotal = ((AggregateMatrixDependencyGroup)group).getMaxDependencies();
			}
			if(depsTotal > maxDependencies) {
				maxDependencies = depsTotal;
			}
			
		}
		
		for(String type:group.getDependencies().keySet()) {
			
			int oldOccurrences = 0;
			
			if(dependencies.containsKey(type)) {
				oldOccurrences = dependencies.get(type);
				dependencies.remove(type);
			}

			dependencies.put(type, group.getDependencies().get(type)+oldOccurrences);
			
		}	
		
		subDependencyGroups.add(group);

	}
	
	/**
	 * 
	 * @return
	 */
	public Iterable <MatrixDependencyGroup> getSubDependencyGroups() {
		return subDependencyGroups;
	}
	
	public int getMaxCoChanges() {
		return maxCoChanges;
	}

	public int getMaxDependencies() {
		return maxDependencies;
	}

}
