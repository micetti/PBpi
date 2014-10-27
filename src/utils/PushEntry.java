package utils;

import org.json.simple.JSONObject;

public class PushEntry {
	public String title;
	public String body;
	public String pushIden;
	public double created;
	public String sourceDevice;
	public String targetDevice;
	
	public PushEntry(JSONObject pushMap) {
		title = pushMap.get("title").toString();
		body = pushMap.get("body").toString();
		pushIden = pushMap.get("iden").toString();
		created = Double.parseDouble(pushMap.get("created").toString());
		sourceDevice = pushMap.get("source_device_iden").toString();
		targetDevice = pushMap.get("target_device_iden").toString();
	}

}
