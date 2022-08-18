package util;

import java.io.*;

public class IO {

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static File createOpen(String filePath) throws IOException {
		File file = new File(filePath);
		// Create directories up to the parent directory
		file.getParentFile().mkdirs();
		// If the parent directory does not exist, something must have gone wrong in the creation
		if (!file.getParentFile().exists()) {
			throw new IOException(
					"At least one directory could not be created for the file: " + filePath);
		}
		file.createNewFile();
		return file;
	}

	public static <T> T jsonFromFile(String pathConfig, Class<T> configClass) {
		try (FileReader reader = new FileReader(pathConfig)) {
			return Convert.gsonPretty.fromJson(reader, configClass);
		} catch (FileNotFoundException e) {
			System.err.println("JSON file not found!\n" + e);
			return null;
		} catch (IOException e) {
			System.err.println("JSON file could not be read!\n" + e);
			return null;
		}
	}

	public static <T> void jsonToFile(T config, String pathConfig) {
		try {
			File configFile = IO.createOpen(pathConfig);
			try (FileWriter writer = new FileWriter(configFile)) {
				Convert.gsonPretty.toJson(config, writer);
			}
		} catch (IOException e) {
			System.err.println("File for JSON could not be created.\n" + e);
		}
	}

}
