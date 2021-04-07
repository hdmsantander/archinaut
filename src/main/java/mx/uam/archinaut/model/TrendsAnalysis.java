package mx.uam.archinaut.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.uam.archinaut.model.MatrixElement.ElementStatus;

/**
 * 
 * @author humbertocervantes
 *
 */
public class TrendsAnalysis implements Serializable {
	
	
	private static final long serialVersionUID = 4901175863975841871L;

	public enum Trend {
		HIGH_DEGRADING,
		DEGRADING,
		STABLE,
		IMPROVING,
		HIGH_IMPROVING,
		UNKNOWN
	}
	
	// The logger
	private static final Logger logger = LoggerFactory.getLogger(TrendsAnalysis.class);
		
	// Matrixes that compose this trend analysis
	private List <DesignStructureMatrix> matrixes;
		
	// Hashmap for speeding up searches
	private Map <String, Integer> nameAndRowMap = new HashMap <>();
	
	// Matrix of elements, columns are versions, rows are elements
	private MatrixElement [][] elements ;
	
	/**
	 * 	
	 * @param matrixes
	 */
	public TrendsAnalysis(List <DesignStructureMatrix> matrixes) {
		
		this.matrixes = matrixes;
		
		// First matrix in the array is the most recent one
		DesignStructureMatrix currentMatrix = matrixes.get(0);
		
		// Rows is the number of elements in the most recent matrix
		// Columns is the number of versions
		elements = new MatrixElement [currentMatrix.getElementsCount()][matrixes.size()];
		
		int row = 0;
		for(MatrixElement element:currentMatrix.getElements()) {
			
			// Add all the element names of the matrix to the array
			String fullName = element.getFullName();
			nameAndRowMap.put(fullName,row);
			
			// Put the elements of the most recent matrix on the rightmost column
			elements [row][matrixes.size()-1] = element;
			
			for(int column = 0; column < matrixes.size()-1 ; column ++) {
				currentMatrix = matrixes.get(matrixes.size()-1-column);
				
				MatrixElement thisElement = currentMatrix.getElementByFullname(fullName);
				
				// If the element was not present in a previous snapshot, thisElement will be null
				elements [row][column] = thisElement;
			}
			row ++;
		}
	}
		
	/**
	 * Returns the values of the metric of an element across all snapshots
	 * if an element is not present in a particular snapshot, the value
	 * for that snapshot be Integer.MIN_VALUE
	 * 
	 * @param elementFullName
	 * @param type
	 * @return
	 */
	public int [] getResultsFor(String elementFullName, ElementMetric type, boolean useDeltas) {
		// First matrix in the array is the most recent one
		int index = nameAndRowMap.get(elementFullName);
		
		if(index == -1) {
			logger.warn("TrendsAnalysis.getResultsFor: Element not found "+elementFullName);
		}
				
		int [] result = new int [matrixes.size()];
		
		if(!useDeltas) {
		
			for(int i = 0; i < matrixes.size(); i++) {
				if(elements[index][i] != null) {
					result[i] = elements[index][i].getMetricValue(type);
				} else {
					
					// THe element is not present
					result[i] = Integer.MIN_VALUE;
				}
				
			}
		} else {
			for(int i = 0; i < matrixes.size(); i++) {
				if(i==0) {
					result[i]=getDelta(elementFullName,type,0,0); 
				} else {
					result[i]=getDelta(elementFullName,type,i,i-1);
				}
			}

		}
		
		return result;
		
	}

	/**
	 * Returns the total number of snapshots
	 * 
	 * @return
	 */
	public int getNumberOfSnapshots() {
		return matrixes.size();
	}
	
	/**
	 * Return the total number of elements which is given
	 * by the first snapshot (deleted elements will not appear)
	 * 
	 * @return
	 */
	public int getElementsCount() {
		return nameAndRowMap.size();
	}
	
	/**
	 * Return a matrix element in a given row
	 * 
	 * @param row
	 * @return
	 */
	public MatrixElement getElement(int row) {
		return elements[row][matrixes.size()-1];
	}
	
	/**
	 * Get the maximum value of a given metric
	 * across all snapshots
	 * 
	 * @param type
	 * @return
	 */
	public int getMaximumValue(ElementMetric type) {
	
		int max = 0;
		
		for(DesignStructureMatrix matrix:matrixes) {
			int currentValue = matrix.getMaximumElementValue(type);

			if(currentValue > max) {
				max = currentValue;
			}
		}
		
		return max;
	}
	
	/**
	 * Return a metric value for an element in a given snapshot
	 * 
	 * @param elementFullName
	 * @param type
	 * @param index
	 * @return
	 */
	public int getMetricValue(String elementFullName, ElementMetric type, int index) {
		DesignStructureMatrix matrix = getSnapshot(index);
		MatrixElement element = matrix.getElementByFullname(elementFullName);
		if(element != null) {
			return element.getMetricValue(type);
		} else {
			return Integer.MIN_VALUE;
		}
	}
	
	/**
	 * Calculate a difference in the value of a metric for an element between two indexes
	 * first index must be bigger than the second index as the snapshots go from older
	 * to more recent
	 * 
	 * 
	 * @param elementFullName
	 * @param type
	 * @param firstIndex index of newer snapshot 1
	 * @param secondIndex index of older snapshot 0
	 * @return
	 */
	public int getDelta(String elementFullName, ElementMetric type, int firstIndex, int secondIndex) {
				
		if(firstIndex != 0 && secondIndex == firstIndex)
			return 0;
		
		if(secondIndex >= matrixes.size() || firstIndex >= matrixes.size())
			throw new IndexOutOfBoundsException("Index bigger than matrix size first: "+firstIndex+" second: "+secondIndex+" size:"+matrixes.size());

		if(secondIndex < 0 || firstIndex < 0)
			throw new IndexOutOfBoundsException("Index less than 0 first: "+firstIndex+" second: "+secondIndex);
		
		if(firstIndex<secondIndex)
			throw new IndexOutOfBoundsException("first index smaller than second index first: "+firstIndex+" second: "+secondIndex);

		// We need to translate the name to the one in the more recentMatrix
		MatrixElement elementInFirst = getSnapshot(firstIndex).getElementByFullname(elementFullName);
		MatrixElement elementInSecond = getSnapshot(secondIndex).getElementByFullname(elementFullName);
		
		// The element is not present in any of the snapshots
		if(elementInFirst == null && elementInSecond == null) {
			return Integer.MIN_VALUE;
		}

		// The element is only present in the first snapshot (it has been added)
		if(firstIndex == 0 || elementInSecond == null) {
			return elementInFirst.getMetricValue(type);
		}

		// The element is only present in the second snapshot (it has been deleted)
		if(elementInFirst == null) {
			return 0-elementInSecond.getMetricValue(type);
		}

		return  elementInFirst.getMetricValue(type) - elementInSecond.getMetricValue(type);
		
	}
	
	/**
	 * Calculates the maximum and minimum values of deltas
	 * across all snapshots for a given metric
	 * 
	 * @param type
	 * @return array with maximum and minimum
	 */
	public int [] getDeltaMinimumAndMaximum(ElementMetric type) {
		
		int deltaMaximum = 0;		
		int deltaMinimum = 0;
		
		// This goes across all files
		for(String currentName:nameAndRowMap.keySet()) {
			int [] results = getResultsFor(currentName, type,false);
			for(int i = 1; i < results.length ; i++) {
				// We do not consider elements that just appeared
				// in the calculation, or elements that have disappeared
				if(results[i] != Integer.MIN_VALUE && results[i-1] != Integer.MIN_VALUE ) {
					int delta = results[i]-results[i-1]; 
					if(delta > 0 && delta > deltaMaximum){
							deltaMaximum = delta;
					} else if(delta < 0 && delta < deltaMinimum){
							deltaMinimum = delta;
						}
				}
			}
		}
				
		return new int[] {deltaMinimum, deltaMaximum};
	}
	
	/**
	 * Return the total decreases and increases for a given snapshot, a metric and a boolean indicating
	 * if deltas should be used
	 * 
	 * @param index
	 * @param type
	 * @param deltas
	 * @return array [decreases, increases]
	 */
	public int [] getTotalsForShapshot(int index, ElementMetric type, boolean deltas) {
		
		if(index <0)
			throw new IndexOutOfBoundsException("Index "+index+" <0");
		
		if(index >= matrixes.size())
			throw new IndexOutOfBoundsException("Index "+index+" >= matrixes size: "+matrixes.size());

		int totalDecreases = 0;
		int totalIncreases = 0;
		int rows = getElementsCount();
		for(int i = 0; i < rows; i++) {
			MatrixElement current = elements[i][index];
			// current may be null for an element not present in a snapshot
			if(current!=null) {
				int rowValue = 0;
				if(!deltas) {
					rowValue = elements[i][index].getMetricValue(type);
				} else {
					if(index == 0) {
						rowValue = elements[i][index].getMetricValue(type);
					} else {
						try {
							rowValue = getDelta(current.getFullName(),type,index,index-1);
						} catch(Exception e) {
							System.out.println("Exception: name"+current+" index:"+index);
						}
					}
				}
				if(rowValue!=Integer.MIN_VALUE) {
					if(rowValue < 0) {
						totalDecreases -= rowValue;
					}
					if(rowValue > 0) {
						totalIncreases += rowValue;
					}
				}
			}
		}
		return new int[] {totalDecreases,totalIncreases};

	}
	
	/**
	 * Calculate the total values of the metric of an element across snapshots,
	 * either for real values or for deltas
	 * 
	 * @param elementFullName
	 * @param type
	 * @param deltas
	 * @return
	 */
	public int getTotalAcrossVersionsFor(String elementFullName, ElementMetric type, boolean deltas) {
		int results [] = getResultsFor(elementFullName,type,false);
		int total = 0;
		int previousValue = 0;
		boolean initial = true;
		for(int i:results) {
			if(i!=Integer.MIN_VALUE) {
				if(!deltas) {
					total+=i;
				} else {
					if(initial) {
						previousValue = i;
						initial = false;
					} else {
						total+=i-previousValue;
						previousValue = i;
					}
				}
				
			}
		}
		return total;
	}
	
	
	/**
	 * Calculate the trend of a metric for a given element
	 * 
	 * @param elementFullName
	 * @param type
	 * @return
	 */
	public Trend getTrendFor(String elementFullName, ElementMetric type) {
		
		SimpleRegression regression = new SimpleRegression();

		int [] results = getResultsFor(elementFullName,type,false);
		
		// We must filter out the entries when the element did not exist
		
		int trimIndex = 0;
		for(int i = 0; i < results.length ; i++ ) {
			if(results[i] == Integer.MIN_VALUE) {
				trimIndex = i+1;
			}
		}
		
		if(trimIndex > 0) {
			results = Arrays.copyOfRange(results, trimIndex, results.length);
		}
		

		for(int i = 0; i < results.length; i++) {
			regression.addData((double)i, (double)results[i]);
		}
		
		double slope = regression.getSlope();
		
		//logger.info("Slope for"+elementFullName+" = "+slope+" intercept = "+intercept);
		
		if(slope < -30) return Trend.HIGH_IMPROVING;
		if(slope >= -30 && slope < -10) return Trend.IMPROVING;
		if(slope >= -10 && slope <= 10) return Trend.STABLE;
		if(slope > 10 && slope <= 30) return Trend.DEGRADING;
		if(slope > 30) return Trend.HIGH_DEGRADING;
		
		// Can happen when there is only one point
		return Trend.UNKNOWN;
		
		
	}
	
	/**
	 * Get snapshots
	 * 
	 * @param index the index of the snapshot, 0 is the oldest recent
	 * @return
	 */
	public DesignStructureMatrix getSnapshot(int index) {
		if(index < 0 || index >= matrixes.size())
		{
			throw new IllegalArgumentException("index out of range:"+index);
		}
		return matrixes.get(matrixes.size()-index-1);
	}
	
	/**
	 * Return the most recent snapshot
	 * 
	 * @return
	 */
	public DesignStructureMatrix getMoreRecentSnapshot() {
		return matrixes.get(0);
	}

	/**
	 * Get the names of snapshots
	 * 
	 * @param includeNewest
	 * @return
	 */	
	public String [] getSnapshotNames() {
		
		int i = 0;
		
		String [] names = new String [matrixes.size()];
		
		i=0;
		
		for(DesignStructureMatrix matrix:matrixes) {
			names[matrixes.size()-1-i] = matrix.getName();
			i++;
		}
		return names;
	}
	
	/**
	 * Creates a report for the different metrics and snapshot, separating the decreased and increased values
	 * 
	 * @param deltas
	 * @return arraylist with strings that contain the results
	 */
	public List<String []> generateMetricsSummaryReport(boolean deltas) {
		
		ArrayList <String []> results = new ArrayList <> (); 

		String [] header = new String [matrixes.size()+2];
		header [0] = "Metric";
		header [1] = "Type";
		
		// Creates the header
		for(int snapshot = 0; snapshot < matrixes.size(); snapshot++) {
			header[snapshot+2] = getSnapshotNames()[snapshot];
		}
		results.add(header);
		
		String [] newFiles = new String [matrixes.size()+2];
		String [] renamed = new String [matrixes.size()+2];
		String [] deleted = new String [matrixes.size()+2];
		
		for(int snapshot = 0; snapshot < matrixes.size(); snapshot++) {
			newFiles[snapshot+2] = Integer.toString(getElements(snapshot, ElementStatus.NEW).size());
			renamed[snapshot+2] = Integer.toString(getElements(snapshot, ElementStatus.RENAMED).size());
			deleted[snapshot+2] = Integer.toString(getElements(snapshot, ElementStatus.DELETED).size());
		}
		
		newFiles[0]="New files";
		renamed[0]="Renamed in next";
		deleted[0]="Deleted in next";
		
		newFiles[1]="";
		renamed[1]="";
		deleted[1]="";
		
		results.add(newFiles);
		results.add(renamed);
		results.add(deleted);
		
		for (ElementMetric metric:ElementMetric.values()) {
			String [] decreases = new String [matrixes.size()+2];
			String [] increases = new String [matrixes.size()+2];
			
			decreases[0]=metric.getText();
			increases[0]=metric.getText();
			
			decreases[1]="decreases";
			increases[1]="increases";
			
			
			for(int snapshot = 0; snapshot < matrixes.size(); snapshot++) {
				int [] totals = getTotalsForShapshot(snapshot,metric,deltas);
				int decreaseForMetric = totals [0];
				int increaseForMetric = totals [1];
				
				decreases[snapshot+2] = Integer.toString(decreaseForMetric);
				increases[snapshot+2] = Integer.toString(increaseForMetric);
			}
			
			results.add(decreases);
			results.add(increases);
		}
		
		return results;
		
	}
	
	/**
	 * Generate a report for a particular metric (using or not deltas)
	 * 
	 * @param metric
	 * @param deltas
	 * @return list with string arrays containing the results
	 */
	public List<String []> generateMetricsReport(ElementMetric metric,boolean deltas) {

		ArrayList <String []> results = new ArrayList <> (); 

		String [] newRow = new String [matrixes.size()+1];
		newRow [0] = "Filename";

		// Creates the header
		for(int snapshot = 0; snapshot < matrixes.size(); snapshot++) {
			newRow[snapshot+1] = getSnapshotNames()[snapshot];
		}
		results.add(newRow);

		
		DesignStructureMatrix currentMatrix = getMoreRecentSnapshot();
		for(MatrixElement element:currentMatrix.getElements()) {
			newRow = new String [matrixes.size()+1];
			newRow[0]=element.getFullName();
			int metricResults [] = getResultsFor(element.getFullName(),metric,deltas);
			for(int i = 0;i<matrixes.size();i++) {
				if(metricResults[i]!= Integer.MIN_VALUE) {
					newRow[i+1]=Integer.toString(metricResults[i]);
				} else {
					newRow[i+1]="";
				}
			}
			results.add(newRow);
		}
		
		return results;
	}

	
	/**
	 * Return list of names of elements based on their status:
	 * PRESENT: elements that are present in the snapshot 
	 * RENAMED: elements that are present in this snapshot but renamed in the next one
	 * DELETED: elements that are present in this snapshot but deleted in the next one
	 * 
	 * @param index
	 * @return
	 */
	public List <String> getElements(int index, ElementStatus status) {	
		
		if(index < 0 || index >= matrixes.size()) {
			throw new IllegalArgumentException("index out of range:"+index);
		}
		
		ArrayList <String> names = new ArrayList <> ();
		DesignStructureMatrix current = getSnapshot(index);
		for(MatrixElement element: current.getElementsAsList()) {
			String fullName = element.getFullName();
			if(status == ElementStatus.PRESENT ) {
				names.add(fullName);
			}
			// Elements present in this one, but renamed in the next one
			if(status == ElementStatus.RENAMED && index < matrixes.size()-1) {
					DesignStructureMatrix next = getSnapshot(index+1);
					ElementStatus nextStatus = next.getElementStatus(element.getFullName());
					if(nextStatus == ElementStatus.RENAMED) {
						names.add(fullName);
				}
			}
			// Elements present in this one but deleted in the next one
			if(status == ElementStatus.DELETED && index < matrixes.size()-1) {
				DesignStructureMatrix next = getSnapshot(index+1);
				ElementStatus nextStatus = next.getElementStatus(element.getFullName());
				if(nextStatus == ElementStatus.NOTPRESENT) {
					names.add(fullName);
				}
				
			}
			// Elements that are new in this one
			if(status == ElementStatus.NEW) {
				if(index > 0) {
					DesignStructureMatrix previous = getSnapshot(index-1);
					ElementStatus previousStatus = previous.getElementStatus(element.getFullName());
					if(previousStatus == ElementStatus.NOTPRESENT) {
						names.add(fullName);
					}
				} else { // if index == 0, they are all new
					names.add(fullName);
				}
				
			}
			
		}
		return names;
	}
	
	

}
