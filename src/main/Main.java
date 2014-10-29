package main;

import java.util.ArrayList;

import utils.PushEntry;
import client.PushbulletDevice;

public class Main {

	public static void main(String[] args) {

		ArrayList<PushEntry> pushList = new ArrayList<PushEntry>();
		PushEntry pEntry;
		PushbulletDevice repeater = new PushbulletDevice();
		while (true) {
			pushList = repeater.read();
			if (pushList.size() > 0) {
				for (int i = 0; i < pushList.size(); i++) {
					pEntry = pushList.get(i);
					repeater.deletePush(pEntry);
					pEntry.targetDevice = pEntry.sourceDevice;
					pEntry.sourceDevice = repeater.getIden();
					pEntry.title = pEntry.title.concat(" -ECHO");
					// TODO: only be temporary Hack for demo. push to "Chrome";
					if (pEntry.targetDevice.equals("NA")) {
						pEntry.targetDevice = "ujC6kIYE4xEsjzWIEVDzOK";
					}
					repeater.push(pEntry);
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
