package mx.uam.archinaut.services;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.MetricsLoader;
import mx.uam.archinaut.data.nameprocessing.CharacterSubstituteNameProcessor;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.data.nameprocessing.PrefixRemovalNameProcessor;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.DesignStructureMatrixModel;
import mx.uam.archinaut.model.ElementMethod;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.ElementMetric;

@Slf4j
@Service
public class MetricsService {
	
	@Autowired
	private MetricsLoader loader;
		
	/**
	 * Load constraints from JSON file
	 * 
	 * @param fileName
	 * @param matrix
	 */
	public int loadMethodMetricsFromCSV(String typeMetricsFilename, String designSmellsFilename, String methodMetricsFilename, DesignStructureMatrix matrix, String prefixToRemove, char pathSeparator) {
		
		log.info("Loading metrics, prefix:"+prefixToRemove);
		
		int matches = 0;
				
		NameProcessor processor = new PrefixRemovalNameProcessor(prefixToRemove);
		NameProcessor processor2 = new CharacterSubstituteNameProcessor('.','_');
		NameProcessor processor3 = new CharacterSubstituteNameProcessor(pathSeparator,'.');
		processor.addSuccessor(processor2);
		processor2.addSuccessor(processor3);
		
		matches = loader.loadTypeMetrics(typeMetricsFilename, matrix, processor);
		
		int maxSmells = 0;
		
		List <String []> designSmells = loader.loadDesignSmells(designSmellsFilename);
		
		for(String [] designSmellEntry: designSmells) {
			String elementName = designSmellEntry [0];
			String designSmell = designSmellEntry [1];
			
			elementName = processor.processName(elementName);
						
			MatrixElement element = matrix.getElementByFullname(elementName);
			if(element != null) {
				matches ++;
				element.addDesignSmell(designSmell);
				if(element.getMetricValue(ElementMetric.DESIGNSMELLS) > maxSmells) {
					maxSmells = element.getMetricValue(ElementMetric.DESIGNSMELLS);
				}
			}
		}

		matrix.setMaximumElementValue(ElementMetric.DESIGNSMELLS, maxSmells);
		
		List <String []> methodMetrics = loader.loadMethodMetrics(methodMetricsFilename);

		MatrixElement currentElement = null;
		
		for(String [] methodMetricsEntry: methodMetrics) {
			
			
			String elementName = methodMetricsEntry [0];
			String methodName = methodMetricsEntry [1];
			int size = Integer.parseInt(methodMetricsEntry [2]);
			int complexity = Integer.parseInt(methodMetricsEntry [3]);
			int params = Integer.parseInt(methodMetricsEntry [4]);
			
			elementName = processor.processName(elementName);
			
			// This is to avoid some searches as the CSV file typically
			// includes all the methods of the same element one after the other
			if(currentElement == null) {
				currentElement = matrix.getElementByFullname(elementName);
			} else {
				if(!currentElement.getFullName().equals(elementName)) {
					currentElement = matrix.getElementByFullname(elementName);
				}
			}

			if(currentElement != null) {
				ElementMethod method = new ElementMethod(methodName,size,complexity,params);
				currentElement.addMethod(method);
				}
			}
				
		return matches;
	}
		
	/**
	 * 
	 * @param folderName
	 * @param matrix
	 * @return
	 */
	public String getPrefixRemovalSuggestion(String folderName, DesignStructureMatrix matrix) {
		return loader.getSampleFilename(folderName, matrix.getElement(0).getFullName(), matrix.getFilesExtension());
		
	}
	
	/**
	 * This method calculates the different numbers for the dependencies in the elements
	 * and assigns it to the elements themselves
	 * 
	 * @param matrix
	 */
	public void calculateDependenciesForElements(DesignStructureMatrix matrix) {
		
		ArrayList <MatrixElement> elements = new ArrayList <> ();
		
		int maxDependents = 0;
		int maxDependees = 0;
		int maxDependencies = 0;
		int maxCoChangePartners = 0;
		
		for(MatrixElement element: matrix.getElements()) {
			elements.add(element);
		}
		
		// We create a temporal DesignStructureMatrixModel which only contains the actual
		// elements in the matrix (no groupings) as the DesignStructureMatrixModel already
		// provides methods for dependencies calculation
		DesignStructureMatrixModel tempModel = new DesignStructureMatrixModel(matrix, elements);
		
		for(MatrixElement element:tempModel.getElements()) {
			int dependents = tempModel.calculateNumberOfDependents(element);
			if(dependents > maxDependents) maxDependents = dependents;

			int dependees = tempModel.calculateNumberOfDependees(element);
			if(dependees > maxDependees) maxDependees = dependees;
			
			if( dependents+dependees > maxDependencies ) maxDependencies = dependents + dependees;

			int coChangePartners = tempModel.calculateNumberOfCoChangePartners(element);
			if(coChangePartners > maxCoChangePartners) maxCoChangePartners = coChangePartners;
			
			
			element.setMetricValue(ElementMetric.DEPENDENT,dependents);
			element.setMetricValue(ElementMetric.DEPENDSON,dependees);
			element.setMetricValue(ElementMetric.DEPENDENCIES,dependents+dependees);

			element.setMetricValue(ElementMetric.COCHANGEPARTNERS,coChangePartners);
			
		}
		matrix.setMaximumElementValue(ElementMetric.DEPENDENT, maxDependents);
		matrix.setMaximumElementValue(ElementMetric.DEPENDSON, maxDependees);
		
		matrix.setMaximumElementValue(ElementMetric.DEPENDENCIES, maxDependencies);
		matrix.setMaximumElementValue(ElementMetric.COCHANGEPARTNERS, maxCoChangePartners);
	}


	public boolean saveMetricsReport(DesignStructureMatrix matrix, String fileName) {
		
		StringBuilder sb = new StringBuilder();
		
		// Generate the header for the csv file
		sb.append("Filename");		
		for(ElementMetric metric:ElementMetric.values()) {
			sb.append(",");
			sb.append(metric.getText());
		}
		sb.append("\n");
				
		for(MatrixElement element:matrix.getElements()) {
			sb.append(element.getFullName());
			for(int metric:element.getMetrics()) {
				sb.append(",");
				sb.append(Integer.toString(metric));
			}
			sb.append("\n");
		}

		return loader.saveReport(fileName, sb);
		
	}
	
	
	
	/**
	 * Load dv8 metrics from JSON file
	 * 
	 * @param fileName
	 * @param matrix
	 */
	public int loadSccFromCSV(String filename, DesignStructureMatrix matrix, String prefixToRemove, char pathSeparator) {
		
		NameProcessor processor = new PrefixRemovalNameProcessor(prefixToRemove);
		NameProcessor processor2 = new CharacterSubstituteNameProcessor('.','_');
		NameProcessor processor3 = new CharacterSubstituteNameProcessor(pathSeparator,'.');
		processor.addSuccessor(processor2);
		processor2.addSuccessor(processor3);

		int matches = 0;
		
		List <String []> sccMetrics = loader.loadSccMetrics(filename, processor);
		
		int maxLines = 0;
		int maxCode = 0;
		int maxComplexity = 0;

		
		for(String [] sccMetricsEntry: sccMetrics) {
			String elementName = sccMetricsEntry [0];
			int lines = Integer.parseInt(sccMetricsEntry [1]);
			int code = Integer.parseInt(sccMetricsEntry [2]);
			int complexity = Integer.parseInt(sccMetricsEntry [3]);
						
			MatrixElement element = matrix.getElementByFullname(elementName);
			
			log.info("Element name: "+ elementName + " Lines: " + lines + " Code: " + code + " Complex: " + complexity + " Element is " + element);
			
			if(element!=null) {
				
				matches++;
				
				if(lines > maxLines) {maxLines = lines;}
				if(code > maxCode) {maxCode = code;}
				if(complexity > maxComplexity) {maxComplexity = complexity;}
				
				log.info("Lines: " + lines + " Code: " + code + " Complex: " + complexity);
				
								
				element.setMetricValue(ElementMetric.SCCLOC, lines);
				element.setMetricValue(ElementMetric.SCCCLOC, code);
				element.setMetricValue(ElementMetric.SCCCOMPLEXITY, complexity);
				
			}
		}
		
		matrix.setMaximumElementValue(ElementMetric.SCCLOC, maxLines);
		matrix.setMaximumElementValue(ElementMetric.SCCCLOC, maxCode);
		matrix.setMaximumElementValue(ElementMetric.SCCCOMPLEXITY, maxComplexity);
				
		return matches;
	}
	
}
