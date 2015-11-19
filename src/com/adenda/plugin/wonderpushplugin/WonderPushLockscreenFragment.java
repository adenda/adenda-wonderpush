package com.adenda.plugin.wonderpushplugin;

import java.util.Locale;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import sdk.adenda.lockscreen.fragments.AdendaFragmentInterface;
import sdk.adenda.widget.DateTimeFragment;

public class WonderPushLockscreenFragment extends Fragment implements AdendaFragmentInterface 
{
	protected static final String NOTIF_CAMPAIGN_ID = "notification_campaign_id";
	protected static final String NOTIF_NOTIF_ID = "notification_notif_id";
	
	private static final int DEFAULT_DATE_TIME_TXT_COLOR = 0xFF000000;
	private static final int DEFAULT_BACKGROUND_COLOR = 0XFFFFFFFF;
	private static final String ADENDA_DATETIME_COLOR_PARAM = "adenda_datetime_color";
	private static final String ADENDA_BKGRD_COLOR_PARAM = "adenda_background_color";
	private static final String ADENDA_ACTION_URI = "adenda_action_uri";
	private static final String ADENDA_EXPAND_CONTENT = "adenda_expand_content";
	
	private int mDateTimeColor;
	private int mBackgroundColor;
	private String mActionUri;
	private boolean mExpandWebView;
	private String mCampaignId;
	private String mNotifId;
	protected ProgressBar mProgressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		if (args != null)
		{
			mCampaignId = args.getString(NOTIF_CAMPAIGN_ID);
			mNotifId = args.getString(NOTIF_NOTIF_ID);
			
			Long txtColor = getDateTimeColor(args);
			mDateTimeColor =  txtColor != null ? (int)txtColor.longValue() : DEFAULT_DATE_TIME_TXT_COLOR;
			Long bkgrndColor = getBackgroundColor(args);
			mBackgroundColor = bkgrndColor != null ? (int)bkgrndColor.longValue() : DEFAULT_BACKGROUND_COLOR;
			mActionUri = getActionUri(args);
			mExpandWebView = getExpandWebView( args);
		}
	}
	
	protected int getLayout(){ return -1;}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		int layoutId = getLayout();
		if (layoutId <= 0)
			return super.onCreateView(inflater, container, savedInstanceState);
		
		// Load layout
		View view = inflater.inflate(layoutId, container, false);
		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);

		// Set background color
		View dateTimeContainer = view.findViewById(R.id.date_time_container);
		if (dateTimeContainer != null)
		{
			dateTimeContainer.setBackgroundColor(mBackgroundColor);
			DateTimeFragment dateTimeFragment = DateTimeFragment.newInstance(DateTimeFragment.TXT_CENTER_JUSTIFY, mDateTimeColor, false);
			getChildFragmentManager().beginTransaction().replace(R.id.date_time_container, dateTimeFragment).commit();
		}
	
		FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.ua_content_container);
		if ( mExpandWebView && frameLayout != null)
		{
			RelativeLayout.LayoutParams layoutParams = (LayoutParams) frameLayout.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			frameLayout.setLayoutParams(layoutParams);
			// Make sure date/time fragment is on top and transparent
			dateTimeContainer.setBackgroundColor(Color.TRANSPARENT);
			dateTimeContainer.bringToFront();
		}
		
		// Load notification content
		loadNotifContent(view);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		AdendaWonderPush.initialize(getActivity());
	}
	
	protected void loadNotifContent(View view)
	{		
	}
	
	@Override
	public boolean expandOnRotation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Intent getActionIntent() 
	{
		// Record WonderPush event
		//AdendaWonderPush.trackAdendaActionFollowed();
		
		// Record that notification was opened
		recordDirectOpen();
		
		if ( mActionUri != null && !mActionUri.isEmpty())
		{
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mActionUri));
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			return i;
		}
		return null;
	}
	
	private void recordDirectOpen()
	{
		if (mCampaignId == null || mNotifId == null)
			return;
	        
        AdendaWonderPush.trackNotificationOpened(mCampaignId, mNotifId);
	}
	
	private Long getBackgroundColor (Bundle message)
    {
    	return getHexParam(message, ADENDA_BKGRD_COLOR_PARAM);
    }
    
    private Long getDateTimeColor( Bundle message)
    {
    	return getHexParam(message, ADENDA_DATETIME_COLOR_PARAM);
    }
    
    private Long getHexParam( Bundle message, String sParamName)
    {
    	if (message == null || message.isEmpty())
    		return null;
    	
    	String sDateTimeColor = message.getString( sParamName);
    	if ( sDateTimeColor == null)
    		return null;
    	
    	return Long.parseLong(sDateTimeColor, 16);
    }
    
    private String getActionUri(Bundle message)
    {
    	if (message == null || message.isEmpty())
    		return null;
    	
    	return message.getString(ADENDA_ACTION_URI);
    }
    
    private boolean getExpandWebView(Bundle message)
    {
    	if (message == null || message.isEmpty())
    		return false;
    	
    	String sExpandContent = message.getString(ADENDA_EXPAND_CONTENT);
    	if (sExpandContent == null)
    		return false;
    	
    	return sExpandContent.toLowerCase(Locale.US).contentEquals("true");
    }

	@Override
	public boolean coverEntireScreen() {
		return true;
	}

	@Override
	public Pair<Integer, Integer> getGlowpadResources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getStartHelperForResult() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onActionFollowedAndLockScreenDismissed() {
		// TODO Auto-generated method stub
	}
}
