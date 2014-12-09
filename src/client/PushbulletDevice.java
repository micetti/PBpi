package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utils.DeviceEntry;
import utils.Props;
import utils.PushEntry;

public class PushbulletDevice {
	private CredentialsProvider credsProvider = new BasicCredentialsProvider();
	private CloseableHttpClient client;
	private DeviceEntry deviceProperties;
	private HashMap<String, DeviceEntry> devicesMap;
	private Logger logger;

	public PushbulletDevice() {
		// Read the properties file.
		try {
			Props.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log("Constructor: Could not read properties file. IOException!");
			e.printStackTrace();
		}
		logger = new Logger(Props.deviceName());
		logger.log("\n+++Starting Constructor+++");

		// Create the HTTP client. Every client will be linked to a
		// device(name).
		client = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
		credsProvider.setCredentials(new AuthScope("api.pushbullet.com", 443),
				new UsernamePasswordCredentials(Props.apiKey(), null));

		// Check if Nickname specified in general.properties is already in use.
		// If so, delete that device. Nicknames must be unique. Afterwards
		// create the new device and set deviceProperties.
		listAllDevices();
		if (devicesMap.containsKey(Props.deviceName())) {
			deleteDevice(devicesMap.get(Props.deviceName()).iden);
		}
		// System.out.println();
		addDevice();
		// System.out.println();
		listAllDevices();
		deviceProperties = devicesMap.get(Props.deviceName());
	}

	/**********************************************************************************************
	 * This will fill the global devicesMap with DeviceEntry-Objects
	 */
	public void listAllDevices() {
		devicesMap = new HashMap<String, DeviceEntry>();

		// Request for a list of all Devices in JSON.
		HttpGet get = new HttpGet(Props.url() + "/devices");
		StringBuilder result = new StringBuilder();
		try {
			CloseableHttpResponse response = client.execute(get);
			logger.log("Requesting all Devices : " + response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			logger.log("listAllDevices: ClientProtocolException occured!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.log("listAllDevices: IOException occured!");
			e.printStackTrace();
		}

		// Now modify the JSON to fit the DeviceEntry-Object and insert them to
		// the global devicesMap.
		String jsonText = result.toString();
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray devicesArray = (JSONArray) responseMap.get("devices");
		for (int i = 0; i < devicesArray.size(); i++) {
			JSONObject map = (JSONObject) devicesArray.get(i);
			if (map.containsKey("nickname")) {
				devicesMap.put(map.get("nickname").toString(), new DeviceEntry(
						map));
			}
		}

	}

	/**********************************************************************************************
	 * Deletes the device with the specified id
	 * 
	 * @param iden
	 *            It is the unique String associated with the device.
	 */
	public void deleteDevice(String iden) {
		HttpDelete delete = new HttpDelete(Props.url() + "/devices/" + iden);
		try {
			HttpResponse response = client.execute(delete);
			logger.log("Deleting a Device: " + response.getStatusLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**********************************************************************************************
	 * Registers a device to the Account specified with
	 * "private String api_key".
	 * 
	 * @param nick
	 *            The Nickname should be chosen to be unique.
	 */
	public void addDevice() {
		HttpPost post = new HttpPost(Props.url() + "/devices");
		StringBuilder result = new StringBuilder();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("nickname", Props
					.deviceName()));
			nameValuePairs.add(new BasicNameValuePair("type", "stream"));
			// nameValuePairs.add(new BasicNameValuePair("pushable", "true"));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			logger.log("Adding the device " + Props.deviceName() + ": "
					+ response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**********************************************************************************************
	 * Push to a device. The PushEntry-Object contains the iden of the device it
	 * should be pushed to.
	 * 
	 * @param pEnty
	 *            PushEnty-Object containing all necessary information.
	 */
	public void push(PushEntry pEntry) {
		HttpPost post = new HttpPost(Props.url() + "/pushes");
		StringBuilder result = new StringBuilder();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "note"));
			nameValuePairs.add(new BasicNameValuePair("device_iden",
					pEntry.targetDevice));
			nameValuePairs.add(new BasicNameValuePair("title", pEntry.title));
			nameValuePairs.add(new BasicNameValuePair("body", pEntry.body));
			nameValuePairs.add(new BasicNameValuePair("source_device_iden",
					pEntry.sourceDevice));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			logger.log("Pushing " + pEntry.title + " to target: "
					+ response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (IOException e) {
			logger.log("push: IOException occured!");
			e.printStackTrace();
		}
	}

	/**********************************************************************************************
	 * Reads all Pushes for the Device and returns all active pushes as an
	 * ArrayList<PushEntry>
	 */
	public ArrayList<PushEntry> read() {
		ArrayList<PushEntry> resultList = new ArrayList<PushEntry>();
		HttpGet get = new HttpGet(Props.url() + "/pushes?modified_after="
				+ deviceProperties.created);
		StringBuilder result = new StringBuilder();
		try {
			CloseableHttpResponse response = client.execute(get);
			logger.log("Reading all pushes for " + deviceProperties.nickName
					+ ": " + response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			logger.log("read: ClientProtocolException occured!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.log("read: IOException occured!");
			e.printStackTrace();
		}

		// Move wanted information to ArrayList<PushEntry>
		String jsonText = result.toString();
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray devicesArray = (JSONArray) responseMap.get("pushes");
		for (int i = 0; i < devicesArray.size(); i++) {
			JSONObject map = (JSONObject) devicesArray.get(i);
			if (map.containsKey("body")) {
				if (map.get("target_device_iden").equals(deviceProperties.iden))
					resultList.add(new PushEntry(map));
			}
		}
		return resultList;
	}

	/**********************************************************************************************
	 * This deletes the specified pushEntry object from the Server
	 * 
	 * @param pEntry
	 *            This is the entry that will be deleted from the server.
	 */
	public void deletePush(PushEntry pEntry) {
		HttpDelete delete = new HttpDelete(Props.url() + "/pushes/"
				+ pEntry.pushIden);
		try {
			HttpResponse response = client.execute(delete);
			logger.log("Deleting push with Titel <" + pEntry.title + ">: "
					+ response.getStatusLine());
			StringBuilder result = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			logger.log("deletePush: ClientProtocolException occured!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.log("deletePush: IOException occured!");
			e.printStackTrace();
		}
	}

	/**********************************************************************************************
	 * Reads only Pushes targeted at all devices and returns all active pushes
	 * as an ArrayList<PushEntry> this is only needed to read data from web
	 * services
	 */
	public ArrayList<PushEntry> allRead() {
		ArrayList<PushEntry> resultList = new ArrayList<PushEntry>();
		HttpGet get = new HttpGet(Props.url() + "/pushes?modified_after=" + 0);
		StringBuilder result = new StringBuilder();
		try {
			CloseableHttpResponse response = client.execute(get);
			logger.log("Reading all pushes for " + deviceProperties.nickName
					+ ": " + response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			logger.log("read: ClientProtocolException occured!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.log("read: IOException occured!");
			e.printStackTrace();
		}

		// Move wanted information to ArrayList<PushEntry>
		String jsonText = result.toString();
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray devicesArray = (JSONArray) responseMap.get("pushes");
		for (int i = 0; i < devicesArray.size(); i++) {
			JSONObject map = (JSONObject) devicesArray.get(i);
			if (map.containsKey("body")
					&& !map.containsKey("target_device_iden")) {

				resultList.add(new PushEntry(map));
			}
		}
		return resultList;
	}

	/**********************************************************************************************
	 * @return returns the url to the file as a string after succesfull upload
	 *         of the file to the servers
	 * @param file
	 *            File object representation of the File that should be pushed
	 */
	public String pushFile(File file) {
		HttpPost post = new HttpPost(Props.url() + "/upload-request");
		StringBuilder result = new StringBuilder();
		String file_url = "";
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("file_name", file
					.getName()));
			nameValuePairs
					.add(new BasicNameValuePair("file_type", "image/jpeg"));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			logger.log("Pushing the File " + file.getName() + ": " + response);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
			// We need information from the result String
			String jsonText = result.toString();
			Object obj = JSONValue.parse(jsonText);
			JSONObject responseMap = (JSONObject) obj;
			post = new HttpPost(responseMap.get("upload_url").toString());
			file_url = responseMap.get("file_url").toString();
			responseMap = (JSONObject) responseMap.get("data");

			// use that information to fill the required fields
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("awsaccesskeyid",
					responseMap.get("awsaccesskeyid").toString());
			builder.addTextBody("acl", responseMap.get("acl").toString());
			builder.addTextBody("key", responseMap.get("key").toString());
			builder.addTextBody("signature", responseMap.get("signature")
					.toString());
			builder.addTextBody("policy", responseMap.get("policy").toString());
			builder.addTextBody("content-type", responseMap.get("content-type")
					.toString());
			builder.addBinaryBody("file", file);
			post.setEntity(builder.build());

			HttpResponse fileResponse = client.execute(post);
			logger.log("Pushing the File " + file.getName() + ": "
					+ fileResponse);

		} catch (IOException e) {
			logger.log("pushFile : IOException occured");
		}
		return file_url;

	}

	public String getNickname() {
		return deviceProperties.nickName;
	}

	public double getTimestamp() {
		return deviceProperties.created;
	}

	public String getIden() {
		return deviceProperties.iden;
	}

	public DeviceEntry getDeviceEntry(String nick) {
		listAllDevices();
		return devicesMap.get(nick);
	}

}
