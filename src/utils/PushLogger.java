package utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import client.PBClient;

public class PushLogger {
	private PBClient client;
	private int logLevel;

	/**********************************************************************************************
	 * @param loggerlevel
	 *            defines the level of the logger if a push has a lower level
	 *            nothing will be pushed.
	 */
	public PushLogger(int loggerlevel) {
		client = new PBClient();
		try {
			client.listAllDevices();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logLevel = loggerlevel;
	}

	/**********************************************************************************************
	 * @param level
	 *            the more important somesting is the higher level should be
	 *            used. If level is below the level of the logger nothing will
	 *            be pushed.
	 */
	public void log(int level, String msg) {
		if (client.getIden("Logger") == null)
			return;
		if (level <= logLevel)
			client.push(Props.deviceName(), msg, client.getIden("Logger"));
	}
}
