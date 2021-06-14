package mx.uam.archinaut.data.loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.opencsv.CSVReaderHeaderAware;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;

@Slf4j
@Component
public class MetricsLoader {
	
	/**
	 * 
	 * @param foldername
	 * @param filename
	 * @param report
	 * @return
	 */
	public boolean saveReport(String filename, StringBuilder report) {
		
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