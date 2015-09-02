package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Conversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.google.common.collect.Lists;

public class ChooseImagesFragment extends Fragment {
	String TAG = "Choose-Images";
	GridView mGrid;
	ImageAdapter adapter;
	BitmapWorkerTask myAsyncBitmapWorker;
	Account currentAccount = null;
	private String accountName = null;
	private List<Encounter> list_encounter;
	public static List<Job> list_job;
	private DomainObjectReader reader;
	File[] acct_folders, job_folders, img_folders;
	File accountPath;
	private String recipient_name, patient_name;
	Long patient_id,enc_id, job_id;
	ArrayList<String> img_paths;
	boolean hasImages = false;
	private ENTConversation conversation;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if(bundle != null){
			patient_name = bundle.getString("patient_name"); 
			recipient_name = bundle.getString("recipient_name");
			patient_id = bundle.getLong("patient_id");
			conversation = (ENTConversation) bundle.getSerializable("conversation");
		}
		
		getActivity().getActionBar().setTitle("Choose Images");
		getActivity().getActionBar().setDisplayUseLogoEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		getActivity().getActionBar().setDisplayShowCustomEnabled(false);
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.frag_choose_images, container, false); 
		mGrid = (GridView)view.findViewById(R.id.grid_choose_images);
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		hasImages = getImagesPath();
		if(hasImages){
			Log.e("img_paths_size", Integer.toString(img_paths.size()));
			adapter = new ImageAdapter();
			mGrid.setAdapter(adapter);
			mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
			mGrid.setMultiChoiceModeListener(new CaptureImagesMultichoiceListener());
		}else{
			Toast.makeText(getActivity(), "No images found", Toast.LENGTH_SHORT).show();
		}
	}
	
	public boolean getImagesPath(){
		UserState state = AndroidState.getInstance().getUserState();
		accountName = state.getCurrentAccount().getName();
		currentAccount = state.getAccount(accountName);
		reader = state.getProvider(currentAccount);
		accountPath = new File(state.getUserData()
				.getUserAccountsDir(), accountName);
		img_paths = new ArrayList<String>();
		
		//get encounters from patient id
		list_encounter = reader.getEncountersByPatient(patient_id);
		if(list_encounter != null && list_encounter.size() > 0){
			for(Encounter encounter :list_encounter) {
				enc_id = encounter.id;
				Log.e("enc", Long.toString(enc_id));
				
				//get jobids from encounter
				list_job = reader.getJobsByEncounter(enc_id);
				if(list_job != null && list_job.size() > 0){
					for(Job job : list_job) {
						job_id = job.id;
						Log.e("jobID", Long.toString(job_id));
		
						//Search inside 'accountPath'(1144....) for images under 'Images' folder in JobID folders
						acct_folders = accountPath.listFiles();
						if (acct_folders == null || acct_folders.length == 0) {
							Log.e(TAG, "No jobs dictated yet!!");
						}else{
							for (int i = 0; i < acct_folders.length; i++) {
								if(acct_folders[i].getName().trim().equals(String.valueOf(job_id))){
									job_folders = acct_folders[i].listFiles();
									for (int j = 0; j < job_folders.length; j++) {
										String subpath = "Images";
										if(job.isFlagSet(Flags.IS_FIRST)){
											subpath = "Images";
										} else {
											subpath = "temp";
										}
										if(job_folders[j].isDirectory() && job_folders[j].getName().trim().equalsIgnoreCase(subpath)){
											img_folders = job_folders[j].listFiles();
											if(img_folders.length > 0)
												hasImages = true;
											for (int k = 0; k < img_folders.length; k++) {
												Log.e("img"+k, img_folders[k].getAbsolutePath());
												img_paths.add(img_folders[k].getAbsolutePath());
											}
											break;
										}								
									}
									break;
								}
								
							}
						}
					}
				}
			}
		}
		return hasImages;
	}
	
	/*
	 * Adapter for images in jobs folder
	 */
	int req_w;
	public class ImageAdapter extends BaseAdapter {
		public List<Boolean> checks;
		

		public ImageAdapter() {
			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	        int width = displayMetrics.widthPixels;
	        req_w = width*9/20;//45% width - we could take 1/2 but that's blocking selector on right side of image on multi-selection 
			
			//Log.e("req_w", Integer.toString(req_w));
			checks = new ArrayList<Boolean>(img_paths.size());
			for (int i = 0; i < img_paths.size(); i++)
				checks.add(false);
		}

		public View getView(int position, View convertView,
				ViewGroup parent) {
			CheckableLayout l;
			ImageView i;

			if (convertView == null) {
				i = new ImageView(getActivity());
				i.setScaleType(ImageView.ScaleType.FIT_XY);
				i.setLayoutParams(new ViewGroup.LayoutParams(req_w, req_w));
				i.setPadding(12, 12, 12, 12);
				l = new CheckableLayout(getActivity());
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
			l.setBackgroundColor(getActivity().getResources().getColor(
					checked ? R.color.selected_list_item
							: R.color.unselected_list_item));
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
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			data = params[0];
			return decodeSampledBitmapFromPath(data, req_w, req_w);
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
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
	
	/*
	 * Multichoice mode listener for GridView
	 */
	ArrayList<String> checkedImages;

	public class CaptureImagesMultichoiceListener implements
			GridView.MultiChoiceModeListener {

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Select Images");
			mode.setSubtitle("One image selected");
			MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_done, menu);
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.item_done:
				mode.finish();
				for (int i = 0; i < checkedImages.size(); i++)
					Log.e("checked img..." + i, checkedImages.get(i));
				
				Bundle b = new Bundle();
        		b.putString("patient_name", patient_name);
        		b.putString("recipient_name", recipient_name);
        		b.putLong("patient_id", patient_id);
        		b.putStringArrayList("list_selected_images", checkedImages);
        		b.putSerializable("conversation", conversation);
        		getFragmentManager().popBackStack();
        		getFragmentManager().popBackStack();
        		NewMessageFragment newMsgFrag = new NewMessageFragment();
        		newMsgFrag.setArguments(b);
        		FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction().addToBackStack(null);
        		ft.replace(R.id.fragcontent, newMsgFrag, null);
        		ft.commit();
			}
			return false;
		}

		public void onDestroyActionMode(ActionMode mode) {
			
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
			switch (selectCount) {
			case 1:
				mode.setSubtitle("1 image selected");
				mode.finish();
				for (int i = 0; i < checkedImages.size(); i++)
					Log.e("checked img..." + i, checkedImages.get(i));
				
				Bundle b = new Bundle();
        		b.putString("patient_name", patient_name);
        		b.putString("recipient_name", recipient_name);
        		b.putLong("patient_id", patient_id);
        		b.putStringArrayList("list_selected_images", checkedImages);
        		b.putSerializable("conversation", conversation);
        		getFragmentManager().popBackStack();
        		getFragmentManager().popBackStack();
        		NewMessageFragment newMsgFrag = new NewMessageFragment();
        		newMsgFrag.setArguments(b);
        		FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction().addToBackStack(null);
        		ft.replace(R.id.fragcontent, newMsgFrag, null);
        		ft.commit();
				break;
			default:
				mode.setSubtitle("" + selectCount + " images selected");
				break;
			}
		}

	}
	
}
