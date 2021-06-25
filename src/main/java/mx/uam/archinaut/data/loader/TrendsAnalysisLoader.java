package mx.uam.archinaut.data.loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.model.dto.TrendsAnalysisDTO;

@Slf4j
@Component
public class TrendsAnalysisLoader {

	public TrendsAnalysisDTO loadFromJSON(String filename) {

		try (Reader reader = new FileReader(filename)) {

			TrendsAnalysisDTO trendsAnalysisDTO = new Gson().fromJson(reader, TrendsAnalysisDTO.class);

			log.info("Loaded analysis with " + trendsAnalysisDTO.getNumberOfSnapshots() + " snapshots");

			return trendsAnalysisDTO;

		} catch (IOException ex) {
			log.error("TrendAnalysisLoader.loadFromJSON", ex);
			return null;
		}

	}

	public boolean saveToJSON(String filename, TrendsAnalysisDTO trendsAnalysisDTO) {

		try(Writer writer = new FileWriter(filename)) {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(trendsAnalysisDTO, writer);
			writer.flush();

		} catch (IOException ex) {
			log.error("TrendAnalysisLoader.saveToJSON", ex);
			return false;
		}

		return true;
	}

	public boolean saveReport(String foldername, String filename, StringBuilder report) {

		File file = new File(foldername + System.lineSeparator() + filename);
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(report.toString());
		} catch (Exception ex) {
			log.error("TrendAnalysisLoader.saveReport", ex);
			return false;
		}
		return true;
	}
}
