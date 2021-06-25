package mx.uam.archinaut.model.dto;

import java.util.ArrayList;
import java.util.List;

public class DesignStructureMatrixDTO {
	private String folderName;
	private String fileName;
	private String matrixName;
	private String elementsNamePrefix;
	private String metricsPrefix;
	private String logPrefix;
	private List<String> exclusionStrings = new ArrayList<>();

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMatrixName() {
		return matrixName;
	}

	public void setMatrixName(String matrixName) {
		this.matrixName = matrixName;
	}

	public String getElementsNamePrefix() {
		return elementsNamePrefix;
	}

	public void setElementsNamePrefix(String elementsNamePrefix) {
		this.elementsNamePrefix = elementsNamePrefix;
	}

	public String getMetricsPrefix() {
		return metricsPrefix;
	}

	public void setMetricsPrefix(String metrixPrefix) {
		this.metricsPrefix = metrixPrefix;
	}

	public String getLogPrefix() {
		return logPrefix;
	}

	public void setLogPrefix(String logPrefix) {
		this.logPrefix = logPrefix;
	}

	public boolean addExclusionString(String exclusionString) {
		return exclusionStrings.add(exclusionString);
	}

	public List<String> getExclusions() {
		return exclusionStrings;
	}

}
