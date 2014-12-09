package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import utils.PushEntry;
import client.PushbulletDevice;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class SelfieMain {

	public static void main(String args[]) throws MalformedURLException, IOException, InterruptedException {

		PushbulletDevice device = new PushbulletDevice();
		ArrayList<PushEntry> pushList = new ArrayList<PushEntry>();
		PushEntry pEntry;
        String s;
        Process p;

		// create instances of the serial communications class
		final Serial rfid = SerialFactory.createInstance();
		
		// create and register the listener for the rfid data
		rfid.addListener(new SerialDataListener() {
			public void dataReceived(SerialDataEvent event) {
				String serial = event.getData();
				serial = serial.substring(3, serial.length() - 5);
				// System.out.println(serial);
				if (serial.length() == 8) {
					PushEntry pE = new PushEntry("Phonebook request", serial,
							device.getDeviceEntry("phonebook").iden, device
									.getIden());
					device.push(pE);
				} else {
					System.out.println("Could not read rfid tag");
				}
			}
		});

		try {
			rfid.open("/dev/ttyAMA0", 9600);
		} catch (SerialPortException ex) {
			System.out
					.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}

		while (true) {
			pushList = device.read();
			if (pushList.size() > 0) {
				for (int i = 0; i < pushList.size(); i++) {
		            p = Runtime.getRuntime().exec("raspistill -w 648 -h 498 -t 500 -o pic.jpg");
		            BufferedReader br = new BufferedReader(
		                new InputStreamReader(p.getInputStream()));
		            while ((s = br.readLine()) != null)
		            p.waitFor();
		            p.destroy();
		    		File file = new File("./pic.jpg");
		    		String msg = device.pushFile(file);
		    		System.out.println(msg);
					pEntry = pushList.get(i);
					device.deletePush(pEntry);
					if (pEntry.title.equals("Telephonebook Entry")) {
						pEntry.targetDevice = device
								.getDeviceEntry(pEntry.body).iden;
						pEntry.sourceDevice = device.getIden();
						pEntry.title = "Your new arm free selfie!";
						pEntry.body = msg;
						// TODO: only be temporary Hack for demo. push to
						// "Chrome";
						if (pEntry.targetDevice.equals("NA")) {
							pEntry.targetDevice = "ujC6kIYE4xEsjzWIEVDzOK";
						}
						device.push(pEntry);
					}
				}
			}
			Thread.sleep(1000);
		}

	}
}
