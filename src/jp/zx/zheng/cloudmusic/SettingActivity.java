package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;

import com.dropbox.sync.android.DbxAccount;

import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import jp.zx.zheng.musictest.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingActivity extends PreferenceActivity {
	
	private static final String TAG = SettingActivity.class.getName();
	private static final int REQUEST_XML_PATH = 100;
	public static final String DROPBOX_LOGIN = "dropboxLogin";
	
	private static SharedPreferences mPref;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        PreferenceScreen reloadPref = (PreferenceScreen) findPreference("reload");
        reloadPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String path = mPref.getString(getString(R.string.path_of_xml_key), "");
				MusicLibraryParser parser = new MusicLibraryParser(SettingActivity.this, path);
				parser.execute();
				return true;
			}
		});
        
        ListPreference libraryStoragePref = (ListPreference) findPreference("pref_storage_for_library_file");                
        libraryStoragePref.setSummary(libraryStoragePref.getValue());
        
        PreferenceScreen pathPref = (PreferenceScreen) findPreference(getString(R.string.path_of_xml_key));
        pathPref.setSummary(mPref.getString(getString(R.string.path_of_xml_key), ""));
        pathPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getApplicationContext(), Finder.class);
				startActivityForResult(intent, REQUEST_XML_PATH);
				return true;
			}
		});
        
        //Preference of Cloud storage
        PreferenceScreen dropboxPref = (PreferenceScreen)findPreference("dropbox_account");
        reloadDropboxAccountSummary();        
        dropboxPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Dropbox.getInstance(getApplicationContext()).login(SettingActivity.this);
				return true;
			}
		});
        
        Intent intent = getIntent();
        if(intent != null) {
        	String action = intent.getAction();
        	if(action != null) {
        		if(action.equals(DROPBOX_LOGIN)) {
        			Dropbox.getInstance(getApplicationContext()).login(SettingActivity.this);
        		}
        	}
        }
	}
	
	private void setXmlPath(String path) {
		Editor editor = mPref.edit();
		editor.putString(getString(R.string.path_of_xml_key), path);
		editor.commit();
	}
	
	private void reloadXmlPath() {
		PreferenceScreen pathPref = (PreferenceScreen) findPreference(getString(R.string.path_of_xml_key));
        pathPref.setSummary(mPref.getString(getString(R.string.path_of_xml_key), null));
	}
	
	private void reloadDropboxAccountSummary() {
		PreferenceScreen dropboxPref = (PreferenceScreen)findPreference("dropbox_account");
		Dropbox dropbox = Dropbox.getInstance(getApplicationContext());
        if(dropbox.isLogin()) {        	
        	dropboxPref.setSummary(getString(R.string.logged_in) + " " 
        			+ dropbox.getAccoutName());
        } else {
        	dropboxPref.setSummary(getString(R.string.not_logged_in));
        }
	}
	
	private void reloadDropboxAccountSummaryAsync() {
		DbxAccount.Listener listener = new DbxAccount.Listener() {
			@Override
			public void onAccountChange(DbxAccount arg0) {
				reloadDropboxAccountSummary();
			}
		};
		Dropbox.getInstance(getApplicationContext()).getAccountNameAsync(listener);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult");
    	switch (requestCode) {
    	case Dropbox.REQUEST_LINK_TO_DBX:
    		Log.d(TAG, "Dropbox logged in");
    		reloadDropboxAccountSummaryAsync();
    		break;
    	case REQUEST_XML_PATH:
    		if(resultCode == RESULT_OK) {
    			setXmlPath(data.getStringExtra("path"));
    			reloadXmlPath();
    		}
    		break;
		default:
			break;
		}
    }
}
