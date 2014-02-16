package jp.zx.zheng.cloudstorage.ybox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.zx.zheng.musictest.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceActivity.Header;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Ybox {
	private static Ybox ybox;
	public final static String appId = "dj0zaiZpPTBpVGFSVVVUZFgyeCZzPWNvbnN1bWVyc2VjcmV0Jng9ZDY-";
	public final static String TAG = Ybox.class.getName();
	public final static String YBOX_PREFERENCE_NAME = "ybox";
	public final static String YBOX_PREFERENCE_SID = "sid";
	public final static String YBOX_PREFERENCE_ROOT_DIR = "rootUniqID";
	
	private boolean isInit = false;
	private boolean hasSid = false;
	private String sid;
	private String accessToken;
	private String rootUniqId;
	private SharedPreferences boxPref;
	private SharedPreferences yConnectPref;
	private RequestQueue mQueue;
	
	public static Ybox getInstance(){
		if(ybox == null){
			ybox = new Ybox();
		}
		return ybox;
	}
	private Ybox(){
		
	}
	
	public void init(Activity activity){
		if(!isInit){
			mQueue = Volley.newRequestQueue(activity);
			boxPref = activity.getSharedPreferences(YBOX_PREFERENCE_NAME, Context.MODE_PRIVATE);
			sid = boxPref.getString(YBOX_PREFERENCE_SID, null);
			rootUniqId = boxPref.getString(YBOX_PREFERENCE_ROOT_DIR, null);
			yConnectPref = activity.getSharedPreferences(
					YConnectImplicitWebViewActivity.YCONNECT_PREFERENCE_NAME,
					Context.MODE_PRIVATE);
			accessToken = YLoginManager.getAccessToken(activity);
			isInit = true;
		}
	}
	
	public void setAccessToken(String accessToken){
		this.accessToken = accessToken;
	}
	
	public void setSid(Activity activity){
		init(activity);
		sid = boxPref.getString(YBOX_PREFERENCE_SID, null);
		rootUniqId = boxPref.getString(YBOX_PREFERENCE_ROOT_DIR, null);
		if(sid == null){
			requsetUserInfo(activity);
			Log.d(TAG, "sid loaded");
		}else{
			hasSid = true;
		}
	}
	
	public boolean hasSid(){
		return hasSid;
	}
	
	public String getSid(){
		if(hasSid){
			return sid;
		}else{
			return "no sid";
		}
	}
	
	private void requsetUserInfo(Activity activity){
		init(activity);
		Log.d(TAG, "requesting user info");
		final String userInfoUrl = "https://ybox.yahooapis.jp/v1/user/fullinfo/";
		String guid = yConnectPref.getString("guid", "null");
		Log.d(TAG, guid);
		mQueue.add(new YboxXmlRequest(userInfoUrl + guid, accessToken, 
				new MusicTestListener(activity),
				new YboxErrorListener()));
	}
	
	public void requestFileList(Activity activity, String uniqID){
		init(activity);
		Log.d(TAG, "requesting file list");
		final String fileListUrl = "https://ybox.yahooapis.jp/v1/filelist/";
		mQueue.add(new YboxXmlRequest(fileListUrl + sid + "/" + uniqID, 
				accessToken, new MusicTestListener(activity), new YboxErrorListener()));
	}
	
	public void requestFileList(Activity activity){
		init(activity);
		Log.d(TAG, "requesting file list");
		final String fileListUrl = "https://ybox.yahooapis.jp/v1/filelist/";
		mQueue.add(new YboxXmlRequest(fileListUrl + sid + "/" + rootUniqId, 
				accessToken, new MusicTestListener(activity), new YboxErrorListener()));
	}
	
	public void request(String url ,String token){
		this.accessToken = token;
		mQueue.add(new StringRequest(url, new YboxListener(), new YboxErrorListener())
		{
		    @Override
		        public Map<String, String> getHeaders() throws AuthFailureError {
		    	Map<String, String> headers = super.getHeaders();
		        // Add AUTH HEADER
		        Map<String, String> newHeaders = new HashMap<String, String>();
		        newHeaders.putAll(headers);
		        //System.out.println(accessToken);
		        newHeaders.put("Authorization", "Bearer "+ accessToken);
		        //newHeaders.put("User-Agent", "Yahoo AppID: " + appId);
		        return newHeaders;
		    }
		});
		mQueue.start();
		Log.d("Volley", "request");
	}
	
	public class YboxListener implements Listener<String>{
		@Override
		public void onResponse(String response) {
			Log.d(TAG, "get response");
			Log.d(TAG, response);
		}		
	}
	
	public class YboxErrorListener implements ErrorListener{
		@Override
		public void onErrorResponse(VolleyError error) {
			//Log.d(TAG, error.getMessage());
			Log.e(TAG, error.getStackTrace().toString());
			//error.printStackTrace();
		}
	}
	
	public class MusicTestListener implements Listener<XmlPullParser>{
		Activity activity;
		public MusicTestListener(Activity activity){
			this.activity = activity;
		}
		@Override
		public void onResponse(XmlPullParser response) {
			Log.d(TAG, response.toString());
			Log.d(TAG, "parsing xml");
			try {
				int xmlEventType = response.getEventType();
				while(xmlEventType != XmlPullParser.END_DOCUMENT){
					switch (xmlEventType) {
					case XmlPullParser.START_TAG:
						if(response.getName().equals("Sid")){
							response.next();
							sid = response.getText();
							Log.d(TAG, "SID: " + sid);
						}else if(response.getName().equals("RootUniqId")){
							response.next();
							rootUniqId = response.getText();
							Log.d(TAG, "RootUniqId: " + rootUniqId);
						}else if(response.getName().equals("Name")){
							response.next();
							Log.d(TAG, "Name: " + response.getText());
						}
						break;

					default:
						break;
					}
					xmlEventType = response.next();
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			SharedPreferences.Editor editor = boxPref.edit();
			editor.putString(YBOX_PREFERENCE_SID, sid);
			editor.putString(YBOX_PREFERENCE_ROOT_DIR, rootUniqId);
			editor.commit();
			TextView text = (TextView)activity.findViewById(R.id.textView1);
			text.setText(sid);
			hasSid = true;
		}
		
	}
	
}
