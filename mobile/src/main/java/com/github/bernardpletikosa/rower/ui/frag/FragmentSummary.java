package com.github.bernardpletikosa.rower.ui.frag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.bernardpletikosa.rower.R;
import com.github.bernardpletikosa.rower.db.Stats;
import com.github.bernardpletikosa.rower.db.Storage;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentSummary extends Fragment {

    @BindView(R.id.workout_total) TextView mTotalWorkouts;
    @BindView(R.id.workout_total_rows) TextView mTotalRows;
    @BindView(R.id.workout_avg_rows) TextView mAvgRows;
    @BindView(R.id.workout_avg_hearth_rate) TextView mAvgHeartRate;

    public FragmentSummary() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        final View view = inflater.inflate(R.layout.fragment_summary, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        update();
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void update() {
        if (getActivity() == null) return;

        final Stats stats = Storage.getInstance(getActivity()).getAllWorkouts();

        mTotalWorkouts.setText(String.valueOf(stats.getTotalWorkouts()));
        mTotalRows.setText(String.valueOf(stats.getTotalRows()));
        mAvgRows.setText(String.valueOf(stats.getAverageRows()));
        mAvgHeartRate.setText(String.valueOf(stats.getAverageHeartRate()));
    }
}
