package utils;

import org.json.simple.JSONObject;

public class DeviceEntry {
	public String nickName;
	public double created;
	public String iden;
	
	public DeviceEntry(JSONObject deviceMap) {
		nickName = deviceMap.get("nickname").toString();
		created = Double.parseDouble(deviceMap.get("created").toString());
		iden = deviceMap.get("iden").toString();
		System.out.println("There is a Device-Object for " + nickName + "!");
	}

}
