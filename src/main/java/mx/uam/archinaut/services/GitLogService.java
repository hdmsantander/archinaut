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
