package jp.zx.zheng.cloudstorage.googledrive;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import jp.zx.zheng.cloudstorage.CloudStoragePath;

public class DrivePathAdapter implements CloudStoragePath {

	Metadata mMeta;
	
	public DrivePathAdapter(Metadata meta) {
		mMeta = meta;
	}
	
	@Override
	public boolean isRoot() {
		return isRoot(mMeta);
	}
	
	public static boolean isRoot(Metadata meta) {
		if(meta == null) {
			return true;
		}
		return meta.getDriveId().equals(GoogleDrive.getInstance(null).getRootId());
	}

	@Override
	public String getName() {
		return mMeta.getTitle();
	}

	@Override
	public CloudStoragePath getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDir() {
		return mMeta.isFolder();
	}

	@Override
	public Object getPath() {
		return mMeta;
	}

	@Override
	public String getRootName() {
		return "Google Drive";
	}
	
	public static List<CloudStoragePath> wrapMetadataBuffer(MetadataBuffer buffer) {
		List<CloudStoragePath> list = new ArrayList<CloudStoragePath>();
		for(Metadata meta: buffer) {
			list.add(new DrivePathAdapter(meta));
		}
		return list;
	}

}
