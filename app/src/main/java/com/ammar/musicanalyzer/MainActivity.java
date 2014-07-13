package com.ammar.musicanalyzer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;


public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private RecyclerView mRecyclerView;
	private RelativeLayout mNowPlaying;
	protected TextView mArtistText;
	protected TextView mTitleText;
	protected TextView mAlbumText;
	protected TextView mStatusText;
	protected LinearLayoutManager mLayoutManager;
	protected MyRemoteControlService mRCService;
	protected boolean mBound = false; //flag indicating if service is bound to Activity
	private boolean playing = false;
	private long lastPosMs = 0;

	private RemoteController.OnClientUpdateListener mClientUpdateListener = new RemoteController.OnClientUpdateListener() {

		@Override
		public void onClientTransportControlUpdate(int transportControlFlags) {

		}

		@Override
		public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {

			switch (state) {

				case RemoteControlClient.PLAYSTATE_PLAYING:
					playing = true;
					Log.d(TAG + " Playback State", "Playing");
					break;
				case RemoteControlClient.PLAYSTATE_PAUSED:
					playing = false;
					Log.d(TAG + " Playback State", "Paused");
					break;
				default:
					playing = false;
					break;
			}

			long timeElapsed;

			if (playing) {
				lastPosMs = currentPosMs;
				mStatusText.setText("Playing started at: " + new Date(stateChangeTimeMs) + ". Current Position is: " + currentPosMs / 1000 + " sec.");
			}
			else {
				timeElapsed = currentPosMs - lastPosMs;
				mStatusText.setText("Playing stopped at: " + new Date(stateChangeTimeMs) + ". Length played: " + timeElapsed / 1000 + " sec.");
			}
		}

		@Override
		public void onClientPlaybackStateUpdate(int state) {

		}

		@Override
		public void onClientMetadataUpdate(RemoteController.MetadataEditor editor) {

			//some players write artist name to METADATA_KEY_ALBUMARTIST instead of METADATA_KEY_ARTIST, so we should double-check it
			mArtistText.setText(editor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
					editor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, getString(R.string.unknown))
			));

			mTitleText.setText(editor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, getString(R.string.unknown)));
			mAlbumText.setText(editor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getString(R.string.unknown)));
		}

		@Override
		public void onClientChange(boolean clearing) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mNowPlaying = (RelativeLayout) findViewById(R.id.current_playing);

		mTitleText = (TextView) findViewById(R.id.title_text);
		mAlbumText = (TextView) findViewById(R.id.album_text);
		mArtistText = (TextView) findViewById(R.id.artist_text);
		mStatusText = (TextView) findViewById(R.id.status_text);
	}

	@Override
	public void onStart() {

		super.onStart();
		Intent intent = new Intent("com.ammar.musicanalyzer.BIND_RC_CONTROL_SERVICE");
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {

			AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

			if (manager.isMusicActive()) {
				Log.d(TAG, "Music is playing");
				playing = true;
			}
			else {
				Log.d(TAG, "Music is not playing");
				playing = false;
			}

			setNowPlayingVisibility();
		}
	}

	private void setNowPlayingVisibility() {

		if (playing && mNowPlaying.getVisibility() != View.VISIBLE) {
			mNowPlaying.setVisibility(View.VISIBLE);
		}
		else if (!playing && mNowPlaying.getVisibility() == View.VISIBLE) {
			mNowPlaying.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStop() {

		super.onStop();
		if (mBound) {
			mRCService.setRemoteControllerDisabled();
		}
		unbindService(mConnection);
	}


	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			//Getting the binder and activating RemoteController instantly
			MyRemoteControlService.RCBinder binder = (MyRemoteControlService.RCBinder) service;
			mRCService = binder.getService();
			mRCService.setRemoteControllerEnabled();
			mRCService.setClientUpdateListener(mClientUpdateListener);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			mBound = false;
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return id == R.id.action_settings || super.onOptionsItemSelected(item);
	}
}
