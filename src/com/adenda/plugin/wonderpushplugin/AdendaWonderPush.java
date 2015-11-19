package com.adenda.plugin.wonderpushplugin;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.wonderpush.sdk.WonderPush;

public class AdendaWonderPush extends WonderPush 
{	
	static public void trackNotificationOpened(String campaignId, String notifId)
	{
		JSONObject trackData = new JSONObject();        
        try {
        		trackData.put("campaignId", campaignId);
        		trackData.put("notificationId", notifId);
        		trackData.put("actionDate", getTime());
		} catch (JSONException e) {
			Log.e(AdendaWonderPush.class.getSimpleName(), e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Could not form JSONObject");
			e.printStackTrace();
		}
        
		WonderPush.trackInternalEvent("@NOTIFICATION_OPENED", trackData, null);
	}
	
	static public void trackAdendaActionFollowed()
	{
		trackEvent("adenda_action_followed");
	}
}
