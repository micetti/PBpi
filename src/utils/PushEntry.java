package utils;

import org.json.simple.JSONObject;

public class PushEntry {
	public String title;
	public String body;
	public String pushIden;
	public double created;
	public String sourceDevice;
	public String targetDevice;

	// TODO: could be empty
	public PushEntry(JSONObject pushMap) {
		if (pushMap.containsKey("title")) {
			title = pushMap.get("title").toString();
		} else {
			title = "Push";
		}
		// Pushes without body are ignored by default right now.
		body = pushMap.get("body").toString();
		pushIden = pushMap.get("iden").toString();
		created = Double.parseDouble(pushMap.get("created").toString());
		// sourceDevice is an optional parameter, not used by the app for example.
		if (pushMap.containsKey("source_device_iden")) {
			sourceDevice = pushMap.get("source_device_iden").toString();
		} else {
			sourceDevice = "NA";
		}
		targetDevice = pushMap.get("target_device_iden").toString();
	}

	/**********************************************************************************************
	 * Push to a device. The PushEntry-Object contains the iden of the device it
	 * should be pushed to.
	 * 
	 * @param Titel
	 *            Titel of the Push.
	 * @param Body
	 *            Content of the Push.
	 * @param TagertDevice
	 *            iden of the Device this push should go to.
	 * @param SourceDevice
	 *            Good for sending back responses. Iden of the sending device.
	 */
	public PushEntry(String Title, String Body, String TargetDevice,
			String SourceDevice) {
		title = Title;
		body = Body;
		pushIden = "";
		created = 0;
		sourceDevice = SourceDevice;
		targetDevice = TargetDevice;
	}

}
