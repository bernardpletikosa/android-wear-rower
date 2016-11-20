package com.github.bernardpletikosa.rower.io;

import android.content.Intent;
import android.util.Log;

import com.github.bernardpletikosa.rower.Constants;
import com.github.bernardpletikosa.rower.data.SensorService;
import com.github.bernardpletikosa.rower.ui.InfoActivity;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/** Created by bp on 13/10/2016. */
public class ListenerService extends WearableListenerService {

    private static final String TAG = "ListenerService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent.getPath());

        if (messageEvent.getPath().equals(Constants.STOP_MEASUREMENT)) {
            stopService(new Intent(this, SensorService.class));
        }
        if (messageEvent.getPath().equals(Constants.START_MEASUREMENT)) {
            final Intent intent = new Intent(this, InfoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }
}
