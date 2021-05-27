package mx.uam.archinaut.model.yaml;

import java.util.List;
import lombok.Data;

@Data
public class YamlConfigurationEntry {

	private String file;

	private String format;

	private RenamingConfiguration renaming;

	private List<Metric> metrics;

}
