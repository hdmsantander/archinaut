package mx.uam.archinaut.model;

import java.util.ArrayList;

/**
 * This subclass of DesignStructureMatrixElement is to create virtual elements
 * that serve as grouping of other elements, to create groups or clusterings
 * 
 * @author humbertocervantes
 *
 */
public class MatrixElementGroup extends MatrixElement {

	public static final int DIRECTIONDOWN = 0;
	public static final int DIRECTIONUP = 1;

	/**
	 * The elements that are part of this group
	 * 
	 */
	private ArrayList<MatrixElement> children = new ArrayList<>();

	/**
	 * Constructor with the name of the group
	 * 
	 * @param name
	 */
	public MatrixElementGroup(String name) {
		super(name);
	}

	/**
	 * Add a child to the group
	 * 
	 * @param child
	 * @return true if added successfully
	 */
	public boolean addChild(MatrixElement child) {
		child.setGroup(this);
		return children.add(child);
	}

	/**
	 * Remove a child
	 * 
	 * @param child
	 * @return
	 */
	public boolean removeChild(MatrixElement child) {
		child.setGroup(null);
		return children.remove(child);
	}

	/**
	 * Returns the array of children
	 * 
	 * @return the children
	 */
	public int getChildrenCount() {
		return children.size();
	}

	/**
	 * 
	 * @return
	 */
	public Iterable<MatrixElement> getChildren() {
		return children;
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public MatrixElement getChild(int index) {
		return children.get(index);
	}

	/**
	 * The dependencies for a group must be obtained through its representation
	 * 
	 * @return null
	 */
	@Override
	public Iterable<MatrixDependencyGroup> getDependencies() {
		return null;
	}

	/**
	 * Returns the depth level
	 * 
	 * @return
	 */
	public int getDepth() {
		if (getGroup() == null) {
			return 0;
		}
		return getGroup().getDepth() + 1;
	}

	/**
	 * 
	 * 
	 * @param elementGroup
	 * @return
	 */
	public MatrixElementGroup findLastCommonAncestor(MatrixElementGroup elementGroup) {

		// The element is part of this group
		if (this == elementGroup) {
			return this;
		}

		// The element is part of a subgroup of this group
		if (isIndirectChild(elementGroup)) {
			return this;
		}

		if (getGroup() != null) {
			return getGroup().findLastCommonAncestor(elementGroup);
		}

		return null;
	}

	/**
	 * Test if this element is an indirect child of a group.
	 * 
	 * @param elementGroup
	 * @return
	 */
	private boolean isIndirectChild(MatrixElementGroup elementGroup) {
		if (this == elementGroup) {
			return true;
		}

		for (MatrixElement e : children) {
			if (e instanceof MatrixElementGroup && ((MatrixElementGroup) e).isIndirectChild(elementGroup)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	/**
	 * Move a children element
	 * 
	 * @param element   the element to move
	 * @param direction the direction of movement
	 * @return true if moved succesfully, false if not
	 */
	public boolean moveElement(MatrixElement element, int direction) {

		int currentIndex = children.indexOf(element);

		children.remove(currentIndex);

		if (direction == DIRECTIONUP) {
			children.add(currentIndex - 1, element);

			return true;
		} else if (direction == DIRECTIONDOWN) {
			children.add(currentIndex + 1, element);

			return true;
		}
		return false;

	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public int getMetricValue(ElementMetric type) {

		int metricValue = 0;

		for (MatrixElement e : children) {
			metricValue += e.getMetricValue(type);
		}

		return metricValue;
	}

}
