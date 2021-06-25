package mx.uam.archinaut.data.loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetricsLoader {

	/**
	 * 
	 * @param foldername
	 * @param filename
	 * @param report
	 * @return
	 */
	public boolean saveReport(String filename, StringBuilder report) {

		File file = new File(filename);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(report.toString());
			writer.close();
		} catch (Exception ex) {
			log.error("MetricsLoader.saveReport", ex);
			return false;
		}
		return true;
	}

}