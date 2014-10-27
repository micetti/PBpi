package client;

import java.io.BufferedReader;
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
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utils.Props;
import utils.PushEntry;
import utils.PushLogger;

public class PBClient {

	private CredentialsProvider credsProvider = new BasicCredentialsProvider();
	private CloseableHttpClient client;
	private HashMap<String, HashMap<String, String>> idenNick;
	static PushLogger logger;

	/**********************************************************************************************
     * 
     */
	public PBClient() {
		
		try {
			Props.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger = new PushLogger(Props.logLevel());
		client = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
		credsProvider.setCredentials(new AuthScope("api.pushbullet.com", 443),
				new UsernamePasswordCredentials(Props.apiKey(), null));
		logger.log(10, "Client ready!");
	}

	/**********************************************************************************************
	 * @return returns all pushes as a String in JSON-style
	 */
	public String getAllPushes() {
		HttpGet get = new HttpGet(Props.url() + "/pushes?modified_after=0");
		StringBuilder result = new StringBuilder();
		CloseableHttpResponse response;
		try {
			response = client.execute(get);

			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = result.toString();
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray pushArray = (JSONArray) responseMap.get("pushes");
		return pushArray.toString();
	}

	/**********************************************************************************************
	 * @param timeStamp
	 *            The method will return all pushes after the timestamp. Format:
	 *            n.nnnnnnnnnne+nn where n is any digit.
	 * @return Returns all pushes as a String in JSON-style.
	 */
	public String getAllPushes(String timeStamp) {
		logger.log(4, "Requesting all Pushes after timeStamp: "
				+ toDouble(timeStamp));
		HttpGet get = new HttpGet(Props.url() + "/pushes?modified_after="
				+ toDouble(timeStamp));
		StringBuilder result = new StringBuilder();
		CloseableHttpResponse response;
		try {
			response = client.execute(get);

			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = result.toString();
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray pushArray = (JSONArray) responseMap.get("pushes");
		logger.log(4, "The pushes found for the requested TimeStamp: "
				+ pushArray.toString());
		return pushArray.toString();
	}

	/**********************************************************************************************
	 * Method to fill the HashMap<String, HashMap<String, String> "idenNick"
	 * with information about all devices registered. Currently every Nickname
	 * is key to access a Hashmap with the two keys iden and created
	 */
	public void listAllDevices() {
		idenNick = new HashMap<String, HashMap<String, String>>();
		HttpGet get = new HttpGet(Props.url() + "/devices");
		StringBuilder result = new StringBuilder();
		CloseableHttpResponse response;
		try {
			response = client.execute(get);
			logger.log(5,
					"Requesting all Devices : " + response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			logger.log(7, "ClientProtocolException while listing all Devices");
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(7, "IOException while listing all Devices");
			e.printStackTrace();
		}
		String jsonText = result.toString();
		logger.log(1, "Response String of URL/Devices request :" + jsonText);
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray devicesArray = (JSONArray) responseMap.get("devices");
		// TODO: deviceArray may be null
		logger.log(4, "Number of total Devices: " + devicesArray.size());

		for (int i = 0; i < devicesArray.size(); i++) {
			JSONObject deviceMap = (JSONObject) devicesArray.get(i);
			if (deviceMap.containsKey("iden")
					&& deviceMap.containsKey("nickname")) {
				if (idenNick.containsKey(deviceMap.get("nickname"))) {
					System.out
							.println("ERROR: Two Devices with same Nickname... "
									+ deviceMap.get("nickName"));
					continue;
				}
				HashMap<String, String> valuePairs = new HashMap<String, String>();
				valuePairs.put("iden", deviceMap.get("iden").toString());
				valuePairs.put("created", deviceMap.get("created").toString());
				idenNick.put(deviceMap.get("nickname").toString(), valuePairs);
			}
		}
	}

	/**********************************************************************************************
	 * Registers a device to the Account specified with
	 * "private String api_key".
	 * 
	 * @param nick
	 *            The Nickname should be choosen to be unique.
	 */
	public void addDevice(String nick) {
		HttpPost post = new HttpPost(Props.url() + "/devices");
		StringBuilder result = new StringBuilder();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("nickname", nick));
			nameValuePairs.add(new BasicNameValuePair("type", "stream"));
			// nameValuePairs.add(new BasicNameValuePair("pushable", "true"));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			logger.log(5, "Registering a Device: " + response.getStatusLine());
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
	 * Deletes the device specified with param 'iden'. Find 'iden' with Nickname
	 * in the Deviceslist called 'idenNick'
	 * 
	 * @param iden
	 *            It is the unique String associated with the device.
	 */
	public void deleteDevice(String iden) {
		HttpDelete delete = new HttpDelete(Props.url() + "/devices/" + iden);
		try {
			HttpResponse response = client.execute(delete);
			logger.log(5, "Deleting a Device: " + response.getStatusLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.listAllDevices();

	}

	/**********************************************************************************************
	 * @param titel
	 *            Titel of the push
	 * @param content
	 *            All Information that should be pushed.
	 * @param targetIden
	 *            Unique number to identify the device this push should go to.
	 */
	public void push(String title, String content, String targetIden, String sourceIden) {
		HttpPost post = new HttpPost(Props.url() + "/pushes");
		StringBuilder result = new StringBuilder();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "note"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", targetIden));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("body", content));
			nameValuePairs.add(new BasicNameValuePair("source_device_iden", sourceIden));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			logger.log(5, "Pushing to device " + targetIden + ": "
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
	 * Method for reading all pushes after "timeStamp" for the specified device
	 * "iden"
	 * 
	 * @param iden
	 *            Unique number to identify which device wants to read it's
	 *            pushes.
	 * @param timeStamp
	 *            Show only pushes newer than "timeStamp" indicates.
	 * @return Returns all pushes as ArrayList of PushEntry Objects
	 */
	public ArrayList<PushEntry> read(String device_iden, String timeStamp) {
		String jsonText = this.getAllPushes(timeStamp);
		// System.out.println(jsonText);
		Object obj = JSONValue.parse(jsonText);
		JSONArray pushArray = (JSONArray) obj;
		ArrayList<PushEntry> resultList = new ArrayList<PushEntry>();
		logger.log(4, "Number of Pushes after the requested timeStamp: "
				+ pushArray.size());
		for (int i = 0; i < pushArray.size(); i++) {
			JSONObject pushMap = (JSONObject) pushArray.get(i);
			if (pushMap.containsKey("target_device_iden")) {
				if (pushMap.get("target_device_iden").toString()
						.equals(device_iden)) {
					resultList
							.add(new PushEntry((JSONObject) pushArray.get(i)));
				}
			} else {
				logger.log(4, pushArray.get(i).toString().substring(0, 25)
						+ " ...did not contain target_device_iden");
			}
		}
		return resultList;
	}

	/**********************************************************************************************
	 * Method for deleting one Push.
	 * 
	 * @param iden
	 *            specifies the push that should be deleted.
	 */
	public void deletePush(String iden) {
		logger.log(1, "Going to delete: " + iden);
		HttpDelete delete = new HttpDelete(Props.url() + "/pushes/" + iden);
		try {
			logger.log(1, "...");
			HttpResponse response = client.execute(delete);
			logger.log(5, "Deleting a post: " + response.getStatusLine());
			StringBuilder result = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
			logger.log(1, "...");
		} catch (ClientProtocolException e) {
			logger.log(1, "ClientProtocolException");
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(1, "IOException");
			e.printStackTrace();
		}
		logger.log(1, "...and it's done");
	}

	/**********************************************************************************************
	 * Changes the String notation of the HTTPS timeStamps to according double
	 * value.
	 */
	public static double toDouble(String string) {
		if (string.contains("E")) {
			return Double.parseDouble(string);
		}
		String[] numbers = string.split("e+");
		if (numbers.length != 2) {
			logger.log(7, "ERROR with timestamp");
			return 0;
		}
		double number = Double.parseDouble(numbers[0]);
		double power = Double.parseDouble(numbers[1]);
		return (number * (Math.pow(10, power)));
	}

	/**********************************************************************************************
     * 
     */
	public String getIden(String nick) {
		if (!idenNick.containsKey(nick)) {
			return null;
		}
		return idenNick.get(nick).get("iden");
	}

	/**********************************************************************************************
     * 
     */
	public String getTimeStamp(String nick) {
		if (!idenNick.containsKey(nick)) {
			return null;
		}
		return idenNick.get(nick).get("created");
	}

	/**********************************************************************************************
     * 
     */
	public String deviceListToString() {
		return idenNick.toString();
	}
}
