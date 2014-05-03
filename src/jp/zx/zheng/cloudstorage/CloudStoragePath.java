package jp.zx.zheng.cloudstorage;

public interface CloudStoragePath {
	public boolean isRoot();
	public String getName();
	public CloudStoragePath getParent();
	public boolean isDir();
	public String toString();
	public Object getPath();
	public String getRootName();
}
