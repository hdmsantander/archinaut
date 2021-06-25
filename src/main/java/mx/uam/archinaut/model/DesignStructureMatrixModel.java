package mx.uam.archinaut.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supports the creation of a representation of a DSM A
 * representation is a "view" of the DSM which only shows certain elements of
 * the DSM. Some of the elements can be groups or they can be the actual
 * elements of the DSM
 * 
 * @author humbertocervantes
 *
 */
public class DesignStructureMatrixModel {

	// The logger
	private static final Logger logger = LoggerFactory.getLogger(DesignStructureMatrixModel.class);

	// THe original matrix
	private DesignStructureMatrix matrix;

	// The elements of this representation
	private List<MatrixElement> elements;

	// The cells of the matrix
	private MatrixDependencyGroup[][] dependencies;

	// The cells of the matrix
	private DependencyConstraint[][] constraints;

	// Maximum number of dependencies in a cell
	private int maximumDependencies = 0;

	// Maximum number of cochanges in a cell
	private int maximumCoChanges = 0;

	/**
	 * 
	 * Create a representation
	 * 
	 * @param source   the original matrix
	 * @param elements the elements that will be part of this representation
	 */
	public DesignStructureMatrixModel(DesignStructureMatrix source, List<MatrixElement> elements) {
		this.matrix = source;
		this.elements = elements;

		recalculateDependencies();
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
	 * Returns the name of an element
	 * 
	 * @param row row of the element (starting with 0)
	 * @return the name of the element
	 */
	public MatrixElement getElement(int row) {
		return elements.get(row);
	}

	/**
	 * Returns a DesignStructureMatrixCell for the row and column
	 * 
	 * @param row    the row of the cell (starts with 0)
	 * @param column the column of the cell (starts with 0)
	 * @return a DesignStructureMatrixCell
	 */
	public MatrixDependencyGroup getDependencyGroup(int row, int column) {
		if (dependencies == null) {
			return null;
		}
		return dependencies[row][column];
	}

	/**
	 * Updates the dependency matrix
	 * 
	 */
	private void recalculateDependencies() {

		dependencies = new MatrixDependencyGroup[elements.size()][elements.size()];

		int row = 0;

		// The calculation is made for the elements that are present in the model
		for (MatrixElement e : elements) {

			// Get the dependencies for the element. The dependencies that are returned
			// are part of this matrix
			List<MatrixDependencyGroup> elementDependencies = MatrixDependencyGroup.getDependencies(e, elements);

			for (MatrixDependencyGroup dependencyGroup : elementDependencies) {

				int column = elements.indexOf(dependencyGroup.getDestination());
				dependencies[row][column] = dependencyGroup;

				// Update the maximum number of dependencies
				if (dependencyGroup.getTotalDependencies() > maximumDependencies) {
					maximumDependencies = dependencyGroup.getTotalDependencies();
				}

				// Update the maximum number of cochanges
				if (dependencyGroup.getCoChanges() > maximumCoChanges) {
					maximumCoChanges = dependencyGroup.getCoChanges();
				}

			}

			// After having filled the rows and columns with dependencies that have a
			// content,
			// we now fill the other cells with empty dependency groups. They are necessary
			// to add constraints.
			for (int column = 0; column < elements.size(); column++) {
				if (dependencies[row][column] == null) {
					MatrixDependencyGroup dependencyGroup = new MatrixDependencyGroup(e, elements.get(column));
					dependencies[row][column] = dependencyGroup;

				}
			}

			row++;
		}

	}

	/**
	 * returns a contraint given a row and a column
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public DependencyConstraint getDependenciesConstraint(int row, int column) {
		if (constraints == null) {
			return null;
		}

		return constraints[row][column];
	}

	/**
	 * This method recalculate the constraints considering the elements that are on
	 * this model
	 * 
	 */
	public void recalculateDependencyConstraints() {

		constraints = new DependencyConstraint[elements.size()][elements.size()];

		for (DependencyConstraint constraint : matrix.getDependencyConstraints()) {
			// We transform the source and destination elements of the constraint into
			// elements that are present in the current model
			List<MatrixElement> sourceElements = getAsElementsInTheModel(constraint.getSource());
			List<MatrixElement> destinationElements = getAsElementsInTheModel(constraint.getDestination());

			// We now add to the constraints array pointers to the constraint
			// in the positions associated with the elements
			for (MatrixElement sourceElement : sourceElements) {
				int sourceElementIndex = elements.indexOf(sourceElement);
				for (MatrixElement destinationElement : destinationElements) {
					int destinationElementIndex = elements.indexOf(destinationElement);
					constraints[sourceElementIndex][destinationElementIndex] = constraint;
				}
			}
		}
	}

	/**
	 * Given a particular element in the matrix, this method returns an array of
	 * elements that correspond to it in the current model. These elements may be
	 * subelements of the element, the element itself or one of its parents
	 * 
	 * @param element the element to consider
	 * @return an arraylist with elements that correspond to the element and that
	 *         are present in this model
	 */
	public List<MatrixElement> getAsElementsInTheModel(MatrixElement element) {
		ArrayList<MatrixElement> results = new ArrayList<>();

		// If the element is in the model we return it directly
		if (elements.contains(element)) {
			results.add(element);
		} else {
			// We first look into the parent elements as this is quicker
			// than looking into the subelements
			MatrixElement parent = findParentElement(element);
			if (parent != null) {
				results.add(parent);
			} else {
				// We then look into the sub elements
				if (element instanceof MatrixElementGroup) {
					findSubelements(results, element);
				}
			}
		}

		// Results should always have at least one element
		assert !results.isEmpty();

		return results;
	}

	/**
	 * Get the indexes of the initial and final rows for an element that is not in
	 * the matrix
	 * 
	 * @param element
	 * @return
	 */
	public int[] getIndexesForElementNotInMatrix(MatrixElement element) {
		int[] result = { -1, -1 };
		int initialIndex = 0;
		int finalIndex = 0;

		if (elements.contains(element)) {
			initialIndex = elements.indexOf(element);
			finalIndex = initialIndex;
		} else {
			List<MatrixElement> elementList = getAsElementsInTheModel(element);

			if (!elementList.isEmpty()) {
				initialIndex = elements.indexOf(elementList.get(0));
				finalIndex = initialIndex;
				if (elementList.size() > 1) {
					for (int i = 1; i < elementList.size(); i++) {
						MatrixElement currentElement = elementList.get(i);
						int currentIndex = elements.indexOf(currentElement);
						if (currentIndex < initialIndex)
							initialIndex = currentIndex;
						if (currentIndex > finalIndex)
							finalIndex = currentIndex;
					}
				}
			}
		}
		result[0] = initialIndex;
		result[1] = finalIndex;
		return result;
	}

	/**
	 * Given an element, this method adds the subelements of this element that are
	 * present in the model to the array that is passed as a parameter
	 * 
	 * @param results the array with the elements
	 * @param parent  the element
	 */
	private void findSubelements(ArrayList<MatrixElement> results, MatrixElement parent) {
		if (parent instanceof MatrixElementGroup) {
			for (MatrixElement element : ((MatrixElementGroup) parent).getChildren()) {
				if (elements.contains(element)) {
					results.add(element);
				} else {
					findSubelements(results, element);
				}
			}
		}

	}

	/**
	 * Given one element, return its immediate parent present in the elements of
	 * this model
	 * 
	 * @param element
	 * @return the immediate parent
	 */
	private MatrixElement findParentElement(MatrixElement element) {

		// element may be null if there is a lookup for an element
		// that is not in the matrix

		if (element != null) {
			MatrixElement parent = element.getGroup();
			while (parent != null) {
				if (elements.contains(parent)) {
					return parent;
				}
				parent = parent.getGroup();
			}
		}
		return null;

	}

	/**
	 * Test if the model contains a particular element
	 * 
	 * @param element
	 * @return
	 */
	public boolean containsElement(MatrixElement element) {
		return elements.contains(element);
	}

	/**
	 * Return the index of the element
	 * 
	 * @param element
	 * @return
	 */
	public int getIndexOfElement(MatrixElement element) {
		return elements.indexOf(element);
	}

	/**
	 * 
	 * @param elementFullName
	 * @return the index of the element or -1
	 */
	public int getIndexOfElement(String elementFullName) {
		MatrixElement element = matrix.getElementByFullname(elementFullName);
		if (element != null) {
			return getIndexOfElement(element);
		} else {
			return -1;
		}

	}

	/**
	 * Return the DSM of this model
	 * 
	 * @return
	 */
	public DesignStructureMatrix getDesignStructureMatrix() {
		return matrix;
	}

	/**
	 * Get an iterator of the elements in the model
	 * 
	 * @return
	 */
	public Iterable<MatrixElement> getElements() {
		return elements;
	}

	/**
	 * 
	 */
	public String toString() {
		return "matrix";
	}

	/**
	 * Returns the list of elements that depend on the element passed as parameter
	 * 
	 * @param element
	 * @return
	 */
	public List<MatrixElement> getListOfDependents(MatrixElement element) {

		ArrayList<MatrixElement> dependents = new ArrayList<>();

		// Right now this only works for visible components
		if (!elements.contains(element)) {
			logger.warn("DesignStructureMatrixModel.getListOfDependents: element not found " + element + " elements:"
					+ elements);
		} else {

			int indexOfElement = elements.indexOf(element);

			for (int row = 0; row < elements.size(); row++) {
				MatrixDependencyGroup group = getDependencyGroup(row, indexOfElement);
				// Dependencies to itself are not counted
				if (row != indexOfElement && group.getTotalDependencies() > 0) {
					dependents.add(group.getSource());
				}
			}
		}
		return dependents;

	}

	/**
	 * Calculate number of dependent elements
	 * 
	 * @param element
	 * @return
	 */
	public int calculateNumberOfDependents(MatrixElement element) {

		return getListOfDependents(element).size();

	}

	/**
	 * Calculate the total number of incoming dependencies for a given element, that
	 * is sum all of the types of dependencies from each element that depends on the
	 * element passed as parameter
	 * 
	 * @param element
	 * @return
	 */
	public int calculateIncomingDependencies(MatrixElement element) {
		// Right now this only works for visible components
		if (!elements.contains(element)) {
			logger.warn("DesignStructureMatrixModel.getIncomingDependencies: element not found " + element
					+ " elements:" + elements);
			return 0;
		}

		int indexOfElement = elements.indexOf(element);
		int totalIncomingDependencies = 0;

		for (int i = 0; i < elements.size(); i++) {
			MatrixDependencyGroup group = getDependencyGroup(i, indexOfElement);
			// Dependencies to itself are not counted
			if (i != indexOfElement) {
				totalIncomingDependencies = totalIncomingDependencies + group.getTotalDependencies();
			}
		}

		return totalIncomingDependencies;

	}

	/**
	 * Returns the list of elements that the element passed as parameter depends on
	 * 
	 * @param element
	 * @return
	 */
	public List<MatrixElement> getListOfDependees(MatrixElement element) {

		ArrayList<MatrixElement> dependees = new ArrayList<>();

		// Right now this only works for visible components
		if (!elements.contains(element)) {
			logger.warn("DesignStructureMatrixModel.getListOfDependendees: element not found " + element + " elements:"
					+ elements);
		} else {
			int indexOfElement = elements.indexOf(element);

			for (int column = 0; column < elements.size(); column++) {
				MatrixDependencyGroup group = getDependencyGroup(indexOfElement, column);
				// Dependencies to itself are not counted
				if (column != indexOfElement && group.getTotalDependencies() > 0) {
					dependees.add(group.getDestination());
				}
			}

		}
		return dependees;

	}

	/**
	 * Calculate the number of elements that the element passed as a parameter
	 * depends upon
	 * 
	 * @param element
	 * @return
	 */
	public int calculateNumberOfDependees(MatrixElement element) {

		return getListOfDependees(element).size();

	}

	/**
	 * Calculate the total number of outgoing dependencies for a given element, that
	 * is sum all of the types of dependencies from each element that is depended on
	 * by the element passed as parameter
	 * 
	 * @param element
	 * @return
	 */
	public int calculateOutgoingDependencies(MatrixElement element) {
		// Right now this only works for visible components
		if (!elements.contains(element)) {
			return 0;
		}

		int indexOfElement = elements.indexOf(element);
		int totalOutgoingDependencies = 0;

		for (int i = 0; i < elements.size(); i++) {
			MatrixDependencyGroup group = getDependencyGroup(indexOfElement, i);
			// Dependencies to itself are not counted
			if (i != indexOfElement) {
				totalOutgoingDependencies = totalOutgoingDependencies + group.getTotalDependencies();
			}
		}

		return totalOutgoingDependencies;

	}

	/**
	 * Returns a list of CoChangePartners for this element
	 * 
	 * @param element
	 * @return
	 */
	public List<MatrixElement> getListOfCoChangePartners(MatrixElement element) {

		List<MatrixElement> partners = new ArrayList<>();

		int indexOfElement = elements.indexOf(element);

		for (int i = 0; i < elements.size(); i++) {
			MatrixDependencyGroup group = getDependencyGroup(indexOfElement, i);
			// Dependencies to itself are not counted
			if (i != indexOfElement && group.getCoChanges() > 0) {
				partners.add(group.getDestination());
			}
		}

		return partners;

	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	public int calculateNumberOfCoChangePartners(MatrixElement element) {
		return getListOfCoChangePartners(element).size();
	}

	/**
	 * 
	 * Calculate the total number of cochange dependencies for the element with
	 * respet to other elements in the matrix
	 * 
	 * @param element
	 * @return
	 */
	public int calculateCoChangeDependencies(MatrixElement element) {
		// Right now this only works for visible components
		if (!elements.contains(element)) {
			return 0;
		}

		int indexOfElement = elements.indexOf(element);
		int totalCoChangeDependencies = 0;

		for (int i = 0; i < elements.size(); i++) {
			MatrixDependencyGroup group = getDependencyGroup(indexOfElement, i);
			// Dependencies to itself are not counted
			if (i != indexOfElement) {
				totalCoChangeDependencies = totalCoChangeDependencies + group.getCoChanges();
			}
		}

		return totalCoChangeDependencies;

	}

	/**
	 * Return the highest value of dependencies in any given cell of this model
	 * 
	 * @return
	 */
	public int getMaximumCellDependencies() {
		return maximumDependencies;

	}

	/**
	 * Return the highest value of coChanges in any given cell
	 * 
	 * @return
	 */
	public int getMaximumCellCoChanges() {
		return maximumCoChanges;

	}

	/**
	 * Returns a model that only contains the elements in the original matrix, this
	 * is useful for using the methods of DesignStructureMatrixModel
	 * 
	 * @param dsm
	 * @return
	 */
	public static DesignStructureMatrixModel getModelFromElementsInMatrix(DesignStructureMatrix dsm) {
		DesignStructureMatrixModel model = new DesignStructureMatrixModel(dsm, dsm.getElementsAsList());
		model.recalculateDependencyConstraints();
		return model;
	}

}
