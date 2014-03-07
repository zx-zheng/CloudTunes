package jp.zx.zheng.cloudstorage.dropbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.zx.zheng.cloudmusic.Track;
import jp.zx.zheng.storage.CacheManager;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Response.Listener;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

public class Dropbox {

	private static final String TAG = Dropbox.class.getName();
    private static final String appKey = "jffm42sh7pd6gqp";
    private static final String appSecret = "6o2w7yyalpeqqt3";

    public static final int REQUEST_LINK_TO_DBX = 0;
    
    private DbxAccountManager mDbxAcctMgr;
    
    public Dropbox (Context context) {
    	mDbxAcctMgr = DbxAccountManager.getInstance(context, appKey, appSecret);
    }
    
    public void login (Activity activity) {
    	if (!mDbxAcctMgr.hasLinkedAccount()) {
    		mDbxAcctMgr.startLink(activity, REQUEST_LINK_TO_DBX);
    	}
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
    
    public FileInputStream downloadFileAndCache (Track track) {
    	try {
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			dbxFs.getSyncStatus();
			DbxFile file = dbxFs.open(CacheManager.pathTodbxPath(track.getLocation()));
			CacheManager.saveCache(file.getReadStream(), track);
			file.close();
			return CacheManager.getCacheFile(track);
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public FileInputStream getFileAndCache (Track track) {
    	return downloadFileAndCache(track);
    }
}
