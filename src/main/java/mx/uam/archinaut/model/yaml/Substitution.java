package mx.uam.archinaut.model.yaml;

import lombok.Data;

@Data
public class Substitution implements Comparable<Substitution> {

	private Integer order;

	private String substitute;

	private String with;

	@Override
	public int compareTo(Substitution substitution) {
		int compareQuantity = substitution.getOrder();
		return this.order - compareQuantity;
	}

}
