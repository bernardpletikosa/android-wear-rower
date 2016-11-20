package com.github.bernardpletikosa.rower.data;

/** Created by bp on 15/10/2016. */
public class DataHandler {

    private static DataHandler instance;

    public static DataHandler instance() {
        if (instance == null) instance = new DataHandler();
        return instance;
    }

    private int rIdx = 0;
    private int hIdx = 0;
    private float rNorm[] = new float[5];
    private float hNorm[] = new float[3];

    private int lastMax = 0;
    private int notChanged = 0;
    private static int totalRows = 0;

    private DataHandler() {}

    // normalize values
    // collect 3 readings and make an average of them
    // because they go all over the place in the beginning of the measurement
    int processHeartRate(int val) {
        if (hIdx < 3) {
            hNorm[hIdx++] = val;
        } else {
            hIdx = 0;
            float sum = 0f;
            for (float n : hNorm) sum += n;
            return (int) (sum / 3f);
        }

        return 0;
    }

    // normalize values
    // collect 5 readings, increase amplitude 10 times and make an average of them
    int processAcceleration(float val) {
        if (rIdx < 5) {
            rNorm[rIdx++] = val * 10;
        } else {
            float sum = 0f;
            for (float n : rNorm) sum += n;

            for (int idx = 0; idx <= 3; )
                rNorm[idx] = rNorm[++idx];
            rNorm[4] = val * 10;

            return calcRows((int) (sum / 5));
        }

        return -1;
    }

    // after normalization row data is a sinusoid
    // every peak in the sinusoid is one row.
    private int calcRows(int lastVal) {
        if (lastVal > 1) {
            if (lastMax < lastVal) lastMax = lastVal;
            else notChanged++;
        } else if (lastVal < 0) {
            if (lastMax > 50 && notChanged > 5) {
                totalRows++;
                lastMax = 0;
                notChanged = 0;
                return totalRows;
            }
        }

        return -1;
    }

    public void clear() {
        rIdx = 0;
        hIdx = 0;
        rNorm = new float[5];
        hNorm = new float[3];
        lastMax = 0;
        totalRows = 0;
        notChanged = 0;
    }

    public int getTotalRows() {
        return totalRows;
    }
}
