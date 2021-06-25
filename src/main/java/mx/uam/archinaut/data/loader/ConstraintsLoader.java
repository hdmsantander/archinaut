package mx.uam.archinaut.data.loader;

import mx.uam.archinaut.model.dto.MatrixConstraintsDTO;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * 
 * @author humbertocervantes
 *
 */
@Component
public class ConstraintsLoader {

	private static final Logger logger = LoggerFactory.getLogger(ConstraintsLoader.class);

	public MatrixConstraintsDTO loadFromJSON(String filename) {

		try {

			// MatrixConstraintsDTO summary = new Gson().fromJson(br,
			// MatrixConstraintsDTO.class);
			Reader reader = new FileReader(filename);
			MatrixConstraintsDTO matrixConstraintsDTO = new Gson().fromJson(reader, MatrixConstraintsDTO.class);
			reader.close();
			return matrixConstraintsDTO;

		} catch (IOException ex) {
			logger.error("ConstraintsLoader.saveToJSON:", ex);
			return null;
		}

	}

	public boolean saveToJSON(String filename, MatrixConstraintsDTO matrixConstraintsDTO) {

		try {

			Writer writer = new FileWriter(filename);
			new Gson().toJson(matrixConstraintsDTO, writer);
			writer.flush();
			writer.close();

		} catch (IOException ex) {
			logger.error("ConstraintsLoader.saveToJSON:", ex);
			return false;
		}

		return true;
	}
}
