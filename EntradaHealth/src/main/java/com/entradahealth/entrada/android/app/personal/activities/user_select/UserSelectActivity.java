package com.entradahealth.entrada.android.app.personal.activities.user_select;

import java.io.File;
import java.io.IOException;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.acra.ACRA;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.ChoosePin;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Setup;
import com.entradahealth.entrada.android.app.personal.activities.add_user.AddUserActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_display.CaptureImages;
import com.entradahealth.entrada.android.app.personal.activities.job_display.ImageDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.pin_entry.PinEntryActivity;
import com.entradahealth.entrada.android.app.personal.utils.AndroidUtils;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.files.FileUtils;
import com.google.common.collect.ImmutableList;

public class UserSelectActivity extends EntradaActivity {
	final static public String PREFS_NAME = "PREFS_NAME";
	final static private String PREF_KEY_SHORTCUT_ADDED = "PREF_KEY_SHORTCUT_ADDED";
	private String TAG = "Entrada";
	private ImmutableList<User> users = null;
	UserPrivate curUser;
	ListView lvUsers;
	SharedPreferences sp;
	Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_select);
		lvUsers = (ListView) findViewById(R.id.userList);
		sp = getSharedPreferences("Entrada", Context.MODE_WORLD_READABLE);
		edit = sp.edit();
		try {
			edit.putString("APP_VER_NAME", this.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0).versionName);
			edit.putInt("APP_VER_CODE", this.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0).versionCode);
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		edit.commit();
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//addShortcut();
		
	}
	
	//Adding shortcut on Home screen
	private void addShortcut() {
        
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	    boolean shortCutWasAlreadyAdded = sharedPreferences.getBoolean(PREF_KEY_SHORTCUT_ADDED, false);
	    if (shortCutWasAlreadyAdded) 
	    	return;
	    
        Intent shortcutIntent = new Intent(getApplicationContext(),
                UserSelectActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Entrada");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.drawable.icon));
 
        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
        
     // Remembering that ShortCut was already added
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_KEY_SHORTCUT_ADDED, true);
        editor.commit();
    }
	

	@Override
	protected void onStart() {
		super.onStart();

		ActionBar ab = getActionBar();
		ab.setTitle(R.string.user_select_title);
		ab.setDisplayHomeAsUpEnabled(false);

		// AndroidUtils aUtils = new AndroidUtils(this);

		if(BundleKeys.PinContext != null)
			BundleKeys.isAppKilled = false;
		else
			BundleKeys.isAppKilled = true;
		
		BundleKeys.PinContext = UserSelectActivity.this;

		File external = AndroidUtils.getExternalStorageLocation();
		boolean externalHandled = false;
		if (!external.exists()) {
			externalHandled = external.mkdirs();
		}

		if (!externalHandled) {
			// TODO: handle unwritable/nonexistent case.
		}

	}

	int total_user = 0;

	public void checkForUser() {

		File userRoot = User.getUserRoot();
		File[] userDirs = userRoot.listFiles(FileUtils.DIRECTORY_FILTER);
		if (userDirs == null || userDirs.length == 0) {
			Intent intent = new Intent(this, ChoosePin.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else {
			
			//Restore sync preferences
			BundleKeys.days_to_sync = sp.getInt("DAYS_TO_SYNC", 1);
			BundleKeys.mins_to_sync = sp.getInt("MINS_TO_SYNC", 5);
			
			// Check if pin time is over
			if (BundleKeys.PASSCODE_EXPIRED) {
				Log.d(TAG, "Checking for user creation");
				BundleKeys.cur_uname = sp.getString("CUR_UNAME", null);
				Intent intent = new Intent(UserSelectActivity.this,
						PinEntryActivity.class);
				Bundle b = new Bundle();
				b.putString(BundleKeys.SELECTED_USER,
						sp.getString("sel_user", null));
				try{
					b.putBoolean("fromSecureMessaging",getIntent().getExtras().getBoolean("fromSecureMessaging"));
				} catch(Exception ex){
				}
				intent.putExtras(b);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {
				BundleKeys.cur_uname = sp.getString("CUR_UNAME", null);
				setState(BundleKeys.cur_uname, sp.getString("PIN_SAVED", "1111"));
				
				BundleKeys.TIME_START = System.currentTimeMillis();
				edit.putLong("TIME_START", BundleKeys.TIME_START);
				edit.commit();				
				
				
				
				if (curUser == null) {
					try {
						users = User.getUsers();
						total_user = users.size();
					} catch (UserLoadException ex) {
						throw new RuntimeException("Failed to get users: ", ex);
					}

					Log.e("total_user", Integer.toString(total_user));

					for (int i = 1; i <= total_user + 1; i++) {
						String uname = "User_" + Integer.toString(i);
						boolean set = setState(uname, sp.getString("PIN_SAVED", "1111"));
						if (set)
							break;
					}
				}

				

				String currentJob = curUser
						.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID);
				if (curUser.getAccounts().size() == 0) {
					// navigate directly to Setup
                    // navigate directly to AddAccountActivity
                    Intent intent = new Intent(this, AddAccountActivity.class);
					//Intent intent = new Intent(this, Setup.class);
					startActivity(intent);
					finish();
				} else if (currentJob != null) {
					long jobId = Long.parseLong(currentJob);
					String accountName = curUser
							.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT);

					try {
						Job j = AndroidState.getInstance().getUserState()
								.getProvider(accountName).getJob(jobId);

						Intent intent;

						if (j != null) {
							if (BundleKeys.fromCaputreImages){
								intent = new Intent();
								intent.putExtra("isModified", true);
								if(j.isFlagSet(Flags.IS_FIRST))
									intent.putExtra("isFirst", true);
								else
									intent.putExtra("isFirst", false);
								//intent.putExtra("isFirst", false);
								intent.putExtra("isDeleted", true);
								intent.putExtra("img_count", 5);
								intent.putExtra("job_type", BundleKeys.job_type);
								intent.putExtra("isFromList", true);
								intent.putExtra("isNew", true);
								intent.putExtra(BundleKeys.SELECTED_JOB, jobId);
								intent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
								intent.putExtra("sel_job_str", "");
								intent.putExtra("interrupted", false);
								intent.setClass(UserSelectActivity.this, CaptureImages.class);
								//activity.startActivity(intent);
							}else if (BundleKeys.fromImageDisplay){
								intent = new Intent();
								intent.putExtra("isModified", true);
								intent.putExtra("img_path", BundleKeys.current_img_path);
								intent.putExtra("pos", BundleKeys.current_img_position);
								intent.putExtra("total", BundleKeys.img_total);
								intent.putExtra(BundleKeys.SELECTED_JOB, jobId);
								intent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
								intent.setClass(UserSelectActivity.this, ImageDisplayActivity.class);
							} else {
							BundleKeys.IS_DIRTY = true;// mark job as dirty in
														// case of data loss
								intent = new Intent(UserSelectActivity.this,
									JobDisplayActivity.class);
							Bundle b = new Bundle();
							intent.putExtra("isDeleted", true);
							intent.putExtra("isModified", true);
							b.putBoolean("isFirst", false);
							intent.putExtra("isFromList", true);
							b.putLong(BundleKeys.SELECTED_JOB, jobId);
							b.putString(BundleKeys.SELECTED_JOB_ACCOUNT,
									accountName);
							boolean isInterrupted = sp.getBoolean("IS_INTERRUPTED", false);
							b.putBoolean("interrupted", isInterrupted);
							intent.putExtras(b);
							}
						} else {
							intent = new Intent(UserSelectActivity.this,
									JobListActivity.class);
						}
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					} catch (Exception e) {
						ACRA.getErrorReporter().handleSilentException(e);
					} finally {
						curUser.setStateValue(
								JobDisplayActivity.JOB_IN_PROGRESS_ID, null);
						curUser.setStateValue(
								JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT,
								null);

						try {
							curUser.save();
						} catch (IOException e) {
							ACRA.getErrorReporter().handleSilentException(e);
						}
					}
				} else {
					Intent intent = new Intent(this, JobListActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}

			}

		}
	}

	public boolean setState(String uname, String pin) {
		BundleKeys.cur_uname = uname;
		try {
			curUser = User.getPrivateUserInformation(uname, pin);
			AndroidState.getInstance().clearUserState();
			AndroidState.getInstance().createUserState(curUser);
		} catch (UserLoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidPasswordException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AccountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (curUser == null)
			return false;
		else
			return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != 1)
				checkForUpdates();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace(); // TODO: Handle this exception type.
		}

		BundleKeys.PASSCODE = sp.getInt("PASSCODE", 0);
		BundleKeys.PASSCODE_MINUTES = sp.getInt("PASSCODE_MINUTES", 0);
		BundleKeys.TIME_START = sp.getLong("TIME_START", 0);
		
		if (BundleKeys.PASSCODE_MINUTES > 0) {
			long timeGoneMillis = System.currentTimeMillis()
					- BundleKeys.TIME_START;
			long timeGoneMins = timeGoneMillis / (60 * 1000);
			if (timeGoneMins > BundleKeys.PASSCODE_MINUTES) {
				BundleKeys.PASSCODE_EXPIRED = true;
			} else {
				BundleKeys.PASSCODE_EXPIRED = false;
			}
		} else {
			BundleKeys.PASSCODE_EXPIRED = true;
		}

		checkForUser();
		
	}

	@Override
	public void onBackPressed() {
		// we have to hijack the back button in pretty much every activity
		// because of the way we're setting this up. We have to more or less
		// kill the task history to control PIN entry, so back presses must
		// be hooked and sent to the right place.

		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.user_select, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		if (item.getItemId() == R.id.addUserMenuItem) {
			Log.d(TAG, "adding new user");
			Intent intent = new Intent(this, AddUserActivity.class);
			intent.putExtra("from_settings", false);
			startActivity(intent);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkForCrashes() {
		CrashManager.register(this, getString(R.string.hockeyapp_api_key));
	}

	private void checkForUpdates() {
		// Remove this for store builds!
		UpdateManager.register(this, getString(R.string.hockeyapp_api_key));
	}

	private void refreshUserList() {
		try {
			users = User.getUsers();
		} catch (UserLoadException ex) {
			throw new RuntimeException("Failed to get users: ", ex);
		}

		ListAdapter adapter = new UserListItemAdapter(this,
				android.R.layout.simple_list_item_1, users);

		if (users.isEmpty()) {
			Intent intent = new Intent(this, AddUserActivity.class);
			startActivity(intent);
			finish();
		} else {
			lvUsers.setAdapter(adapter);
		}

	}

	public class UserListItemAdapter extends ArrayAdapter<User> {
		private final Activity _activity;

		public UserListItemAdapter(Activity activity, int textViewResourceId,
				ImmutableList<User> objects) {
			super(activity, textViewResourceId, objects);
			this._activity = activity;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			UserHolder holder;

			if (row == null) {
				LayoutInflater inflater = _activity.getLayoutInflater();
				row = inflater.inflate(R.layout.user_select_list_item, parent,
						false);
				TextView dn = (TextView) row
						.findViewById(R.id.UserSelect_DisplayName);
				holder = new UserHolder(dn);
				row.setTag(holder);
			} else {
				holder = (UserHolder) row.getTag();
			}

			final User user = getItem(position);
			Log.d(TAG, String.valueOf(position) + ": "
					+ (user != null ? user : "null"));
			holder.displayNameView.setText(user.getDisplayName());

			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(UserSelectActivity.this,
							PinEntryActivity.class);

					Bundle b = new Bundle();
					b.putString(BundleKeys.SELECTED_USER, user.getName());
					Log.d(TAG, "bundle: " + (b != null ? b.toString() : "null"));
					Editor edit = sp.edit();
					edit.putString("sel_user", user.getName());
					edit.commit();

					intent.putExtras(b);
					startActivity(intent);
					finish();
				}
			});

			return row;
		}

		public class UserHolder {
			public final TextView displayNameView;

			public UserHolder(TextView displayNameView) {
				this.displayNameView = displayNameView;
			}
		}
	}
}
