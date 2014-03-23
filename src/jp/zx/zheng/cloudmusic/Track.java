package jp.zx.zheng.cloudmusic;

import jp.zx.zheng.storage.CacheManager;

public class Track {
	
	private String mName;
	private String mArtist;
	private String mAlbumArtist;
	private String mAlbum;
	private String mLocation;
	public boolean isUploaded = false;
	public boolean isCached = false;
	
	public Track(String name, String artist, String albumArtist,
			String album, String location) {
		super();
		this.mName = name;
		this.mArtist = artist;
		this.mAlbumArtist = albumArtist;
		this.mAlbum = album;
		this.mLocation = location;
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

	@Override
	public String toString() {
		return mName;
	}
	
	public boolean isCached() {
		return CacheManager.isCached(this);
	}
	
}
