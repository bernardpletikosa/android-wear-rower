package com.github.bernardpletikosa.rower.io;

import android.util.Log;

import com.github.bernardpletikosa.rower.Constants;
import com.github.bernardpletikosa.rower.Utils;
import com.github.bernardpletikosa.rower.db.Storage;
import com.github.bernardpletikosa.rower.db.Stroke;
import com.github.bernardpletikosa.rower.db.Workout;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.greenrobot.eventbus.EventBus;

public class ReceiverService extends WearableListenerService {

    private static final String TAG = "ReceiverService";
    private static Workout sWorkout;

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i(TAG, "event");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                String path = dataItem.getUri().getPath();

                if (sWorkout == null) sWorkout = Storage.getInstance(getApplicationContext()).initWorkout();

                if (path.startsWith(Constants.PATH_DATA)) {
                    Log.i(TAG, "event - stroke");
                    new Stroke(DataMapItem.fromDataItem(dataItem).getDataMap(), sWorkout).save();
                    sWorkout.calculate();
                    EventBus.getDefault().post(new Utils.UpdateWorkoutEvent(Constants.TOTAL));
                } else if (path.startsWith(Constants.CONTROL_START)) {
                    Log.i(TAG, "event - start");
                    sWorkout.restart();
                    EventBus.getDefault().post(new Utils.UpdateWorkoutEvent(Constants.CONTROL_START));
                } else if (path.startsWith(Constants.CONTROL_PAUSE)) {
                    Log.i(TAG, "event - pause");
                    sWorkout.pause();
                    EventBus.getDefault().post(new Utils.UpdateWorkoutEvent(Constants.CONTROL_PAUSE));
                } else if (path.startsWith(Constants.CONTROL_END)) {
                    Log.i(TAG, "event - end");
                    Storage.getInstance(getApplicationContext()).clear();
                    sWorkout.end();
                    sWorkout = null;
                    EventBus.getDefault().post(new Utils.UpdateWorkoutEvent(Constants.CONTROL_END));
                }
            }
        }
    }
}
