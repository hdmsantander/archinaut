package mx.uam.archinaut.services;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.stereotype.Service;

import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.ElementMetric;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.xsd.ObjectFactory;
import mx.uam.archinaut.model.xsd.Testsuite.Testcase;
import mx.uam.archinaut.model.xsd.Testsuites;
import mx.uam.archinaut.model.xsd.Testsuites.Testsuite;

@Service
public class JunitService {
	
	// Get the map of the environment variables
	private Map<String,String> environmentVariables = System.getenv();
		
	public void createJunitReport(DesignStructureMatrix matrix) throws JAXBException {
		
		// Our XML object factory if needed
		ObjectFactory of = new ObjectFactory();
		
		// Create the test suite container
		Testsuites testSuites = of.createTestsuites();
		
		// Iterate over the elements, if we have a metric for the element it is tested
		for(MatrixElement element : matrix.getElements()) {
			
			// New test suite for this element
			Testsuite ts = new Testsuite();
			ts.setName(element.getFullName());
			
			// We start everything at 0
			int numberOfTests = 0;
			int errors = 0;
			int failures = 0;
			int skipped = 0;
		
			// Iterate over the metrics for this element
			for(ElementMetric metric:ElementMetric.values()) {
								
				// Try to generate a test case for this metric for this element
				Optional<Testcase> t = getTestCaseForMetric(element, metric);
				
				if (t.isPresent()) {
					
					ts.getTestcase().add(t.get());
					numberOfTests++;
					
					if ( null != t.get().getError()) {
						errors++;
					}
					
				}
			
			}
			
			ts.setTests(numberOfTests);
			ts.setErrors(errors);
			ts.setFailures(failures);
			ts.setSkipped(skipped);
			testSuites.getTestsuite().add(ts);
						
		}
				
		File file = new File("archinaut.xml");
		
		JAXBContext jaxbContext = JAXBContext.newInstance("mx.uam.archinaut.model.xsd");
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		jaxbMarshaller.marshal(testSuites, file);
	}
	
	private Optional<Testcase> getTestCaseForMetric(MatrixElement m, ElementMetric e) {
		
		String envName = "INPUT_" + e.getText().toUpperCase().replace(" ", "_").trim();
		
		try {
			
			if (environmentVariables.containsKey(envName)) {
				
				Integer value = Integer.parseInt(environmentVariables.get(envName));
				
				Testcase t = new Testcase();
				t.setName(e.getText().toUpperCase());
				t.setClassname(m.getName());
				
				if (m.getMetricValue(e) < value) {
					t.setError(null);
				} else {
					Testsuite.Testcase.Error error = new Testsuite.Testcase.Error();
					error.setMessage("Max threshold was reached for " + e.getText().toUpperCase() + " value is: " + m.getMetricValue(e) + " threshold is: " + value);
					t.setError(error);
					error.setType("maximumValueReached");
					error.setValue("Max threshold was reached for " + e.getText().toUpperCase() + " value is: " + m.getMetricValue(e) + " threshold is: " + value);
				}
				
				return Optional.of(t);				
				
			} else {
				
				return Optional.empty();
			}
			
		} catch (Exception ex) {
			
			return Optional.empty();
		}
				
	}

}
