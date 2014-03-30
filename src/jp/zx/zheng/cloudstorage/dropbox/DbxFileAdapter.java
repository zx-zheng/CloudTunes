package jp.zx.zheng.cloudstorage.dropbox;

import java.io.FileInputStream;
import java.io.IOException;

import jp.zx.zheng.cloudstorage.CloudStorageFile;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;

public class DbxFileAdapter implements CloudStorageFile {

	public DbxFile dbxFile;
	
	public DbxFileAdapter(DbxFile file){
		dbxFile = file;
	}

	@Override
	public FileInputStream getReadStream() throws DbxException, IOException {
		return dbxFile.getReadStream();
	}

	@Override
	public void close() {
		dbxFile.close();
	}
}
