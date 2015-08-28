package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.acra.ACRA;
import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.google.common.collect.Lists;

public class CaptureImages extends EntradaActivity {
	private UserState state;
	private final int ID_MENU_SELECT = 1;
	private boolean isFromList = false, isNew = false, isInterrupted = false, isFirst = false, 
			openCamera = false, toDisplay = false, isDeleted = false, isModified = false, toJDisplay = false;
	private long jobId;
	private int img_count = 0;
	private String accountName = null, sel_job_str;
	private Job job = null;
	private DomainObjectReader reader;
	private Account account = null;
	GridView mGrid;
	private String path;
	private ImageView ivCamera, ivDelete;
	File imgPath, imgTempPath;
	File[] contents, total_files;
	AppsAdapter adapter;
	ArrayList<String> img_paths;
	AsyncTaskLoadFiles myAsyncTaskLoadFiles;
	AsyncTaskDeleteFiles myAsyncTaskDeleteFiles;
	BitmapWorkerTask myAsyncBitmapWorker;
	static AlertDialog dgDeletePhoto;
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pictures);
		getActionBar().setTitle(BundleKeys.job_type);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		BundleKeys.fromCaputreImages = false;
		BundleKeys.fromImageDisplay = false;
		BundleKeys.fromSecureMessaging = false;
		toDisplay = false;

		img_count = getIntent().getExtras().getInt("img_count");
		isDeleted = getIntent().getExtras().getBoolean("isDeleted");
		isModified = getIntent().getExtras().getBoolean("isModified");
		isFirst = getIntent().getExtras().getBoolean("isFirst");
		isFromList = getIntent().getExtras().getBoolean("isFromList");
		isNew = getIntent().getExtras().getBoolean("isNew");
		sel_job_str = getIntent().getExtras().getString("sel_job_str");
		jobId = getIntent().getExtras().getLong(BundleKeys.SELECTED_JOB);
		accountName = getIntent().getExtras().getString(
				BundleKeys.SELECTED_JOB_ACCOUNT);

		mGrid = (GridView) findViewById(R.id.myGrid);
		mGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				toDisplay = true;
				Intent i = new Intent();
				i.setClass(CaptureImages.this, ImageDisplayActivity.class);
				i.putExtra("isDeleted", isDeleted);
				i.putExtra("isModified", isModified);
				i.putExtra("isFromList", isFromList);
				i.putExtra("img_path", img_paths.get(position));
				i.putExtra("finalFolder", finalFolder);
				i.putExtra("pos", position + 1);
				i.putExtra("total", img_paths.size());
				i.putExtra(BundleKeys.SELECTED_JOB, jobId);
				i.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
			}

		});

		ivCamera = (ImageView) findViewById(R.id.iv_footer_camera);
		ivCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(myAsyncBitmapWorker == null || (myAsyncBitmapWorker != null && myAsyncBitmapWorker.getStatus() == AsyncTask.Status.FINISHED))
					startCameraActivity();
				else
					Toast.makeText(getApplicationContext(), "Please wait..", 1000).show();
			}
		});
		ivDelete = (ImageView) findViewById(R.id.iv_footer_delete);
		ivDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				for (int i = 0; i < checkedImages.size(); i++)
					Log.e("img..." + i, checkedImages.get(i));

				AlertDialog.Builder builder = new AlertDialog.Builder(
						CaptureImages.this);
				builder.setTitle(null);
				if (checkedImages.size() > 1)
					builder.setMessage("Delete " + checkedImages.size()
							+ " photos ?");
				else
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
		ivDelete.setEnabled(false);
		ivDelete.setAlpha(0.6f);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		dialog = new ProgressDialog(this);
		myAsyncTaskLoadFiles = new AsyncTaskLoadFiles();
		myAsyncTaskLoadFiles.execute();

	}

	File accountPath, dbPath, finalPath;
	String finalFolder;
	public class AsyncTaskLoadFiles extends AsyncTask<Void, String, Void> {
		public AsyncTaskLoadFiles() {
			
		}

		@Override
		protected void onPreExecute() {
			//dialog.show();
			state = AndroidState.getInstance().getUserState();
			accountPath = new File(state.getUserData()
					.getUserAccountsDir(), accountName);
			dbPath = new File(accountPath, String.valueOf(jobId));
			dbPath.mkdirs();
			
			account = state.getAccount(accountName);
			reader = state.getProvider(account);
			job = reader.getJob(jobId);
			
			//if (isFirst || img_count == 0){
			if(job.isFlagSet(Flags.IS_FIRST)){
				// create folder for images
				imgPath = new File(dbPath, "Images");
				imgPath.mkdirs();
				path = imgPath.getAbsolutePath();
				finalPath = imgPath;
				finalFolder = "Images";
			}else{
				imgPath = new File(dbPath, "Images");
				
				// create folder for images
				imgTempPath = new File(dbPath, "temp");
				imgTempPath.mkdirs();
				path = imgTempPath.getAbsolutePath();
				finalPath = imgTempPath;
				finalFolder = "temp";
				if(!isDeleted){
					try {
						FileUtils.copyDirectory(imgPath,imgTempPath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			Log.e("current_path_CaptureImages", finalPath.toString());
			
			//imgPath = new File(dbPath, "Images");
			//imgPath.mkdirs();
			

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			//contents = imgPath.listFiles();
			contents = finalPath.listFiles();

			if (contents == null || contents.length == 0) {
				img_paths = new ArrayList<String>(1);
				if (isFirst){
					startCameraActivity();
				}

			} else {
				img_paths = new ArrayList<String>(contents.length);
				for (int i = 0; i < contents.length; i++) {
					img_paths.add(contents[i].getAbsolutePath());
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			mGrid = (GridView) findViewById(R.id.myGrid);
			adapter = new AppsAdapter();
			mGrid.setAdapter(adapter);
			mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
			mGrid.setMultiChoiceModeListener(new CaptureImagesMultichoiceListener());

			// check if the dictation already has 5 images and disable camera
			// image if so
			if (contents.length >= 5) {
				ivCamera.setEnabled(false);
				ivCamera.setAlpha(0.6f);
			} else {
				ivCamera.setEnabled(true);
				ivCamera.setAlpha(1.0f);
			}
			//if(dialog!= null && dialog.isShowing())dialog.dismiss();
			super.onPostExecute(result);
		}

	}

	public class AsyncTaskDeleteFiles extends AsyncTask<Void, String, Void> {

		public AsyncTaskDeleteFiles() {

		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			isDeleted = false;
			//dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			for (int i = 0; i < checkedImages.size(); i++) {
				File path = new File(checkedImages.get(i));
				path.delete();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ivDelete.setEnabled(false);
			ivDelete.setAlpha(0.6f);
			isDeleted = true;
			isModified = true;
			myAsyncTaskLoadFiles = new AsyncTaskLoadFiles();
			myAsyncTaskLoadFiles.execute();
			//dialog.dismiss();
			/*if(myAsyncBitmapWorker.getStatus() == AsyncTask.Status.FINISHED){
				dialog.dismiss();
			}*/
			super.onPostExecute(result);
		}

	}

	protected void startCameraActivity() {
		Log.i("MakeMachine", "startCameraActivity()");
		int i = 1;
		File file = null;
		//contents = imgPath.listFiles();
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
		file = new File(finalPath.getAbsolutePath(), "IMG_"+ timeStamp +".jpg");
		openCamera = true;

		Uri outputFileUri = Uri.fromFile(file);
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, 0);
	}

	private void deleteLastPhotoTaken() {

	    String[] projection = new String[] {
	            MediaStore.Images.ImageColumns._ID,
	            MediaStore.Images.ImageColumns.DATA,
	            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
	            MediaStore.Images.ImageColumns.DATE_TAKEN,
	            MediaStore.Images.ImageColumns.MIME_TYPE };

	    final Cursor cursor = getContentResolver().query(
	            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, 
	            null,null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

	    if (cursor != null) {
	        cursor.moveToFirst();

	        int column_index_data =  
	                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

	        String image_path = cursor.getString(column_index_data);

	        File file = new File(image_path);
	        if (file.exists()) {
	            file.delete();
	        }
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("MakeMachine", "resultCode: " + resultCode);
		openCamera = false;
		switch (resultCode) {
		case 0:
			Log.i("MakeMachine", "User cancelled");
			isFirst = false;
			break;

		case -1:
			deleteLastPhotoTaken();
			// onPhotoTaken1();
			//if(!BundleKeys.isCapture)
				isModified = true;
			isFirst = false;
			break;
		}
	}

	protected void onPhotoTaken1() {
		Log.i("MakeMachine", "onPhotoTaken");
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
		/*imgTempPath = new File(dbPath, "temp");
		//imgTempPath.mkdirs();
		imgPath = new File(dbPath, "Images");
		//imgPath.mkdirs();
		if(img_count > 0){
			try {
				FileUtils.copyDirectory(imgTempPath,imgPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		toJDisplay = true;
		BundleKeys.fromCaputreImages = false;
		Intent jdIntent = new Intent();
		jdIntent.putExtra("img_count", img_count);
		jdIntent.putExtra("isDeleted", isDeleted);
		if(BundleKeys.isCapture)
			jdIntent.putExtra("isFirst", true);
		else
			jdIntent.putExtra("isFirst", false);
		jdIntent.putExtra("isFromList", isFromList);
		jdIntent.putExtra("isModified", isModified);
		jdIntent.putExtra("isNew", false);
		jdIntent.putExtra("sel_job_str", sel_job_str);
		jdIntent.putExtra("interrupted", false);
		jdIntent.putExtra(BundleKeys.SELECTED_JOB, jobId);
		jdIntent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
		jdIntent.setClass(CaptureImages.this, JobDisplayActivity.class);
		jdIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(jdIntent);
		finish();
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
			toJDisplay = true;
			BundleKeys.fromCaputreImages = false;
			Intent jdIntent = new Intent();
			jdIntent.putExtra("img_count", img_count);
			jdIntent.putExtra("isDeleted", isDeleted);
			if(BundleKeys.isCapture)
				jdIntent.putExtra("isFirst", true);
			else
				jdIntent.putExtra("isFirst", false);
			jdIntent.putExtra("isFromList", isFromList);
			jdIntent.putExtra("isModified", isModified);
			jdIntent.putExtra("isNew", false);
			jdIntent.putExtra("sel_job_str", sel_job_str);
			jdIntent.putExtra("interrupted", false);
			jdIntent.putExtra(BundleKeys.SELECTED_JOB, jobId);
			jdIntent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
			jdIntent.setClass(CaptureImages.this, JobDisplayActivity.class);
			jdIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(jdIntent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	int req_w;
	public class AppsAdapter extends BaseAdapter {
		public List<Boolean> checks;
		
		public AppsAdapter() {
			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	        int width = displayMetrics.widthPixels;
	        req_w = width*9/20;//45% width - we could take 1/2 but that's blocking selector on right side of image on multi-selection 
			checks = new ArrayList<Boolean>(5);//Lists.newArrayListWithCapacity(img_paths.size());
			for (int i = 0; i < 5; i++)
				checks.add(false);
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			CheckableLayout l;
			ImageView i;

			if (convertView == null) {
				i = new ImageView(CaptureImages.this);
				i.setScaleType(ImageView.ScaleType.FIT_XY);
				i.setLayoutParams(new ViewGroup.LayoutParams(req_w, req_w));
				i.setPadding(12, 12, 12, 12);
				l = new CheckableLayout(CaptureImages.this);
				l.setLayoutParams(new GridView.LayoutParams(
						GridView.LayoutParams.WRAP_CONTENT,
						GridView.LayoutParams.WRAP_CONTENT));
				l.addView(i);
			} else {
				l = (CheckableLayout) convertView;
				i = (ImageView) l.getChildAt(0);
			}

			myAsyncBitmapWorker = new BitmapWorkerTask(i);
			myAsyncBitmapWorker.execute(img_paths.get(position));

			boolean checked = checks.get(position);
			l.setBackgroundColor(CaptureImages.this.getResources().getColor(
					checked ? R.color.selected_list_item
							: R.color.unselected_list_item));
			/*if(position == img_paths.size() - 1)
				dialog.dismiss();*/
			return l;
		}

		public final int getCount() {
			return img_paths.size();
		}

		public final Object getItem(int position) {
			return img_paths.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	public class CheckableLayout extends FrameLayout implements Checkable {
		private boolean mChecked;

		public CheckableLayout(Context context) {
			super(context);
		}

		@SuppressWarnings("deprecation")
		public void setChecked(boolean checked) {
			mChecked = checked;
			setBackgroundDrawable(checked ? getResources().getDrawable(
					R.color.selected_list_item) : null);
		}

		public boolean isChecked() {
			return mChecked;
		}

		public void toggle() {
			setChecked(!mChecked);
		}

	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String data = null;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//dialog.show();
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			data = params[0];
			// return decodeSampledBitmapFromResource(getResources(), data, 100,
			// 100));
			// return BitmapFactory.decodeFile(data);
			return decodeSampledBitmapFromPath(data, req_w, req_w);
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
					// imageView.setImageBitmap(decodeSampledBitmapFromPath(getResources(),
					// R.id.myimage, 100, 100));
				}
			}
			//dialog.dismiss();
		}
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

	/*
	 * Multichoice mode listener for GridView
	 */
	ArrayList<String> checkedImages;

	public class CaptureImagesMultichoiceListener implements
			GridView.MultiChoiceModeListener {

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Select Images");
			mode.setSubtitle("One image selected");
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			ivDelete.setEnabled(false);
			ivDelete.setAlpha(0.6f);
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			adapter.checks.set(position, checked);
			checkedImages = Lists.newArrayListWithCapacity(img_paths.size());
			for (int i = 0; i < adapter.checks.size(); ++i) {
				if (adapter.checks.get(i))
					checkedImages.add(img_paths.get(i));
			}

			int selectCount = mGrid.getCheckedItemCount();

			if (selectCount > 0) {
				ivDelete.setEnabled(true);
				ivDelete.setAlpha(1.0f);
			} else {
				ivDelete.setEnabled(false);
				ivDelete.setAlpha(0.6f);
			}

			switch (selectCount) {
			case 1:
				mode.setSubtitle("1 image selected");
				break;
			default:
				mode.setSubtitle("" + selectCount + " images selected");
				break;
			}
		}

	}

		
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (dgDeletePhoto != null && dgDeletePhoto.isShowing())
			dgDeletePhoto.dismiss();
		
		if(myAsyncBitmapWorker != null)
			myAsyncBitmapWorker = null;
		
		if(openCamera || toDisplay){
			BundleKeys.fromCaputreImages = true;
		}else{
			if(toJDisplay){
			BundleKeys.fromCaputreImages = false;
			}else{
				BundleKeys.fromCaputreImages = true;
			//finish();
		}
			
		}
		
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
            Toast.makeText(CaptureImages.this, "Failed to save user state.", Toast.LENGTH_LONG).show();
	    }
		
		Log.e("BundleKeys.fromCaputreImages", Boolean.toString(BundleKeys.fromCaputreImages));
		
	}
	
	
}