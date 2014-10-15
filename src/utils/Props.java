package utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {

	private static final Properties properties = new Properties();

	public static String url() {
		return properties.getProperty("URL");
	}

	public static String apiKey() {
		return properties.getProperty("api_key");
	}

	public static void read() throws IOException {
		BufferedInputStream stream;
		stream = new BufferedInputStream(new FileInputStream(
				"src/general.properties"));
		properties.load(stream);
		stream.close();

	}

}
