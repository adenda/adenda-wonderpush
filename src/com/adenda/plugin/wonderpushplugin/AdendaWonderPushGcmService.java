package com.adenda.plugin.wonderpushplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.wonderpush.sdk.WonderPush;
import com.wonderpush.sdk.WonderPushGcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import sdk.adenda.lockscreen.AdendaAgent;

public class AdendaWonderPushGcmService extends WonderPushGcmListenerService {

    private static final String ADENDA_LOCKSCREEN_PARAM = "adenda_lockscreen";
    private static final String WONDERPUSH_NOTIFICATION_EXTRA_KEY = "_wp";
    private static final String WONDERPUSH_NOFICIATION_HTML = "message";
    private static final String WONDERPUSH_NOTIFICATION_BASE_URL = "baseUrl";
    private static final String WONDERPUSH_NOTIFICATION_ID = "n";
    private static final String WONDERPUSH_CAMPAIGN_ID = "c";
    private static final String ADENDA_WP_NOTIF_PREFS = "adenda_wp_notif_prefs";
    private static final String ADENDA_WP_NOTIF_PREFIX = "adenda_wp_notif-";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        // This function is called whenever a push notification is *received*.
        boolean bIsAdenda = handleAdendaPushMessage(from, data);
        super.onMessageReceived(from, data);
    }

    private boolean handleAdendaPushMessage(String from, Bundle data)
    {
        if (data == null)
            return false;

        String sCustomField = data.getString("custom");
        boolean bIsAdenda = false;

        if (sCustomField == null)
            return false;

        try {
            JSONObject custom = new JSONObject(data.getString("custom"));
            // Process your custom payload
            bIsAdenda = custom.getBoolean(ADENDA_LOCKSCREEN_PARAM);
            String sWpExtras = data.getString(WONDERPUSH_NOTIFICATION_EXTRA_KEY);

            // If it is an Adenda message, but we're not opted in
            if (bIsAdenda && !AdendaAgent.isOptedIn(this))
                // Just ignore the message, but mark it as processed
                return true;

            if (!bIsAdenda || sWpExtras == null || sWpExtras.isEmpty())
                return bIsAdenda;

            JSONObject wpData = new JSONObject(sWpExtras);
            String sHTML = null, sBaseUrl = null, sNotifId = null, sCampId = null, sNotifTag = null;

            sHTML = getString(wpData, WONDERPUSH_NOFICIATION_HTML);
            sBaseUrl = getString(wpData, WONDERPUSH_NOTIFICATION_BASE_URL);
            sNotifId = getString(wpData, WONDERPUSH_NOTIFICATION_ID);
            sCampId = getString(wpData, WONDERPUSH_CAMPAIGN_ID);
            JSONObject wpAlert = wpData.optJSONObject("alert");
            if (wpAlert != null)
                sNotifTag = getString(wpAlert, "tag");
            if (sNotifTag == null)
                sNotifTag = sCampId;

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
            args.putString(WonderPushLockscreenFragment.NOTIF_NOTIF_TAG, sNotifTag);

            // Get identifier
            String sIdentifier = "WPID:";
            if (sNotifId != null)
                sIdentifier = sIdentifier + sNotifId;
            else if (sCampId != null)
                sIdentifier = sIdentifier + sCampId;

            // Notify Adenda to display this next
            String sVideoUri = args.getString(WpVideoLockscreenFragment.NOTIFICATION_VIDEO_URL);
            long lockScreenNotifId;
            if (sVideoUri != null && !sVideoUri.isEmpty())
                lockScreenNotifId = AdendaAgent.addCustomFragmentContent(getApplicationContext(), null, WpVideoLockscreenFragment.class.getName(), args, sIdentifier, false, true);
            else
                lockScreenNotifId = AdendaAgent.addCustomFragmentContent(getApplicationContext(), null, WpHtmlLockscreenFragment.class.getName(), args, sIdentifier, false, true);
            // Flush Content so that the Urban Airship notification screen appears right away
            AdendaAgent.flushContentCache(getApplicationContext());

            // Save Lock Screen Notification ID in case we need to dismiss it later
            getSharedPreferences(ADENDA_WP_NOTIF_PREFS, Context.MODE_PRIVATE).edit().putLong(ADENDA_WP_NOTIF_PREFIX + sNotifId, lockScreenNotifId).apply();

            registerOnClickListener();

        } catch (JSONException e) {
            Log.e(AdendaWonderPushGcmService.class.getSimpleName(), e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Exception handling broadcast message");
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }

    private void registerOnClickListener()
    {
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Read whether the user clicked the notification (true) or if it was automatically opened (false)
                boolean fromUserInteraction = intent.getBooleanExtra(WonderPush.INTENT_NOTIFICATION_OPENED_EXTRA_FROM_USER_INTERACTION, true);
                // Get the original push notification received intent
                Intent pushNotif = intent.getParcelableExtra(WonderPush.INTENT_NOTIFICATION_OPENED_EXTRA_RECEIVED_PUSH_NOTIFICATION);
                if (pushNotif == null || pushNotif.getExtras() == null)
                    return;

                // Perform desired action, like reading custom key-value payload
                Bundle extras = pushNotif.getExtras();
                try {
                    // Make sure it was an Adenda notification first
                    JSONObject custom = new JSONObject(extras.getString("custom"));
                    boolean bIsAdenda = custom.getBoolean(ADENDA_LOCKSCREEN_PARAM);
                    if (!bIsAdenda)
                        return;

                    // Get Notification Extras
                    String sWpExtras = extras.getString(WONDERPUSH_NOTIFICATION_EXTRA_KEY);
                    if (sWpExtras == null || sWpExtras.isEmpty())
                        return;

                    // Get Notification ID
                    JSONObject wpData = new JSONObject(sWpExtras);
                    String sNotifId = getString(wpData, WONDERPUSH_NOTIFICATION_ID);

                    // Get Lock Screen ID
                    long lockScreenNotifId = context.getSharedPreferences(ADENDA_WP_NOTIF_PREFS, Context.MODE_PRIVATE)
                            .getLong(ADENDA_WP_NOTIF_PREFIX + sNotifId, -1);

                    // Remove lock screen content from queue
                    if (lockScreenNotifId > 0) {
                        AdendaAgent.removeCustomContent(context.getApplicationContext(), lockScreenNotifId);
                        AdendaAgent.flushContentCache(context.getApplicationContext());
                    }
                } catch (JSONException e) {
                    Log.e(AdendaWonderPushGcmService.class.getSimpleName(), e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Exception handling broadcast message");
                    e.printStackTrace();
                }
            }
        }, new IntentFilter(WonderPush.INTENT_NOTIFICATION_OPENED));
    }
}
