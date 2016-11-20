package com.github.bernardpletikosa.rower.ui.frag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.bernardpletikosa.rower.R;

import butterknife.ButterKnife;

public class FragmentCloud extends Fragment {

    public FragmentCloud() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        final View view = inflater.inflate(R.layout.fragment_cloud, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

}
