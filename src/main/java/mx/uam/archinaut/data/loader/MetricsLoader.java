package mx.uam.archinaut.data.loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.opencsv.CSVReaderHeaderAware;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.ElementMetric;

@Slf4j
@Component
public class MetricsLoader {
	
	private static final String FILENAME_HEADER = "File Name";
	private static final String FILESIZE_HEADER = "LOC";
	private static final String FILECOMPLEXITY_HEADER = "WMC";
	private static final String CODESMELL_HEADER = "Code Smell";
	private static final String METHODNAME_HEADER = "Method Name";
	private static final String METHODSIZE_HEADER = "LOC";
	private static final String METHODCOMPLEXITY_HEADER = "CC";
	private static final String METHODPARAMETERS_HEADER = "PC";
	private static final String REFACTOROPS_HEADER = "Refactoring Operations";

	private static final String DEFAULT_SCC_FILENAME = "scc-files.csv";
	
	/**
	 * 
	 * 
	 * @param foldername
	 * @param matrix
	 * @param processor
	 * @return number of files that matched
	 */
	public int loadTypeMetrics(String filename, DesignStructureMatrix matrix, NameProcessor processor) {
		
		int matches = 0;
		int maxSize = 0;
		int maxComplexity = 0;
		
		try {
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(filename));
			Map<String, String> values = reader.readMap();

			while(values != null) {
				
				String fileName = values.get(FILENAME_HEADER);
				if(!fileName.equals("")) {
					int size = Integer.parseInt(values.get(FILESIZE_HEADER));
					int complexity = Integer.parseInt(values.get(FILECOMPLEXITY_HEADER));
	
					String className = processor.processName(fileName);
					MatrixElement element = matrix.getElementByFullname(className);
					if(element!=null) { // if null, the element was not found
						matches++;
						
						// We load previous values if they exist, this is to
						// consider inner classes
						size = size+element.getMetricValue(ElementMetric.SIZE);
						complexity = complexity+element.getMetricValue(ElementMetric.COMPLEXITY);
						
						if(size > maxSize) {
							maxSize = size;
						}
						element.setMetricValue(ElementMetric.SIZE,size);
						if(complexity > maxComplexity) {
							maxComplexity = complexity;
						}
						element.setMetricValue(ElementMetric.SIZE,size);
	
						element.setMetricValue(ElementMetric.COMPLEXITY,complexity);
					} 			
				}
				values = reader.readMap();
			}
			
			reader.close();
			
		} catch(FileNotFoundException ex) {
			log.error("loadTypeMetrics: Exception while loading: "+ex);
		} catch (Exception ex) {
			log.error("loadTypeMetrics: Exception while loading: ",ex);
			return 0;
		}		
		
		log.info("Maximum Size:"+maxSize);
		matrix.setMaximumElementValue(ElementMetric.SIZE,maxSize);
		log.info("Maximum Complexity:"+maxComplexity);
		matrix.setMaximumElementValue(ElementMetric.COMPLEXITY,maxComplexity);
		
		return matches;
	}
	
	/**
	 * 
	 * 
	 * @param foldername
	 * @param matrix
	 * @param processor
	 * @return number of files that matched
	 */
	public List <String []> loadDesignSmells(String foldername) {
		
		ArrayList <String []> results = new ArrayList <> ();
		
		try {
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(foldername+"/designCodeSmells.csv"));
			// File Name	Project Name	Package Name	Type Name	Code Smell
			Map<String, String> values = reader.readMap();
			while(values != null) {
				String fileName = values.get(FILENAME_HEADER);
				String codeSmell = values.get(CODESMELL_HEADER);
				
				String [] result = {fileName, codeSmell};
				
				results.add(result);
				
				values = reader.readMap();
			}
			reader.close();
			
		} catch(FileNotFoundException ex) {
			log.error("loadDesignSmells: Exception while loading: "+ex);
		} catch (Exception ex) {
			log.error("loadDesignSmells: Exception while loading: ",ex);
		}		
		
		return results;
	}

	/**
	 * 
	 * 
	 * @param foldername
	 * @param matrix
	 * @param processor
	 * @return number of files that matched
	 */
	public List <String []> loadMethodMetrics(String methodMetricsFilename) {
		
		ArrayList <String []> results = new ArrayList <> ();
		
		try {
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(methodMetricsFilename));
			// File Name	Method Name	LOC	CC	PC
			Map<String, String> values = reader.readMap();
			while(values != null) {
				String fileName = values.get(FILENAME_HEADER);
				String methodName = values.get(METHODNAME_HEADER);
				String size = values.get(METHODSIZE_HEADER);
				String complexity = values.get(METHODCOMPLEXITY_HEADER);
				String params = values.get(METHODPARAMETERS_HEADER);
				
				String [] result = {fileName, methodName, size, complexity, params};
				
				results.add(result);
				
				values = reader.readMap();
			}
			reader.close();
			
		} catch(FileNotFoundException ex) {
			log.error("loadMethodMetrics: Exception while loading: "+ex);
		} catch (Exception ex) {
			log.error("loadMethodMetrics: Exception while loading: ",ex);
		}
		
		
		return results;
	}


	/**
	 * 
	 * @param foldername
	 * @return
	 */
	public String getSampleFilename(String typeMetricsFilename, String sampleName, String extension) {
				
		String fileName = "";
		
		try {
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(typeMetricsFilename));
			Map<String, String> values = reader.readMap();
			while(!fileName.endsWith(extension)) {
				fileName = values.get(FILENAME_HEADER);
				values = reader.readMap();
			}
			reader.close();
		} catch(FileNotFoundException ex) {
			log.error("getSampleFilename: Exception while loading: "+ex);
		} catch (Exception ex) {
			log.error("getSampleFilename: Exception while loading: ",ex);
		}
		
		return fileName;
	}
	
	/**
	 * 
	 * @param foldername
	 * @param filename
	 * @param report
	 * @return
	 */
	public boolean saveReport(String filename, StringBuilder report) {
		
//		logger.info("Saving report to folder: "+foldername+" and file "+filename);
		File file = new File(filename);
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(file));
		    writer.write(report.toString());
		    writer.close();
		} catch(Exception ex) {
			log.error("MetricsLoader.saveReport",ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * 
	 * @param foldername
	 * @param matrix
	 * @param processor
	 * @return number of files that matched
	 */
	public List <String []> loadSccMetrics(String filename, NameProcessor processor) {
		
		log.info("Loading SCC info from: " + filename);
		
		ArrayList <String []> results = new ArrayList <> ();
		
		try {
			
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(filename));
			
			
			// File Name	Method Name	LOC	CC	PC
			Map<String, String> values = reader.readMap();
			while(values != null) {
				
				String fileName = processor.processName(values.get("Location"));
				String lines = values.get("Lines");
				String code = values.get("Code");
				String complexity = values.get("Complexity");
				
				String [] result = {fileName, lines, code, complexity };
				
				log.info("Result: " + result[0] + " Lines: " + result[1] + " Code: " + code + " Complexity: " + complexity);
				
				results.add(result);
				
				values = reader.readMap();
			}
			reader.close();
			
		} catch (Exception ex) {
			log.error("loadStructure101Metrics: Exception while loading",ex);
		}		
		
		
		return results;
	}



}