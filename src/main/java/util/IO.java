package util;

import java.io.File;
import java.io.IOException;

public class IO {

	public static File createOpen(String filePath) throws IOException {
		File file = new File(filePath);
		// Create directories up to the parent directory
		file.getParentFile().mkdirs();
		// If the parent directory does not exist, something must have gone wrong in the creation
		if(!file.getParentFile().exists()) {
			throw new IOException("At least one directory could not be created for the file: " + filePath);
		}
		file.createNewFile();
		return file;
	}

}
