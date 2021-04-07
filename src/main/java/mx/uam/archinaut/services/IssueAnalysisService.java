package mx.uam.archinaut.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.DesignStructureMatrixModel;
import mx.uam.archinaut.model.ElementMetric;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.MatrixElement.HotspotData;
import mx.uam.archinaut.model.TrendsAnalysis;

@Service
public class IssueAnalysisService {
	
	
	private static float SIZEREDUCTIONTHRESHOLD=0.3f; //30%
	private static final Logger logger = LoggerFactory.getLogger(IssueAnalysisService.class);
	
	@Autowired
	private DesignStructureMatrixService dsmService;
		
	public boolean findRefactorings(TrendsAnalysis analysis) {
		
		logger.info("Finding refactorings");
		
		/*
		 * Steps of the algoritm
		 * Go from snapshot 1 to n (not including snapshot 0).
		 * For each snapshot:
		 * 1. Identify files which are present in the most recent snapshot whose size diminshed in a given percentage in a particular release
		 * 2. For that file get all the dependents that are new in this release
		 * 
		 * 
		 */
		
		DesignStructureMatrix latestMatrix = analysis.getMoreRecentSnapshot();
		
		for(int i=1;i<analysis.getNumberOfSnapshots();i++) {
			
			List <MatrixElement> candidates = new ArrayList <>();
			DesignStructureMatrix currentMatrix = analysis.getSnapshot(i);
			
			// First we identify the candidates
			for(MatrixElement element:currentMatrix.getElements()) {
				
				// We only do the processing if the element exists in the latest matrix
				if(latestMatrix.getElementByFullname(element.getFullName())!=null) {

					// We check if there has been a reduction in size for this element
					int delta = analysis.getDelta(element.getFullName(), ElementMetric.SIZE, i, i-1);
					
					if(delta < 0) {
						int previousSize = analysis.getMetricValue(element.getFullName(), ElementMetric.SIZE, i-1);
						int currentSize = analysis.getMetricValue(element.getFullName(), ElementMetric.SIZE, i);
						
						// We now calculate the percentage in reduction
						float percent = 1.0f - (float)currentSize / (float)previousSize;
						
						// The change is above the threshold, it is a candidate
						if(percent >= SIZEREDUCTIONTHRESHOLD) {
							
							//logger.info("Found candidate in snapshot:"+i+" name: "+element.getFullName()+ "reduction in size "+100*percent+"%");
							
							candidates.add(element);
						}
					}
				}
			}

			
			// We now check each one of the candidates
			for(MatrixElement candidate:candidates) {

				List <MatrixElement> refactoringDependents = new ArrayList <> ();

				DesignStructureMatrixModel dependentsModel = dsmService.getDependentsModel(currentMatrix, candidate);

				List <MatrixElement> dependents = dependentsModel.getListOfDependents(candidate);
				
				for(MatrixElement dependent:dependents) {
					// We have to check if the dependent was just created
					if(analysis.getSnapshot(i-1).getElementByFullname(dependent.getFullName())==null) {
						// This is a refactoring dependent!
						refactoringDependents.add(dependent);
					}
				}
				
				dependents = dependentsModel.getListOfDependees(candidate);
				
				for(MatrixElement dependent:dependents) {
					// We have to check if the dependent was just created
					if(analysis.getSnapshot(i-1).getElementByFullname(dependent.getFullName())==null) {
						// This is a refactoring dependent!
						refactoringDependents.add(dependent);
					}
				}
				
				if(refactoringDependents.size()>0) {
					
					//int delta = analysis.getDelta(candidate.getFullName(), ElementMetric.SIZE, i, i-1);
					
					int previousSize = analysis.getMetricValue(candidate.getFullName(), ElementMetric.SIZE, i-1);
					int currentSize = analysis.getMetricValue(candidate.getFullName(), ElementMetric.SIZE, i);
					
					// We now calculate the percentage in reduction
					float percent = 1.0f - (float)currentSize / (float)previousSize;

					
					System.out.println("Element "+candidate+" Went from "+previousSize+" LOC to "+currentSize+" LOC (-"+100*percent+"%)  in snapshot "+analysis.getSnapshot(i).getName()+" ("+i+")");
					System.out.println("New elements with which it depends:");
					for(MatrixElement element:refactoringDependents) {
						System.out.println(" - "+element.getFullName()+" Size: "+analysis.getMetricValue(element.getFullName(), ElementMetric.SIZE, i));
					}
					System.out.println("----------------");
				}
			}
		}
		
		return true;
		
	}
	
	/**
	 * 
	 * @param analysis
	 * @param metrics list of metrics that have to be analyzed
	 * @param minSnapshots number of snapshots where deviation must be bigger than standard deviation
	 * @return
	 */
	public int findHotspots(TrendsAnalysis analysis, ElementMetric [] metrics, int minSnapshots, int minDeviations, boolean ignoreTestFiles) {
		
		// Remove previous hotspot selection
		DesignStructureMatrix matrix = analysis.getMoreRecentSnapshot();
		for(MatrixElement element:matrix.getElements()) {
			element.setHotspot(false);
		}
		
		Set <String> initialCandidates = new HashSet <String> ();
		
		// Step 1: We get a list of candidates for the first candidate
		Map <String,HotspotData> firstMetricCandidates = findHotspotCandidates(analysis, metrics[0], minDeviations, minSnapshots);
		logger.info("Found "+firstMetricCandidates.size()+" cantidates for "+metrics[0].getText());

		List <Map <String,HotspotData>> lists = null;
		
		// Step 2: If there are other metrics we check if the files that we identified for the first metric are also
		// present for the other metrics
		if(metrics.length > 1) {
			lists = new ArrayList <> ();
			
			// First we create lists for each metric
			for(int i = 1; i < metrics.length; i++) {
				Map <String,HotspotData> nextMetricCandidates = findHotspotCandidates(analysis, metrics[i], minDeviations, minSnapshots);
				logger.info("Found "+firstMetricCandidates.size()+" cantidates for "+metrics[i].getText());
				lists.add(nextMetricCandidates);

			}

			// We check if the files are present in all of the lists
			for(String elementName:firstMetricCandidates.keySet()) {
				
				boolean present = true;
				
				for(Map <String,HotspotData> nextMetricCandidates:lists) {
					if(!nextMetricCandidates.keySet().contains(elementName)) {
						present = false;
						break;
					}					
				}
				
				if(present) {
					//logger.info("New candidate: "+elementName);
					initialCandidates.add(elementName);
				}
			}

		} else {
			initialCandidates = firstMetricCandidates.keySet();
		}
		
		
		// Step 3: We filter the test files
		
		String extension = analysis.getMoreRecentSnapshot().getFilesExtension();

		if(ignoreTestFiles) {
			Set <String> tempCandidates = new HashSet <String> ();

			if(extension.equals("java")) {
				for(String elementName:initialCandidates) {
					if(!elementName.contains("test.")) {
						tempCandidates.add(elementName);
					}
				}
			}
			
			logger.info("removed "+(initialCandidates.size()-tempCandidates.size())+" test files");
			initialCandidates = tempCandidates;
		}
		

		
		logger.info("Final list of candidates contains : "+initialCandidates.size());
		
		
		// Step 3: We set the hotspot flag for the candidates
		for(String elementName:initialCandidates) {
			MatrixElement element = matrix.getElementByFullname(elementName);
			if(element != null) {
				element.setHotspot(true);
				
				// We retrieve the HotspotData for each one of the lists
				Set <HotspotData> hotspotData = new HashSet <>();
				hotspotData.add(firstMetricCandidates.get(elementName));
				
				if(lists != null) {
					for(Map <String,HotspotData> nextMetricCandidates:lists) {
						hotspotData.add(nextMetricCandidates.get(elementName));
					}
				}
				element.setHotspotData(hotspotData);				
			}
		}
		
		
		
		return initialCandidates.size();
	
	}
	
	/**
	 * 
	 * @param analysis
	 * @param metric
	 * @param minSnapshots
	 * @param minDeviations
	 * @return
	 */
	private Map <String,HotspotData> findHotspotCandidates(TrendsAnalysis analysis,ElementMetric metric, int minSnapshots, int minDeviations) {
		
		double mean [] = new double [analysis.getNumberOfSnapshots()];
		double standardDev [] = new double [analysis.getNumberOfSnapshots()];
		
		// Step 1: Calculation of average deltas and standard deviation across snapshots
		for(int i = 1; i < analysis.getNumberOfSnapshots() ; i++ ) {
			
			List <Double> consideredDeltas = new ArrayList <>();
			
			DesignStructureMatrix currentMatrix = analysis.getSnapshot(i);
			DesignStructureMatrix previousMatrix = analysis.getSnapshot(i-1);
			
			// First we check if the element was already present in the previous matrix
			// because we do not want to include the initial size as part of the
			// delta calculation
			for(MatrixElement element:currentMatrix.getElements()) {
				
				MatrixElement previousElement = previousMatrix.getElementByFullname(element.getFullName());
				
				if(previousElement != null) {
					double delta = analysis.getDelta(element.getFullName(), metric, i, i-1);
					consideredDeltas.add(delta);
				}				
			}

			// We perform a calculation of the mean and standard deviation
			Mean meanCalculator = new Mean();
			StandardDeviation stdevCalculator = new StandardDeviation(); // true because we consider only a sample of the population
			
			double deltaResults [] = new double [consideredDeltas.size()];

			int index = 0;
			for(double value:consideredDeltas) {
				deltaResults[index]=value;
				index++;
			}
					
			mean[i] = meanCalculator.evaluate(deltaResults);
			standardDev[i] = stdevCalculator.evaluate(deltaResults);
		}

		logger.info("Totals for "+metric.getText());
		
		Map <String,HotspotData> candidates = new HashMap <String,HotspotData> ();

		// Step 2: Filtering of files that surpass the standard deviation threshold (minDeviations)
		for(int i = 1; i < analysis.getNumberOfSnapshots() ; i++ ) {
			
			DesignStructureMatrix currentMatrix = analysis.getSnapshot(i);
			DesignStructureMatrix previousMatrix = analysis.getSnapshot(i-1);
			
			float totalCurrent = currentMatrix.getTotalForMetric(metric);
			float totalPrevious = previousMatrix.getTotalForMetric(metric);
			float percentageChange = 100 * (totalCurrent - totalPrevious)/totalPrevious;
			
			logger.info(analysis.getSnapshot(i).getName()+" Average delta: "+mean[i]+" standard deviation:"+standardDev[i]+" project percentage Change:"+percentageChange);
			
			for(MatrixElement element:currentMatrix.getElements()) {
				
				// First we check if the element was already present in the previous matrix
				MatrixElement previousElement = previousMatrix.getElementByFullname(element.getFullName());
				
				if(previousElement != null) {

					double delta = analysis.getDelta(element.getFullName(), metric, i, i-1);
					double deviationsFromMean = (delta - mean[i])/standardDev[i];
					
					// The element is candidate as it has surpassed the minumum number of deviations
					if(deviationsFromMean >= minDeviations) {

						// The first time this element is encountered, we add it to the list of candidates
						if(candidates.get(element.getFullName())==null) {
							
							// We multiply the number of deviations from the mean by the index of the
							// array so that newer values get a higher multiplication factor							
							candidates.put(element.getFullName(),element.new HotspotData(metric,1,i*deviationsFromMean));
						} else {
							// The element had already been encountered before, we update its values
							HotspotData currentPoint = candidates.get(element.getFullName());
							currentPoint.setDeviationTimes(currentPoint.getDeviationTimes()+1); 
							currentPoint.setDeviationIndex(currentPoint.getDeviationIndex()+(i*deviationsFromMean));
						}						
					}
				}				
			}
		}
		
		Map <String,HotspotData> finalCandidates = new HashMap <> ();
		
		DesignStructureMatrix matrix = analysis.getMoreRecentSnapshot();
		Iterator<String> elementNames = candidates.keySet().iterator();		
		
		// Step 3: Filtering of candidates that surpass the standard deviaton
		// threshold a minimum amount of times (minSnapshots)
		while(elementNames.hasNext()) {
			String elementName = elementNames.next();
			HotspotData currentPoint = candidates.get(elementName);
			
			if(currentPoint.getDeviationTimes() >= minSnapshots) { 
				MatrixElement element = matrix.getElementByFullname(elementName);
				if(element != null) {
					finalCandidates.put(elementName,candidates.get(elementName));
				}
			}
		}
		return finalCandidates;
	}

}
