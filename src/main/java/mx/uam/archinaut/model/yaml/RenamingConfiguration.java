package mx.uam.archinaut.model.yaml;

import java.util.List;

import lombok.Data;

@Data
public class RenamingConfiguration {

	private String pathSeparator;

	private String prefix;

	private String suffix;

	private List<Substitution> substitutions;

}