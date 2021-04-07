package mx.uam.archinaut.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.uam.archinaut.data.loader.TrendsAnalysisLoader;
import mx.uam.archinaut.model.ArchitecturalDiff;
import mx.uam.archinaut.model.ArchitecturalDiff.DiffType;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.ElementMetric;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.TrendsAnalysis;
import mx.uam.archinaut.model.dto.DesignStructureMatrixDTO;
import mx.uam.archinaut.model.dto.TrendsAnalysisDTO;

/**
 * 
 * @author humbertocervantes
 *
 */
@Service
public class TrendsAnalysisService {
	
	private static final Logger logger = LoggerFactory.getLogger(TrendsAnalysisService.class);

	@Autowired
	private TrendsAnalysisLoader loader;
	
	@Autowired
	private DesignStructureMatrixService dsmService;
		
	/**
	 * 
	 * @param currentMatrix
	 * @param previousMatrix
	 * @return
	 */
	public boolean calculateDiffs(DesignStructureMatrix currentMatrix, DesignStructureMatrix previousMatrix) {
		
		ArchitecturalDiff currentMatrixDiff = new ArchitecturalDiff(currentMatrix, previousMatrix, DiffType.DIFFTARGET_OLDER);
		ArchitecturalDiff previousDiff = new ArchitecturalDiff(previousMatrix, currentMatrix, DiffType.DIFFTARGET_NEWER);
		
		
		currentMatrix.setDiff(currentMatrixDiff);
		previousMatrix.setDiff(previousDiff);
		
		return true;
		
	}

	/**
	 * 
	 * @param matrixes
	 * @return
	 */
	public TrendsAnalysis calculateTrendsAnalysis(List <DesignStructureMatrix> matrixes) {
		
		if(matrixes.size()<2) {
			return null;
		}
		
		return new TrendsAnalysis(matrixes);
		
		
	}
	
	/**
	 * Creates a matrix whose elements contain delta values between the current and the previous
	 * 
	 * @param current
	 * @param previous
	 * @return
	 */
	public DesignStructureMatrix createDeltaMatrix(DesignStructureMatrix current, DesignStructureMatrix previous) {
		
		ArrayList <MatrixElement> deltaElements = new ArrayList <> ();
		
		Map <ElementMetric,Integer> maxValuesMap = new HashMap <>();
		Map <ElementMetric,Integer> minValuesMap = new HashMap <>();
		
		// Initialize the maximum values map with 0
		for(ElementMetric metric:ElementMetric.values()) {
			maxValuesMap.put(metric, 0);
			minValuesMap.put(metric, 0);
		}
		
		for(MatrixElement currentElement:current.getElements()) {
			MatrixElement deltaElement = new MatrixElement(currentElement.getFullName());
			deltaElements.add(deltaElement);
			MatrixElement previousElement = previous.getElementByFullname(currentElement.getFullName());
			// The element did not exist previously, so all the metrics will have a baseline value
			if(previousElement == null) {
				for(ElementMetric metric:ElementMetric.values()) {
					deltaElement.setMetricValue(metric, 0);
				}
			} else {
				for(ElementMetric metric:ElementMetric.values()) {
					int currentValueForMetric = currentElement.getMetricValue(metric);
					int previousValueForMetric = previousElement.getMetricValue(metric);
					int deltaValue = currentValueForMetric - previousValueForMetric;
					
					// Check if this is a new maximum
					if(deltaValue >= 0) {
						int maxValue = maxValuesMap.get(metric);
						if(deltaValue > maxValue) {
							maxValuesMap.replace(metric, deltaValue);
						}
					} else {
						int minValue = minValuesMap.get(metric);
						if(deltaValue < minValue) {
							minValuesMap.replace(metric, deltaValue);
						}
					}
					
					deltaElement.setMetricValue(metric, deltaValue);
				}
			}
		}
		
		DesignStructureMatrix matrix = new DesignStructureMatrix("delta_"+current.getName(),current.getFileName(),deltaElements);
		
		// Update maximums
		for(ElementMetric metric:maxValuesMap.keySet()) {
			matrix.setMaximumElementValue(metric, maxValuesMap.get(metric));
			matrix.setMinumElementValue(metric, minValuesMap.get(metric));
		}
		
		return matrix;
	}



	/**
	 * Load a trend analysis
	 * 
	 * @param filename
	 * @return
	 */
	public TrendsAnalysis load(String filename, TrendsAnalysisProgressLoadObserver loadObserver) {
		
		List <DesignStructureMatrix> dsms= new ArrayList <> ();
		TrendsAnalysisDTO taDto = loader.loadFromJSON(filename);
		
		int index = 0;
		boolean loadRenames = false;
		
		// Matrixes are saved from oldest to newest, but we load newest to oldest
		// so that renames are loaded initially
		loadRenames = true;
		
		for(index = 0;index < taDto.getNumberOfSnapshots() ; index++) {
			logger.info("Loading snapshot "+(index+1)+" / "+taDto.getNumberOfSnapshots());			
			boolean continueExecution = loadObserver.snapshotLoaded(index+1, taDto.getNumberOfSnapshots());
			
			if(!continueExecution) {
				logger.info("Canceled");
				return null;
			}
			
			DesignStructureMatrixDTO matrixDto = taDto.getDesignStructureMatrixDTO(taDto.getNumberOfSnapshots()-1-index);
			
			// Calculate full path
			Path basePath = Paths.get(filename).getParent(); // Get the path of the trendsanalysis filename
			String folderName = matrixDto.getFolderName(); // partial path relative to the base path
			Path fullPath = basePath.resolve(folderName); // calculated full 
			
			//logger.info("Loading filename:"+matrixDto.getFileName()+" from path:"+fullPath);
			
			// TODO: Fix DTO structure for historic purposes
			DesignStructureMatrix matrix = dsmService.loadMatrixAndData(matrixDto.getMatrixName(),
					matrixDto.getFileName(), matrixDto.getElementsNamePrefix(), matrixDto.getMetricsPrefix(),
					matrixDto.getLogPrefix(),'/',loadRenames, matrixDto.getExclusions(),null, null, null, null, null, null, null);
			
			if(loadRenames) {
				loadRenames = false;
			}
			if(matrix!=null) {
				dsms.add(matrix);
			} 
		}

		return new TrendsAnalysis(dsms); 
	}
	
	/**
	 * Save a trend analysis
	 * 
	 */
	public boolean save(String fileName, TrendsAnalysis analysis) {
		
		Path basePath = Paths.get(fileName);
		logger.info("basePath = "+basePath);

		TrendsAnalysisDTO taDTO = new TrendsAnalysisDTO();
		for(int i = 0; i<analysis.getNumberOfSnapshots();i++) {
			DesignStructureMatrix current = analysis.getSnapshot(i);
			DesignStructureMatrixDTO matrixDTO = new DesignStructureMatrixDTO();
			
			// We must now calculate the relative path
			// TODO: Check if this fixes path generation
			Path absolutePath = Paths.get(current.getFileName()).getParent();
			Path relativePath = basePath.relativize(absolutePath);
			relativePath = relativePath.subpath(1, relativePath.getNameCount());
			Path matrixName = Paths.get(current.getName()).getFileName();

			logger.info("Saving path "+absolutePath+" to relative path:"+relativePath+" fileName = "+matrixName);
			
			matrixDTO.setMatrixName(current.getName());
			
			matrixDTO.setFileName(current.getFileName());
			matrixDTO.setFolderName(relativePath.toString());
			
			matrixDTO.setLogPrefix(current.getLogNamesPrefix());
			matrixDTO.setMetricsPrefix(current.getMetricsNamesPrefix());
			matrixDTO.setElementsNamePrefix(current.getElementNamesPrefix());
			
			for(String exclusion:current.getExclusionStrings()) {
				matrixDTO.addExclusionString(exclusion);
			}
			
			taDTO.addDesignStructureMatrixDTO(matrixDTO);		
		}
		
		return loader.saveToJSON(fileName, taDTO);
	}
	
	/**
	 * Generate a report
	 * 
	 * @param filename
	 * @param analysis
	 * @return
	 */
	public boolean generateTrendsAnalysisReport(String folderName, TrendsAnalysis analysis) {
		List <String []> report = analysis.generateMetricsSummaryReport(true);

		StringBuilder sb = new StringBuilder();

		for(String [] currentLine:report) {
			boolean first = true;
			for(String value:currentLine) {
				if(!first)
					sb.append(",");
				sb.append(value);
				first = false;
			}
			sb.append("\n");			
		}
		
		boolean result = loader.saveReport(folderName,"analysisSummary.csv", sb);
		logger.info("Saved analysisSummary.csv report: "+result);
		
		for(ElementMetric currentMetric:ElementMetric.values()) {
			report = analysis.generateMetricsReport(currentMetric, true);
	
			sb = new StringBuilder();
	
			for(String [] currentLine:report) {
				boolean first = true;
				for(String value:currentLine) {
					if(!first)
						sb.append(",");
					sb.append(value);
					first = false;
				}
				sb.append("\n");			
			}
			
			String fileName = currentMetric.getText()+"Summary.csv";
			result = loader.saveReport(folderName,fileName, sb);
			logger.info("Saved "+fileName+" report: "+result);
		}
		return result;
	}
	
}
