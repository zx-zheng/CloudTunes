package jp.zx.zheng.cloudstorage;

import java.io.FileInputStream;
import java.io.IOException;

import com.dropbox.sync.android.DbxException;

public interface CloudStorageFile {
	public FileInputStream getReadStream() throws Exception;
	public void close();
}
