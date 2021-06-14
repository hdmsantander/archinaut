package mx.uam.archinaut.services;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.MetricsLoader;
import mx.uam.archinaut.data.nameprocessing.CharacterSubstituteNameProcessor;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.data.nameprocessing.PrefixRemovalNameProcessor;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.DesignStructureMatrixModel;
import mx.uam.archinaut.model.ElementMethod;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.yaml.Metric;
import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;
import mx.uam.archinaut.model.ElementMetric;

@Slf4j
@Service
public class MetricsService {
	
	@Autowired
	private MetricsLoader loader;
	

	public boolean saveMetricsReport(DesignStructureMatrix matrix, String fileName) {
		
		StringBuilder sb = new StringBuilder();
		
		// Generate the header for the csv file
		sb.append("Filename");
		
		for (String metric : matrix.getMetricNames()) {
			
			sb.append(",");
			sb.append(metric);
			
		}
		sb.append("\n");
		
		for (MatrixElement element : matrix.getElements()) {
			sb.append(element.getFullName());
			for(int metric:element.getMetrics().values()) {
				sb.append(",");
				sb.append(Integer.toString(metric));
			}
			sb.append("\n");
		}
		
		return loader.saveReport(fileName, sb);
		
	}
	
	public DesignStructureMatrix loadMetrics(List<YamlConfigurationEntry> configurationEntries, DesignStructureMatrix matrix) throws IOException, CsvValidationException {
		
		// Iterate over the configuration entries and load the metrics into the matrix
		for (YamlConfigurationEntry entry : configurationEntries) {
			
			// Handle CSV entries
			if (entry.getFormat().equalsIgnoreCase("CSV")) {
				loadCsvFileIntoMatrix(entry, matrix);
			}
			
		}
		
		return matrix;
	}
	
	private DesignStructureMatrix loadCsvFileIntoMatrix(YamlConfigurationEntry yamlConfiguration, DesignStructureMatrix matrix) throws IOException, CsvValidationException {
		
		// Get the current file loaded into the reader
		try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(yamlConfiguration.getFile()))) {
			
			Map<String, String> values = reader.readMap();
			
			while(values != null) {
				
				String fileName = values.get(yamlConfiguration.getFilenameMetricName());
				
				// Only proceed with this entry if there's a filename and that filename is in the matrix already
				if (!StringUtils.isBlank(fileName) && matrix.hasElementWithName(fileName)) {
					
					MatrixElement element = matrix.getElementByFullname(fileName);
											
					// For every metric in the configuration file, search for the value in the row and add it
					for (Metric metric : yamlConfiguration.getMetrics()) {
						
						// Create an element metric and add its value to this element
						ElementMetric elementMetric = new ElementMetric(metric.getRename(), Integer.parseInt(metric.getName()));
						element.addMetricValue(elementMetric);
									
					}
					
				}
				
				// Read the next line
				values = reader.readMap();
				
			}
			
			return matrix;
								
		} catch (CsvValidationException | IOException e) {
			log.error("Error loading " + yamlConfiguration.getFile(), e);
			throw e;
		}
		
	}
	
}
