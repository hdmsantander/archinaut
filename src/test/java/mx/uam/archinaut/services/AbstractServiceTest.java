package mx.uam.archinaut.services;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import mx.uam.archinaut.data.loader.YamlLoader;
import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;

public class AbstractServiceTest {
	
	@Autowired
	private YamlLoader yamlLoader;
	
	protected YamlConfigurationEntry dependsConfigurationEntry;
	
	protected List<YamlConfigurationEntry> nonDependsConfigurationEntries;
	
	@BeforeEach
	public void prepare() throws FileNotFoundException {
		
		dependsConfigurationEntry = yamlLoader.getDependsConfigurationEntry("configuration.yml");
		nonDependsConfigurationEntries = yamlLoader.getNonDependsConfigurationEntries("configuration.yml");
		
	}

}
