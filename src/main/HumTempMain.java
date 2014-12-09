package main;

import java.util.ArrayList;

import utils.PushEntry;
import client.PushbulletDevice;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class HumTempMain {
	private static String hum;
	private static String temp;

	public static void main(String args[]) throws InterruptedException {

		PushbulletDevice device = new PushbulletDevice();
		ArrayList<PushEntry> pushList = new ArrayList<PushEntry>();
		PushEntry pEntry;
		String weather, sunset, sunrise, wind, highTemp, lowTemp;

		pushList = device.allRead();
		pEntry = pushList.get(pushList.size() - 1);
		weather = pEntry.title;
		String[] content = pEntry.body.split("!");
		String[] pair = content[0].split("=");
		sunrise = pair[1];
		pair = content[1].split("=");
		sunset = pair[1];
		pair = content[2].split("=");
		highTemp = pair[1];
		pair = content[3].split("=");
		lowTemp = pair[1];
		pair = content[4].split("=");
		wind = pair[1];

		// create instances of the serial communications class
		final Serial rfid = SerialFactory.createInstance();
		final Serial arduino = SerialFactory.createInstance();

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

		// create and register the listener for the arduino data with some
		// conversions
		arduino.addListener(new SerialDataListener() {
			public void dataReceived(SerialDataEvent event) {
				String serial = event.getData();
				// System.out.println(serial + " has length :" +
				// serial.length());
				if (serial.length() == 22 && serial.contains("Z")
						&& serial.contains("temp:") && serial.contains("hum:")) {
					String[] tmp = serial.split("Z");
					String[] humtmp = tmp[1].split(":");
					String[] temptmp = tmp[0].split(":");
					hum = humtmp[1];
					// System.out.println(hum);
					temp = temptmp[1];
					// System.out.println(temp);
				}
			}
		});

		try {
			rfid.open("/dev/ttyAMA0", 9600);
			arduino.open("/dev/ttyACM0", 9600);
		} catch (SerialPortException ex) {
			System.out
					.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}

		while (true) {
			pushList = device.read();
			if (pushList.size() > 0) {
				for (int i = 0; i < pushList.size(); i++) {
					pEntry = pushList.get(i);
					device.deletePush(pEntry);
					if (pEntry.title.equals("Telephonebook Entry")) {
						pEntry.targetDevice = device
								.getDeviceEntry(pEntry.body).iden;
						pEntry.sourceDevice = device.getIden();
						pEntry.title = "Temperature and Humidity";
						pEntry.body = "Temperature: " + temp
								+ "° Celsius. Humidity: " + hum
								+ "% . Weather will be " + weather
								+ " with temperatures ranging from " + lowTemp
								+ " to " + highTemp
								+ "° Celsius. Windspeeds of " + wind + " kph can be reached.";
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