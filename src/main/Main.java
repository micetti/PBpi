package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import utils.PushEntry;
import client.PushbulletDevice;

public class Main {

	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {

		ArrayList<PushEntry> pushList = new ArrayList<PushEntry>();
		PushEntry pEntry;
		PushbulletDevice repeater = new PushbulletDevice();

        String s;
        Process p;
		while (true) {
			pushList = repeater.read();
			if (pushList.size() > 0) {
				for (int i = 0; i < pushList.size(); i++) {
		            p = Runtime.getRuntime().exec("raspistill -vf -hf -w 648 -h 498 -t 500 -o pic.jpg");
		            BufferedReader br = new BufferedReader(
		                new InputStreamReader(p.getInputStream()));
		            while ((s = br.readLine()) != null)
		            p.waitFor();
		            p.destroy();
		    		File file = new File("./pic.jpg");
		    		String msg = repeater.pushFile(file);
		    		System.out.println(msg);
					pEntry = pushList.get(i);
					repeater.deletePush(pEntry);
					pEntry.targetDevice = pEntry.sourceDevice;
					pEntry.sourceDevice = repeater.getIden();
					pEntry.title = "Temperature and Humidity in Fyfe House G36";
					pEntry.body = msg;
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

//		BufferedImage img = ImageIO.read(new URL(msg));
//	    File outputfile = new File("./saved.jpg");
//	    ImageIO.write(img, "jpg", outputfile);
	}
}
