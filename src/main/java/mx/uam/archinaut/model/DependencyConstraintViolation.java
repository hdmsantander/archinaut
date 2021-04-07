package mx.uam.archinaut.model;

public class DependencyConstraintViolation {
	
	// The associated constraint
	private DependencyConstraint constraint;
	
	// The dependency group at the element level (not groups)
	private MatrixDependencyGroup dependencyGroup;
	
	public DependencyConstraintViolation(MatrixDependencyGroup dependencyGroup, DependencyConstraint constraint) {
		this.dependencyGroup = dependencyGroup;
		this.constraint = constraint;
	}

	public DependencyConstraint getConstraint() {
		return constraint;
	}
	
	public MatrixDependencyGroup getMatrixDependencyGroup() {
		return dependencyGroup;
	}
	
	public String toString() {
		return "Violation : "+constraint+" Element source : "+dependencyGroup.getSource()+" and Element destination : "+dependencyGroup.getDestination();
	}
}
