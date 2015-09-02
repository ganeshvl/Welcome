package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.acra.ACRA;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.UserPrivate;

public class ImageDisplayActivity extends EntradaActivity {
	private UserState state;
	private ImageView ivCamera, ivDelete;
	static AlertDialog dgDeletePhoto;
	File imgPath;
	File[] contents;
	private long jobId;
	String img_path, path, accountName = null, finalFolder = "Images";
	int pos, total;
	ImageView ivDisplayImage;
	Bitmap bmp;
	AsyncTaskDeleteFiles myAsyncTaskDeleteFiles;
	private boolean openCamera = false, isDeleted = false, isModified = false, isFromList = false;
	int req_w;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_display);
		
		BundleKeys.fromImageDisplay = false;
		BundleKeys.fromSecureMessaging = false;
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ivDisplayImage = (ImageView) findViewById(R.id.ivDisplayImage);
		ivCamera = (ImageView) findViewById(R.id.iv_footer_camera);
		ivCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startCameraActivity();
			}
		});
		ivDelete = (ImageView) findViewById(R.id.iv_footer_delete);
		ivDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				AlertDialog.Builder builder = new AlertDialog.Builder(
						ImageDisplayActivity.this);
				builder.setTitle(null);
				builder.setMessage("Delete photo ?");
				builder.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								myAsyncTaskDeleteFiles = new AsyncTaskDeleteFiles();
								myAsyncTaskDeleteFiles.execute();
							}
						});
				builder.setNegativeButton("Cancel", null);
				dgDeletePhoto = builder.create();
				dgDeletePhoto.show();
			}
		});
		
		ivCamera.setEnabled(false);
		ivCamera.setAlpha(0.6f);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		startCaptureActivity();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case android.R.id.home:
			startCaptureActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		jobId = getIntent().getExtras().getLong(BundleKeys.SELECTED_JOB);
		accountName = getIntent().getExtras().getString(
				BundleKeys.SELECTED_JOB_ACCOUNT);
		isModified = getIntent().getExtras().getBoolean("isModified");
		isDeleted = getIntent().getExtras().getBoolean("isDeleted");
		isFromList = getIntent().getExtras().getBoolean("isFromList");
		img_path = getIntent().getStringExtra("img_path");
		finalFolder = getIntent().getStringExtra("finalFolder");
		pos = getIntent().getIntExtra("pos", 1);
		total = getIntent().getIntExtra("total", 1);

		getActionBar().setTitle(pos + " of " + total);
		
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels; 
		
		// bmp = BitmapFactory.decodeFile(img_path);
		bmp = decodeSampledBitmapFromPath(img_path, width/2, height/2);
		ivDisplayImage.setImageBitmap(bmp);

		if (total >= 5) {
			ivCamera.setEnabled(false);
			ivCamera.setAlpha(0.6f);
		} else {
			ivCamera.setEnabled(true);
			ivCamera.setAlpha(1.0f);
		}

		state = AndroidState.getInstance().getUserState();
		File accountPath = new File(state.getUserData().getUserAccountsDir(),
				accountName);
		File dbPath = new File(accountPath, String.valueOf(jobId));
		dbPath.mkdirs();

		//imgPath = new File(dbPath, "Images");
		if(finalFolder == null)
			finalFolder = "Images";
		imgPath = new File(dbPath, finalFolder);
		imgPath.mkdirs();
		path = imgPath.getAbsolutePath();
	}

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

	public class AsyncTaskDeleteFiles extends AsyncTask<String, String, Void> {

		public AsyncTaskDeleteFiles() {

		}

		@Override
		protected Void doInBackground(String... params) {
			Log.e("img_path_in_Display", img_path);
			File path = new File(img_path);
			path.delete();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			isDeleted = true;
			isModified = true;
			startCaptureActivity();
		}

	}

	protected void startCameraActivity() {
		Log.i("MakeMachine", "startCameraActivity()");
		int i = 1;
		File file = null;
		contents = imgPath.listFiles();
		/*if (contents == null || contents.length == 0) {
			file = new File(path, "1.jpg");
		} else {

			for (int j = 1; j <= contents.length + 1; j++) {
				file = new File(path, i + ".jpg");
				if (file.exists())
					i++;
				else
					break;
			}
		}*/
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		file = new File(imgPath.getAbsolutePath(), "IMG_"+ timeStamp +".jpg");
		openCamera = true;
		
		Uri outputFileUri = Uri.fromFile(file);
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("MakeMachine", "resultCode: " + resultCode);
		openCamera = false;
		switch (resultCode) {
		case 0:
			Log.i("MakeMachine", "User cancelled");
			break;

		case -1:
			isModified = true;
			startCaptureActivity();
			break;
		}
	}
	
	public void startCaptureActivity(){
		Intent intent = new Intent();
		intent.putExtra("isDeleted", isDeleted);
		intent.putExtra("isModified", isModified);
		intent.putExtra("isFirst", false);
		intent.putExtra("img_count", 5);
		intent.putExtra("job_type", BundleKeys.job_type);
		intent.putExtra("isFromList", isFromList);
		intent.putExtra("isNew", true);
		intent.putExtra(BundleKeys.SELECTED_JOB, jobId);
		intent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
		intent.putExtra("sel_job_str", "");
		intent.putExtra("interrupted", false);
		intent.setClass(ImageDisplayActivity.this, CaptureImages.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
		
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if(openCamera){
			BundleKeys.fromImageDisplay = true;
		}else{
			
			try
	        {
	            UserPrivate user = AndroidState.getInstance().getUserState().getUserData();
	            user.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID, Long.toString(jobId));
	            user.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT, accountName);
	            user.save();
	        }
	        catch (Exception ex)
	        {
	            ACRA.getErrorReporter().handleSilentException(ex);
	            Toast.makeText(ImageDisplayActivity.this, "Failed to save user state.", Toast.LENGTH_LONG).show();
		    }
			
			BundleKeys.fromCaputreImages = false;
			BundleKeys.fromImageDisplay = true;
			BundleKeys.current_img_path = img_path;
			BundleKeys.current_img_position = pos;
			BundleKeys.img_total = total;
			//finish();
		}
		
		
	}

}
