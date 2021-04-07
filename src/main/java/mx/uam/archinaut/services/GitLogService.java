package mx.uam.archinaut.services;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.LogLoader;
import mx.uam.archinaut.data.loader.MetricsLoader;
import mx.uam.archinaut.data.nameprocessing.CharacterSubstituteNameProcessor;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.data.nameprocessing.PrefixRemovalNameProcessor;
import mx.uam.archinaut.model.DependencyMetric;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.ElementMetric;
import mx.uam.archinaut.model.MatrixDependencyGroup;
import mx.uam.archinaut.model.MatrixElement;

@Slf4j
@Service
public class GitLogService {
		
	private LogLoader loader;
	
	public GitLogService(LogLoader analizador) {
		this.loader = analizador;
	}
		
	
	/**
	 * Load constraints from JSON file
	 * 
	 * @param fileName
	 * @param matrix
	 */
	public int loadGitLogAnalysisFromCSV(DesignStructureMatrix matrix, String prefixToRemove,
			char pathSeparator, String frecuenciesFilename, String couplingFilename) {
		
		int matches = 0;
				
		NameProcessor processor = new PrefixRemovalNameProcessor(prefixToRemove);
		NameProcessor processor2 = new CharacterSubstituteNameProcessor('.','_');
		NameProcessor processor3 = new CharacterSubstituteNameProcessor(pathSeparator,'.');
		processor.addSuccessor(processor2);
		processor2.addSuccessor(processor3);
		
		List <String []> results;

		//logger.info("Loading commit frequencies ");

		
		results = loader.loadCommitFrequencies(processor, frecuenciesFilename);
		
		int maxRevs = 0;
		int maxChurn = 0;
		int maxBugCommits = 0;

		log.info("Loaded commit frequencies: "+results.size()+" results");
		
		for(String []result:results) {
			MatrixElement element = matrix.getElementByFullname(result[0]);
			if(element!=null) {
				matches++;
				// We first parse the revisions
				try {
					int revs = Integer.parseInt(result[1]);
					if(revs > maxRevs) {
						maxRevs = revs;
					}
					element.setMetricValue(ElementMetric.REVISIONS,revs);
				} catch(NumberFormatException ex) {
					log.warn("Could not parse revisions for element "+element);
				}
				// Parse churn added and removed
				try {
					int added = Integer.parseInt(result[2]);
					int removed = Integer.parseInt(result[3]);
					// https://core.ac.uk/download/pdf/80767743.pdf
					int churn = added+removed;
					if(churn > maxChurn) {
						maxChurn = churn;
					}
					element.setMetricValue(ElementMetric.CHURN,churn);
				} catch(NumberFormatException ex) {
					log.warn("Could not parse churn for element "+element);
				}
				// Parse bug commits
				try {
					int bugcommits = Integer.parseInt(result[4]);
					element.setMetricValue(ElementMetric.BUGCOMMITS,bugcommits);
					if(bugcommits > maxBugCommits) {
						maxBugCommits = bugcommits;
					}

				} catch(NumberFormatException ex) {
					log.warn("Could not parse churn for element "+element);
				}

			} 
		}
		
		matrix.setMaximumElementValue(ElementMetric.CHURN,maxChurn);
		matrix.setMaximumElementValue(ElementMetric.REVISIONS,maxRevs);
		matrix.setMaximumElementValue(ElementMetric.BUGCOMMITS,maxBugCommits);
		
		// Usar el cochanges 2!!
		
		boolean newCoChanges = false;
		results = loader.loadCoChanges2(couplingFilename, processor); //loader.loadCoChanges(folderName, processor, headless, couplingFilename);
		newCoChanges = true;
				
		if(results == null) {
			log.info("Not found old CoChanges file, using new format");
			results = loader.loadCoChanges2(couplingFilename, processor);		
			newCoChanges = true;
		}
		
		if(results!=null) {
			int maxCoChanges = 0;
			
			for(String []result:results) {
				
				for (String s : result) {
					System.out.print(s + " ");
				}
				System.out.println("");
				
				MatrixElement source = matrix.getElementByFullname(result[0].replaceFirst("src\\.", ""));
				if(source != null) {
					MatrixElement destination = matrix.getElementByFullname(result[1].replaceFirst("src\\.", ""));
					
					int coChanges = 0;
					
					if(!newCoChanges) {
						int degree = Integer.parseInt(result[2]);
						int averageRevs = Integer.parseInt(result[3]);
						float coChangesf = ((float)averageRevs*((float)degree/(float)100.0));
						coChanges = Math.round(coChangesf);
					} else {
						coChanges = Integer.parseInt(result[2]);
					}
					if(coChanges > maxCoChanges) {
						maxCoChanges = coChanges;
					}
	
					if(destination != null) {
						matches ++;
						
						// The dependency must be created on both ways, this is from source to destinations
						boolean foundDependency = false;
						for(MatrixDependencyGroup dependencyGroup: source.getDependencies()) {
							if(dependencyGroup.getDestination()==destination) {
								// There is already a dependency between these two elements
								dependencyGroup.setCoChanges(coChanges);
								foundDependency = true;
	
								break;
							}
						}
						if(!foundDependency) {
							// There is no structural dependency between these two elements,
							// we must create a temporal dependency
							MatrixDependencyGroup dependencyGroup = new MatrixDependencyGroup(source, destination);
	
							dependencyGroup.setCoChanges(coChanges);
							source.addDependency(dependencyGroup);
						}
	
						// The dependency must be created on both ways, this is from destination to source
						foundDependency = false;
						for(MatrixDependencyGroup dependencyGroup: destination.getDependencies()) {
							if(dependencyGroup.getDestination()==source) {
								// There is already a dependency between these two elements
								dependencyGroup.setCoChanges(coChanges);
								foundDependency = true;
	
								break;
							}
						}
						if(!foundDependency) {
							// There is no structural dependency between these two elements,
							// we must create a temporal dependency
							MatrixDependencyGroup dependencyGroup = new MatrixDependencyGroup(destination, source);
	
							dependencyGroup.setCoChanges(coChanges);
							destination.addDependency(dependencyGroup);
						}
	
					}
				}
			}
			
			matrix.setMaximumDependencyValue(DependencyMetric.COCHANGES,maxCoChanges);
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
	 * 
	 * @param matrix
	 * @param folderName
	 * @param prefix
	 * @param pathSeparator
	 * @param extension
	 * @return
	 */
	public boolean loadRenameFile(DesignStructureMatrix matrix, String renamedFilesFilename, String prefixToRemove, char pathSeparator) {

		NameProcessor processor = new PrefixRemovalNameProcessor(prefixToRemove);
		NameProcessor processor2 = new CharacterSubstituteNameProcessor('.','_');
		NameProcessor processor3 = new CharacterSubstituteNameProcessor(pathSeparator,'.');
		processor.addSuccessor(processor2);
		processor2.addSuccessor(processor3);

		
		Map  <String,List<String>> renames = new HashMap <>();
		List <String> renameList = loader.loadRenameFile(renamedFilesFilename);
		
		if(renameList.size()==0) {
			return false;
		}
		
		for(String currentRename:renameList) {
			StringTokenizer tokenizer = new StringTokenizer(currentRename,",");
			ArrayList <String> singleElementRenames = new ArrayList <> ();
			
			// First we separate each name
			while(tokenizer.hasMoreTokens()) {
				String nextName = tokenizer.nextToken();
				if(!nextName.endsWith(matrix.getFilesExtension())) {
					break;
				}
				nextName = processor.processName(nextName);
				singleElementRenames.add(nextName); 
			}
			
			if(singleElementRenames.size() > 0) {
			
				for(String singleElementName:singleElementRenames) {
					@SuppressWarnings("unchecked")
					ArrayList <String> tempArray = (ArrayList<String>) singleElementRenames.clone();
					tempArray.remove(singleElementName);
					renames.put(singleElementName,tempArray);
				}
			}
		}
		
		DesignStructureMatrix.setRenameMap(renames);
		
		log.info("Renames loaded :"+renames.size());

		/*
		Iterator<String> keys=renames.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			logger.info("Key :"+key+" values: "+renames.get(key));
			
		}*/
		
		return true;
	}


}
