package com.entradahealth.entrada.core.remote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Contact;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.QBPermission;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.android.app.personal.utils.http.HttpPatch;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.Queue;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.entradahealth.entrada.core.domain.senders.UploadData;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * TODO: Document this!
 * 
 * @author edwards
 * @since 5/10/13
 */
public class APIService implements RemoteService {

	private static final String LOG_NAME = "Entrada-Service";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String API_PREFIX = "/api";
	SharedPreferences sp;
	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
	}

	//DefaultHttpClient client = new DefaultHttpClient();
	MyHttpClient client = new MyHttpClient(EntradaApplication.getAppContext());
	HttpHost host;

	String username;
	String password; 
	String clinicCode;
	
	public class MyHttpClient extends DefaultHttpClient {
		 
	    final Context context;
	 
	    public MyHttpClient(Context context) {
	        this.context = context;
	    }
	 
	    @Override
	    protected ClientConnectionManager createClientConnectionManager() {
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        // Register for port 443 our SSLSocketFactory with our keystore
	        // to the ConnectionManager
	        registry.register(new Scheme("https", newSslSocketFactory(), 443));
	        return new SingleClientConnManager(getParams(), registry);
	    }
	 
	    private SSLSocketFactory newSslSocketFactory() {
	        try {
	            // Get an instance of the Bouncy Castle KeyStore format
	            KeyStore trusted = KeyStore.getInstance("BKS");
	            // Get the raw resource, which contains the keystore with
	            // your trusted certificates (root and any intermediate certs)
	            InputStream in = context.getResources().openRawResource(R.raw.mykeystorepem);
	            try {
	                // Initialize the keystore with the provided trusted certificates
	                // Also provide the password of the keystore
	                trusted.load(in, "android".toCharArray());
	            } finally {
	                in.close();
	            }
	            // Pass the keystore to the SSLSocketFactory. The factory is responsible
	            // for the verification of the server certificate.
	            SSLSocketFactory sf = new SSLSocketFactory(trusted);
	            sf.setHostnameVerifier(new MyHostVerifier());
	            // Hostname verification from certificate
	            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
	            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
	            return sf;
	        } catch (Exception e) {
	            throw new AssertionError(e);
	        }
	    }
	}

	/*public static class WebClientDevWrapper {

		public static HttpClient wrapClient(HttpClient base) {
			try {
				X509TrustManager tm = new X509TrustManager() {

					public void checkClientTrusted(X509Certificate[] xcs,
							String string) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] xcs,
							String string) throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[] { tm }, null);
				SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
				ssf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
				ClientConnectionManager ccm = base.getConnectionManager();
				SchemeRegistry sr = ccm.getSchemeRegistry();
				//sr.register(new Scheme("https", ssf, 443));
				sr.register(new Scheme("https", newSslSocketFactory(), 443));
				return new DefaultHttpClient(ccm, base.getParams());
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		
		private static SSLSocketFactory newSslSocketFactory() {
	        try {
	             KeyStore trusted = KeyStore.getInstance("BKS");
	             InputStream in = BundleKeys.PinContext.getResources().openRawResource(R.raw.mykeystore);
	             try {
	                 trusted.load(in, "android".toCharArray());
	             }
	             finally {
	                  in.close();
	             }

	             SSLSocketFactory mySslFact = new MySSLSocketFactory(trusted);
	             //mySslFact.setHostNameVerifier(new MyHstNameVerifier());
	             return mySslFact;
	         } catch(Exception e) {
	         throw new AssertionError(e);
	        }
	    }
	}*/

	private URI apiPath(String path, Object... args) throws ServiceException {
		return apiPath(path, null, args);
	}

	private URI apiPath(String path, String query, Object... args)
			throws ServiceException {
		try {
			return new URI(host.getSchemeName(), null, host.getHostName(),
					host.getPort(), String.format(API_PREFIX + path, args),
					query, null);
		} catch (URISyntaxException e) {
			throw new ServiceException("Failed to build URI.", e);
		}
	}

	private static URI optionalSchemeUri(String txt) {
		txt = txt.trim();
		if (!txt.startsWith("https://") && !txt.startsWith("http://"))
			txt = "https://" + txt;
		return URI.create(txt);
	}
	
	public APIService(String host) throws MalformedURLException {
		this(optionalSchemeUri(host), "", "", "");
	}

	public APIService(HttpHost host, String username, String password,
			String clinicCode) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.clinicCode = clinicCode;

		sp = EntradaApplication.getAppContext().getSharedPreferences("Entrada", Context.MODE_WORLD_READABLE);
		
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0,
				false));
		String dev_model = android.os.Build.MODEL;
		String dev_version = android.os.Build.VERSION.RELEASE;
		String userAgent = dev_model + " OS " + dev_version
				+ "/Entrada Mobile " + sp.getString("APP_VER_NAME", "5.3.0") + " Build "
				+ sp.getInt("APP_VER_CODE", 1);
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
				userAgent);
		Log.e("VER -- BUILD", sp.getString("APP_VER_NAME", "5.3.0")+" -- "+sp.getInt("APP_VER_CODE", 1));
	}

	public APIService(String host, String username, String password,
			String clinicCode) throws MalformedURLException {
		this(optionalSchemeUri(host), username, password, clinicCode);
	}
	
	public APIService(String host, String username, String password) throws MalformedURLException{
		this(optionalSchemeUri(host), username, password, null);
	}

	public APIService(URI host, String username, String password,
			String clinicCode) throws MalformedURLException {
		this(new HttpHost(host.getHost(), host.getPort(), host.getScheme()),
				username, password, clinicCode);
	}

	public APIService(Account account) throws MalformedURLException {
		this(account.getApiHost(), account.getRemoteUsername(), account
				.getRemotePassword(), account.getClinicCode());
	}

	public boolean isValidHost() {
		try {
			int version = getRemoteVersion();
			return (version >= 1);
		} catch (ServiceException ex) {
			return false;
		}
	}

	@Override
	public SyncData retrieveServiceData() throws ServiceException {
		// TODO: Create a setting for this number.
		return retrieveServiceData(BundleKeys.days_to_sync);
	}

	public SyncData retrieveServiceData(int maxpatientdays)
			throws ServiceException {
		return readRequestObject(
				new HttpPost(apiPath("/mobilesync", String.format(
						"maxpatients=500&maxfuturedays=%d", maxpatientdays))),
				SyncData.class);
	}

	public String retrieveServiceDataString() throws ServiceException {
		// TODO: Create a setting for this number.
		return retrieveServiceDataString(BundleKeys.days_to_sync);
	}

	public String retrieveServiceDataString(int maxpatientdays)
			throws ServiceException {
		return readRequestObject(new HttpPost(apiPath("/mobilesync",
				String.format("maxpatients=500&maxfuturedays=%d",
						maxpatientdays))));
	}

	@Override
	@Deprecated
	public void sendServiceData(UploadData data) throws ServiceException {
		// Note: Exceptions are caught and ignored here because failing will
		// abort sync.
		for (Dictation dict : data.dictations) {
			// this.updateDictation(dict);

			UserState state = AndroidState.getInstance().getUserState();

			synchronized (state) {
				DomainObjectWriter writer = state.getProvider(state
						.getCurrentAccount());

				try {
					APIService service = new APIService(
							state.getCurrentAccount());
					JSONObject jsonObject = new JSONObject();
					// jsonObject.accumulate("DictationID", dict.dictationId);
					jsonObject.accumulate("JobID", dict.jobId);
					jsonObject.accumulate("Status", dict.status.value);
					jsonObject.accumulate("QueueID", dict.queueId);

					// Convert JSONObject to JSON to String
					String json = "";
					json = jsonObject.toString();
					StringEntity se = new StringEntity(json);
					service.updateDictation(json, dict.dictationId);
					// Log.d("Encounter_created", e.toString());

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for (Job job : data.jobs) {
			this.updateJob(job);
		}

	}

	public int getRemoteVersion() throws ServiceException {
		return readRequestObject(new HttpGet(apiPath("/version")),
				Integer.class);
	}

//	public boolean isValidAuthentication() throws ServiceException {
//		try {
//			ObjectNode requestObject = JsonUtils.fromParams("DictatorName",
//					username, "DictatorPassword", password, "ClinicCode",
//					clinicCode);
//			Log.d("Entrada-ServiceX", requestObject.toString());
//			HttpPost request = new HttpPost(apiPath("/dictators/authenticate"));
//			request.setEntity(new StringEntity(mapper
//					.writeValueAsString(requestObject)));
//			request.setHeader("Content-type", "application/json");
//
//			HttpResponse response = makeRequest(request);
//			int code = response.getStatusLine().getStatusCode();
//			String data = response.getStatusLine().getReasonPhrase();
//
//			System.out
//					.println(String.format("auth request: %s", requestObject));
//			System.out.println(String.format(
//					"auth response; code %d, string '%s'", code,
//					data != null ? "\"" + data + "\"" : "null"));
//			return 200 <= code && code < 300;
//		} catch (Exception e) {
//			throw new ServiceException("Unable to authenticate.", e);
//		}
//	}

	public String createEncounter(String e) throws ServiceException {
		return writeRequestObject(new HttpPost(apiPath("/encounters/")), e);
	}

	public Job getJob(int jobId) throws ServiceException {
		return readRequestObject(new HttpGet(apiPath("/jobs/%s", jobId)),
				Job.class);
	}

	public Job updateJob(Job j) throws ServiceException {
		return writeRequestObject(new HttpPatch(apiPath("/jobs/%s", j.id)), j);
	}

	public String insertJob(String j) throws ServiceException {
		return writeRequestObject(new HttpPost(apiPath("/jobs/")), j);
	}

	public Dictation getDictation(long id) throws ServiceException {
		return readRequestObject(new HttpGet(apiPath("/dictations/%s", id)),
				Dictation.class);
	}

	public Dictation updateDictation(Dictation dictation)
			throws ServiceException {
		return writeRequestObject(
				new HttpPatch(apiPath("/dictations/%s", dictation.dictationId)),
				dictation);
	}

	public String updateDictation(String dict, long dictId)
			throws ServiceException {
		return writeRequestObject(
				new HttpPatch(apiPath("/dictations/%s", dictId)), dict);
	}

	public Dictation insertDictation(Dictation dictation)
			throws ServiceException {
		return writeRequestObject(
				new HttpPost(apiPath("/dictations", dictation.jobId)),
				dictation);
	}

	public String insertDictation(String dict) throws ServiceException {
		return writeRequestObject(new HttpPost(apiPath("/dictations")), dict);
	}

	public void subscribeToExpressQueues(Collection<Queue> queues)
			throws ServiceException {
		long[] queueIds = new long[queues.size()];
		if(queueIds.length == 0)
			Log.e("QUEUES", "No Express Queues Found");
		int idx = 0;
		for (Queue q : queues){
			queueIds[idx] = q.id;
			Log.e("queueIds.."+idx, Long.toString(queueIds[idx]));
			idx++;
			
		}
		subscribeToExpressQueues(queueIds);
	}

	public void subscribeToExpressQueues(long[] queues) throws ServiceException {
		writeRequestObject(new HttpPut(apiPath("/dictators/expressqueues")),
				queues);
	}
	
	//HttpGet for Patient clinical Info
	public static boolean isMappingNeeded=false;
	public String getClinicalInfo(Long patientId) throws ServiceException {
  		isMappingNeeded=true;
  		return readRequestObject1(new HttpGet(apiPath("/patients/%s/clinicals",patientId)),
  				String.class);
  	}
	
	public String getDemographicInfo(String sessionToken, long patientId) throws ServiceException {
		return getDemographicInfo(sessionToken, null, patientId);
	}
	
	public String getDemographicInfo(String sessionToken, String threadId, long patientId) throws ServiceException {
		
		Log.e("Entrada", "Patient DemographicInfo request GET Request API url --"+ apiPath("/patients/%s/demographic", patientId));
		HttpGet request = new HttpGet(apiPath("/patients/%s/demographic", patientId)); 
		request.setHeader("SessionToken", sessionToken);
		if(threadId!=null) {
			request.setHeader("ThreadId", threadId);
		}
		HttpResponse response = makeRequest(request);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.e("Entrada", "Patient DemographicInfo Response--"+s);
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}
	
	public int uploadLogs(InputStream data, String checksum, File fileToUpload) throws ServiceException{
		Log.d(LOG_NAME, "Uploading logs");
		if(data != null)
			Log.e("has Content", "true");
		else
			Log.e("has Content", "false");
		
				
		HttpPost request = new HttpPost(apiPath("/upload/logs"));
		request.addHeader("filename", fileToUpload.getName());
		request.addHeader("checksum", checksum);
		//request.setHeader("Content-type", "application/zip");
		//request.setHeader("Content-type", "text/plain");
//		request.setEntity(new InputStreamEntity(data, -1));
		
		InputStreamEntity reqEntity = null;
		try {
			reqEntity = new InputStreamEntity(
			        new FileInputStream(fileToUpload), -1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reqEntity.setContentType("binary/octet-stream");
		reqEntity.setChunked(true);
		request.setEntity(reqEntity);
		
		HttpResponse response = makeRequest(request);
		int statusCode = response.getStatusLine().getStatusCode();
		Log.d(LOG_NAME, String.format("Response = %d %s", statusCode, response
				.getStatusLine().getReasonPhrase()));
		
		if(statusCode == 201)
			Log.d(LOG_NAME, "Logs uploaded successfully");
		else
			Log.d(LOG_NAME, "Upload logs failed");
		
		return statusCode;
	}


	public int uploadDictation(InputStream data, String checksum, Dictation dictation, long jobId,
			long duration, Long dictationId, DomainObjectProvider provider) throws ServiceException,
			DomainObjectWriteException {
		HttpPost request = new HttpPost(apiPath("/upload"));
		long dictId = 0;
		if(dictation != null)
		dictId = dictation.dictationId;
		Job j = provider.getJob(jobId);
		if (dictation != null && dictId != 0) {
			request.addHeader("filename", String.format("%s.ogg", dictId));
			request.addHeader("dictationid", Long.toString(dictId));
		} else {
			/*if(BundleKeys.dictId == 0L){
			request.addHeader("filename", "dictation.ogg");
			}else{
				request.addHeader("filename", String.format("%s.ogg", BundleKeys.dictId));
				request.addHeader("dictationid", Long.toString(BundleKeys.dictId));
			}*/
			request.addHeader("filename", "dictation.ogg");
			request.addHeader("jobtypeid", Long.toString(j.getJobTypeId()));
		}

		request.addHeader("duration", Long.toString(duration));

		if (checksum != null)
			request.addHeader("checksum", checksum);

		if (j.isFlagSet(Job.Flags.LOCALLY_CREATED)) {
			if(dictId == 0)
			request.addHeader("createjob", "true");

			Encounter e = provider.getEncounter(j.encounterId);
			if (e != null) {
				request.addHeader("patientid", Long.toString(e.patientId));
			}
		}

		request.addHeader("stat", j.stat ? "true" : "false");
		request.addHeader("jobtype", Long.toString(j.jobTypeId));
		request.setHeader("Content-type", "audio/ogg");
		request.setEntity(new InputStreamEntity(data, -1));

		HttpResponse response = makeRequest(request);
		int statusCode = response.getStatusLine().getStatusCode();
		Log.d(LOG_NAME, String.format("Response = %d %s", statusCode, response
				.getStatusLine().getReasonPhrase()));
		BundleKeys.STATUS_CODE = statusCode;
		if (statusCode < 200 || statusCode >= 300) {
			j = j.setFlag(Job.Flags.UPLOAD_PENDING);
			j = j.setFlag(Job.Flags.UPLOAD_IN_PROGRESS);
			j = j.clearFlag(Job.Flags.UPLOAD_COMPLETED);
			j = j.setFlag(Job.Flags.FAILED);
		} else {
			j = j.clearFlag(Job.Flags.UPLOAD_PENDING);
			j = j.clearFlag(Job.Flags.UPLOAD_IN_PROGRESS);
			j = j.setFlag(Job.Flags.UPLOAD_COMPLETED);
			j = j.clearFlag(Job.Flags.FAILED);
		}
		Log.e("Local_Flag_in_API", j.getFlagsString());
		/*if (runAfterUpload != null)
			runAfterUpload.run();*/
		return statusCode;
	}

	public int uploadDictation(InputStream data, String checksum,
			Dictation dict, DomainObjectProvider provider) throws ServiceException,
			DomainObjectWriteException {
		return uploadDictation(data, checksum, dict, dict.jobId, dict.duration,
				dict.dictationId, provider);
	}
	
	public void createJob(long jobId, DomainObjectProvider provider, Runnable postUploadImages) throws ServiceException{
		
			Dictation dict = null;
        	try {
        		
        		Job j = provider.getJob(jobId);
        		Encounter enc = provider.getEncounter(j.encounterId);
			
				JSONObject jsonObject = new JSONObject();
				jsonObject.accumulate("JobTypeID", Long.toString(j.getJobTypeId()));
				jsonObject.accumulate("PatientID", Long.toString(enc.patientId));
				jsonObject.accumulate("Stat", j.stat ? "true" : "false");
				jsonObject.accumulate("AppointmentDate", String.valueOf(enc.appointmentDate));
				
				// Convert JSONObject to JSON to String
				String json = "";
				json = jsonObject.toString();
				String str = writeRequestObject(new HttpPost(apiPath("/createjob")), json);
				Log.e("str..response", str);
				
				try {
					JSONObject jobj = new JSONObject(str);
					//JSONObject jobj_dict = jobj.getJSONObject("Dictation");
					dict = new Dictation(jobj.getJSONObject("Dictation").getLong("DictationID"), 
							jobId, 
							jobj.getJSONObject("Dictation").getLong("DictatorID"), 
							jobj.getJSONObject("Dictation").getLong("DictationTypeID"), 
							jobj.getJSONObject("Dictation").getInt("Status"),
							jobj.getJSONObject("Dictation").getLong("Duration"),
							jobj.getJSONObject("Dictation").getString("MachineName"), 
							jobj.getJSONObject("Dictation").getString("Filename"), 
							jobj.getJSONObject("Dictation").getInt("QueueID"), 
							"");
					try {
						provider.writeDictation(dict);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//BundleKeys.dictId = jobj.getJSONObject("Dictation").getLong("DictationID");
					//Log.e("dictId", Long.toString(BundleKeys.dictId));
					
					//Clear local flag since we now have dictation available
					j.clearFlag(Flags.LOCALLY_CREATED);
					try {
						provider.updateJob(j);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if(postUploadImages != null)
        		postUploadImages.run();
		
	}

	public void uploadImages(InputStream data, String checksum, long jobId, 
			Long dictId, DomainObjectProvider provider, Runnable runAfterUpload, boolean done) throws ServiceException, DomainObjectWriteException {

		HttpPost request = new HttpPost(apiPath("/upload/%s/image",
				dictId));
		Job j = provider.getJob(jobId);
		
		request.addHeader("checksum", checksum);
		request.addHeader("dictationid", Long.toString(dictId));
		request.addHeader("jobtypeid", Long.toString(j.getJobTypeId()));
		request.setHeader("Content-type", "image/jpeg");
		request.setEntity(new InputStreamEntity(data, -1));
		
		HttpResponse response = makeRequest(request);
		int statusCode = response.getStatusLine().getStatusCode();
		/*Log.d(LOG_NAME, String.format("Response = %d %s", statusCode, response
				.getStatusLine().getReasonPhrase()));*/

		if (runAfterUpload != null && done)
			runAfterUpload.run();
	}
	
	public void dictateAPIVersio(){
		try {
			HttpGet request = new HttpGet(apiPath("/api/version"));
			HttpResponse response = client.execute(host, request);
			int statusCode = response.getStatusLine().getStatusCode();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.d("Entrada/APIService--Dictate APIVesion", "Dictate APIVesion--"+ s +"--StatusCode--"+statusCode);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	//Terms of Use
		public String TOU(String Username, String Password, String UDID) throws ServiceException {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.accumulate("Username", Username);
				jsonObject.accumulate("Password", Password);
				jsonObject.accumulate("DeviceId", UDID);
				String json = "";
				json = jsonObject.toString();
				String str = writeRequestObject(new HttpPost(apiPath("/users/authenticate")), json);
				
				return str;
			} catch (Exception e) {
				throw new ServiceException("Unable to authenticate.", e);
			}
		}
		
		//HttpGet for TOU Content
		public String getTOUContent(String SessionToken) throws ServiceException {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.accumulate("SessionToken", SessionToken);
				String json = "";
				json = jsonObject.toString();
				String str = readRequestObject(new HttpGet(apiPath("/users/getentradacontent")), SessionToken);
				JSONObject jsonObj = new JSONObject(str);
				return jsonObj.getString("Content");
			} catch (Exception e) {
				throw new ServiceException("Unable to authenticate.", e);
			}
		}

	
	public String authenticate(String Username, String Password, String UDID) throws ServiceException {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("Username", Username);
			jsonObject.accumulate("Password", Password);
			jsonObject.accumulate("DeviceId", UDID);
			String json = "";
			json = jsonObject.toString();
			Log.e("Entrada", "Users Authenticate Request API url --"+ apiPath("/users/authenticate"));
			Log.e("Entrada", "Users Authenticate Request data--"+ json.toString());
			String str = writeRequestObject(new HttpPost(apiPath("/users/authenticate")), json);
			Log.e("Entrada", "Users Authenticate Response--"+str);
			return str;
		} catch (Exception e) {
			throw new ServiceException("Unable to authenticate.", e);
		}
	}
	
	public String validateRegistrationCode(String registrationCode) throws ServiceException{
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("RegistrationCode", registrationCode);
			String json = "";
			json = jsonObject.toString();
			Log.e("Entrada", "Users Validate RegistrationCode Request API url --"+ apiPath("/users/validateregistrationcode"));
			Log.e("Entrada", "Users Validate RegistrationCode data--"+ json.toString());
			String str = writeRequestObject(new HttpPost(apiPath("/users/validateregistrationcode")), json, false);
			Log.e("Entrada", "Users Validate RegistrationCode Response--"+str);
			return str;
		} catch (Exception e) {
			throw new ServiceException("Unable to validate.", e);
		}
	}

	public int registerUser(ENTUser user) throws ServiceException{
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("RegistrationCode",
					user.getRegistrationCode());
			jsonObject.accumulate("FirstName", user.getFirstName());
			jsonObject.accumulate("MI", user.getMI());
			jsonObject.accumulate("LastName", user.getLastName());
			jsonObject.accumulate("PhoneNumber", user.getPhoneNumber());
			jsonObject.accumulate("EmailAddress", user.getEmailAddress());
			jsonObject.accumulate("Password", user.getPassword());
			String json = "";
			json = jsonObject.toString();
			Log.e("Entrada", "Registration NewUser Request API url --"
					+ apiPath("/users/registernewuser"));
			Log.e("Entrada", "Registration NewUser data--" + json.toString());

			HttpPost request = new HttpPost(apiPath("/users/registernewuser"));
			request.setHeader("Content-type", "application/json");

			StringEntity se = new StringEntity(jsonObject.toString());
			request.setEntity(se);
			ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
			request.getEntity().writeTo(reqOut);
			
			HttpResponse response = makeRequest(request, false);
			int statusCode = response.getStatusLine().getStatusCode();
			Log.e("Entrada", "Registration NewUser Response--" + statusCode);
			return statusCode;
		} catch (Exception e) {
			throw new ServiceException("Unable to register user.", e);
		}
	}
	
	/**
	 * Method to get the schedule data from the server.
	 * @param requestData
	 * @return response string
	 * @throws ServiceException
	 */
	public String getScheuleList(String requestData) throws ServiceException{
		StringEntity tmp = null;
		try {
			Log.e("Entrada", "GetScheuleList Request API url --"+ apiPath("/schedules/getschedulelist"));
			Log.e("Entrada", "GetScheuleList data--" + requestData);
			HttpPost request = new HttpPost(apiPath("/schedules/getschedulelist"));
			request.setHeader("Content-type", "application/json");
			request.setHeader("User-Agent",
							  "Mozilla/5.0 (X11; U; Linux "
							+ "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");
			request.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
			
			if(requestData != null){
				try {
					tmp = new StringEntity(requestData, "UTF-8");
					request.setEntity(tmp);
				} catch (UnsupportedEncodingException e) {
						Log.d("UTIL", "UnsupportedEncodingException Ex::" + e);
				}
			}
			
			ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
			request.getEntity().writeTo(reqOut);
			
			HttpResponse response = makeRequest(request, true);
			int statusCode = response.getStatusLine().getStatusCode();
			Log.e("Entrada", "GetScheuleList Response--" + statusCode);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				String str = out.toString();
				return str;
			} catch (IOException e) {
				e.printStackTrace();
				throw new ServiceException("Unable to read HTTP body.", e);
			}  catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			throw new ServiceException("Unable to validate.", e);
		}
		return null;
	}
	
	/**
	 * Method to get the schedule appointment dates for the month given.
	 * @param requestData
	 * @return response string
	 * @throws ServiceException
	 */
	public String getScheduleDatesList(String requestData) throws ServiceException{
		StringEntity tmp = null;
		try {
			Log.e("Entrada", "Get Schedule Dates Request API url --"+ apiPath("/schedules/getscheduledates"));
			Log.e("Entrada", "Get Schedule Dates data--" + requestData);
			HttpPost request = new HttpPost(apiPath("/schedules/getscheduledates"));
			request.setHeader("Content-type", "application/json");
			request.setHeader("User-Agent",
							  "Mozilla/5.0 (X11; U; Linux "
							+ "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");
			request.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
			
			if(requestData != null){
				try {
					tmp = new StringEntity(requestData, "UTF-8");
					request.setEntity(tmp);
				} catch (UnsupportedEncodingException e) {
						Log.d("UTIL", "UnsupportedEncodingException Ex::" + e);
				}
			}
			
			ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
			request.getEntity().writeTo(reqOut);
			
			HttpResponse response = makeRequest(request, true);
			int statusCode = response.getStatusLine().getStatusCode();
			Log.e("Entrada", "Get Schedule Dates Response--code" + statusCode);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				String str = out.toString();
				Log.e("Entrada", "Get Schedule Dates Response--" + str);
				return str;
			} catch (IOException e) {
				e.printStackTrace();
				throw new ServiceException("Unable to read HTTP body.", e);
			}  catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			throw new ServiceException("Unable to validate.", e);
		}
		return null;
	}
	
	@Override
	public List<Dictator> getAssociatedDictators(String sessionToken) throws ServiceException{
		HttpGet getter = new HttpGet(apiPath("/users/getassociateddictators"));
		Log.e("Entrada", "Get Associated Dictators Request API url --"+ apiPath("/users/getassociateddictators"));
		getter.setHeader("SessionToken", sessionToken);
		Log.e("Entrada", "Get Associated Dictators Request API header -- SessionToken : "+sessionToken);
		List<Dictator> dictators = new ArrayList<Dictator>();
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String str = out.toString();
			Log.e("Entrada", "Get Associated Dictators API Response--"+str);
			JSONObject jsonObj = new JSONObject(str);
			String strDictators =  jsonObj.getString("Dictators");
			JSONArray array = new JSONArray(strDictators);
			Gson gson = new Gson();
			for(int i=0; i< array.length(); i++) {
				Dictator dictator = gson.fromJson(array.getJSONObject(i).toString(),Dictator.class);
				dictators.add(dictator);
			}
			return dictators;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method to get the resource names from the server.
	 * return response string
	 */
	@Override
	public List<Resource> getResourceNames(String sessionToken, String dictator)throws ServiceException {
		
		HttpGet getter = new HttpGet(apiPath("/schedules/getresources"));
		Log.e("Entrada", "Get Resource Names Request API url --"+ apiPath("/schedules/getresources"));
		getter.setHeader("SessionToken", sessionToken);
		getter.setHeader("dictator", dictator);
		Log.e("Entrada", "Get GetResources Request API header -- SessionToken : "+sessionToken);
		Log.e("Entrada", "Get GetResources Request API header -- DictatorId : "+dictator);
		List<Resource> resources = new ArrayList<Resource>();
		HttpResponse response = makeRequest(getter);
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String str = out.toString();
			Log.e("Entrada", "GetResources API Response--"+str);
			JSONArray jsonObj = new JSONArray(str);
			
			for(int i=0;i<jsonObj.length();i++){
				JSONObject jsonobject;
				// Get the names
				Resource bean = new Resource();
				try {
					jsonobject = jsonObj.getJSONObject(i);
					bean.setResourceId(jsonobject.getString("ResourceId"));
					bean.setResourceName(jsonobject.getString("ResourceName"));
					resources.add(bean);
					//Insert in DB
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return resources;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String createThread(String sessionToken, String threadId){
		try {
			HttpPost post = new HttpPost(apiPath("/threads/createthread/"+threadId));
			Log.e("Entrada", "Create Thread Request API url --"+ apiPath("/threads/createthread/"+threadId));
			post.setHeader("SessionToken", sessionToken.trim());
			Log.e("Entrada", "Create Thread Request API header -- SessionToken : "+sessionToken);
			String str = writeRequestObject(post, null);
			Log.e("Entrada", "Create Thread Request API Response--"+str);
			JSONObject jsonObj = new JSONObject(str);
			return jsonObj.getString("PassPhrase");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getMessageThreadDetails(String sessionToken, String threadId) throws ServiceException{
		HttpGet getter = new HttpGet(apiPath("/threads/getmessagethreaddetails/"+threadId));
		Log.e("Entrada", "GetMessageThreadDetails Request API url --"+ apiPath("/threads/getmessagethreaddetails/"+threadId));
		getter.setHeader("SessionToken", sessionToken);
		Log.e("Entrada", "GetMessageThreadDetails Request API header -- SessionToken : "+sessionToken);
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String str = out.toString();
			Log.e("Entrada", "GetMessageThreadDetails API Response--"+str);
			JSONObject jsonObj = new JSONObject(str);
			return jsonObj.getString("PassPhrase");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int inviteSMUser(String sessionToken, Contact contact){
		JSONObject jsonObject = new JSONObject();
		int statusCode=0;
		try {
			jsonObject.accumulate("PhoneNumber", contact.getContactNo());
			jsonObject.accumulate("EmailAddress", contact.getEmail());
			jsonObject.accumulate("FirstName", contact.getContactName());
			jsonObject.accumulate("MI", "");
			jsonObject.accumulate("LastName", "");
		
		String json = "";
		json = jsonObject.toString();
		Log.e("Entrada", "Invite SMUser Request API url --"+ apiPath("/users/invitesmuser"));
		Log.e("Entrada", "Invite SMUser Request data--"+ json.toString());
		HttpPost post = new HttpPost(apiPath("/users/invitesmuser"));
		post.setHeader("Content-type", "application/json");
		post.setHeader("SessionToken", sessionToken);
		Log.e("Entrada", "Invite SMUser Request header-- SessionToken : "+ sessionToken);
		StringEntity se = new StringEntity(json);
		post.setEntity(se);
		HttpResponse response = makeRequest(post);
		statusCode = response.getStatusLine().getStatusCode();
		Log.e("Entrada", "Invite SMUser Response--"+statusCode);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statusCode;
		
	}
	
	public String validateSessionToken(String sessionToken) throws ServiceException{
		HttpGet getter = new HttpGet(apiPath("/users/validatesessiontoken"));
		getter.setHeader("SessionToken", sessionToken);
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}
	
	public String getPendingInvitations(String sessionToken) throws ServiceException{
		HttpGet getter = new HttpGet(apiPath("/users/getpendinginvitations"));
		getter.setHeader("SessionToken", sessionToken);
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.e("Entrada", "Users Pending Invitations Response--"+s);
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}
	
	public String getContactsFromMyClinics(String sessionToken) throws ServiceException{
		HttpGet getter = new HttpGet(apiPath("/users/getcontactsfrommyclinics"));
		getter.setHeader("SessionToken", sessionToken);
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.e("Entrada", "Users From My Clinics Response--"+s);
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}
	
	public String getSMContacts(String sessionToken) throws ServiceException{
		HttpGet getter = new HttpGet(apiPath("/users/getsmcontacts"));
		getter.setHeader("SessionToken", sessionToken);
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.e("Entrada", "SM contacts Response--"+s);
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}
	
	public String getPatienInfoSharingPermission(String sessionToken, String threadId) throws ServiceException{
		Log.e("Entrada", "Patient Info sharing permission request GET Request API url --"+ apiPath("/threads/getpermision/"+threadId));
		HttpGet getter = new HttpGet(apiPath("/threads/getpermision/"+threadId));
		getter.setHeader("SessionToken", sessionToken);
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.e("Entrada", "Patient Info sharing permission Response--"+s);
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}

	public int grantPatienInfoSharingPermission(String sessionToken, ENTConversation conversation) throws ServiceException{
		return patientInfoSharingPermission(sessionToken, conversation, 2);
	}
	
	public int revokePatienInfoSharingPermission(String sessionToken, ENTConversation conversation) throws ServiceException{
		return patientInfoSharingPermission(sessionToken, conversation, 1);
	}

	private int patientInfoSharingPermission(String sessionToken,
			ENTConversation conversation, int permissionCode) throws ServiceException {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("ThreadID", conversation.getId());
			jsonObject.accumulate("PatientID", conversation.getPatientID());
			String[] occupants = conversation.getOccupantsIds();
			UserState state = AndroidState.getInstance().getUserState();
			EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
			SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
			List<QBPermission> list = new ArrayList<QBPermission>();
			for (String userId: occupants){
				ENTUser user = reader.getBuddyById(userId);	
				QBPermission per = new QBPermission(user.getLogin(), permissionCode);
				list.add(per);
			} 
			Gson gson = new Gson();
			jsonObject.accumulate("UserPermissions", gson.toJson(list));
			String json = "";
			json = jsonObject.toString().replace("\\", "");
			json = json.replace("\"[", "[");
			json = json.replace("]\"", "]");
			String permission = permissionCode == 1 ? "Revoke ":"Grant ";
			Log.e("Entrada", permission+"Patient Info sharing permission request POST Request API url --"+ apiPath("/threads/share"));
			Log.e("Entrada", permission+"Patient Info sharing permission request POST data--"+ json.toString());
			HttpPost post = new HttpPost(apiPath("/threads/share"));
			post.setHeader("SessionToken", sessionToken);
			try {
				//post.setEntity(new StringEntity(mapper.writeValueAsString(json)));
				post.setEntity(new StringEntity(json));
			} catch (IOException e) {
				throw new ServiceException("Unable to serialize " + json.toString(), e);
			}
			post.setHeader("Content-type", "application/json");
			int statusCode = 0;
			try {
				ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
				post.getEntity().writeTo(reqOut);
				String in = reqOut.toString();

				HttpResponse response = makeRequest(post);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				statusCode = response.getStatusLine().getStatusCode();
			} catch (IOException e) {
				throw new ServiceException("Unable to read HTTP body.", e);
			}
			Log.e("Entrada", permission+"Patient Info sharing permission Response--"+statusCode);
			return statusCode;
		} catch (Exception e) {
			throw new ServiceException("Patient Info sharing permission failed.", e);
		}
	}	
	
	private String readRequestObject(HttpGet getter, String SessionToken) throws ServiceException {
		// TODO Auto-generated method stub
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			
			int maxLogSize = 1000;
			for(int i = 0; i <= s.length() / maxLogSize; i++) {
			    int start = i * maxLogSize;
			    int end = (i+1) * maxLogSize;
			    end = end > s.length() ? s.length() : end;
			    Log.d("Entrada/APIService", s.substring(start, end));
			}
			
			return s;
		} catch (IOException e) {
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}

	
	DomainObjectProvider provider = null;
	ArrayList<Queue> queues = null;
	Account currentAccount = null;
	Queue queue;
	int DefaultQueueID;

	<T> T readRequestObject(HttpRequest getter, Class<T> obj)
			throws ServiceException {
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			
			int maxLogSize = 1000;
			for(int i = 0; i <= s.length() / maxLogSize; i++) {
			    int start = i * maxLogSize;
			    int end = (i+1) * maxLogSize;
			    end = end > s.length() ? s.length() : end;
			    Log.d("Entrada/APIService", s.substring(start, end));
			}
			
			return mapper.readValue(s, obj);
		} catch (IOException e) {
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}
	
	<T> T readRequestObject1(HttpRequest getter, Class<T> obj)
			throws ServiceException {
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			
			int maxLogSize = 1000;
			for(int i = 0; i <= s.length() / maxLogSize; i++) {
			    int start = i * maxLogSize;
			    int end = (i+1) * maxLogSize;
			    end = end > s.length() ? s.length() : end;
			    Log.d("Entrada/APIService", s.substring(start, end));
			}
			
			//Log.d("Entrada/APIService", s);

			// saveQueue(s);
			if(isMappingNeeded){
            	return (T) s;
            }
            isMappingNeeded=false;

			return mapper.readValue(s, obj);
		} catch (IOException e) {
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}

	<T> String readRequestObject(HttpRequest getter) throws ServiceException {
		HttpResponse response = makeRequest(getter);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			String s = out.toString();
			Log.d("Entrada/APIService", s);

			// saveQueue(s);

			try {
				JSONObject jobj = new JSONObject(s);
				JSONArray jarr_jobs = jobj.getJSONArray("Jobs");
				Log.e("jarr_jobs", jarr_jobs.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return s;
		} catch (IOException e) {
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}

	
	<T> T writeRequestObject(HttpEntityEnclosingRequestBase request, String obj)
			throws ServiceException {
		return writeRequestObject(request, obj, true);
	}
	
	<T> T writeRequestObject(HttpEntityEnclosingRequestBase request, String obj, boolean needsSessionToken)
			throws ServiceException {

		request.setHeader("Content-type", "application/json");

		try {
			if(obj != null){
				StringEntity se = new StringEntity(obj);
				request.setEntity(se);
				ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
				request.getEntity().writeTo(reqOut);
				String in = reqOut.toString();
			}

			HttpResponse response = makeRequest(request, needsSessionToken);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (response.getEntity() == null)
				return null;

			String s = out.toString();
			Log.d("Entrada/APIService", s);
			response.getEntity().writeTo(out);
			return (T) out.toString();
		} catch (IOException e) {
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}

	<T> T writeRequestObject(HttpEntityEnclosingRequestBase request, T obj)
			throws ServiceException {
		try {
			request.setEntity(new StringEntity(mapper.writeValueAsString(obj)));
		} catch (IOException e) {
			throw new ServiceException("Unable to serialize " + obj.toString(),
					e);
		}
		request.setHeader("Content-type", "application/json");

		try {
			ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
			request.getEntity().writeTo(reqOut);
			String in = reqOut.toString();

			HttpResponse response = makeRequest(request);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (response.getEntity() == null)
				return null;

			String s = out.toString();
			Log.d("Entrada/APIService", s);
			response.getEntity().writeTo(out);
			return (T) mapper.readValue(out.toString(), obj.getClass());
		} catch (IOException e) {
			throw new ServiceException("Unable to read HTTP body.", e);
		}
	}

	HttpResponse makeRequest(HttpRequest getter) throws ServiceException{
		return makeRequest(getter, true);
	}
	
	HttpResponse makeRequest(HttpRequest getter, boolean needsSessionToken) throws ServiceException {
		try {
			Log.d(LOG_NAME, "Performing " + getter.getRequestLine().getMethod()
					+ " request to " + getter.getRequestLine().getUri());
			EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
			getter.setHeader("Host", this.host.getHostName());
			if(application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID)!=null && !application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID).isEmpty() && needsSessionToken){
				getter.setHeader("dictator", application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID));
			}
			if(needsSessionToken){
				getter.setHeader("SessionToken", application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN));
			}

			for (Header h : getter.getAllHeaders()) {
				Log.d(LOG_NAME,
						String.format("Header %s: %s", h.getName(),
								h.getValue()));
				if(h.getName().equals("ThreadId")){
					getter.removeHeaders("dictator");
				}
			}

			HttpResponse response = client.execute(host, getter);
			return response;
		} catch (IOException e) {
			throw new ServiceException("Error making HTTP request.", e);
		}
	}

}

