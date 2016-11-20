package com.github.bernardpletikosa.rower.db;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/** Created by bp on 18/10/2016. */

@Table(name = "Workouts")
public class Workout extends Model {

    @Column(name = "start") public long start;
    @Column(name = "stop") public long stop;
    @Column(name = "break") public long breakTs;
    @Column(name = "duration") public long duration;//in millis
    @Column(name = "total") public int total;
    @Column(name = "avgRows") public float avgRows;//per second
    @Column(name = "avgHeartRate") public int avgHeartRate;

    public Workout() {}

    public static Workout createNew() {
        final Workout workout = new Workout();
        workout.start = System.currentTimeMillis();
        workout.breakTs = System.currentTimeMillis();
        workout.save();
        return workout;
    }

    public static Workout getCurrent() {
        List<Workout> workouts = new Select().from(Workout.class).execute();
        for (Workout workout : workouts)
            if (workout.start > 0 && workout.stop == 0) return workout;
        return null;
    }

    public void pause() {
        duration += System.currentTimeMillis() - breakTs;
        breakTs = System.currentTimeMillis();
        calculate();

        Log.i("Workout pause", toString());
    }

    public void restart() {
        breakTs = System.currentTimeMillis();
        calculate();

        Log.i("Workout restart", toString());
    }

    public void end() {
        duration += System.currentTimeMillis() - breakTs;
        stop = System.currentTimeMillis();

        calculate();
    }

    public void calculate() {
        int totalStrokes = 0;
        for (Stroke stroke : items()) {
            totalStrokes++;
            if (stroke.heartRate <= 0) continue;
            avgHeartRate = avgHeartRate <= 0 ? stroke.heartRate : (stroke.heartRate + avgHeartRate) / 2;
        }

        total = totalStrokes;
        if (total > 0 && duration >= 1000) avgRows = total / (duration / 1000f);

        save();

        Log.i("Workout calculate", toString());
    }

    private List<Stroke> items() {
        return getMany(Stroke.class, "Workout");
    }

    @Override public String toString() {
        return "Workout{" +
                "start=" + start +
                ", breakTs=" + breakTs +
                ", duration=" + duration +
                ", total=" + total +
                ", avgRows=" + avgRows +
                ", avgHeartRate=" + avgHeartRate +
                "} " + super.toString();
    }
}
