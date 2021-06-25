package mx.uam.archinaut.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import mx.uam.archinaut.data.loader.YamlLoader;
import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;

@SpringBootTest
class YamlLoaderTest {
	
	@Autowired
	private YamlLoader yamlLoader;

	@Test
	void testDependsConfigurationEntry() throws FileNotFoundException {
		
		YamlConfigurationEntry entry = yamlLoader.getDependsConfigurationEntry("configuration.yml");
		
		assertEquals("DEPENDS", entry.getFormat());
		assertEquals("depends.json", entry.getFile());
		
		assertEquals(".", entry.getRenaming().getPathSeparator());
		assertEquals("main.java.", entry.getRenaming().getPrefix());
		assertEquals("", entry.getRenaming().getSuffix());

		assertEquals(1, entry.getRenaming().getSubstitutions().get(0).getOrder());
		assertEquals(".", entry.getRenaming().getSubstitutions().get(0).getSubstitute());
		assertEquals("_", entry.getRenaming().getSubstitutions().get(0).getWith());
		
		assertEquals("Call", entry.getMetrics().get(0).getName());
		assertEquals("Import", entry.getMetrics().get(1).getName());
		assertEquals("Return", entry.getMetrics().get(2).getName());
		assertEquals("Use", entry.getMetrics().get(3).getName());
		assertEquals("Parameter", entry.getMetrics().get(4).getName());
		assertEquals("Contain", entry.getMetrics().get(5).getName());
		assertEquals("Implement", entry.getMetrics().get(6).getName());
		assertEquals("Create", entry.getMetrics().get(7).getName());
		assertEquals("Extend", entry.getMetrics().get(8).getName());
		
	}
	
	@Test
	void testNonDependsConfigurationEntries() throws FileNotFoundException {
		
		List<YamlConfigurationEntry> entries = yamlLoader.getNonDependsConfigurationEntries("configuration.yml");
		
		assertEquals(3, entries.size());
		
		for (YamlConfigurationEntry y : entries) {
			
			if (y.getFile().equals("scc.csv")) {
				
				assertEquals("CSV", y.getFormat());
				
				assertEquals("/", y.getRenaming().getPathSeparator());
				assertEquals("src/main/java/", y.getRenaming().getPrefix());
				assertEquals("", y.getRenaming().getSuffix());

				assertEquals(1, y.getRenaming().getSubstitutions().get(0).getOrder());
				assertEquals(".", y.getRenaming().getSubstitutions().get(0).getSubstitute());
				assertEquals("_", y.getRenaming().getSubstitutions().get(0).getWith());
				
				assertEquals("Location", y.getMetrics().get(0).getName());
				assertEquals(true, y.getMetrics().get(0).getFilename());
				
				assertEquals("Lines", y.getMetrics().get(1).getName());
				assertEquals("SCC_LOC", y.getMetrics().get(1).getRename());
				
				assertEquals("Code", y.getMetrics().get(2).getName());
				assertEquals("SCC_CLOC", y.getMetrics().get(2).getRename());
				
				assertEquals("Complexity", y.getMetrics().get(3).getName());
				assertEquals("SCC_COMPLEXITY", y.getMetrics().get(3).getRename());
				
			}

			if (y.getFile().equals("frecuencies.csv")) {
				
				assertEquals("CSV", y.getFormat());
				
				assertEquals("/", y.getRenaming().getPathSeparator());
				assertEquals("src/main/java/", y.getRenaming().getPrefix());
				assertEquals("", y.getRenaming().getSuffix());

				assertEquals(1, y.getRenaming().getSubstitutions().get(0).getOrder());
				assertEquals(".", y.getRenaming().getSubstitutions().get(0).getSubstitute());
				assertEquals("_", y.getRenaming().getSubstitutions().get(0).getWith());
				
				assertEquals("entity", y.getMetrics().get(0).getName());
				assertEquals(true, y.getMetrics().get(0).getFilename());
				
				assertEquals("n-revs", y.getMetrics().get(1).getName());
				assertEquals("ARCH_REVISIONS", y.getMetrics().get(1).getRename());
				
				assertEquals("bugs", y.getMetrics().get(2).getName());
				assertEquals("BUG_COMMITS", y.getMetrics().get(2).getRename());
				
				assertEquals("added", y.getMetrics().get(3).getName());
				assertEquals("LINES_ADDED", y.getMetrics().get(3).getRename());
				
				assertEquals("removed", y.getMetrics().get(4).getName());
				assertEquals("LINES_REMOVED", y.getMetrics().get(4).getRename());
				
			}

			if (y.getFile().equals("coupling.csv")) {
				
				assertEquals("CSV", y.getFormat());
				
				assertEquals("/", y.getRenaming().getPathSeparator());
				assertEquals("src/main/java/", y.getRenaming().getPrefix());
				assertEquals("", y.getRenaming().getSuffix());

				assertEquals(1, y.getRenaming().getSubstitutions().get(0).getOrder());
				assertEquals(".", y.getRenaming().getSubstitutions().get(0).getSubstitute());
				assertEquals("_", y.getRenaming().getSubstitutions().get(0).getWith());
				
				assertEquals("entity", y.getMetrics().get(0).getName());
				assertEquals(true, y.getMetrics().get(0).getFilename());
				
				assertEquals("cochanges", y.getMetrics().get(1).getName());
				assertEquals("COCHANGES", y.getMetrics().get(1).getRename());
												
			}
			
		}
		
	}
	
	
	
}
