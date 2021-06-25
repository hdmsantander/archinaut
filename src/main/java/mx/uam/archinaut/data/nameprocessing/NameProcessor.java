
package mx.uam.archinaut.data.nameprocessing;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import mx.uam.archinaut.model.yaml.RenamingConfiguration;
import mx.uam.archinaut.model.yaml.Substitution;

@Component
public class NameProcessor {

	public String processName(RenamingConfiguration renamingConfiguration, String nameToProcess) {

		String result = nameToProcess.replace(renamingConfiguration.getPrefix(), "");

		List<Substitution> substutions = renamingConfiguration.getSubstitutions();
		Collections.sort(substutions);

		for (Substitution substitution : substutions) {
			result = result.replace(substitution.getSubstitute(), substitution.getWith());
		}

		return result;

	}

}
