package mx.uam.archinaut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;

@SpringBootTest
class YamlLibraryTests {
	
	@Test
	void testRawYamlLoading() {

		Yaml yamlMultiple = new Yaml(new Constructor(YamlConfigurationEntry.class));

		InputStream is2 = getClass().getClassLoader().getResourceAsStream("archinaut.yml");

		List<YamlConfigurationEntry> entries = new ArrayList<>();

		for (Object o : yamlMultiple.loadAll(is2)) {

			assertTrue(o instanceof YamlConfigurationEntry);

			YamlConfigurationEntry entry = (YamlConfigurationEntry) o;
			entries.add(entry);

		}

		assertEquals(4, entries.size());
		
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
				
				assertEquals("coupled", y.getMetrics().get(1).getName());
				assertEquals("COUPLED", y.getMetrics().get(1).getRename());
				
				assertEquals("cochanges", y.getMetrics().get(2).getName());
				assertEquals("COCHANGES", y.getMetrics().get(2).getRename());
								
			}

			if (y.getFile().equals("depends.json")) {
				
				assertEquals("DEPENDS", y.getFormat());
				
				assertEquals(".", y.getRenaming().getPathSeparator());
				assertEquals("main.java.", y.getRenaming().getPrefix());
				assertEquals("", y.getRenaming().getSuffix());

				assertEquals(1, y.getRenaming().getSubstitutions().get(0).getOrder());
				assertEquals(".", y.getRenaming().getSubstitutions().get(0).getSubstitute());
				assertEquals("_", y.getRenaming().getSubstitutions().get(0).getWith());
				
				assertEquals("Call", y.getMetrics().get(0).getName());
				assertEquals("Import", y.getMetrics().get(1).getName());
				assertEquals("Return", y.getMetrics().get(2).getName());
				assertEquals("Use", y.getMetrics().get(3).getName());
				assertEquals("Parameter", y.getMetrics().get(4).getName());
				assertEquals("Contain", y.getMetrics().get(5).getName());
				assertEquals("Implement", y.getMetrics().get(6).getName());
				assertEquals("Create", y.getMetrics().get(7).getName());
				assertEquals("Extend", y.getMetrics().get(8).getName());
				
			}
			
		}
		
	}

}
