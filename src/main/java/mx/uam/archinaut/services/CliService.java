package mx.uam.archinaut.services;

import java.io.File;

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

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.YamlLoader;
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

	public static final String CONFIGURATION_OPTION = "configuration";
	public static final String CONFIGURATION_SHORT_OPTION = "c";

	@Override
	public void run(String... args) throws Exception {

		log.info("Archinaut version: " + buildProperties.getVersion());

		// Create command line parser and formatter to use for option parsing
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();

		options.addOption(Option.builder(CONFIGURATION_SHORT_OPTION).longOpt(CONFIGURATION_OPTION).required(true)
				.desc("Path to the configuration.yml file").hasArg().build());

		try {

			// Parse the command line args
			CommandLine line = parser.parse(options, args);

			// Check if we have the configuration filename option
			if (!line.getOptionValue(CONFIGURATION_OPTION).isBlank()) {

				log.info("Loading " + line.getOptionValue(CONFIGURATION_OPTION));

				DesignStructureMatrix matrix = dsmService
						.loadDataBasedOnConfigurationFile(line.getOptionValue(CONFIGURATION_OPTION));

				metricsService.saveMetricsReport(matrix, "archinaut.csv");

				junitService.createJunitReport(matrix);

				File results = new File("archinaut.csv");

				if (results.exists()) {
					System.exit(0);
				} else {
					System.exit(1);
				}

			} else {

				// If we miss the configuration arguments, invoke help
				formatter.printHelp("java -jar archinaut.jar --configuration configuration.yml", options);
				System.exit(1);

			}

		} catch (ParseException e) {
			log.error("Error parsing command line options: " + e.getMessage(), e);
		} catch (JAXBException je) {
			log.error("Error creating JUnit report: " + je.getMessage(), je);
		}
	}

}
