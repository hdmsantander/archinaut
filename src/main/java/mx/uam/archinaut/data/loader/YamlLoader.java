package mx.uam.archinaut.data.loader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import mx.uam.archinaut.model.yaml.YamlConfigurationEntry;

@Component
public class YamlLoader {

	public YamlConfigurationEntry getDependsConfigurationEntry() {

		// Instantiate loader
		Yaml yaml = new Yaml(new Constructor(YamlConfigurationEntry.class));

		// Load configuration file as InputStream
		InputStream is = getClass().getClassLoader().getResourceAsStream("configuration.yml");

		// For every entry in the file cast them to YamlConfigurationEntry and return
		// the depends configuration entry, if found
		for (Object o : yaml.loadAll(is)) {

			assert (o instanceof YamlConfigurationEntry) : "A loaded YAML object was not an instance of YamlConfigurationEntry!";
			YamlConfigurationEntry entry = (YamlConfigurationEntry) o;

			if (entry.getFormat().equals("DEPENDS")) {
				return entry;
			}

		}

		throw new IllegalArgumentException("Depends setting missing in configuration file!");

	}

	public List<YamlConfigurationEntry> getNonDependsConfigurationEntries() {

		// Instantiate loader
		Yaml yaml = new Yaml(new Constructor(YamlConfigurationEntry.class));

		// Load configuration file as InputStream
		InputStream is = getClass().getClassLoader().getResourceAsStream("configuration.yml");

		List<YamlConfigurationEntry> entries = new ArrayList<>();

		// For every entry in the file cast them to YamlConfigurationEntry and add them
		// to the list.
		for (Object o : yaml.loadAll(is)) {

			assert (o instanceof YamlConfigurationEntry) : "A loaded YAML object was not an instance of YamlConfigurationEntry!";
			YamlConfigurationEntry entry = (YamlConfigurationEntry) o;

			if (!entry.getFormat().equals("DEPENDS")) {
				entries.add(entry);
			}

		}

		return entries;

	}

}
