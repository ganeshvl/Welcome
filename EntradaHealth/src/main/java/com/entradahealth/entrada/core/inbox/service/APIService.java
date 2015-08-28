package com.entradahealth.entrada.core.inbox.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;


public class APIService {

	private MyHttpClient client;
	private HttpHost host;
	private static final String SM_MODULE = "SM_ENTRADA";

	public APIService(Context context, String hostStr) {
		client = new MyHttpClient(context);
		host = new HttpHost(hostStr);
	}

	public class MyHttpClient extends DefaultHttpClient {

		final Context context;

		public MyHttpClient(Context context) {
			this.context = context;
		}

		@Override
		protected ClientConnectionManager createClientConnectionManager() {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", newSslSocketFactory(), 443));
			return new SingleClientConnManager(getParams(), registry);
		}

		private SSLSocketFactory newSslSocketFactory() {
			try {
				KeyStore trusted = KeyStore.getInstance("BKS");
				InputStream in = context.getResources().openRawResource(
						R.raw.mykeystorepem);
				try {
					trusted.load(in, "android".toCharArray());
				} finally {
					in.close();
				}
				SSLSocketFactory sf = new SSLSocketFactory(trusted);
				sf.setHostnameVerifier(new MyHostVerifier());
				sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
				return sf;
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}
	
	public HttpResponse sendPutRequest(String requestfor, String url, String data, String qbToken) throws UnsupportedEncodingException, ClientProtocolException, IOException{
		HttpPut request = new HttpPut(url);
		return sendRequest(requestfor, data, qbToken, request);
	}
	
	public HttpResponse sendGetRequest(String requestfor, String url, String qbToken) throws UnsupportedEncodingException, ClientProtocolException, IOException{
		HttpGet request = new HttpGet(url);
		return sendRequest(requestfor, null, qbToken, request);
	}
	
	public HttpResponse sendPostRequest(String requestfor, String url, String postData, String qbToken) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(url);
		return sendRequest(requestfor, postData, qbToken, request);
	}
	
	public HttpResponse sendDeleteRequest(String requestfor, String url, String qbToken) throws ClientProtocolException, IOException {
		HttpDelete request = new HttpDelete(url);
		return sendRequest(requestfor, null, qbToken, request);
	}

	private HttpResponse sendRequest(String requestfor, String postData,
			String qbToken, HttpRequestBase request)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		request.setHeader("Content-type", "application/json");
		request.setHeader("QuickBlox-REST-API-Version", "0.1.1");
		if(qbToken != null){
			request.setHeader("QB-Token", qbToken);
		}
		if(postData != null) {
			((HttpEntityEnclosingRequest)request).setEntity(new StringEntity(postData));
		}
		Log.i(SM_MODULE,
				requestfor+" - Request URL : " + request.getURI());
		Header[] headers = request.getAllHeaders();
		for (Header header : headers) {
			Log.i(SM_MODULE, requestfor+" - Request Headers : "
					+ header.getName() + " : " + header.getValue());
		}
		if(postData != null) {
		Log.i(SM_MODULE, requestfor+" - Request POSTData : "
				+ EntityUtils.toString(((HttpEntityEnclosingRequest)request).getEntity()));
		}
		HttpResponse response = client.execute(host, request);
		return response;
	}

	public HttpResponse sendPostRequest(String requestfor, String url, String postData) throws Exception {
		return sendPostRequest(requestfor, url, postData, null);
	}

	public ENTResponse buildENTResponse(String responsefor, HttpResponse response) throws ParseException, IOException{
		int code = response.getStatusLine().getStatusCode();
		String data = response.getStatusLine().getReasonPhrase();
		HttpEntity entity = response.getEntity();
		String responseData = EntityUtils.toString(entity);
		Log.i(SM_MODULE, responsefor+" - Response Status Code : "
				+ code);
		Log.i(SM_MODULE, responsefor+" - Response Status : " + data);
		Log.i(SM_MODULE, responsefor+" - Response Data : "
				+ responseData);
		ENTResponse entResponse = new ENTResponse();
		entResponse.setStatusCode(code);
		entResponse.setStatusMessage(data);
		entResponse.setResponseData(responseData);
		return entResponse;
	}
	
	public JSONObject parseJSONResponse(String responsefor, HttpResponse response)
			throws JSONException, ParseException, IOException {
		int code = response.getStatusLine().getStatusCode();
		String data = response.getStatusLine().getReasonPhrase();
		HttpEntity entity = response.getEntity();
		String responseData = EntityUtils.toString(entity);
		Log.i(SM_MODULE, responsefor+" - Response Status Code : "
				+ code);
		Log.i(SM_MODULE, responsefor+" - Response Status : " + data);
		Log.i(SM_MODULE, responsefor+" - Response Data : "
				+ responseData);
		JSONObject json = new JSONObject(responseData);
		return json;
	}

	public String hmacDigest(String msg, String keyString, String algo) {
		String digest = null;
		try {
			SecretKeySpec key = new SecretKeySpec(
					(keyString).getBytes("UTF-8"), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key);

			byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (UnsupportedEncodingException e) {
		} catch (InvalidKeyException e) {
		} catch (NoSuchAlgorithmException e) {
		}
		return digest;
	}

	public String getTimeStamp() {
		String time = new Long(new Date().getTime()).toString();
		String timestamp = time.substring(0, time.length() - 3);
		return timestamp;
	}
}
