package mx.uam.archinaut.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An element of the design structure matrix which is a source of dependencies
 * 
 * @author humbertocervantes
 *
 */
public class MatrixElement implements Comparable <MatrixElement> {
	
	// The logger
	private static final Logger logger = LoggerFactory.getLogger(MatrixElement.class);
	
	// Full name of the element
	private String fullName;
	
	// Short name if it is in a cluster
	private String name;
	
	// Dependencies for which this element is source
	private Set <MatrixDependencyGroup> dependencies = new HashSet <>();
			
	// The group this belongs to (in case it does)
	private MatrixElementGroup group;
	
	// Methods of the element
	private List <ElementMethod> methods = new ArrayList <>();

	// Names of design smells associated with this element
	private Set <String> designSmells;

	// Mark as hotspot
	private boolean hotspot = false;
	
	// Element constraints
	private List <ElementConstraint> constraints = new ArrayList <> ();
	
	// Element metrics
	private int [] metrics = new int [ElementMetric.values().length];
	
	private Set <HotspotData> hotspotData;
	
	public enum ElementStatus {
		NEW,
		PRESENT,
		RENAMED,
		NOTPRESENT,
		DELETED
	}

	public class HotspotData {
		ElementMetric metric;
		int deviationTimes;
		double deviationIndex;
		
		public HotspotData(ElementMetric metric, int deviationTimes, double deviationIndex) {
			this.metric = metric;
			this.deviationTimes = deviationTimes;
			this.deviationIndex = deviationIndex;
		}
		
		public void setDeviationTimes(int deviationTimes) {
			this.deviationTimes = deviationTimes;
			
		}
		
		public void setDeviationIndex(double deviationIndex) {
			this.deviationIndex = deviationIndex;
		}
		
		public ElementMetric getMetric() {
			return metric;
		}
		
		public int getDeviationTimes() {
			return deviationTimes;
		}
		
		public double getDeviationIndex() {
			return deviationIndex;
		}
	}

	
	
	/**
	 * Constructor
	 * 
	 * @param name the name of the element
	 */
	public MatrixElement(String name) {
		this.name = name;	
		this.fullName = name;
	}

	/**
	 * Get the name of the element
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	
	/**
	 * 
	 * Setter to change the name of the element
	 * 
	 * @param newName the new name for the element
	 * @return
	 */
	public boolean setName(String newName) {
		name = newName;
		return true;
	}

	
	/**
	 * Get the name of the element
	 * 
	 * @return the name
	 */
	public String getFullName() {
		return fullName;
	}
	
	/**
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public boolean setMetricValue(ElementMetric type, int value) {
		metrics[type.getIndex()] = value;
		return true;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public int getMetricValue(ElementMetric type) {
		return metrics[type.getIndex()];
	}

	/**
	 * Return the metrics for this element;
	 * 
	 * @return
	 */
	public int [] getMetrics() {
		return metrics;
	}
	
	
	/**
	 * Mark this element as a hotspot
	 * 
	 * @param value
	 */
	public boolean setHotspot(boolean value) {
		hotspot = value;
		return value;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHotspot() {
		
		// Groups cannot be marked as hotspots
		if(this instanceof MatrixElementGroup) {
			return false;
		}

		return hotspot;
	}
	
	public void setHotspotData(Set <HotspotData> hotspotData) {
		this.hotspotData = hotspotData;
	}
	
	public HotspotData getHotspotData(ElementMetric metric) {
		if(!hotspot) {
			return null;
		}
		
		if(hotspotData != null) {
			for(HotspotData data:hotspotData) {
				if(data.metric == metric) {
					return data;
				}
			}
		}
		
		return null;
		
	}
		
	public double getHotspotIndex() {
		double hotspotIndex = 0.0;
		
		if(hotspotData != null) {
			for(HotspotData data:hotspotData) {
				hotspotIndex += data.getDeviationIndex();
			}
		}
		
		return hotspotIndex;
	}
	
	/**
	 * Add a dependency for this element
	 * 
	 * @param dependency where the source must be this element
	 * @return true if added successfully, false if this element is not the source
	 */
	public boolean addDependency(MatrixDependencyGroup dependency) {
		
		if(dependency.getSource() != this) {
			logger.error("MatrixElement.addDependency: adding a dependency group whose source is not this element");
			return false;
		}
		
		return dependencies.add(dependency);
	}
	
	/**
	 * Remove a dependency for this element
	 * 
	 * @param dependency where the source must be this element
	 * @return true if added successfully, false if this element is not the source
	 */
	public boolean removeDependency(MatrixDependencyGroup dependency) {
		
		if(dependency.getSource() != this) {
			logger.error("MatrixElement.addDependency: removing a dependency group whose source is not this element");
			return false;
		}
		
		return dependencies.remove(dependency);
	}

	
	/**
	 * Retrieve this element's dependencies
	 * 
	 * @return an arraylist with the dependencies
	 */
	public Iterable <MatrixDependencyGroup> getDependencies() {
		return dependencies;
	}

	/**
	 * Sets the group of the element. This is normally called by DesignStructureMatrixElementGroup when
	 * the element is added to a group.
	 * 
	 * @param group
	 */
	boolean setGroup(MatrixElementGroup group) {
		this.group = group;
		return true;
	}
	
	/**
	 * Get the group this element belongs to
	 * 
	 * @return
	 */
	public MatrixElementGroup getGroup() {
		return group;
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * 
	 * @param smell
	 */
	public void addDesignSmell(String smell) {
		if(designSmells == null) {
			designSmells = new HashSet <> ();
		}
		
		designSmells.add(smell);
		metrics[ElementMetric.DESIGNSMELLS.getIndex()]=designSmells.size();
	}
	
	/**
	 * 
	 * @return
	 */
	public Iterable<String> getDesignSmells() {
		return designSmells;
	}

	/**
	 * 
	 * @param method
	 */
	public void addMethod(ElementMethod method) {
		methods.add(method);
		
	}
	
	/**
	 * 
	 * @return
	 */
	public Iterable <ElementMethod> getMethods() {
		Collections.sort(methods,Collections.reverseOrder());
		return methods;
	}

	/**
	 * 
	 * @return
	 */
	public int getMethodCount() {
		return methods.size();

	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public ElementMethod getMethod(int index) {		
		return methods.get(index);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isGroup() {
		return false;
	}
	
	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public boolean addConstraint(ElementConstraint constraint) {
		logger.info("Added constraint"+constraint);
		return constraints.add(constraint);
	}
	
	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public boolean removeConstraint(ElementConstraint constraint) {
		logger.info("Removed constraint"+constraint);
		return constraints.remove(constraint);
	}
	
	/**
	 * 
	 * @return
	 */
	public Iterable <ElementConstraint> getConstraints() {
		return constraints;
	}
	
	/**
	 * Get a string with the element details
	 * 
	 * @return
	 */
	public String getDetails() {
		StringBuilder message = new StringBuilder ("Element: "+getFullName());
		
		for(ElementMetric metric:ElementMetric.values()) {
			message.append("\n"+metric.getText()+": "+getMetricValue(metric));
		}
		
		
		return message.toString();

	}

	
	/**
	 * Method to support comparisons for sorting in Table
	 */
	@Override
	public int compareTo(MatrixElement compared) {
		return this.fullName.compareTo(compared.getName());
	}

	@Override
	public boolean equals(Object target) {
		MatrixElement compared = (MatrixElement) target;
		return (this.fullName.equals(compared.fullName));
	}
	
}
