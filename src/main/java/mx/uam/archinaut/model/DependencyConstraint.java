package mx.uam.archinaut.model;

public class DependencyConstraint {
	// number that identifies the source of the dependency
	private MatrixElement source;
	
	// number that identifies the destination of the dependency
	private MatrixElement destination;

	/**
	 * Create a contraint.
	 * 
	 * Note: Constraints cannot be connected to dependency groups because the groups 
	 * are created and destroyed as different DesignStructureMatrixModels are created
	 * only dependency groups between leaf elements remain. For this reason, constraints
	 * are added directly to the matrix.
	 * 
	 * @param source
	 * @param destination
	 */
	public DependencyConstraint(MatrixElement source, MatrixElement destination) {
		this.source = source;
		this.destination = destination;
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
	 * 
	 */
	@Override
	public String toString() {
		return "Constraint between source: "+source+" and destination: "+destination;
	}


}
