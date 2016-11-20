package com.github.bernardpletikosa.rower.ui.frag;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.bernardpletikosa.rower.Constants;
import com.github.bernardpletikosa.rower.R;
import com.github.bernardpletikosa.rower.Utils;
import com.github.bernardpletikosa.rower.db.Storage;
import com.github.bernardpletikosa.rower.db.Workout;
import com.github.bernardpletikosa.rower.io.WearableManager;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentMain extends Fragment {

    @BindView(R.id.control_toggle) ImageView mControlStart;
    @BindView(R.id.control_total) TextView mControlTotal;

    private List<Node> mNodes;
    private static WearableManager remoteSensorManager;

    private boolean workoutActive = false;
    private Workout mWorkout;

    public static FragmentMain instance() {
        return new FragmentMain();
    }

    public FragmentMain() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);

        remoteSensorManager = WearableManager.getInstance(getActivity());
        workoutActive = Storage.getInstance(getActivity()).isWorkoutRunning();

        initLayout();

        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @SuppressWarnings("unused") @OnClick(R.id.control_toggle)
    public void onToggle() {
        if (workoutActive) stopWorkoutDialog();
        else startWorkout();
    }

    @SuppressWarnings("unused") @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Utils.UpdateWorkoutEvent event) {
        switch (event.getEvent()) {
            case Constants.CONTROL_START:
            case Constants.CONTROL_PAUSE:
            case Constants.CONTROL_END:
                workoutActive = Storage.getInstance(getActivity()).isWorkoutRunning();
                initLayout();
                break;
            case Constants.TOTAL:
                if (!workoutActive) {
                    workoutActive = true;
                    initLayout();
                }
                checkWorkout();
                break;
        }

    }

    private void initLayout() {
        mControlStart.setBackgroundResource(workoutActive ? R.drawable.workout : R.drawable.idle);
        mControlTotal.setVisibility(workoutActive ? View.VISIBLE : View.GONE);
    }

    private void stopWorkoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.ma_frag_main_finish)
                .setMessage(R.string.ma_frag_main_finish_msg)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        workoutActive = false;
                        remoteSensorManager.stopMeasurement();
                        dialog.dismiss();
                        initLayout();
                    }
                })
                .create()
                .show();
    }

    private void startWorkout() {
        remoteSensorManager.startMeasurement();
        remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull final NodeApi.GetConnectedNodesResult nodes) {
                mNodes = nodes.getNodes();
                for (Node node : mNodes) Log.e("NODE", node.getDisplayName());
            }
        });
        workoutActive = true;
        initLayout();
    }

    private void checkWorkout() {
        if (Storage.getInstance(getActivity()).isWorkoutRunning()) mWorkout = Workout.getCurrent();
        if (mWorkout != null) mControlTotal.setText("Total: " + mWorkout.total + " \nHeart: " + mWorkout.avgHeartRate);
        else Log.w("FMAin", "Can't find current workout");
    }
}
