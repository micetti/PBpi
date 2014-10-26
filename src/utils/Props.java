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
		return properties.getProperty("apiKey");
	}
	
	public static String deviceName() {
		return properties.getProperty("deviceName");
	}
	
	public static int logLevel() {
		return Integer.parseInt(properties.getProperty("logLevel"));
	}

	public static void read() throws IOException {
		BufferedInputStream stream;
		stream = new BufferedInputStream(new FileInputStream(
				"./general.properties"));
		properties.load(stream);
		stream.close();

	}

}
