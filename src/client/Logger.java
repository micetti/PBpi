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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utils.DeviceEntry;
import utils.Props;

public class Logger {
	private CredentialsProvider credsProvider = new BasicCredentialsProvider();
	private CloseableHttpClient client;
	private HashMap<String, DeviceEntry> devicesMap;
	private String pushIden;
	private String nickName;
	
	public Logger(String nick) {
		System.out.println("\n+++Starting Logger+++");
		// Read the properties file.
		try {
			Props.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out
					.println("Constructor: Could not read properties file. IOException!");
			e.printStackTrace();
		}

		// Create the HTTP client. The logger will not have a own Device. It
		// depends on an existing Logger device for the Account
		client = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
		credsProvider.setCredentials(new AuthScope("api.pushbullet.com", 443),
				new UsernamePasswordCredentials(Props.apiKey(), null));
		
		listAllDevices();
		if (devicesMap.containsKey("Logger")){
			pushIden = devicesMap.get("Logger").iden;
		} else if (devicesMap.containsKey("Chrome")) {
			pushIden = devicesMap.get("Chrome").iden;
		} else pushIden = "corrupted";
		nickName = nick;
	}
	
	
	/**********************************************************************************************
	 * This will fill the global devicesMap with DeviceEntry-Objects
	 */
	private void listAllDevices() {
		devicesMap = new HashMap<String, DeviceEntry>();

		// Request for a list of all Devices in JSON.
		HttpGet get = new HttpGet(Props.url() + "/devices");
		StringBuilder result = new StringBuilder();
		try {
			CloseableHttpResponse response = client.execute(get);
			System.out.println("Requesting all Devices : "
					+ response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			System.out
					.println("listAllDevices: ClientProtocolException occured!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("listAllDevices: IOException occured!");
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
	 * Log a line and send it to the Logger device.
	 * 
	 * @param pEnty
	 *            PushEnty-Object containing all necessary information.
	 */
	public void log(String msg) {
		HttpPost post = new HttpPost(Props.url() + "/pushes");
		StringBuilder result = new StringBuilder();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "note"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", pushIden));
			nameValuePairs.add(new BasicNameValuePair("title", nickName));
			nameValuePairs.add(new BasicNameValuePair("body", msg));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			System.out.println("New Log Entry: "
					+ response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (IOException e) {
			System.out.println("push: IOException occured!");
			e.printStackTrace();
		}
	}
}
