package com.crosspromotion.sdk.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.openmediation.sdk.utils.DeveloperLog;

public class VolumeReceiver extends BroadcastReceiver {

    public static final String ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
    public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    public static final String RINGER_MODE_CHANGED_ACTION = "android.media.RINGER_MODE_CHANGED";
    private MuteModeListener mMuteModeListener;
    int systemVolume = 0;

    public void setMuteModeListener(MuteModeListener muteModeListener) {
        this.mMuteModeListener = muteModeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (this) {
            if (ACTION_VOLUME_CHANGED.equals(intent.getAction())) {
                handleSystemVolumeChange(context, intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1));
            } else if (RINGER_MODE_CHANGED_ACTION.equals(intent.getAction())) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                final int ringerMode = audioManager.getRingerMode();
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        //normal

                        handleSystemVolumeChange(context, AudioManager.STREAM_MUSIC);
                        DeveloperLog.LogD("normal mode");
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        //vibrate
                        handleSystemVolumeChange(context, AudioManager.STREAM_MUSIC);
                        DeveloperLog.LogD("vibrate mode");
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        //silent
                        if (mMuteModeListener != null) {
                            mMuteModeListener.onMuteMode(true);
                        }
                        DeveloperLog.LogD("silent mode");
                        break;
                }
            }
        }
    }

    public void register(Context context) {
        if (context == null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VOLUME_CHANGED);
        filter.addAction(RINGER_MODE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    public void unRegister(Context context) {
        if (context != null) {
            context.unregisterReceiver(this);
        }
    }

    private void handleSystemVolumeChange(Context context, int type) {
        if (type != AudioManager.STREAM_SYSTEM) {
            return;
        }
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int streamVolume;
        if (mAudioManager != null) {
            streamVolume = mAudioManager.getStreamVolume(type);
            if (streamVolume == 0) {
                systemVolume = 0;
                if (mMuteModeListener != null) {
                    mMuteModeListener.onMuteMode(true);
                }
            } else {
                if (systemVolume == streamVolume || systemVolume > 0) {
                    return;
                }
                systemVolume = streamVolume;
                DeveloperLog.LogD("system volume" + streamVolume);
                if (mMuteModeListener != null) {
                    mMuteModeListener.onMuteMode(false);
                }
            }
        }
    }

    public interface MuteModeListener {
        void onMuteMode(boolean mute);
    }
}
