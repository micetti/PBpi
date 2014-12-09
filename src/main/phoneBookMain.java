package main;

import java.util.ArrayList;
import java.util.HashMap;

import utils.PushEntry;
import client.PushbulletDevice;

public class phoneBookMain {
	public static void main(String[] args) throws InterruptedException {

		HashMap<String, String> phoneBook = new HashMap<String, String>();
		phoneBook.put("006C1A0D", "A");
		phoneBook.put("006C499F", "B");
		phoneBook.put("006C261D", "C");
		phoneBook.put("006C3726", "D");
		phoneBook.put("006C0F6A", "E");
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
					pEntry.title = "Telephonebook Entry";
					if (phoneBook.containsKey(pEntry.body)) {
						pEntry.body = phoneBook.get(pEntry.body);
					}
					// TODO: only be temporary Hack for demo. push to "Chrome"
					// if request came from Android app or Chrome
					if (pEntry.targetDevice.equals("NA")) {
						pEntry.targetDevice = "ujC6kIYE4xEsjzWIEVDzOK";
					}
					repeater.push(pEntry);
				}
			}
			Thread.sleep(1000);
		}
	}
}
