package com.github.bernardpletikosa.rower.io;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.bernardpletikosa.rower.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Created by bp on 15/10/2016. */
public class DataDispatch {

    private static final String TAG = "DataDispatch";
    private static DataDispatch instance;
    private final LocalBroadcastManager broadcastManager;

    private int mLastHeartRate;

    public static DataDispatch getInstance(Context context) {
        if (instance == null) instance = new DataDispatch(context.getApplicationContext());
        return instance;
    }

    private GoogleApiClient googleApiClient;
    private ExecutorService executorService;

    private DataDispatch(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        broadcastManager = LocalBroadcastManager.getInstance(context);
        executorService = Executors.newCachedThreadPool();
    }

    public void sendHeart(int rate) {
        if (rate <= 0) return;
        mLastHeartRate = rate;
        broadcast(Constants.HEART, rate);
    }

    public void sendStroke(float amplitude, int strokes) {
        broadcast(Constants.TOTAL, strokes);
        sendSensorData(amplitude);
    }

    public void sendControl(final String path) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final PutDataMapRequest dataMap = PutDataMapRequest.create(path);
                dataMap.getDataMap().putLong(Constants.TIMESTAMP, System.currentTimeMillis());
                send(dataMap.asPutDataRequest());
            }
        });
    }

    private void sendSensorData(final float amplitude) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.PATH_DATA);

                dataMap.getDataMap().putLong(Constants.TIMESTAMP, System.currentTimeMillis());
                dataMap.getDataMap().putFloat(Constants.AMPLITUDE, amplitude);
                dataMap.getDataMap().putInt(Constants.HEART, mLastHeartRate);

                send(dataMap.asPutDataRequest());
            }
        }, new ResultCallback<Result>() {
            @Override public void onResult(@NonNull Result result) {
                Log.e("RESULT", "" + result.getStatus());
            }
        });
    }

    private void broadcast(String key, int value) {
        Intent messageIntent = new Intent();
        messageIntent.setAction(Intent.ACTION_SEND);
        messageIntent.putExtra(key, value);
        broadcastManager.sendBroadcast(messageIntent);
    }

    private void send(PutDataRequest request) {
        if (!validateConnection()) return;

        Wearable.DataApi.putDataItem(googleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult result) {
                        if (!result.getStatus().isSuccess())
                            Log.e(TAG, "Sending failed");
                    }
                });
    }

    private boolean validateConnection() {
        return googleApiClient.isConnected() ||
                googleApiClient.blockingConnect(Constants.CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).isSuccess();
    }

}
