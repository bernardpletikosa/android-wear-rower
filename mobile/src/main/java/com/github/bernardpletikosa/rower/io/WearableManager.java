package com.github.bernardpletikosa.rower.io;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.bernardpletikosa.rower.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.bernardpletikosa.rower.Constants.CLIENT_CONNECTION_TIMEOUT;

public class WearableManager {

    private static final String TAG = "WearableManager";

    private static WearableManager instance;

    private ExecutorService executorService;
    private GoogleApiClient googleApiClient;

    public static synchronized WearableManager getInstance(Context context) {
        if (instance == null) instance = new WearableManager(context.getApplicationContext());
        return instance;
    }

    private WearableManager(Context context) {
        this.googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        this.executorService = Executors.newCachedThreadPool();
    }

    private boolean validateConnection() {
        return googleApiClient.isConnected() ||
                googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).isSuccess();
    }

    public void startMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlInBackground(Constants.START_MEASUREMENT);
            }
        });
    }

    public void stopMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlInBackground(Constants.STOP_MEASUREMENT);
            }
        });
    }

    public void getNodes(ResultCallback<NodeApi.GetConnectedNodesResult> pCallback) {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(pCallback);
    }

    private void controlInBackground(final String path) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();

            Log.i(TAG, "Sending to nodes: " + nodes.size());

            for (Node node : nodes) {
                Log.i(TAG, "add node " + node.getDisplayName());
                Wearable.MessageApi
                        .sendMessage(googleApiClient, node.getId(), path, null)
                        .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(@NonNull MessageApi.SendMessageResult result) {
                                Log.i(TAG, "controlInBackground(" + path + "): " + result.getStatus().isSuccess());
                            }
                        });
            }
        } else {
            Log.e(TAG, "No connection possible");
        }
    }
}
