package mx.uam.archinaut.model.dto;

import java.util.ArrayList;
import java.util.List;

public class TrendsAnalysisDTO {

	List <DesignStructureMatrixDTO> dsmDtos = new ArrayList<> ();

	
	public Iterable<DesignStructureMatrixDTO> getDsmFolders() {
		return dsmDtos;
	}
	
	public DesignStructureMatrixDTO getDesignStructureMatrixDTO(int index) {
		return dsmDtos.get(index);
	}
	
	public boolean addDesignStructureMatrixDTO(DesignStructureMatrixDTO dsmDto) {
		return dsmDtos.add(dsmDto);
	}
	
	public int getNumberOfSnapshots() {
		return dsmDtos.size();
	}
	

}
