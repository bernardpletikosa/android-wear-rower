package com.github.bernardpletikosa.rower.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.bernardpletikosa.rower.Constants;
import com.github.bernardpletikosa.rower.R;
import com.github.bernardpletikosa.rower.Utils;
import com.github.bernardpletikosa.rower.data.DataHandler;
import com.github.bernardpletikosa.rower.data.SensorService;
import com.github.bernardpletikosa.rower.io.DataDispatch;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** Created by bp on 13/10/2016. */
public class InfoActivity extends WearableActivity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    @Nullable @BindView(R.id.rect_layout) RelativeLayout mRectBackground;
    @Nullable @BindView(R.id.round_layout) RelativeLayout mRoundBackground;

    @BindView(R.id.info) View mInfo;
    @BindView(R.id.time) TextView mTime;
    @BindView(R.id.total) TextView mTotal;
    @BindView(R.id.heart) TextView mHeart;
    @BindView(R.id.status) TextView mStatus;
    @BindView(R.id.control_play) ImageView mControlPlay;
    @BindView(R.id.control_cancel) ImageView mControlStop;
    @BindView(R.id.delayed_confirm) DelayedConfirmationView mDelayedView;
    @BindView(R.id.dismiss_overlay) DismissOverlayView mDismissOverlayView;

    private static final int ANIMATION_DURATION = 500;//millis

    private BroadcastReceiver mMessageReceiver;
    private GestureDetectorCompat mGestureDetector;

    private Timer mTimer;
    private SimpleDateFormat mFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                ButterKnife.bind(InfoActivity.this);

                mDelayedView.setListener(InfoActivity.this);
                mGestureDetector = new GestureDetectorCompat(InfoActivity.this, new GestureDetector.SimpleOnGestureListener() {
                    public void onLongPress(MotionEvent ev) {
                        Utils.sDismissing = true;
                        mDismissOverlayView.show();
                    }
                });

                Utils.checkPermissions(InfoActivity.this);
                setAmbientEnabled();
                receiveMessages();

                if (SensorService.running) startWorkout();
                else clearAfterWorkout();
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (Utils.sDismissing) {
            DataDispatch.getInstance(InfoActivity.this).sendControl(Constants.CONTROL_END);
            stopService(new Intent(InfoActivity.this, SensorService.class));
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateForAmbient(true);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateForAmbient(false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) || super.dispatchTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grants) {
        Utils.updatePermission(requestCode, grants);
    }

    @SuppressWarnings("unused") @OnClick(R.id.control_play)
    public void onPlayPauseClicked() {
        if (SensorService.running) {
            SensorService.pause();
            mControlPlay.setImageResource(R.drawable.play);
            DataDispatch.getInstance(InfoActivity.this).sendControl(Constants.CONTROL_PAUSE);
        } else {
            startWorkout();
            mControlPlay.setImageResource(R.drawable.pause);
            DataDispatch.getInstance(InfoActivity.this).sendControl(Constants.CONTROL_START);
        }
    }

    @SuppressWarnings("unused") @OnClick(R.id.control_cancel)
    public void onStopClicked() {
        mDelayedView.setVisibility(View.VISIBLE);
        mDelayedView.setTotalTimeMs(2000);
        mDelayedView.start();
    }

    @SuppressWarnings("unused") @OnClick(R.id.status)
    public void onStatusClicked() {
        if (!SensorService.running) startWorkout();
    }

    @Override public void onTimerSelected(View v) {
        mDelayedView.reset();
        mDelayedView.setVisibility(View.GONE);
    }

    @Override public void onTimerFinished(View v) {
        SensorService.clear();
        DataHandler.instance().clear();

        stopService(new Intent(InfoActivity.this, SensorService.class));
        DataDispatch.getInstance(InfoActivity.this).sendControl(Constants.CONTROL_END);

        clearAfterWorkout();
        finish();
    }

    private void receiveMessages() {
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int total = intent.getIntExtra(Constants.TOTAL, 0);
                int heart = intent.getIntExtra(Constants.HEART, 0);

                if (total > 0) mTotal.setText(String.valueOf(total));
                if (heart > 0) mHeart.setText(String.valueOf(heart));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, messageFilter);
    }

    private void updateForAmbient(boolean on) {
        if (mStatus == null) return;

        final int colorRes = on ? R.color.white : R.color.colorPrimary;

        mStatus.setTextColor(ContextCompat.getColor(this, colorRes));
        mStatus.setVisibility(SensorService.running ? View.GONE : View.VISIBLE);

        if (SensorService.running) {
            mControlPlay.setImageResource(R.drawable.pause);
            mControlPlay.setVisibility(on ? View.GONE : View.VISIBLE);
            mControlStop.setVisibility(on ? View.GONE : View.VISIBLE);

            mTotal.setTextColor(ContextCompat.getColor(this, colorRes));
            mTime.setTextColor(ContextCompat.getColor(this, colorRes));
            mHeart.setTextColor(ContextCompat.getColor(this, colorRes));
        } else {
            mControlPlay.setVisibility(View.GONE);
            mControlPlay.setImageResource(R.drawable.play);
            mControlStop.setVisibility(View.GONE);
            mInfo.setVisibility(View.GONE);
        }
    }

    public void onLayoutClicked(View view) {
        if (Utils.sPermissionGranted && Utils.isServiceRunning(this, SensorService.class)) return;

        final RelativeLayout layout = mRectBackground != null ? mRectBackground : mRoundBackground;
        layout.setEnabled(false);
        layout.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade));
        layout.postDelayed(new Runnable() {
            @Override public void run() {
                layout.setEnabled(true);
                startWorkout();
            }
        }, ANIMATION_DURATION);
    }

    private void startWorkout() {
        if (!Utils.sPermissionGranted) return;
        DataDispatch.getInstance(InfoActivity.this).sendControl(Constants.CONTROL_START);

        if (Utils.isServiceRunning(this, SensorService.class)) SensorService.start();
        else startService(new Intent(this, SensorService.class));

        mStatus.setVisibility(View.GONE);
        mInfo.setVisibility(View.VISIBLE);
        mTime.setVisibility(View.VISIBLE);
        mControlStop.setVisibility(View.VISIBLE);
        mControlPlay.setVisibility(View.VISIBLE);

        mControlPlay.setImageResource(R.drawable.pause);
        mTotal.setText(String.valueOf(DataHandler.instance().getTotalRows()));

        setUpTimer();
    }

    private void clearAfterWorkout() {
        mStatus.setVisibility(View.VISIBLE);
        mInfo.setVisibility(View.GONE);
        mTime.setVisibility(View.GONE);
        mTime.setText(R.string.default_time);
        mTotal.setText("0");
        mControlPlay.setVisibility(View.GONE);
        mControlStop.setVisibility(View.GONE);
        mDelayedView.setVisibility(View.GONE);
    }

    private void setUpTimer() {
        if (mTimer == null) mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (SensorService.running) mTime.setText(mFormatter.format(SensorService.getTotalTime()));
                    }
                });
            }
        }, 1000, 1000);
    }
}
