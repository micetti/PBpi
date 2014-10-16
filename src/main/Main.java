package main;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import utils.Props;
import client.PBClient;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();

//		Logger log = Logger.getLogger(PBClient.class.getName());
//		Handler handler;
//		try {
//			// handler = new FileHandler("log.txt");
//			handler = new ConsoleHandler();
//			log.setUseParentHandlers(false);
//			log.setLevel(Level.FINEST);
//			handler.setLevel(Level.FINEST);
//			handler.setFormatter(new Formatter() {
//				public String format(LogRecord record) {
//					return "[" + record.getLevel() + "] "
//							+ record.getSourceClassName() + "."
//							+ record.getSourceMethodName() + " :: "
//							+ record.getMessage() + "\n";
//				}
//			});
//		} catch (SecurityException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}// catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		PBClient client = new PBClient();
		try {
			client.listAllDevices();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (client.getIden(Props.deviceName()) != null) {
//			log.severe("A Device with name " + Props.deviceName()
//					+ " is already in use. Delete first.");
//			return;
			client.deleteDevice(client.getIden(Props.deviceName()));
		}
		client.addDevice(Props.deviceName());
//		try {
//			client.cleanPushHistory();
//		} catch (ClientProtocolException e) {
//			System.out.println("ClientProtocolException");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.out.println("IOException");
//			e.printStackTrace();
//		}
		while (true) {
			client.push("PiTestTime", "Uptime: "
					+ (System.currentTimeMillis() - start) / 1000 + "seconds",
					client.getIden("TestDevice"));
			Thread.sleep(60);
		}
	}

}
