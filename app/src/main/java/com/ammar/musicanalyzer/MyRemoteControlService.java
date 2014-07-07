package com.ammar.musicanalyzer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteController;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by agitham on 7/7/2014.
 */
public class MyRemoteControlService extends NotificationListenerService implements RemoteController.OnClientUpdateListener {

    private static final String TAG = "MyRemoteControlService";
    private RemoteController mRemoteController;
    private Context mContext;
    //external callback provided by user.
    private RemoteController.OnClientUpdateListener mExternalClientUpdateListener;

    //Binder for our service.
    private IBinder mBinder = new RCBinder();

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mRemoteController = new RemoteController(mContext, this);
    }

    @Override
    public void onDestroy() {
        setRemoteControllerDisabled();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getAction().equals("com.ammar.musicanalyzer.BIND_RC_CONTROL_SERVICE")) {
            return mBinder;
        } else {
            return super.onBind(intent);
        }
    }

    //Following method will be called by Activity usign IBinder

    /**
     * Enables the RemoteController thus allowing us to receive metadata updates.
     */
    public void setRemoteControllerEnabled() {
        if(!((AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)).registerRemoteController(mRemoteController)) {
            throw new RuntimeException("Error while registering RemoteController!");
        }
    }

    /**
     * Disables RemoteController.
     */
    public void setRemoteControllerDisabled() {
        ((AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(mRemoteController);
    }

    public void setClientUpdateListener(RemoteController.OnClientUpdateListener listener) {
        mExternalClientUpdateListener = listener;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }

    //implementation of RemoteController.OnClientUpdateListener. Does nothing other than calling external callback.
    @Override
    public void onClientChange(boolean clearing) {
        if(mExternalClientUpdateListener != null) {
            mExternalClientUpdateListener.onClientChange(clearing);
        }
    }

    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        if(mExternalClientUpdateListener != null) {
            mExternalClientUpdateListener.onClientMetadataUpdate(metadataEditor);
        }
    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
        if(mExternalClientUpdateListener != null) {
            mExternalClientUpdateListener.onClientPlaybackStateUpdate(state);
        }
    }

    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        if(mExternalClientUpdateListener != null) {
            mExternalClientUpdateListener.onClientPlaybackStateUpdate(state, stateChangeTimeMs, currentPosMs, speed);
        }
    }

    @Override
    public void onClientTransportControlUpdate(int transportControlFlags) {
        if(mExternalClientUpdateListener != null) {
            mExternalClientUpdateListener.onClientTransportControlUpdate(transportControlFlags);
        }

    }

    public class RCBinder extends Binder {

        public MyRemoteControlService getService() {
            return MyRemoteControlService.this;
        }
    }
}
