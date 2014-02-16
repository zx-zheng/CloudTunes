package jp.zx.zheng.cloudstorage.ybox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class YboxXmlRequest extends Request<XmlPullParser> {
	private Listener<XmlPullParser> mlistener;
	private String accessToken;
	
	public YboxXmlRequest(String url,
			String accessToken,
			Listener<XmlPullParser> listener,
			ErrorListener errorListener) {
		this(Method.GET, url, accessToken, listener, errorListener);
	}
	
	public YboxXmlRequest(int method, String url,
			String accessToken,
			Listener<XmlPullParser> listener,
			ErrorListener errorListener) {
		super(method, url, errorListener);
		this.accessToken = accessToken;
		mlistener = listener;
	}

	@Override
	protected Response<XmlPullParser> parseNetworkResponse(
			NetworkResponse response) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new ByteArrayInputStream(response.data), "UTF-8");
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return Response.success(parser, HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(XmlPullParser response) {
		mlistener.onResponse(response);
	}
	
	@Override
    public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = super.getHeaders();
		// Add AUTH HEADER
		Map<String, String> newHeaders = new HashMap<String, String>();
		newHeaders.putAll(headers);
		newHeaders.put("Authorization", "Bearer "+ accessToken);
		System.out.println(accessToken);

		//newHeaders.put("User-Agent", "Yahoo AppID: " + appId);
		return newHeaders;
	}

}
