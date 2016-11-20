package com.github.bernardpletikosa.rower.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.activeandroid.query.Delete;
import com.github.bernardpletikosa.rower.R;
import com.github.bernardpletikosa.rower.db.Storage;
import com.github.bernardpletikosa.rower.db.Stroke;
import com.github.bernardpletikosa.rower.db.Workout;
import com.github.bernardpletikosa.rower.ui.frag.FragmentCloud;
import com.github.bernardpletikosa.rower.ui.frag.FragmentMain;
import com.github.bernardpletikosa.rower.ui.frag.FragmentSummary;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tabs) TabLayout mTabView;
    @BindView(R.id.viewpager) ViewPager mViewPager;

    private final Fragment[] mFragments = new Fragment[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        setupViewPager(savedInstanceState);
        setUpTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            new Delete().from(Stroke.class).execute();
            new Delete().from(Workout.class).execute();
            Storage.getInstance(this).clear();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpTabs() {
        mTabView.setupWithViewPager(mViewPager);
        mTabView.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                mViewPager.setCurrentItem(position);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}

            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        mTabView.getTabAt(0).setText(R.string.ma_frag_main);
        mTabView.getTabAt(1).setText(R.string.ma_frag_summary);
        mTabView.getTabAt(2).setText(R.string.ma_frag_cloud);
    }

    private void setupViewPager(Bundle savedInstanceState) {
        if (savedInstanceState != null &&
                getSupportFragmentManager() != null &&
                getSupportFragmentManager().getFragments() != null) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof FragmentMain)
                    mFragments[0] = fragment;
                else if (fragment instanceof FragmentSummary)
                    mFragments[1] = fragment;
                else if (fragment instanceof FragmentCloud)
                    mFragments[2] = fragment;
            }
        }

        if (mFragments[0] == null) mFragments[0] = FragmentMain.instance();
        if (mFragments[1] == null) mFragments[1] = new FragmentSummary();
        if (mFragments[2] == null) mFragments[2] = new FragmentCloud();

        if (getSupportFragmentManager() == null) return;
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFrag(mFragments[0]);
        mViewPagerAdapter.addFrag(mFragments[1]);
        mViewPagerAdapter.addFrag(mFragments[2]);

        mViewPager.setAdapter(mViewPagerAdapter);
    }
}
