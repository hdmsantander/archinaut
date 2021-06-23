package mx.uam.archinaut.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opencsv.exceptions.CsvValidationException;

import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.MatrixElement;

@SpringBootTest
public class MetricsServiceTest extends AbstractServiceTest  {
	
	@Autowired
	private DesignStructureMatrixService designStructureMatrixService;
	
	@Autowired
	private MetricsService metricsService;
	
	@Test
	void testLoadMetrics() throws CsvValidationException, IOException {
		
		DesignStructureMatrix matrix = designStructureMatrixService.loadMatrixFromJSON(dependsConfigurationEntry);
		assertNotNull(matrix);
		
		matrix = metricsService.loadMetrics(nonDependsConfigurationEntries, matrix);
		assertNotNull(matrix);
		
		for (MatrixElement m : matrix.getElements()) {
			assertEquals(8, m.getMetrics().size());			
		}
				
	}

}
