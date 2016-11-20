package com.github.bernardpletikosa.rower;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

import static android.Manifest.permission.BODY_SENSORS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/** Created by bp on 20/11/2016. */
public class Utils {

    public static final int PERMISSION_REQ = 2376;
    public static boolean sPermissionGranted = false;
    public static boolean sDismissing = false;

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void updatePermission(int requestCode, int[] grants) {
        if (requestCode != PERMISSION_REQ) return;
        sPermissionGranted = grants.length > 0 && grants[0] == PERMISSION_GRANTED;
    }

    public static void checkPermissions(Activity context) {
        if (ActivityCompat.checkSelfPermission(context, BODY_SENSORS) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(context, new String[]{BODY_SENSORS}, PERMISSION_REQ);
        else
            sPermissionGranted = true;
    }
}
