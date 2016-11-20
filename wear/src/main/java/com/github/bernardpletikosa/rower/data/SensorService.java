package com.github.bernardpletikosa.rower.data;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.github.bernardpletikosa.rower.R;
import com.github.bernardpletikosa.rower.io.DataDispatch;
import com.github.bernardpletikosa.rower.ui.InfoActivity;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_HEART_RATE;

/** Created by bp on 13/10/2016. */
public class SensorService extends Service implements SensorEventListener {

    private static final String TAG = "SensorService";
    private static final int SENS_ACCELEROMETER = TYPE_ACCELEROMETER;

    public static boolean running;
    private static long startTs;
    private static long totalTime;

    private SensorManager mSensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        startMeasurement();

        final Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.swipe_for_options))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, InfoActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_launcher).build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMeasurement();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!running) return;

        if (event.sensor.getType() == TYPE_HEART_RATE) {
            final int heartRate = DataHandler.instance().processHeartRate((int) (event.values[0]));
            DataDispatch.getInstance(this).sendHeart(heartRate);
        } else if (event.sensor.getType() == TYPE_ACCELEROMETER) {
            if (Math.abs(event.values[0]) < 1) return;

            final int strokes = DataHandler.instance().processAcceleration(event.values[0]);
            if (strokes > 0) DataDispatch.getInstance(this).sendStroke(event.values[0], strokes);
        }
    }

    private void stopMeasurement() {
        running = false;
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    protected void startMeasurement() {
        start();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (mSensorManager == null) {
            Log.e(TAG, "No SensorManager found");
            return;
        }

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
        if (accelerometerSensor == null)
            Log.w(TAG, "No accelerometerSensor found");
        else
            mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Sensor heartRateSensor = mSensorManager.getDefaultSensor(TYPE_HEART_RATE);
        if (heartRateSensor == null)
            Log.w(TAG, "No heartRateSensor found");
        else
            mSensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static void clear() {
        running = false;
        totalTime = 0;
        startTs = 0;
    }

    public static void pause() {
        running = false;
        totalTime += System.currentTimeMillis() - startTs;
        startTs = 0;
    }

    public static void start() {
        running = true;
        startTs = System.currentTimeMillis();
    }

    public static long getTotalTime() {
        return totalTime + (System.currentTimeMillis() - startTs);
    }
}
