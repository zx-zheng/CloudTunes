package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import jp.zx.zheng.cloudstorage.CloudStorageFile;
import jp.zx.zheng.storage.CacheManager;

public class Track {
	
	private int id;
	private String mName;
	private String mArtist;
	private String mAlbumArtist;
	private String mAlbum;
	private String mLocation;
	private CloudStorageFile cloudFile;
	private FileInputStream file;
	public boolean isUploaded = false;
	public boolean isPrepared = false;
	
	public Track(int id, String name, String artist, String albumArtist,
			String album, String location) {
		super();
		this.mName = name;
		this.mArtist = artist;
		this.mAlbumArtist = albumArtist;
		this.mAlbum = album;
		this.mLocation = location;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return mName;
	}

	public String getArtist() {
		return mArtist;
	}

	public String getAlbumArtist() {
		return mAlbumArtist;
	}

	public String getAlbum() {
		return mAlbum;
	}

	public String getLocation() {
		return mLocation;
	}
	
	public FileInputStream getFile() {
		return CacheManager.getCacheFile(this);
	}
	
	public byte[] getAlbumArt(Track track) {
		try {
			if(track.getFile() != null) {
				MediaMetadataRetriever mediaMetaData = new MediaMetadataRetriever();
				mediaMetaData.setDataSource(CacheManager.getCachePath(track));
				byte[] albumArt = mediaMetaData.getEmbeddedPicture(); 
				mediaMetaData.release();
				return albumArt;
			} else {
				return null;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public void setFile(FileInputStream file) {
		this.file = file;
	}
	
	public void setFile() {
		if(isCached()) {
			file = CacheManager.getCacheFile(this);
		}
	}

	@Override
	public String toString() {
		return mName;
	}
	
	public boolean isCached() {
		return CacheManager.isCached(this);
	}
	
}
