package main;

import java.util.ArrayList;

import utils.Props;
import utils.PushEntry;
import client.PBClient;

public class Main {

	public static void main(String[] args) {
		PBClient client = new PBClient();
		client.listAllDevices();
		if (client.getIden(Props.deviceName()) != null) {
			client.deleteDevice(client.getIden(Props.deviceName()));
		}
		client.addDevice(Props.deviceName());
//		logger.log(7, "Registered the Device " + Props.deviceName() + ". Now ready to echo Pushes!");
		client.listAllDevices();
		ArrayList<PushEntry> pushList;
		while (true) {
			pushList = client.read(client.getIden(Props.deviceName()), client.getTimeStamp(Props.deviceName()));
			if (pushList.size() != 0){
				for (int i = 0; i < pushList.size(); i++) {
//					client.push(pushList.get(i).title + "-ECHO", pushList.get(i).body, pushList.get(i).sourceDevice);
				}
				for (int i = 0; i < pushList.size(); i++) {
					client.deletePush(pushList.get(i).pushIden);
				}
			}
		}
	}

}
