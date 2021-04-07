package mx.uam.archinaut.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opencsv.CSVReaderHeaderAware;

import mx.uam.archinaut.data.nameprocessing.NameProcessor;

@Component
public class LogLoader {
	
	private static final String FILENAMEHEADER = "entity";
	private static final String REVISIONSHEADER = "n-revs";
	private static final String COUPLEDHEADER = "coupled";
	private static final String DEGREEHEADER = "degree";
	private static final String AVERAGEREVSHEADER = "average-revs";
	private static final String CHURNADDED = "added";
	private static final String CHURNREMOVED = "removed";
	private static final String COCHANGESHEADER = "cochanges";
	private static final String BUGCOMMITS = "bugs";
	
	private static final String DEFAULT_FREQUENCIES_FILENAME = "frequencies.csv";

	// The logger
	private static final Logger logger = LoggerFactory.getLogger(LogLoader.class);


	/**
	 * 
	 * @param foldername
	 * @return
	 */
	public String getSampleFilename(String foldername, String sampleName, String extension) {
		String fileName = "";

		/*
		// Infer extension
		StringTokenizer tokenizer = new StringTokenizer(sampleName,"_");
		String extension = "";
		while(tokenizer.hasMoreTokens()) {
			extension = tokenizer.nextToken();
		}
		logger.info("Inferred file extension:"+extension);
		*/

		
		try {
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(foldername+"/frequencies.csv"));
			Map<String, String> values = reader.readMap();
			while(!fileName.endsWith(extension)) {
				fileName = values.get(FILENAMEHEADER);
				values = reader.readMap();
			}
			reader.close();
		} catch(FileNotFoundException ex) {
			logger.error("getSampleFilename: Exception while loading: "+ex);
		} catch (Exception ex) {
			logger.error("getSampleFilename: Exception while loading: ",ex);
		}
		
		return fileName;
	}

	/**
	 * 
	 * @param foldername
	 * @param processor
	 * @return
	 */
	public List <String []> loadCommitFrequencies(NameProcessor processor, String frequenciesFilename) {
		
		ArrayList <String []> results = new ArrayList <>();
		
		try {
			
			CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(frequenciesFilename));
						
			Map<String, String> values = reader.readMap();
			while(values != null) {
				String fileName = values.get(FILENAMEHEADER);
				String revisions = values.get(REVISIONSHEADER);
				String className = processor.processName(fileName);
				String churnAdded = values.get(CHURNADDED);
				String churnRemoved = values.get(CHURNREMOVED);
				String bugCommits = values.get(BUGCOMMITS);

				String [] result= new String [] {className,revisions,churnAdded,churnRemoved,bugCommits};
				results.add(result);
				
				values = reader.readMap();
			}
			reader.close();

		} catch(FileNotFoundException ex) {
			logger.error("loadCommitFrequencies: Exception while loading: "+ex);
		} catch (Exception ex) {
			logger.error("loadCommitFrequencies: Exception while loading: ",ex);
		}		
		
		return results;
	
	}

	
	/**
	 * 
	 * @param foldername
	 * @param processor
	 * @return
	 */
	public List <String []> loadCoChanges(String foldername, NameProcessor processor, boolean headless, String filename) {
		ArrayList <String []> results = new ArrayList <>();
		
		File tempFile;
		
		if (!headless)
			tempFile = new File(foldername+"/coupling.csv");
		else
			tempFile = new File(filename);
		
		if(tempFile.exists()) {
		
			try {
				
				CSVReaderHeaderAware reader;
				
				if (!headless)
					reader = new CSVReaderHeaderAware(new FileReader(foldername+"/coupling.csv"));
				else
					reader = new CSVReaderHeaderAware(new FileReader(filename));
				
				Map<String, String> values = reader.readMap();
				while(values != null) {
					String source = values.get(FILENAMEHEADER);
					String destination = values.get(COUPLEDHEADER);
					String degree = values.get(DEGREEHEADER);
					String averageRevs = values.get(AVERAGEREVSHEADER);
	
					
					source = processor.processName(source);
					destination = processor.processName(destination);
					
					String [] result= new String [] {source,destination,degree,averageRevs};
					results.add(result);
					
					values = reader.readMap();
				}
				reader.close();
				
			} catch(FileNotFoundException ex) {
				logger.error("loadCoChanges: Exception while loading: "+ex);
			} catch (Exception ex) {
				logger.error("loadCoChanges: Exception while loading: ",ex);
			}		
			return results;
		}
		
		return null;

	}
	
	/**
	 * 
	 * @param foldername
	 * @param processor
	 * @return
	 */
	public List <String []> loadCoChanges2(String couplingFilename, NameProcessor processor) {
		ArrayList <String []> results = new ArrayList <>();
		
		
		// TODO: Check in a better way for a file
		File tempFile = new File(couplingFilename);
		if(tempFile.exists()) {
			
			try {
				CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(couplingFilename));
				Map<String, String> values = reader.readMap();
				while(values != null) {
					String source = values.get(FILENAMEHEADER);
					String destination = values.get(COUPLEDHEADER);
					String cochanges = values.get(COCHANGESHEADER);
					
					source = processor.processName(source);
					destination = processor.processName(destination);
					
					String [] result= new String [] {source,destination,cochanges};
					results.add(result);
					
					values = reader.readMap();
				}
				reader.close();
				
			} catch(FileNotFoundException ex) {
				logger.error("loadCoChanges2: Exception while loading: "+ex);
			} catch (Exception ex) {
				logger.error("loadCoChanges2: Exception while loading: ",ex);
			}		
			return results;
		}
		
		return null;

	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public List <String> loadRenameFile(String renamedFilesFilename) {
		
		ArrayList <String> renameArray = new ArrayList <>();
		
		try {
			Scanner scanner = new Scanner(new File(renamedFilesFilename));
			while (scanner.hasNextLine()) {
				renameArray.add(scanner.nextLine());
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			logger.error("LoadRenameFile"+ e);
		}
		
		return renameArray;
	}

}

