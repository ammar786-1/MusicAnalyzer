package com.ammar.musicanalyzer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.media.RemoteController;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity {

    protected TextView mArtistText;
    protected TextView mTitleText;
    protected TextView mAlbumText;
    protected MyRemoteControlService mRCService;
    protected boolean mBound = false; //flag indicating if service is bound to Activity

    private RemoteController.OnClientUpdateListener mClientUpdateListener = new RemoteController.OnClientUpdateListener() {

        @Override
        public void onClientTransportControlUpdate(int transportControlFlags) {

        }

        @Override
        public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {

        }

        @Override
        public void onClientPlaybackStateUpdate(int state) {

        }

        @Override
        public void onClientMetadataUpdate(RemoteController.MetadataEditor editor) {

            Log.d("ACtivity", "onClientMetadataUpdate called");

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

        mTitleText = (TextView)findViewById(R.id.title_text);
        mAlbumText = (TextView)findViewById(R.id.album_text);
        mArtistText = (TextView)findViewById(R.id.artist_text);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent("com.ammar.musicanalyzer.BIND_RC_CONTROL_SERVICE");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mBound) {
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
