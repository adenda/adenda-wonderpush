package com.adenda.plugin.wonderpushplugin;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

public class WpVideoLockscreenFragment extends WonderPushLockscreenFragment 
{
	public static final String NOTIFICATION_VIDEO_URL = "adenda_video_uri";
	
	// Private members 
	ScreenStateReceiver mReceiver;
	VideoView mVideoView;
	Button mVolumeButton;
	boolean mIsVolumeOn;
	MediaPlayer mMediaPlayer;
	boolean mScreenOn;
	
	private class ScreenStateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) 
	        {
				mScreenOn = false;
				mVideoView.pause();
	        }
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				mScreenOn = true;
				mVideoView.start();
			}
		}	
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Volume off by default
		mIsVolumeOn = false;
	    setRetainInstance(true);
	}
	
	@Override
	protected int getLayout()
	{
		return R.layout.video_layout;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		if (mProgressBar != null)
		{
			mProgressBar.setIndeterminate(true);
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
		// Setup Volume Button
		mVolumeButton = (Button) view.findViewById(R.id.volume_button);
		mVolumeButton.setBackgroundResource(R.drawable.ic_lockscreen_volume_off);
		// Only enable button when video is ready
		mVolumeButton.setEnabled(false);
		mVolumeButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onVolumeChange();
			}});
		
		mVideoView = (VideoView) view.findViewById(R.id.customVideoView);
		
		return view;
	}
	
	@SuppressLint({ "NewApi", "InlinedApi" })
	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		// Get initial screen state
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH)
		{
			// Get initial screen state
			PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
			mScreenOn = pm.isScreenOn();
		}
		
		else
		{
			DisplayManager dm = (DisplayManager)getActivity().getSystemService(Context.DISPLAY_SERVICE);
			mScreenOn = false;
			Display display = dm.getDisplay(Display.DEFAULT_DISPLAY);
			if (display != null && display.getState() == Display.STATE_ON)
				mScreenOn = true;
		}
		
		// Initialize Receiver
		intializeReceiver();	
		
		// Get video URI
		String sVideoUri = getArguments().getString(NOTIFICATION_VIDEO_URL);
		// If we don't have a video uri, nothing else to do
		if (sVideoUri == null)
			return;
 
        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(getActivity());
            mediacontroller.setAnchorView(mVideoView);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(sVideoUri);
            mVideoView.setMediaController(mediacontroller);
            mVideoView.setVideoURI(video);
 
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
 
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp) 
			{
				// Save MediaPlayer so that we can adjust volume later
				mMediaPlayer = mp;
				// Dismiss progress bar
				mProgressBar.setVisibility(View.GONE);
				// Set volume to low
				mp.setVolume(0f, 0f);
				// Enable volume button
				mVolumeButton.setEnabled(true);
				if (mScreenOn)
					mVideoView.start();
			}
		});
        mVideoView.setOnErrorListener(new OnErrorListener(){

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// Make sure we dismiss the progress bar
				mProgressBar.setVisibility(View.GONE);
				return false;
			}});
	}
	
	private void onVolumeChange()
	{
		// If volume is off
		if (!mIsVolumeOn)
		{
			// Set volume on
			mMediaPlayer.setVolume(1f, 1f);
			mIsVolumeOn = true;
			// Change button background
			mVolumeButton.setBackgroundResource(R.drawable.ic_lockscreen_volume_on);
		}
		else
		{
			// Set volume off
			mMediaPlayer.setVolume(0f, 0f);
			mIsVolumeOn = false;
			mVolumeButton.setBackgroundResource(R.drawable.ic_lockscreen_volume_off);
		}
	}
	
	private void intializeReceiver()
	{
		// Instantiate receiver
		mReceiver = new ScreenStateReceiver();
		// Create Screen On and Screen Off filters for BroadcastReceiver
	    IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
	    // Register screen filters
	    getActivity().registerReceiver(mReceiver, screenFilter);
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		// Unregister receiver to avoid memory leaks!
		getActivity().unregisterReceiver(mReceiver);
	}

	@Override
	public boolean expandOnRotation() {
		return true;
	}

	@Override
	public Intent getActionIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("audio/*");
		return Intent.createChooser(intent, "Music File");
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
	}
}
