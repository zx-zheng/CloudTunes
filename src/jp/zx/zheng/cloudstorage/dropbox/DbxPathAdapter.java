package jp.zx.zheng.cloudstorage.dropbox;

import com.dropbox.sync.android.DbxPath;

import jp.zx.zheng.cloudstorage.CloudStoragePath;

public class DbxPathAdapter implements CloudStoragePath {
	
	DbxPath mDbxPath;
	
	public DbxPathAdapter(DbxPath path) {
		mDbxPath = path;
	}
	
	@Override
	public DbxPath getPath() {
		return mDbxPath;
	}
	
	@Override
	public boolean isRoot() {		
		return mDbxPath.equals(DbxPath.ROOT);
	}

	@Override
	public String getName() {
		return mDbxPath.getName();
	}

	@Override
	public CloudStoragePath getParent() {
		return new DbxPathAdapter(mDbxPath.getParent());
	}

	@Override
	public boolean isDir() {
		return Dropbox.getInstance().isDir(mDbxPath);
	}
	
	@Override
	public String toString() {
		if(mDbxPath.equals(DbxPath.ROOT)) {
			return "/";
		} else {
			return mDbxPath.toString();
		}
	}

}
