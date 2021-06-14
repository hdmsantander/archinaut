package mx.uam.archinaut.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.model.DesignStructureMatrix;

@Component
@Slf4j
public class CliService implements CommandLineRunner {
	
	@Autowired
	private BuildProperties buildProperties;

	@Autowired
	private DesignStructureMatrixService dsmService;

	@Autowired
	private MetricsService metricsService;

	@Autowired
	private JunitService junitService;

	public static final String HEADLESS_LONG_OPTION = "headless";
	public static final String DEPENDS_LONG_OPTION = "depends";
	public static final String FRECUENCIES_LONG_OPTION = "frequencies";
	public static final String COUPLING_LONG_OPTION = "coupling";
	public static final String SCC_LONG_OPTION = "scc";

	public static final String HEADLESS_SHORT_OPTION = "h";
	public static final String DEPENDS_SHORT_OPTION = "d";
	public static final String FRECUENCIES_SHORT_OPTION = "f";
	public static final String COUPLING_SHORT_OPTION = "c";
	public static final String SCC_SHORT_OPTION = "s";

	@Override
	public void run(String... args) throws Exception {

		log.info("Archinaut version: " + buildProperties.getVersion());

		// Create command line parser and formatter to use for option parsing
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();

		options.addOption(HEADLESS_SHORT_OPTION, HEADLESS_LONG_OPTION, false, "Run application in headless mode");
		options.addOption(Option.builder(DEPENDS_SHORT_OPTION).longOpt(DEPENDS_LONG_OPTION).required(false)
				.desc("Depends results file in JSON format").hasArg().build());
		options.addOption(Option.builder(FRECUENCIES_SHORT_OPTION).longOpt(FRECUENCIES_LONG_OPTION).required(false)
				.desc("Gitlog analyzer commit frequencies CSV file").hasArg().build());
		options.addOption(Option.builder(COUPLING_SHORT_OPTION).longOpt(COUPLING_LONG_OPTION).required(false)
				.desc("Gitlog analyzer coupling CSV file").hasArg().build());
		options.addOption(Option.builder(SCC_SHORT_OPTION).longOpt(SCC_LONG_OPTION).required(false).desc("SCC CSV file")
				.hasArg().build());

		try {

			// Parse the command line args
			CommandLine line = parser.parse(options, args);

			// If app was invoked in headless mode run in headless mode, else run in
			// windowed mode
			if (line.hasOption(HEADLESS_LONG_OPTION)) {

				// In headless mode we need some arguments to continue
				if (!line.hasOption(DEPENDS_LONG_OPTION) || !line.hasOption(FRECUENCIES_LONG_OPTION)
						|| !line.hasOption(COUPLING_LONG_OPTION) || !line.hasOption(SCC_LONG_OPTION)) {

					// If we miss some arguments, invoke help
					formatter.printHelp("java -jar archinaut.jar --headless", options);

					System.out.println(
							"\nAdditionally one or more environment variables may be set to create a JUnit report.\nThe environment variables and the associated metrics to test are as follows:\n");
					System.out.println(
							"INPUT_MIN_COCHANGES:\t\t" + "Minimum number of cochanges to report in coupling analysis");
					System.out.println(
							"INPUT_SCC_CLOC:\t\t\t" + "Maximum logical lines of code allowed for a single file");
					System.out.println("INPUT_SCC_COMPLEXITY:\t\t" + "Maximum complexity allowed for a single file");
					System.out
							.println("INPUT_SCC_LOC:\t\t\t" + "Maximum total lines of code allowed for a single file");
					System.out.println("INPUT_ARCH_REVISIONS:\t\t" + "Maximum number of commits for a single file");
					System.out.println("INPUT_ARCH_DEPENDENT_PARTNERS:\t"
							+ "Maximum number of files that can depend on a single file");
					System.out.println("INPUT_ARCH_DEPENDS_ON_PARTNERS:\t"
							+ "Maximum number of files that a file is allowed to depend from");
					System.out.println("INPUT_ARCH_TOTAL_DEPENDENCIES:\t" + "Maximum sum of the previous two metrics");
					System.out.println("INPUT_ARCH_COCHANGE_PARTNERS:\t"
							+ "Maximum number of other simultaneous files to be modified in the same commit to a file");
					System.out.println("INPUT_ARCH_CHURN:\t\t"
							+ "Maximum accumulated lines of code changed during all commits to a file");

					System.exit(1);
				}
				headless(line.getOptionValue(DEPENDS_LONG_OPTION), line.getOptionValue(FRECUENCIES_LONG_OPTION),
						line.getOptionValue(COUPLING_LONG_OPTION), line.getOptionValue(SCC_LONG_OPTION));
			}
			
		} catch (ParseException e) {
			log.error("Error parsing command line options: " + e.getMessage(), e);
		} catch (JAXBException je) {
			log.error("Error creating JUnit report: " + je.getMessage(), je);
		}
	}

	public void headless(String dependsFilename, String frecuenciesFilename, String couplingFilename,
			String sccFilename) throws JAXBException, CsvValidationException, IOException {

		DesignStructureMatrix matrix = dsmService.loadDataBasedOnConfigurationFile();
		
		metricsService.saveMetricsReport(matrix, "archinaut.csv");

		// TODO: Check that the Junit report is also created
		junitService.createJunitReport(matrix);

		File results = new File("archinaut.csv");

		if (results.exists()) {
			System.exit(0);
		} else {
			System.exit(1);
		}

	}

}
