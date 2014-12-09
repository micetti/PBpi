package main;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class ArduinoReaderMain {
	private static String hum;
	private static String temp;
	
    public static void main(String args[]) throws InterruptedException {              
        System.out.println(" ... connect using settings: 38400, N, 8, 1.");
        System.out.println(" ... data received on serial port should be displayed below.");
        
        // create an instance of the serial communications class
        final Serial serial = SerialFactory.createInstance();

		// create and register the listener for the arduino data with some
		// conversions
		serial.addListener(new SerialDataListener() {
			public void dataReceived(SerialDataEvent event) {
				String serial = event.getData();
				System.out.println(serial + " has length :" + serial.length());
				if (serial.length() == 22 && serial.contains("Z") && serial.contains("temp:") && serial.contains("hum:")) {
					String[] tmp = serial.split("Z");
					String[] humtmp = tmp[1].split(":");
					String[] temptmp = tmp[0].split(":");
					hum = humtmp[1];
					System.out.println(hum);
					temp = temptmp[1];
					System.out.println(temp);
				}
			}
		});
                
        try {
        	// open the default serial port provided on the GPIO header
            serial.open("/dev/ttyACM0", 9600);

            // continuous loop to keep the program running until the user terminates the program
            for (;;) {
                try {

                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
              Thread.sleep(1000);
            }
      
        }
        catch(SerialPortException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return;
        }
    }
}
