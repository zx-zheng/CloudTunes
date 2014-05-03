package jp.zx.zheng.cloudmusic;

import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import jp.zx.zheng.cloudstorage.CloudStorageFile;
import jp.zx.zheng.cloudstorage.dropbox.Downloader;
import jp.zx.zheng.db.MusicLibraryDBAdapter;
import jp.zx.zheng.db.MusicLibraryDBHelper;
import jp.zx.zheng.storage.CacheManager;

public class Track {
	private static final String TAG = Track.class.getName();
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
	
	public byte[] getAlbumArt() {
		try {
			if(this.getFile() != null) {
				MediaMetadataRetriever mediaMetaData = new MediaMetadataRetriever();
				mediaMetaData.setDataSource(CacheManager.getCachePath(this));
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
	
	public void updateInfo() {
		try {
			if(this.getFile() != null) {
				MediaMetadataRetriever mediaMetaData = new MediaMetadataRetriever();
				mediaMetaData.setDataSource(CacheManager.getCachePath(this));
				this.mName = mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				this.mArtist = mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				this.mAlbumArtist = mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
				this.mAlbum = mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				mediaMetaData.release();
			}				
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	
	}
	
	public void insertToDb() {
		try {
			Log.d(TAG, "track is inserted to DB");
			if(this.getFile() != null) {
				MediaMetadataRetriever mediaMetaData = new MediaMetadataRetriever();
				mediaMetaData.setDataSource(CacheManager.getCachePath(this));
				String artist = mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				String albumArtist = mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
				if(albumArtist == null) {
					artist = albumArtist;
				}
				MusicLibraryDBAdapter.instance.insertTrackToCloudLibrary(
						mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE), 
						artist,
						albumArtist,
						mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
						mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
						0,//Integer.parseInt(mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)),
						0,//Integer.parseInt(mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)),
						0,//Integer.parseInt(mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)),
						0,//Integer.parseInt(mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)),
						0,
						Integer.parseInt(mediaMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)),
						CacheManager.pathTodbxPathString(mLocation));
				mediaMetaData.release();
			}				
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
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
