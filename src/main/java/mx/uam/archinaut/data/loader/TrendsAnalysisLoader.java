package mx.uam.archinaut.data.loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mx.uam.archinaut.model.dto.TrendsAnalysisDTO;

@Component
public class TrendsAnalysisLoader {

	private static final Logger logger = LoggerFactory.getLogger(TrendsAnalysisLoader.class);

	public TrendsAnalysisDTO loadFromJSON(String filename) {

		try {

			// MatrixConstraintsDTO summary = new Gson().fromJson(br,
			// MatrixConstraintsDTO.class);
			Reader reader = new FileReader(filename);
			TrendsAnalysisDTO trendsAnalysisDTO = new Gson().fromJson(reader, TrendsAnalysisDTO.class);
			reader.close();

			logger.info("Loaded analysis with " + trendsAnalysisDTO.getNumberOfSnapshots() + " snapshots");

			return trendsAnalysisDTO;

		} catch (IOException ex) {
			logger.error("TrendAnalysisLoader.loadFromJSON", ex);
			return null;
		}

	}

	public boolean saveToJSON(String filename, TrendsAnalysisDTO trendsAnalysisDTO) {

		try {

			Writer writer = new FileWriter(filename);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(trendsAnalysisDTO, writer);
			writer.flush();
			writer.close();

		} catch (IOException ex) {
			logger.error("TrendAnalysisLoader.saveToJSON", ex);
			return false;
		}

		return true;
	}

	public boolean saveReport(String foldername, String filename, StringBuilder report) {

//		logger.info("Saving report to folder: "+foldername+" and file "+filename);
		File file = new File(foldername + "/" + filename);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(report.toString());
			writer.close();
		} catch (Exception ex) {
			logger.error("TrendAnalysisLoader.saveReport", ex);
			return false;
		}
		return true;
	}
}
