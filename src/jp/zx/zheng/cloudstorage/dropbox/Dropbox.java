package jp.zx.zheng.cloudstorage.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jp.zx.zheng.cloudmusic.Track;
import jp.zx.zheng.cloudstorage.CloudStorageFile;
import jp.zx.zheng.storage.CacheManager;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Response.Listener;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxAccountInfo;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

public class Dropbox {

	private static final String TAG = Dropbox.class.getName();
    private static final String appKey = "jffm42sh7pd6gqp";
    private static final String appSecret = "6o2w7yyalpeqqt3";
    private static final String DROPBOX_APP_CACHE_DIR = "app_DropboxSyncCache";

    public static final int REQUEST_LINK_TO_DBX = 0;
    
    private static Dropbox instance;
    private static DbxAccountManager mDbxAcctMgr;
    private Context mContext;
    private AtomicInteger mDownloadCount = new AtomicInteger(0);
    
    public static Dropbox getInstance(Context context) {
    	if(instance == null) {
    		instance = new Dropbox(context);
    	}
    	return instance;
    }
    
    private Dropbox (Context context) {
    	mDbxAcctMgr = DbxAccountManager.getInstance(context, appKey, appSecret);
    	mContext = context;
    }
    
    public void startDownloadTask() {
    	mDownloadCount.incrementAndGet();
    }
    
    public boolean isDownloading() {
    	return mDownloadCount.get() > 0;
    }
    
    public void endDownloadTask() {
    	mDownloadCount.decrementAndGet();
    }
    
    public void login (Activity activity) {
    	if (mDbxAcctMgr.hasLinkedAccount()) {
    		mDbxAcctMgr.unlink();
    	}
    	mDbxAcctMgr.startLink(activity, REQUEST_LINK_TO_DBX);
    }
    
    public void getAccountNameAsync(DbxAccount.Listener listener) {
    	DbxAccount account = mDbxAcctMgr.getLinkedAccount();
    	account.addListener(listener);
    }
    
    public String getAccoutName() {
    	DbxAccountInfo accountInfo = mDbxAcctMgr.getLinkedAccount().getAccountInfo();
    	if(accountInfo != null) {
    		return accountInfo.displayName;
    	}
    	return "";
    }
    
    public boolean isLogin () {
    	return mDbxAcctMgr.hasLinkedAccount();
    }
    
    public List<String> listDirectory (DbxPath dir) {
    	List<String> fileList = new ArrayList<String>();
    	try {
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			dbxFs.getSyncStatus();
			if (dbxFs.isFolder(dir)) {
				List<DbxFileInfo> infos = dbxFs.listFolder(dir);
				for (DbxFileInfo info : infos) {
					fileList.add(info.path.toString());
					Log.d(TAG, info.path.toString());
				}
			} else {
				return null;
			}
    	} catch (DbxException.InvalidParameter e) {
    		Log.v(TAG, dir.toString() + " is a file");
    		return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return fileList;
    }
    
    public CloudStorageFile downloadFile(String path) {
    	try {
    		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
    		dbxFs.getSyncStatus();
    		DbxFile file = dbxFs.open(new DbxPath(path));
    		return new DbxFileAdapter(file);
    	} catch  (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public FileInputStream downloadTrackFileAndCache (Track track) throws DbxException.AlreadyOpen{
    	try {
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			dbxFs.getSyncStatus();
			DbxPath dbxPath = CacheManager.pathTodbxPath(track.getLocation());
			if(dbxFs.exists(dbxPath)){
				track.isUploaded = true;
				Log.d(TAG, "open: " + dbxPath.toString());
				DbxFile file = dbxFs.open(dbxPath);
				CacheManager.saveCache(file.getReadStream(), track);
				file.close();
				return CacheManager.getCacheFile(track);
			} else {
				track.isUploaded = false;
				Log.w(TAG, dbxPath.toString() + " not found");
			}
    	} catch (DbxException.AlreadyOpen e) {
    		Log.d(TAG, track + " is already opened and downloading");
    		track.isUploaded = true;
    		throw e;
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public void deleteCache() {
    	if(isDownloading()) {
    		Log.d(TAG, "Downloading. Deleting cache is canceled");
    		return;
    	}
    	Log.d(TAG, "delete cache");
    	File appDir = new File(mContext.getCacheDir().getParent());
    	if(appDir.exists()) {
    		File cacheDir = new File(appDir, DROPBOX_APP_CACHE_DIR);
    		File dropboxAppDir = new File(cacheDir, appKey);
    		if(dropboxAppDir.exists()) {
    			String[] children = dropboxAppDir.list();
    			for(String dir : children) {
    				File targetDir = new File(dropboxAppDir, dir);
    				File fileDir = new File(targetDir, "files");
    				if(fileDir.exists()) {
    					for(String file : fileDir.list()) {
    						new File(fileDir, file).delete();
    					}
    					new File(targetDir, "cache.db").delete();    					
    				}
    			}
    		}
    		DbxFileSystem dbxFs;
			try {
				dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
				dbxFs.awaitFirstSync();
			} catch (Unauthorized e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	}
    }
    
}
