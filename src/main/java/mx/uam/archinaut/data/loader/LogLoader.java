package mx.uam.archinaut.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opencsv.CSVReaderHeaderAware;

import mx.uam.archinaut.data.nameprocessing.NameProcessor;

@Component
public class LogLoader {

	private static final String FILENAMEHEADER = "entity";
	private static final String REVISIONSHEADER = "n-revs";
	private static final String COUPLEDHEADER = "coupled";
	private static final String DEGREEHEADER = "degree";
	private static final String AVERAGEREVSHEADER = "average-revs";
	private static final String CHURNADDED = "added";
	private static final String CHURNREMOVED = "removed";
	private static final String COCHANGESHEADER = "cochanges";
	private static final String BUGCOMMITS = "bugs";

	private static final String DEFAULT_FREQUENCIES_FILENAME = "frequencies.csv";

	// The logger
	private static final Logger logger = LoggerFactory.getLogger(LogLoader.class);

}
