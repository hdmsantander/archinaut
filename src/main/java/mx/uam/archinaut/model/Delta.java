package mx.uam.archinaut.model;

import mx.uam.archinaut.model.ArchitecturalDiff.DiffType;
import mx.uam.archinaut.model.MatrixElement.ElementStatus;

public class Delta {

	public enum ChangeStatus {
		DELETED, CHANGED_DECREASE, CHANGED_INCREASE, ADDED, RENAMED, NONE;

		private int index;

		private static ChangeStatus[] list = ChangeStatus.values();

		static {
			int i = 0;
			for (ChangeStatus e : values()) {
				e.index = i;
				i++;
			}

		}

		public static ChangeStatus valueOf(int changeStatus) {
			return list[changeStatus];
		}

		public int getValue() {
			return index;
		}
	}

	private ChangeStatus elementChangeStatus = ChangeStatus.NONE;

	private MatrixElement original;

	private MatrixElement diffTarget;

	private DiffType diffType;

	private ElementStatus targetElementStatus;

	/**
	 * 
	 * Create a Delta between two elements. If diffType is DIFFTARGET_OLDER, the
	 * target element is from a previous version, if it is DIFFTARGET_NEWER it is
	 * from a more recent version.
	 * 
	 * @param original
	 * @param diffTarget can be DIFFTARGET_OLDER or DIFFTARGET_NEWER
	 */
	public Delta(MatrixElement original, MatrixElement diffTarget, ElementStatus status, DiffType diffType) {

		this.original = original;
		this.diffTarget = diffTarget;
		this.diffType = diffType;
		this.targetElementStatus = status;
	}

	public MatrixElement getOriginalElementVersion() {
		return original;
	}

	public MatrixElement getDiffTargetElementVersion() {
		return diffTarget;
	}

	public String toString() {
		return "Delta for element " + original + " Status:" + elementChangeStatus;
	}

}
