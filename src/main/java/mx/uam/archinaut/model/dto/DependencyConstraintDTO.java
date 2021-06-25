package mx.uam.archinaut.model.dto;

public class DependencyConstraintDTO {
	private String sourceElement;
	private String destinationElement;

	public DependencyConstraintDTO(String sourceElement, String destinationElement) {
		this.sourceElement = sourceElement;
		this.destinationElement = destinationElement;
	}

	public String getSourceElement() {
		return sourceElement;
	}

	public String getDestinationElement() {
		return destinationElement;
	}

}
