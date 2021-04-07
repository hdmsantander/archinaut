package mx.uam.archinaut.model;

public enum ElementMetric {
	SIZE("DES_Size"),
	COMPLEXITY("DES_Complexity"),
	DESIGNSMELLS("DES_Design Smells"),
	SCCLOC("SCC_LOC"),
	SCCCLOC("SCC_CLOC"),
	SCCCOMPLEXITY("SCC_COMPLEXITY"),
	REVISIONS("ARCH_Revisions"),
	DEPENDENT("ARCH_Dependent Partners"),
	DEPENDSON("ARCH_Depends on Partners"),	
	DEPENDENCIES("ARCH_Total Dependencies"),
	COCHANGEPARTNERS("ARCH_CoChange Partners"),
	BUGCOMMITS("ARCH_Bug Commits"),
//	REFACTORINGOPS("Refactoring operations"),
	CHURN("ARCH_Churn"),
	DV8LOC("DV8_LOC"),
	DV8TARGETCHANGECOUNT("DV8_TargetChangeCount"),
	DV8TARGETCHURN("DV8_TargetChurn"),
	DV8CHANGECOUNT("DV8_ChangeCount"),
	DV8CHANGECHURN("DV8_ChangeChurn"),
	TOTALISSUES("DV8_TotalIssues"),
	CLIQUE("DV8_Clique"),
	CROSSING("DV8_Crossing"),
	ISCENTER("DV8_IsCenter"),
	MODULARITYVIOLATION("DV8_ModularityViolation"),
	PACKAGECYCLE("DV8_PackageCycle"),
	UNHEALTHYINHERITANCE("DV8_UnhealthyInheritance"),
	UNSTABLEINTERFACE("DV8_UnstableInterface"),
	ISUNSTABLEINTERFACE("DV8_isUnstableInterface"),
	PRESENTINISSUES("DV8_PresentInIssues"),
	SQISSUES("SQ_Issues"),
	SQCODESMELLS("SQ_CodeSmells"),
	SQBUGS("SQ_Bugs"),
	SQVULNERABILITIES("SQ_Vulnerabilities"),
	SQSECURITYSPOTS("SQ_SecuritySpots"),
	FAT("S101_Fat"),
	CLASSTANGLES("S101_ClassTangles"),
	PACKAGETANGLES("S101_PkgTangles")
	;



    private String text;
	private int index;
	
	private static ElementMetric[] list = ElementMetric.values();
	
	
	static {
        int i = 0;
        for(ElementMetric e:values()) {
        	e.index = i;
        	i++;
        }
		
	}
		
    public static ElementMetric getMetricByIndex(int i) {
        return list[i];
    }
	 
    public static ElementMetric getMetricByText(String txt) {
    	for(ElementMetric e:values()) {
    		if(e.text.equals(txt)) {
    			return e;
    		}
    	}
    	return null;
    }
    
	ElementMetric(String text) {
        this.text = text;
    }
 
    public String getText() {
        return text;
    }
    
    public int getIndex() {
    	return index;
    }
    
    @Override
    public String toString() {
    	return text;
    }

}
