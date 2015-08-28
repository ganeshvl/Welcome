package com.entradahealth.entrada.android.app.personal.activities.inbox.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.acra.ACRA;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.xiph.vorbis.encoder.EncodeFeed;
import org.xiph.vorbis.encoder.VorbisEncoder;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.ImageFullscreenView;
import com.entradahealth.entrada.android.app.personal.activities.inbox.SecureMessaging;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ChatMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.encryption.AES256Cipher;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;

public class ConversationAdapter extends BaseAdapter{
	private Context mContext;
	private List<ChatMessage> mMessages;
	private LayoutInflater inflater;
	private MediaPlayer	mediaPlayer;
	private String _source = null;
	private ViewHolder _holder = null;
	private Handler _seekHandler = null;
	private int _position;
	private String patient_name=null, recipient_name=null;
	int req_w;
	private EntradaApplication application;
	private String conversationId;
	private String passPhrase;
    private AES256Cipher cipher;
    
	public ConversationAdapter(Context context, ArrayList<ChatMessage> messages, String recipient_name, String patient_name, String conversationId, String passPhrase) {
		super();
		this.mContext = context;
		this.mMessages = messages;
		this.inflater = LayoutInflater.from(context);
		this.mediaPlayer = new MediaPlayer();
		this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		this.patient_name = patient_name;
		this.recipient_name = recipient_name;
		this.application = (EntradaApplication) EntradaApplication.getAppContext(); 
		this.conversationId = conversationId;
		this.passPhrase = passPhrase;
		cipher = new AES256Cipher();
	}
	
	@Override
	public int getCount() {
		return mMessages.size();
	}
	
	@Override
	public Object getItem(int position) {		
		return mMessages.get(position);
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		int contentType = getItemViewType(position);

		if (convertView == null)
			convertView = getConvertView(contentType, position);

		ViewHolder holder = (ViewHolder) convertView.getTag();
		updateView(contentType, position, holder);
		return convertView;
	}

	private View getConvertView(int contentType, int position) {
		View view = null;
		ViewHolder holder = new ViewHolder();
		if (contentType == 0) {
			view = inflater.inflate(R.layout.sent_chat_row, null);
			holder._rootLayout = (LinearLayout) view.findViewById(R.id.message_box);
		} else {
			view = inflater.inflate(R.layout.received_chat_row, null);
			holder.fromContact = (TextView) view.findViewById(R.id.fromContact);
			holder.rootLayout = (RelativeLayout) view.findViewById(R.id.rootLayout);
		}
		holder.audioMessage = (RelativeLayout) view.findViewById(R.id.audioMessage);		
		holder.audioSeekBar = (SeekBar) view.findViewById(R.id.audioSeekbar);
		holder.message = (TextView) view.findViewById(R.id.message_text);
		holder.playPauseButton = (ImageButton) view.findViewById(R.id.playPauseButton);
		holder.timePlayed = (TextView) view.findViewById(R.id.timePlayed);
		holder.time = (TextView) view.findViewById(R.id.time);
		holder.attach_img = (ImageView) view.findViewById(R.id.attach_img);
		holder.alert = (TextView) view.findViewById(R.id.alert);
		//holder.attach_img.setTag(mMessages.get(position).getImagePath());
		/*holder.attach_img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("sel_img_path", v.getTag().toString());
			}
		});*/
		
		holder.statusIndicator1 = (ImageView)view.findViewById(R.id.statusIndicator1);
		holder.status = (TextView) view.findViewById(R.id.status);
		view.setTag(holder);
		return view;
	}
	
	class MPSeekCompletionListner implements OnSeekCompleteListener{

		private ChatMessage message;
		
		public	MPSeekCompletionListner(ChatMessage message){
			this.message = message;
		}
		
		@Override
		public void onSeekComplete(MediaPlayer mp) {
			// TODO Auto-generated method stub
			message.setAudioPosition(0);
			mMessages.set(_position, message);
		}
		
	}

	private void updateView(int contentType, final int position, ViewHolder holder) {
		ChatMessage message = mMessages.get(position);
		_position= position;
		DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int width = displayMetrics.widthPixels;
        req_w = width * 1/2;
		if(contentType == 0) {
			LayoutParams lp = (LayoutParams) holder._rootLayout.getLayoutParams();
			lp.setMargins(convertDpToPixel(30), convertDpToPixel(10), convertDpToPixel(10), 0);
			if(message.getMessageType() == ChatMessage.MSGTYPE_ALERT){
				holder._rootLayout.setVisibility(View.GONE);
				holder.alert.setVisibility(View.VISIBLE);
			} else {
				if(message.getMessageType() == ChatMessage.MSGTYPE_AUDIO) {
					lp.setMargins(convertDpToPixel((int)(dpWidth/2.5)), convertDpToPixel(10), convertDpToPixel(10), 0);
				}
				holder._rootLayout.setVisibility(View.VISIBLE);
				holder.alert.setVisibility(View.GONE);
				holder._rootLayout.setLayoutParams(lp);
			}
		} else {
			if(message.getMessageType() == ChatMessage.MSGTYPE_ALERT){
				holder.rootLayout.setVisibility(View.GONE);
				holder.alert.setVisibility(View.VISIBLE);
			} else {
				LayoutParams lp = (LayoutParams) holder.rootLayout.getLayoutParams();
				if(!message.isGroup()){
					holder.fromContact.setVisibility(View.GONE);
				} else {
					holder.fromContact.setText(message.getFromContact());
					holder.fromContact.setVisibility(View.VISIBLE);
				}
				if(message.getMessageType() == ChatMessage.MSGTYPE_TEXT){
					updateMessageView(message, dpWidth, lp); 
				} else if(message.getMessageType() == ChatMessage.MSGTYPE_AUDIO) {
					lp.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel((int)(dpWidth/2.5)), 0);
				} else if(message.getMessageType() == ChatMessage.MSGTYPE_IMAGE){
					lp.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel((int)(dpWidth/2)), 0);
				}
				holder.rootLayout.setVisibility(View.VISIBLE);
				holder.alert.setVisibility(View.GONE);
				holder.rootLayout.setLayoutParams(lp);
			}
		}
		if(message.getMessageType() == ChatMessage.MSGTYPE_TEXT){
			holder.audioMessage.setVisibility(View.GONE);
			holder.attach_img.setVisibility(View.GONE);
			holder.message.setVisibility(View.VISIBLE);
			holder.message.setMaxWidth(convertDpToPixel(dpWidth-40));
			holder.message.setText(message.getMessage());
		} else if(message.getMessageType() == ChatMessage.MSGTYPE_ALERT){
			holder.alert.setText(message.getMessage());
		} else if(message.getMessageType() == ChatMessage.MSGTYPE_AUDIO){
			String audioPath = User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId+"/"+message.getAttachmentId()+".ogg";
			message.setMessage(audioPath);
			holder.audioMessage.setVisibility(View.VISIBLE);
			holder.message.setVisibility(View.GONE);
			holder.attach_img.setVisibility(View.GONE);
			File file = new File(audioPath);
			if(file.exists()){
				loadAudio(holder, message);
			} else {
				downloadContent(Integer.valueOf(message.getAttachmentId()), message.getMessageType(), holder, message, position);
			}
		} else if(message.getMessageType() == ChatMessage.MSGTYPE_IMAGE) {
			holder.audioMessage.setVisibility(View.GONE);
			holder.message.setVisibility(View.GONE);
			holder.attach_img.setVisibility(View.VISIBLE);
			holder.attach_img.setImageResource(R.drawable.loading);
			String imagePath = null;
			if(message.getAttachmentId()!=null && !message.getAttachmentId().equals("0")){
				imagePath = User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId+"/images/"+message.getAttachmentId()+".jpg";
				File file = new File(imagePath);
				if(file.exists()){
					renderImageView(holder, message, position, imagePath);
				} else {
					downloadContent(Integer.valueOf(message.getAttachmentId()), message.getMessageType(), holder, message, position);
				}
			} 
			//holder.attach_img.getLayoutParams().width = req_w;
		}		
		DateFormat dateFormat = new SimpleDateFormat("dd MMM hh:mm a");
		holder.time.setText(dateFormat.format(message.getMessageTime()));
		String status = message.isRead() ? "Read" : "Delivered";
		holder.status.setText(status);
	}
	
	private void renderImageView(final ViewHolder holder,
			final ChatMessage message, final int position,
			String imagePath) {
		message.setImagePath(imagePath);
		if(message.getImagePath() != null) {
			holder.attach_img.setImageBitmap(decodeSampledBitmapFromPath(message.getImagePath(), req_w, req_w));
		} else {
			holder.attach_img.setImageResource(R.drawable.loading);
		}
		holder.attach_img.getLayoutParams().height = holder.attach_img.getLayoutParams().width = req_w;
		holder.attach_img.setTag(message.getImagePath());
		holder.attach_img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("sel_img_path", v.getTag().toString());
				Bundle b = new Bundle();
				b.putInt("index", position);
				b.putString("attachmentId", message.getAttachmentId());
				b.putString("recipient_name", mMessages.get(position).getSelectedContact());
				b.putString("patient_name", patient_name);
				b.putString("conversationId", conversationId);
				ImageFullscreenView fullScreen = new ImageFullscreenView();
				fullScreen.setArguments(b);
				FragmentTransaction ft = ((SecureMessaging) mContext).getFragmentManager().beginTransaction().addToBackStack(null);
				ft.replace(R.id.fragcontent, fullScreen, null);
				ft.commit();
			}
		});
	}
	
	class DownloadImageTask extends AsyncTask{
		private Bundle params;
		private int attachmentID, type, position;
		private ChatMessage message;
		private ViewHolder holder;
		private String imagePath;
		
		public DownloadImageTask(Bundle params, int attachmentID, int type, ViewHolder holder, ChatMessage message, int position){
			this.params = params;
			this.attachmentID = attachmentID;
			this.type = type;
			this.holder = holder;
			this.message = message;
			this.position = position;
		}

		@Override
		protected Object doInBackground(Object... params1) {
			// TODO Auto-generated method stub
			try{
				File file = new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				if(!file.exists()) {
					new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)).mkdir();
				}
				file = new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId);
				if(!file.exists()) {
					new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId).mkdir();
				}
				file = new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId+"/images");
				if(!file.exists()) {
					new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId+"/images").mkdir();
				}
				imagePath = User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId+"/images/"+attachmentID+".jpg";
				FileOutputStream fos = new FileOutputStream(imagePath);
	    	    byte[] content = params.getByteArray(com.quickblox.core.Consts.CONTENT_TAG);
	      		Log.e("", "Download Encrypted bytearray--"+content.length);
	    	    Log.e("", "passPhrase--"+ passPhrase);
	    	    content = cipher.decrypt(content, passPhrase);
	    	    BitmapFactory.Options options = new BitmapFactory.Options(); options.inSampleSize = 8;
	    	    Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length, options); 
	    	    bitmap.compress(CompressFormat.JPEG, 80, fos);
	            fos.close();
			} catch(Exception ex){
				ex.printStackTrace();
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
            renderImageView(holder, message, position, imagePath);
		}
	}
	
	public void downloadContent(final int attachmentID, final int type, final ViewHolder holder, final ChatMessage message, final int position) {
		QBContent.downloadFileTask(attachmentID, new QBEntityCallbackImpl<InputStream>(){
		    @Override
		    public void onSuccess(final InputStream inputStream, Bundle params) {		    	
				try {
					if(type == ENTMessage.IMAGE) {
						new DownloadImageTask(params, attachmentID, type, holder, message, position).execute();
					} else if(type == ENTMessage.AUDIO){
/*						File file = new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
						if(!file.exists()) {
							new File(User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)).mkdir();
						}
						String audioPath = User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversationId+"/"+attachmentID+".ogg";
						final FileOutputStream fos = new FileOutputStream(audioPath);
						EncodeFeed encodeFeed = new EncodeFeed() {
							@Override
							public long readPCMData(byte[] pcmDataBuffer, int amountToWrite) {
								int read = 0;
								try {
									if(inputStream.available()!=0){
										read = inputStream.read(pcmDataBuffer, 0, amountToWrite);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return read;
							}

							@Override
							public int writeVorbisData(byte[] vorbisData, int amountToRead) {
								try {
									fos.write(vorbisData, 0, amountToRead);
								} catch (IOException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();														
								}
								return 0;
							}
							
							@Override
							public void stop() {
								try {
									inputStream.close();
									fos.close();
									loadAudio(holder, message);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void stopEncoding() {
							}

							@Override
							public void start() {
								
							}
						};

						VorbisEncoder.startEncodingWithBitrate(44100, 1, 68000, encodeFeed);*/
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }

		    @Override
		    public void onError(List<String> errors) {
		 
		    }
		    
		});
	}

	private void loadAudio(ViewHolder holder, ChatMessage message) {
		try {
			if(message.getMessage() == null || message.getMessage() == "") {
				Handler seekHandler = new Handler();
				//message.setMessage("test.mp3");
				holder.playPauseButton.setOnClickListener(new PlayPauseClickListener(holder, message, seekHandler));
			} else {
				if(_source == null || !(_source.equals(message.getMessage()))) {
					Handler seekHandler = new Handler();
					_source = message.getMessage();
					holder.playPauseButton.setOnClickListener(new PlayPauseClickListener(holder, message, seekHandler));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateMessageView(ChatMessage message, float dpWidth,
			LayoutParams lp) {
		lp.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel(30), 0);
		if(message.getMessage()!=null) {
		int messageLength = message.getMessage().length();
		int fromContactLength = message.getFromContact().length();
		int max_length = message.getMessage().length();
		if(message.isGroup()){
			max_length = Math.max(messageLength, fromContactLength);
		}
		if(max_length>0 && max_length<10){
			lp.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel((int)(dpWidth/1.5)), 0);
		}
		else if(max_length>=10 && max_length<=20){
			lp.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel((int)(dpWidth/2)), 0);
		}
		else if(max_length>20 && max_length<=25){
			lp.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel((int)(dpWidth/2.5)), 0);
		}
		}
	}
	
	public void seekUpdation() {
		if(mediaPlayer.isPlaying()){
			_holder.playPauseButton.setImageResource(R.drawable.icon_pause);
		} else {
			_holder.playPauseButton.setImageResource(R.drawable.icon_play);
		}
		int originalSeconds = (int) (mediaPlayer.getCurrentPosition() / 1000.0f);
		int mins = (int)(originalSeconds/60);
    	int secs = (int)(originalSeconds%60);
		_holder.audioSeekBar.setProgress(mediaPlayer.getCurrentPosition()); 
		_holder.timePlayed.setText(String.valueOf(mins) +" : "+ String.valueOf(secs));
		_seekHandler.postDelayed(new Runnable() {
			@Override 
			public void run() {
				seekUpdation(); 
			}} , 100); 
	}

	class PlayPauseClickListener implements OnClickListener{

		private ViewHolder holder;
		private ChatMessage message;
		private Handler seekHandler;
		
		public PlayPauseClickListener(ViewHolder holder, ChatMessage message, Handler seekHandler) {
			this.holder = holder;
			this.message = message;
			this.seekHandler = seekHandler;
		}
		
		@Override
		public void onClick(View v) {
			_holder = holder;
			_seekHandler = seekHandler;
			if(mediaPlayer.isPlaying()){
				message.setAudioPosition(mediaPlayer.getCurrentPosition());	
				mediaPlayer.setOnSeekCompleteListener(new MPSeekCompletionListner(message));				
				mMessages.set(_position, message);
				holder.playPauseButton.setImageResource(R.drawable.icon_play);
				mediaPlayer.pause();
			} else {
					mediaPlayer.stop();
					mediaPlayer.reset();
					try {
							if(message.getMessage().contains("mp3")) {
							AssetFileDescriptor afd = mContext.getAssets().openFd("test.mp3");
							mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
							afd.close();
						} else {
							mediaPlayer.setDataSource(message.getMessage());							
						}
						seekUpdation();
					 	mediaPlayer.prepare();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mediaPlayer.setOnSeekCompleteListener(new MPSeekCompletionListner(message));
					mediaPlayer.seekTo(message.getAudioPosition());
					holder.audioSeekBar.setMax(mediaPlayer.getDuration());
					message.setAudioDuration(mediaPlayer.getDuration());
					message.setAudioPosition(mediaPlayer.getCurrentPosition());					
					mMessages.set(_position, message);
					holder.playPauseButton.setImageResource(R.drawable.icon_pause);
					mediaPlayer.start();				 	
			}
		}
		
	}

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

	private static class ViewHolder
	{
		TextView message;
		RelativeLayout rootLayout;
		LinearLayout _rootLayout;
		TextView fromContact;
		RelativeLayout audioMessage;
		SeekBar audioSeekBar;
		ImageButton playPauseButton;
		TextView timePlayed;
		TextView time;
		ImageView attach_img;
		ImageView statusIndicator1;
		TextView alert;
		TextView status;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return position;
	}

	public void addMessage(ChatMessage chatMessage){
		mMessages.add(chatMessage);	
	}
	
	public void addMessages(List<ChatMessage> messages){
		mMessages.addAll(messages);			
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return (mMessages.get(position).isMine()) ? 0 : 1;
	}

	
	//Bitmap options
		public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,
				int reqHeight) {

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);

			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap bmp = BitmapFactory.decodeFile(path, options);
			return bmp;
		}

		public static int calculateInSampleSize(BitmapFactory.Options options,
				int reqWidth, int reqHeight) {

			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {
				if (width > height) {
					inSampleSize = Math.round((float) height / (float) reqHeight);
				} else {
					inSampleSize = Math.round((float) width / (float) reqWidth);
				}
			}
			return inSampleSize;
		}
		
		public Date getLastMessageTimeStamp(){
			int size = mMessages.size()-1;
			ChatMessage message = mMessages.get(size);
			return message.getMessageTime();
		}

		public void setPassPhrase(String passPhrase) {
			this.passPhrase = passPhrase;
		}
		
}


