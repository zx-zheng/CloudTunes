package jp.zx.zheng.cloudstorage.googledrive;

import java.util.ArrayList;
import java.util.List;

import jp.zx.zheng.cloudmusic.FinderRowArrayAdapter;
import jp.zx.zheng.cloudstorage.CloudStoragePath;
import jp.zx.zheng.cloudstorage.dropbox.Dropbox;
import android.R;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

public class GoogleDrive implements ConnectionCallbacks, 
com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener{

	private static final String TAG = GoogleDrive.class.getName();
	private static GoogleDrive instance;
	private GoogleApiClient mGoogleApiClient;
	private Activity mActivity;
	private Context mContext;
	private DriveId mRootId;
	private ListView mListView;
	private Metadata mCurrentMeta;
	public static final int RESOLVE_CONNECTION_REQUEST_CODE = 10001;

	public static GoogleDrive getInstance(Context context) {
		if(instance == null) {
    		instance = new GoogleDrive(context);
    	}
    	return instance;
	}
	
	private GoogleDrive(Context context) {
		mGoogleApiClient = new GoogleApiClient.Builder(context)
        .addApi(Drive.API)
        .addScope(Drive.SCOPE_FILE)
        //.addScope(Drive.SCOPE_APPFOLDER)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		mContext = context;
		//List<String> scope = new ArrayList<String>();
		//scope.add(DriveScopes.DRIVE);
		//GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, scope);
		//credential.setSelectedAccountName(accountName);
		//com.google.api.services.drive.Drive service = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
	}
	
	public void connect(Activity activity) {
		mActivity = activity;
		mGoogleApiClient.connect();
		//Drive.DriveApi.requestSync(mGoogleApiClient);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
	        try {
	            connectionResult.startResolutionForResult(mActivity, RESOLVE_CONNECTION_REQUEST_CODE);
	        } catch (IntentSender.SendIntentException e) {
	            // Unable to resolve, message user appopriately
	        	Log.e(TAG, "Exception while starting resolution activity", e);
	        }
	    } else {
	        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mActivity, 0).show();
	    }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		Log.i(TAG, "GoogleApiClient connected");
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		
	}
	
	public DriveId getRootId() {
		return mRootId;
	}
	
	public boolean listDirectory(ListView view, CloudStoragePath path) {
		Log.d(TAG, "list dir");
		mListView = view;
		DriveFolder folder;
		if(path == null) {
			Log.d(TAG, "list drive root");
			folder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
			mRootId = folder.getDriveId();
		} else {
			mCurrentMeta = (Metadata) path.getPath();
			folder = Drive.DriveApi.getFolder(mGoogleApiClient, mCurrentMeta.getDriveId());
		}		 
		folder.listChildren(mGoogleApiClient).setResultCallback(new
				ResultCallback<MetadataBufferResult>() {
			@Override
			public void onResult(MetadataBufferResult result) {
				if (!result.getStatus().isSuccess()) {
					Log.d(TAG, "list dir failed");
					return;
				}
				Log.d(TAG, "list dir success: " + result.getMetadataBuffer().getCount() );
				MetadataBuffer buffer = result.getMetadataBuffer();
				mListView.setAdapter(new FinderRowArrayAdapter(mContext,
						jp.zx.zheng.musictest.R.layout.finder_row,
						DrivePathAdapter.wrapMetadataBuffer(result.getMetadataBuffer()),
						!DrivePathAdapter.isRoot(mCurrentMeta)));
	        }
		});

		return true;
	}
}
