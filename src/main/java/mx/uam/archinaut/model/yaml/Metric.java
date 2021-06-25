package mx.uam.archinaut.model.yaml;

import lombok.Data;

@Data
public class Metric {

	private String name;

	private String rename;

	private Boolean filename = false;

}
