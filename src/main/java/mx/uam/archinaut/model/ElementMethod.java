package mx.uam.archinaut.model;

/**
 * 
 * @author humbertocervantes
 *
 */
public class ElementMethod implements Comparable<ElementMethod> {

	private String name;
	private int size;
	private int complexity;
	private int parameters;

	public ElementMethod(String name, int size, int complexity, int parameters) {
		this.name = name;
		this.size = size;
		this.complexity = complexity;
		this.parameters = parameters;

	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public int getComplexity() {
		return complexity;
	}

	public int getParameters() {
		return parameters;
	}

	@Override
	public int compareTo(ElementMethod elementMethod) {
		return Integer.compare(size, elementMethod.size);
	}

	@Override
	public boolean equals(Object method) {
		return this == method;
	}

}
