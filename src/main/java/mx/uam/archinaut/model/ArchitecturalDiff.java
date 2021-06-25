package mx.uam.archinaut.model;

import java.util.HashMap;
import java.util.Map;

import mx.uam.archinaut.model.MatrixElement.ElementStatus;

/**
 * 
 * @author humbertocervantes
 *
 */
public class ArchitecturalDiff {

	public enum DiffType {
		DIFFTARGET_OLDER, DIFFTARGET_NEWER
	}

	private DesignStructureMatrix originalMatrix;
	private DesignStructureMatrix diffTargetMatrix;
	private DiffType diffType;

	Map<String, Delta> deltasMap = new HashMap<>();

	public ArchitecturalDiff(DesignStructureMatrix original, DesignStructureMatrix diffTarget, DiffType diffType) {
		this.originalMatrix = original;
		this.diffTargetMatrix = diffTarget;
		this.diffType = diffType;
	}

	public DiffType getDiffType() {
		return diffType;
	}

	/**
	 * Get delta for an element
	 * 
	 * @param fullName
	 * @return null if the element is not in the matrix, a delta if it is
	 */
	public Delta getDeltaForElement(String fullName) {

		if (originalMatrix.getElementByFullname(fullName) == null) {
			return null;
		}

		Delta delta = deltasMap.get(fullName);

		if (delta == null) {
			MatrixElement originalElementVersion = originalMatrix.getElementByFullname(fullName);
			MatrixElement diffTargetElementVersion = diffTargetMatrix.getElementByFullname(fullName);
			ElementStatus targetStatus = originalMatrix.getElementStatus(fullName);

			delta = new Delta(originalElementVersion, diffTargetElementVersion, targetStatus, diffType);
			deltasMap.put(fullName, delta);

		}

		return delta;
	}

}
