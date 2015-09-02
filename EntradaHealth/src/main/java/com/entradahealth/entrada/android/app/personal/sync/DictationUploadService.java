package com.entradahealth.entrada.android.app.personal.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.acra.ACRA;
import org.apache.commons.io.FileUtils;
import org.xiph.vorbis.encoder.EncodeFeed;
import org.xiph.vorbis.encoder.VorbisEncoder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.audio.AudioDB;
import com.entradahealth.entrada.android.app.personal.utils.AndroidUtils;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;

public class DictationUploadService extends Service{

	private static final String LOG_NAME = "Entrada-DictationUploadService";
	
    private static boolean running = false;
    private ExecutorService executor;
    private static Account account;
    private static UserState userState;
    private EntradaApplication application;

	public static boolean isRunning() {
		return running;
	}

	static Handler handler = null;
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        executor = EntradaApplication.getExecutor();
        application = (EntradaApplication) EntradaApplication.getAppContext();
        Log.e("", "--OnCreate()-- **"+ executor);
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Runnable thread = new UploadThread(this, intent, startId, userState, account);
		try {
			executor.execute(thread);
		} catch(RejectedExecutionException ex){
			executor = Executors.newFixedThreadPool(application.getThreadPoolSize());
			executor.execute(thread);
		}
		return START_REDELIVER_INTENT;
	}
	
	public static void startUpload(Activity activity, UserState _userState,
			Account _account, Job job) {
		File accountPath = new File(_userState.getUserData()
				.getUserAccountsDir(), _account.getName());
		File dbPath = new File(accountPath, String.valueOf(job.id));
        Intent i = new Intent(activity, DictationUploadService.class);
        i.putExtra(BundleKeys.UPLOADING_DICTATION, dbPath);
        i.putExtra(BundleKeys.SELECTED_JOB, job.id);
        i.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, _account.getName());
        account = _account;
        userState = _userState;
        activity.startService(i);
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class UploadThread implements Runnable{
		
		private DictationUploadService uploadService;
		private Intent intent;
		private Job job;
		private Dictation dict;
		private File[] contents;
		private AudioDB db;
		private APIService service;
		private DomainObjectProvider provider;
		private int startId;
		private Account account;
		private UserState userState;
		
		UploadThread(DictationUploadService uploadService, Intent intent, int startId, UserState userState, Account account){
			this.uploadService = uploadService;
			this.intent = intent;
			this.startId = startId;
			this.account = account;
			this.userState = userState;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.e("", Thread.currentThread().getName()+ "--Thread Start-- Job**"+intent.getLongExtra(BundleKeys.SELECTED_JOB,-1337));
			  try
		        {
		            running = true;

		            NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		            Notification.Builder builder = new Notification.Builder(uploadService)
		                        .setSmallIcon(R.drawable.icon)
		                        .setOngoing(true)
		                        .setContentTitle("Uploading dictation")
		                        .setContentText("Uploading dictations.");
		
		            notificationMgr.notify(R.string.notification_dictation_upload, builder.getNotification());
		            
		            if(userState == null){
		            	userState = AndroidState.getInstance().getUserState();
		            	account = userState.getCurrentAccount();
		            } 
					provider = userState.getProvider(account);
					job = userState.getProvider(account).getJob(
							intent.getLongExtra(BundleKeys.SELECTED_JOB, -1337));
					File dbPath = (File) intent
							.getSerializableExtra(BundleKeys.UPLOADING_DICTATION);
		            
		            if(job.isFlagSet(Flags.UPLOAD_PENDING)){
		            	job = job.clearFlag(Job.Flags.FAILED);
		            	job = job.clearFlag(Job.Flags.UPLOAD_IN_PROGRESS);
		                job = job.clearFlag(Job.Flags.UPLOAD_COMPLETED);
		                job = job.setFlag(Job.Flags.UPLOAD_PENDING);
		            }
		            
		            if (job.isFlagSet(Job.Flags.UPLOAD_COMPLETED) || job.isFlagSet(Job.Flags.UPLOAD_IN_PROGRESS)){
		                return; // Do nothing if this job has already been given to DictationUploadService.
		            }

		    		
					db = null;
					service = null;
		            try {
		                service = new APIService(account);

		                db = AudioDB.load(dbPath);

		                job = job.clearFlag(Job.Flags.UPLOAD_PENDING);
		                job = job.setFlag(Job.Flags.UPLOAD_IN_PROGRESS);

		                provider.writeJob(job);

		            } catch (Exception ex) {
		                Log.e(LOG_NAME, "Error saving job status.", ex);
		                handler.post(new Runnable() {
		                    @Override
		                    public void run() {
		                        Toast.makeText(uploadService, "Unable to save job status. Please contact support.", Toast.LENGTH_SHORT).show();
		                    }
		                });
		                ACRA.getErrorReporter().handleSilentException(ex);
		                return;
		            }

		            notificationMgr.cancel(R.string.notification_dictation_upload);
		            
		            job = job.setFlag(Job.Flags.FAILED);
		            job = job.setFlag(Job.Flags.UPLOAD_PENDING);
		            job = job.setFlag(Job.Flags.UPLOAD_IN_PROGRESS);
		            job = job.clearFlag(Job.Flags.UPLOAD_COMPLETED);
		            
		               try {
			                List<Dictation> dicts = provider.getDictationsByJob(job.id);
			                if (!dicts.isEmpty())
			                    dict = dicts.get(0);
			                if(dict != null)
			                Log.e("Server-DICT", Long.toString(dict.dictationId));		
			                final File imgPath = new File(dbPath, "Images");
			                Log.e("","ImagePath--"+ imgPath);
			                if(!imgPath.exists()){
			                	imgPath.mkdir();
			                }			                
							contents = imgPath.listFiles(); 
							ArrayList<String> img_paths = new ArrayList<String>(contents.length);
									  
							for(int i=0;i<contents.length;i++){
								img_paths.add(contents[i].getAbsolutePath());
							}														
							
							try { 
								  service = new APIService(account); 
								 } 
							  catch(MalformedURLException e) { 
								  // TODO Auto-generated catch block
								  e.printStackTrace(); 
							  } 
							 
							  for(int i=0;i<img_paths.size();i++) 
								  Log.e("Path.."+i, img_paths.get(i)); 
							

							try {
								
								 final Runnable postUpload = new Runnable() {
									@Override
									public void run() {
										
										//Delete "Images" folder from device
										
										try {
											FileUtils.deleteDirectory(imgPath);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										final File tempFile = new File(
												AndroidUtils.getTempFileLocation(), job.id + ".ogg");

										try {
											final FileOutputStream fs = new FileOutputStream(tempFile);
											ByteArrayOutputStream outStream = new ByteArrayOutputStream();
											outStream = (ByteArrayOutputStream) db.writeData(outStream);
											final ByteArrayInputStream input = new ByteArrayInputStream(outStream.toByteArray());
											EncodeFeed encodeFeed = new EncodeFeed() {
												@Override
												public long readPCMData(byte[] pcmDataBuffer, int amountToWrite) {
													int read = 0;
													if(input.available()!=0){
														read = input.read(pcmDataBuffer, 0, amountToWrite);
													}
													return read;
												}

												@Override
												public int writeVorbisData(byte[] vorbisData, int amountToRead) {
													try {
														fs.write(vorbisData, 0, amountToRead);
													} catch (IOException e) {
														e.printStackTrace();
													} catch (Exception e) {
														e.printStackTrace();														
													}
													return 0;
												}
												
												@Override
												public void stop() {
													Log.e("", "--Encoding stopped--Job**"+job.id);
													try {
														input.close();
														fs.close();
														int statusCode = uploadService(tempFile);
														try {
															updateJobFlags(statusCode);
														} catch (ServiceException e) {
															  executor.shutdown();
															  stopService(intent);
															// TODO Auto-generated catch block
															e.printStackTrace();
														} catch (DomainObjectWriteException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
														try {
															provider.writeJob(job);
														} catch (DomainObjectWriteException ex) {
															ACRA.getErrorReporter().handleSilentException(ex);
															Log.e(LOG_NAME, "Error writing job status.", ex);
															handler.post(new Runnable() {
																@Override
																public void run() {
																	Toast.makeText(
																			uploadService,
																			"Unable to save job status. Please contact support.",
																			Toast.LENGTH_SHORT).show();
																}
															});
														}
													} catch (IOException e) {
														e.printStackTrace();
													} finally {
													}
												}

												@Override
												public void stopEncoding() {
												}

												@Override
												public void start() {
													Log.e("", "--Encoding started--Job**"+job.id);
												}
											};

											//VorbisEncoder.startEncodingWithBitrate(16000, 2, 68000, encodeFeed);
											VorbisEncoder.startEncodingWithBitrate(44100, 1, 68000, encodeFeed);
											//VorbisEncoder.startEncodingWithQuality(16000, 1, 0.5F, encodeFeed);

										} catch (Exception ex) {
											Log.e("Entrada-AudioDB", "Failure in WAV encoding: ",
													ex);
											try {
												throw ex;
											} catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
										
									}

								};
								
								if(contents.length > 0 && !job.isFlagSet(Job.Flags.LOCALLY_CREATED)){
									uploadImages(postUpload, intent, img_paths, service, provider, dict, false);
								}else{
									boolean isLocal = true;
									Dictation local_dict01 = null;
							    	List<Dictation> local_dicts01 = provider.getDictationsByJob(job.id);
						            if (!local_dicts01.isEmpty())
						            	local_dict01 = local_dicts01.get(0);
					                if(local_dict01 != null)
					                	isLocal = false;
					                else
					                	isLocal = true;
					                
					                Runnable postUploadImages = uploadImagesRunnable(postUpload, intent, img_paths, service, provider, job, isLocal);
			
									if(contents.length > 0 && job.isFlagSet(Job.Flags.LOCALLY_CREATED) && local_dict01 == null){
										try{
											service.createJob(intent.getLongExtra(BundleKeys.SELECTED_JOB, -1337), provider, postUploadImages); 
											//provider.writeDictation(local_dict);
										}catch (ServiceException e) { 
											  // TODO Auto-generated catch block
											  executor.shutdown();
											  stopService(intent);
											e.printStackTrace(); 
										}
									}else{
										postUpload.run();
									}
								}
								
								
							} catch (Exception ex) {
								Log.e("Entrada-AudioDB",
										"Failure in checksum construction: ", ex);
								throw ex;
							} 
			
			            
					} catch (Exception e) {
						Log.e(LOG_NAME, "Error uploading dictation.", e);
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										uploadService,
										"Unable to upload dictation. Will retry later.",
										Toast.LENGTH_SHORT).show();
							}
						});
					} 
					
				} finally {
					try {
						if (job.isComplete()) {
					
						final File tempFile = new File(
								AndroidUtils.getTempFileLocation(), job.id
										+ ".ogg");
						DeleteRecursive(tempFile);
						Log.e("", "--Deleting--" + tempFile);
						}
						stopSelf(startId);
				
					} catch (Exception e) {
					}
					Log.e("", Thread.currentThread().getName()+ "--Thread End");
				}
		}

		private void updateJobFlags(int statusCode) throws ServiceException,
				DomainObjectWriteException {
			if (dict != null) {
				Log.e("Has_Dict", "true");
				dict = service.getDictation(dict.dictationId);
				if (dict.duration == 0) {
					job = job.setFlag(Job.Flags.FAILED);
					job = job.setFlag(Job.Flags.UPLOAD_IN_PROGRESS);
					job = job.setFlag(Job.Flags.UPLOAD_PENDING);
					job = job.clearFlag(Job.Flags.UPLOAD_COMPLETED);
				} else {
					job = job.clearFlag(Job.Flags.FAILED);
					job = job.clearFlag(Job.Flags.UPLOAD_IN_PROGRESS);
					job = job.clearFlag(Job.Flags.UPLOAD_PENDING);
					job = job.setFlag(Job.Flags.UPLOAD_COMPLETED);
					provider.writeDictation(dict);
				}
				// Log.e("Local_Flag_in_upload", job.getFlagsString());
			} else {
				Log.e("Has_Dict", "false");

				if (statusCode < 200
						|| statusCode >= 300) {
					job = job.setFlag(Job.Flags.UPLOAD_PENDING);
					job = job.setFlag(Job.Flags.UPLOAD_IN_PROGRESS);
					job = job.clearFlag(Job.Flags.UPLOAD_COMPLETED);
					job = job.setFlag(Job.Flags.FAILED);
				} else {
					job = job.clearFlag(Job.Flags.UPLOAD_PENDING);
					job = job.clearFlag(Job.Flags.UPLOAD_IN_PROGRESS);
					job = job.setFlag(Job.Flags.UPLOAD_COMPLETED);
					job = job.clearFlag(Job.Flags.FAILED);
				}

			}
			BundleKeys.STATUS_CODE = 500;
			//BundleKeys.dictId = 0L;
			Log.e("Local_Flag_in_upload", job.getFlagsString());
		}
		
		private int uploadService(
				final File tempFile) {
			Log.e("", "--Upload started--Job**"+job.id);
			int statusCode = 0;
			FileInputStream is1 = null;
            MessageDigest md1 = messageDigest();
            String checksum1 = null;
            DigestInputStream dis1 = null;
            try
            {
                is1 = new FileInputStream(tempFile);
                dis1 = new DigestInputStream(is1, md1);
                while (dis1.read() != -1)
            continue;
                checksum1 = Strings.padStart(new BigInteger(1, md1.digest()).toString(16), 32, '0');
            }
            catch (Exception ex)
            {
            	Log.e("Entrada-AudioDB", "Failure in checksum construction: ", ex);
                try {
                	throw ex;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
            
            try {
				is1 = new FileInputStream(tempFile);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            Dictation local_dict = null;
	    	List<Dictation> local_dicts = provider.getDictationsByJob(job.id);
            if (!local_dicts.isEmpty())
            	local_dict = local_dicts.get(0);
            
            try{
				if (job.isFlagSet(Job.Flags.LOCALLY_CREATED))
                    statusCode = service.uploadDictation(is1, checksum1, local_dict, job.id, db.getDurationInSecs(), null, provider);
                else
                    statusCode = service.uploadDictation(is1, checksum1, dict, provider);
            }catch (ServiceException e) {
				  executor.shutdown();
				  stopService(intent);
				  // TODO Auto-generated catch block
				  e.printStackTrace(); 
			  } catch (DomainObjectWriteException e) { 
				  // TODO Auto-generated catch block
				  e.printStackTrace(); 
			  }
            Log.e("", "--Upload completed--Job**"+job.id);
            return statusCode;
		}
		
		private MessageDigest messageDigest() {
			MessageDigest md1 = null;
			try {
				md1 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return md1;
		}
		

	}
	
	
	private Runnable uploadImagesRunnable(final Runnable postUpload, final Intent intent, final ArrayList<String> img_paths, 
			final APIService service, final DomainObjectProvider provider, final Job job, final boolean isLocal) {
		Runnable postUploadImages = new Runnable() {

		    @Override
			public void run() {
				// TODO Auto-generated method stub
		    	Log.e("Job-ID", Long.toString(job.id));
		    	Dictation local_dict = null;
		    	List<Dictation> local_dicts = provider.getDictationsByJob(job.id);
                if (!local_dicts.isEmpty())
                	local_dict = local_dicts.get(0);
                uploadImages(postUpload, intent, img_paths, service, provider, local_dict, isLocal);
			}

		};
		return postUploadImages;
	}	
	
	
	private void uploadImages(final Runnable postUpload, Intent intent, ArrayList<String> img_paths, APIService service, 
			DomainObjectProvider provider, Dictation dict, boolean isLocalJob) {
		FileInputStream is = null;
		MessageDigest md  = null;
		String checksum = null;
		DigestInputStream dis = null;
		
		for(int i=0;i<img_paths.size();i++){
			File imgFile = new File(img_paths.get(i));
			try { 
				  md = MessageDigest.getInstance("MD5"); 
			}
			  catch(NoSuchAlgorithmException e1) { 
				  // TODO Auto-generated catch block
				  e1.printStackTrace(); 
			  }
			
			try {
				is = new FileInputStream(imgFile);
				dis = new DigestInputStream(is, md);
				while (dis.read() != -1)
					continue;
				checksum = Strings.padStart(
						new BigInteger(1, md.digest()).toString(16),
						32, '0');
			} catch (Exception ex) {
				Log.e("Entrada-AudioDB",
						"Failure in checksum construction: ", ex);
				try {
					throw ex;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} finally {
				Closeables.closeQuietly(dis);
				Closeables.closeQuietly(is);
    }
			
			try {
				is = new FileInputStream(imgFile);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			boolean noMoreImages = false;
			if(i == (img_paths.size()-1))
				noMoreImages = true;
			//Check if local/server job and assign dictId accordingly
			/*long dictId;
			if(isLocalJob){
				dictId = BundleKeys.dictId;
			}else{
				dictId = dict.dictationId;
			}*/
			Log.e("Local - DictId - Image path", imgFile.getAbsolutePath());
			Log.e("Local - DictId - ImageCount", Boolean.toString(isLocalJob)+ "--"+Long.toString(dict.dictationId)+"--"+Integer.toString(img_paths.size()));
			
			try {
				  int statusCode = service.uploadImages(is, checksum, intent.getLongExtra(BundleKeys.SELECTED_JOB, -1337), dict.dictationId, provider);
				  if(statusCode == 200) {
					  deleteFile(imgFile.getAbsolutePath());
				  }
				  if(postUpload!=null && noMoreImages) {
					  postUpload.run();
				  }
			  }catch (ServiceException e) { 
				  executor.shutdown();
				  stopService(intent);
				  // TODO Auto-generated catch block
				  e.printStackTrace(); 
			  } catch (DomainObjectWriteException e) { 
				  // TODO Auto-generated catch block
				  e.printStackTrace(); 
			  }finally {
                    Closeables.closeQuietly(dis);
                    Closeables.closeQuietly(is);
              }
		}
	}
	
	
	void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		running = false;
//		executor.shutdown();
		Log.e("", "--OnDestroy()--"+executor);
	}
}
