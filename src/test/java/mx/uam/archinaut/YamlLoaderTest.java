package mx.uam.archinaut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import mx.uam.archinaut.model.yaml.Metric;
import mx.uam.archinaut.model.yaml.RenamingConfiguration;
import mx.uam.archinaut.model.yaml.Substitution;
import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;

@SpringBootTest
class YamlLoaderTest {

	@Test
	void testYamlLoading() {

		Yaml yamlMultiple = new Yaml(new Constructor(YamlConfigurationEntry.class));

		InputStream is2 = getClass().getClassLoader().getResourceAsStream("configuration.yml");

		int count = 0;

		for (Object o : yamlMultiple.loadAll(is2)) {

			count++;

			assertTrue(o instanceof YamlConfigurationEntry);

			YamlConfigurationEntry entry = (YamlConfigurationEntry) o;

			assertEquals("scc.csv", entry.getFile());
			assertEquals("CSV", entry.getFormat());

			RenamingConfiguration renaming = entry.getRenaming();

			assertEquals("/", renaming.getPathSeparator());
			assertEquals("", renaming.getPrefix());
			assertEquals("", renaming.getSuffix());

			List<Substitution> substitutions = renaming.getSubstitutions();

			assertEquals(1, substitutions.size());
			
			assertEquals(1, substitutions.get(0).getOrder());
			assertEquals(".", substitutions.get(0).getSubstitute());
			assertEquals("_", substitutions.get(0).getWith());

			List<Metric> metrics = entry.getMetrics();

			assertEquals(4, metrics.size());

			assertTrue(metrics.get(0).getFilename());
			assertFalse(metrics.get(1).getFilename());

		}

		assertEquals(2, count);

	}
	
	@Test
	void testLoading() {
		
		
		
	}

}
