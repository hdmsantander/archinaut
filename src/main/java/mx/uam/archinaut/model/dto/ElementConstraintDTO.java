package mx.uam.archinaut.model.dto;

public class ElementConstraintDTO {

	private String elementName;
	private String metric;
	private int threshold;

	public ElementConstraintDTO(String elementName, String metric, int threshold) {
		this.elementName = elementName;
		this.metric = metric;
		this.threshold = threshold;
	}

	public String getElementName() {
		return elementName;
	}

	public String getMetric() {
		return metric;
	}

	public int getThreshold() {
		return threshold;
	}
}
