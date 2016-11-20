package com.github.bernardpletikosa.rower.db;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.github.bernardpletikosa.rower.Constants;
import com.google.android.gms.wearable.DataMap;

/** Created by bp on 18/10/2016. */

@Table(name = "Strokes")
public class Stroke extends Model {

    @Column(name = "timestamp") public long timestamp;
    @Column(name = "amplitude") public float amplitude;
    @Column(name = "heartRate") public int heartRate;

    @Column(name = "Workout") public Workout workout;

    public Stroke() {
        super();
    }

    public Stroke(DataMap dataMap, Workout current) {
        this();
        timestamp = dataMap.getLong(Constants.TIMESTAMP);
        amplitude = Math.abs(dataMap.getFloat(Constants.AMPLITUDE));
        heartRate = dataMap.getInt(Constants.HEART);
        workout = current;
        save();

        Log.i("Stroke", "Amp: " + amplitude + ", hr: " + heartRate);
    }
}
