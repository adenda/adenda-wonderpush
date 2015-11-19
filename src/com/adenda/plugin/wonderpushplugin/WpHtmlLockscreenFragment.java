package com.adenda.plugin.wonderpushplugin;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WpHtmlLockscreenFragment extends WonderPushLockscreenFragment 
{
	protected static final String NOTIFICATION_HTML = "notification_html";
	protected static final String NOTIFICATION_BASE_URL = "notification_base_url";
	
	private WebView mWebView;
	private String mNotifHTML;
	private String mNotifBaseUrl;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null)
		{
			mNotifHTML = args.getString(NOTIFICATION_HTML);
			mNotifBaseUrl = args.getString(NOTIFICATION_BASE_URL);
		}
	}
	
	@Override
	protected int getLayout()
	{
		return R.layout.html_page;
	}
	
	@Override
	protected void loadNotifContent(View view)
	{
		mWebView = (WebView) view.findViewById(android.R.id.primary);
		// Load actual notification!
		if (mNotifHTML != null && mWebView != null && mProgressBar != null)
		{
			 mWebView.setWebViewClient(new WebViewClient() {
		            @Override
		            public void onPageFinished(WebView view, String url) {
		                // Make sure to call through to the super's implementation
		                // or the javascript native bridge will not be fully loaded
		                super.onPageFinished(view, url);
		                // Hide the progress bar when the page is loaded
		                mProgressBar.setVisibility(View.GONE);
		            }
		        });
			 
			 mWebView.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					WpHtmlLockscreenFragment.this.getActivity().onTouchEvent(event);
					return false;
				}});
			 
			 // Load data
			 mWebView.loadDataWithBaseURL(mNotifBaseUrl != null ? mNotifBaseUrl : "", mNotifHTML, "text/html", "UTF-8", "");
		}
	}
}
