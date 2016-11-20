package com.github.bernardpletikosa.rower.db;

/** Created by bp on 10/11/2016. */
public class Stats {

    private int totalWorkouts = 0;
    private int totalRows = 0;
    private int averageRows = 0;
    private int averageHeartRate = 0;

    public Stats(int totalWorkouts) {
        this.totalWorkouts = totalWorkouts;
    }

    public void addRows(int total) {
        totalRows += total;
    }

    public void addRowRate(int avgRows) {
        averageRows = averageRows == 0 ? avgRows : (averageRows + avgRows) / 2;
    }

    public void addHeartRate(int avgHeartRate) {
        averageHeartRate = averageHeartRate == 0 ? avgHeartRate : (averageHeartRate + avgHeartRate) / 2;
    }

    public int getTotalWorkouts() {
        return totalWorkouts;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getAverageRows() {
        return averageRows;
    }

    public int getAverageHeartRate() {
        return averageHeartRate;
    }
}
