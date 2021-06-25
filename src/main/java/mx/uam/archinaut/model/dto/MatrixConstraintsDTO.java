package mx.uam.archinaut.model.dto;

import java.util.ArrayList;
import java.util.List;

public class MatrixConstraintsDTO {
	private ArrayList<DependencyConstraintDTO> dependencyconstraints = new ArrayList<DependencyConstraintDTO>();
	private ArrayList<ElementConstraintDTO> elementConstraints = new ArrayList<ElementConstraintDTO>();
	private String matrixName;

	public MatrixConstraintsDTO(String matrixName) {
		this.matrixName = matrixName;
	}

	public void addDependencyConstraint(DependencyConstraintDTO constraint) {
		dependencyconstraints.add(constraint);
	}

	public void addElementConstraint(ElementConstraintDTO constraint) {
		elementConstraints.add(constraint);
	}

	public String getMatrixName() {
		return matrixName;
	}

	public List<DependencyConstraintDTO> getDependencyConstraints() {
		return dependencyconstraints;
	}

	public List<ElementConstraintDTO> getElementConstraints() {
		return elementConstraints;
	}
}
