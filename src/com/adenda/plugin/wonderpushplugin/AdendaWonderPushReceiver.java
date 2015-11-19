package com.adenda.plugin.wonderpushplugin;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import sdk.adenda.lockscreen.AdendaAgent;

import com.wonderpush.sdk.WonderPushBroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AdendaWonderPushReceiver extends WonderPushBroadcastReceiver 
{

	private static final String ADENDA_LOCKSCREEN_PARAM = "adenda_lockscreen";
	private static final String WONDERPUSH_NOTIFICATION_EXTRA_KEY = "_wp";
    private static final String WONDERPUSH_NOFICIATION_HTML = "message";
    private static final String WONDERPUSH_NOTIFICATION_BASE_URL = "baseUrl";
    private static final String WONDERPUSH_NOTIFICATION_ID = "n";
    private static final String WONDERPUSH_CAMPAIGN_ID = "c";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// This function is called whenever a push notification is *received*.
		boolean bIsAdenda = handlePushRequest(context, intent);
		
		if (!bIsAdenda)
			super.onReceive(context, intent);
	}
	
	public static boolean handlePushRequest(Context context, Intent intent)
	{
		if (intent == null || !intent.hasExtra("custom") || intent.getExtras() == null)
			return false;
		
		boolean bIsAdenda = false;
		Bundle extras = intent.getExtras();
	    try {
	        JSONObject custom = new JSONObject(intent.getExtras().getString("custom"));
	        // Process your custom payload
	        bIsAdenda = custom.getBoolean(ADENDA_LOCKSCREEN_PARAM);
	        String sWpExtras = extras.getString(WONDERPUSH_NOTIFICATION_EXTRA_KEY);
	        
	        if (bIsAdenda && sWpExtras != null && !sWpExtras.isEmpty())
	        {
	        	JSONObject wpData = new JSONObject(sWpExtras);
        		String sHTML = null, sBaseUrl = null, sNotifId = null, sCampId = null;

        		sHTML = getString(wpData, WONDERPUSH_NOFICIATION_HTML);
        		sBaseUrl = getString(wpData, WONDERPUSH_NOTIFICATION_BASE_URL);
        		sNotifId = getString(wpData, WONDERPUSH_NOTIFICATION_ID);
        		sCampId = getString(wpData, WONDERPUSH_CAMPAIGN_ID);
        		
	        	Bundle args = new Bundle();
	        	// Get extra adenda args
	        	for (Iterator<?> keys = custom.keys(); keys.hasNext();)
	        	{
	        		String sKey = (String) keys.next();
		        	args.putString(sKey, (String)custom.get(sKey));
	        	}
	        	
	        	// Add HTML arg
	        	if (sHTML != null)
	        		args.putString(WpHtmlLockscreenFragment.NOTIFICATION_HTML, sHTML);
	        	if (sBaseUrl != null)
	        		args.putString(WpHtmlLockscreenFragment.NOTIFICATION_BASE_URL, sBaseUrl);
	        	
	        	// Add notification and campaign IDs
	        	args.putString(WonderPushLockscreenFragment.NOTIF_CAMPAIGN_ID, sCampId);
	        	args.putString(WonderPushLockscreenFragment.NOTIF_NOTIF_ID, sNotifId);
	        	
	        	// Get identifier
	        	String sIdentifier = "WPID:";
	        	if (sNotifId != null)
	        		sIdentifier = sIdentifier + sNotifId;
	        	else if (sCampId != null)
	        		sIdentifier = sIdentifier + sCampId;
	        	
	        	// Notify Adenda to display this next
	        	String sVideoUri = args.getString(WpVideoLockscreenFragment.NOTIFICATION_VIDEO_URL);
	        	if (sVideoUri != null && !sVideoUri.isEmpty())
	        		AdendaAgent.addCustomFragmentContent(context.getApplicationContext(), null, WpVideoLockscreenFragment.class.getName(), args, sIdentifier, false, true);
	        	else
	        		AdendaAgent.addCustomFragmentContent(context.getApplicationContext(), null, WpHtmlLockscreenFragment.class.getName(), args, sIdentifier, false, true);
				// Flush Content so that the Urban Airship notification screen appears right away
            	AdendaAgent.flushContentCache(context.getApplicationContext());
	        	
	        }
	    } catch (JSONException e) {
	        Log.e(AdendaWonderPushReceiver.class.getSimpleName(), e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Exception handling broadcast message");
	    }
	    
	    return bIsAdenda;
	}
	
	private static String getString(JSONObject jsonObject, String sName)
	{
		try{
			return jsonObject.getString(sName);
		}
		catch(JSONException e)
		{	        			
		}
		
		return null;
	}
}
