package mx.uam.archinaut.model;

/**
 * 
 * @author humbertocervantes
 *
 */
public class ElementConstraint {
	private MatrixElement element;
	private ElementMetric metric;
	private int threshold;
	
	public ElementConstraint(MatrixElement element, ElementMetric metric, int threshold) {
		this.element = element;
		this.metric = metric;
		this.threshold = threshold;
	}
	
	public MatrixElement getElement() {
		return element;
	}
	
	public ElementMetric getMetric() {
		return metric;
	}
	
	public int getThreshold() {
		return threshold;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Constraint for element "+element.getFullName()+" Metric: "+metric+" threshold: "+threshold;
	}


}
