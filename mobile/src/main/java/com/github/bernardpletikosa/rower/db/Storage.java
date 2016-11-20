package com.github.bernardpletikosa.rower.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.activeandroid.query.Select;

import java.util.List;

/** Created by bp on 18/10/2016. */
public class Storage {

    private static final String NAME = "Storage";
    private static final String WORKOUT = "workout";

    private static Storage sStorage = null;
    private SharedPreferences sharedPreferences;

    public static Storage getInstance(Context context) {
        if (sStorage == null) sStorage = new Storage(context);
        return sStorage;
    }

    private Storage(Context context) {
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public Stats getAllWorkouts() {
        List<Workout> workouts = new Select().from(Workout.class).execute();
        final Stats stats = new Stats(workouts.size());
        for (Workout workout : workouts) {
            stats.addRows(workout.total);
            stats.addRowRate((int) (workout.avgRows * 60));
            stats.addHeartRate(workout.avgHeartRate);
        }
        return stats;
    }

    public Workout initWorkout() {
        Workout workout = Workout.getCurrent();
        if (workout == null) {
            workout = Workout.createNew();
        }
        sharedPreferences.edit().putBoolean(WORKOUT, true).apply();

        return workout;
    }

    public boolean isWorkoutRunning() {
        return sharedPreferences.getBoolean(WORKOUT, false);
    }
}
