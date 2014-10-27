package utils;

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

public class PushLogger {
	private CredentialsProvider credsProvider = new BasicCredentialsProvider();
	private CloseableHttpClient client;
	private int logLevel;
	private HashMap<String, HashMap<String, String>> idenNick;

	/**********************************************************************************************
	 * @param loggerlevel
	 *            defines the level of the logger if a push has a lower level
	 *            nothing will be pushed.
	 */
	public PushLogger(int loggerlevel) {
		try {
			Props.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		client = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
		credsProvider.setCredentials(new AuthScope("api.pushbullet.com", 443),
				new UsernamePasswordCredentials(Props.apiKey(), null));
		logLevel = loggerlevel;
		listAllDevices();
	}

	/**********************************************************************************************
	 * @param level
	 *            the more important somesting is the higher level should be
	 *            used. If level is below the level of the logger nothing will
	 *            be pushed.
	 */
	public void log(int level, String msg) {
		if (getIden("Logger") == null)
			return;
		if (level >= logLevel) {
			push(Props.deviceName(), msg, getIden("Logger"));
		}
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
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String jsonText = result.toString();
		Object obj = JSONValue.parse(jsonText);
		JSONObject responseMap = (JSONObject) obj;
		JSONArray devicesArray = (JSONArray) responseMap.get("devices");
		// TODO: deviceArray may be null
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
	 * @param titel
	 *            Titel of the push
	 * @param content
	 *            All Information that should be pushed.
	 * @param iden
	 *            Unique number to identify the device this push should go to.
	 */
	public void push(String title, String content, String iden) {
		HttpPost post = new HttpPost(Props.url() + "/pushes");
		StringBuilder result = new StringBuilder();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "note"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", iden));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("body", content));
			nameValuePairs.add(new BasicNameValuePair("source_device_iden",
					this.getIden(Props.deviceName())));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
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
     * 
     */
	public String getIden(String nick) {
		if (!idenNick.containsKey(nick)) {
			return null;
		}
		return idenNick.get(nick).get("iden");
	}
	
}
