package mx.uam.archinaut.model;

public enum DependencyMetric {
	COCHANGES("CoChanges"), DEPENDENCIES("Dependencies");

	private String text;
	private int index;

	private static DependencyMetric[] list = DependencyMetric.values();

	static {
		int i = 0;
		for (DependencyMetric e : values()) {
			e.index = i;
			i++;
		}

	}

	public static DependencyMetric getMetricType(int i) {
		return list[i];
	}

	DependencyMetric(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public int getIndex() {
		return index;
	}

}
