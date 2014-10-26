package main;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import utils.Props;
import client.PBClient;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();

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
		while (true) {
			client.push("PiTestTime", "Uptime: "
					+ (System.currentTimeMillis() - start) / 1000 + "seconds",
					client.getIden("TestDevice"));
			Thread.sleep(60*60*15);
		}
	}

}
