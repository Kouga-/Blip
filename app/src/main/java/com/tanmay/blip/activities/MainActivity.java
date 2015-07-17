/*
 * Copyright 2015, Tanmay Parikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tanmay.blip.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;

import com.tanmay.blip.R;
import com.tanmay.blip.database.SharedPrefs;
import com.tanmay.blip.fragments.FavouritesFragment;
import com.tanmay.blip.fragments.FeedFragment;
import com.tanmay.blip.fragments.RandomFragment;
import com.tanmay.blip.networking.XKCDDownloader;

public class MainActivity extends BaseActivity {

    private static final int DOWNLOAD_REQUEST = 1045;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setUpViews();
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        if (SharedPrefs.getInstance().getFirstRun()) {
            startActivityForResult(new Intent(this, DownloadActivity.class), DOWNLOAD_REQUEST);
        } else if (System.currentTimeMillis() - SharedPrefs.getInstance().getLastRedownloadTime() > 259200000) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_SUCCESS);
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_FAIL);
            LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, intentFilter);
            Intent intent = new Intent(this, XKCDDownloader.class);
            intent.setAction(XKCDDownloader.DOWNLOAD_LAST_TEN);
            startService(intent);
        } else if (System.currentTimeMillis() - SharedPrefs.getInstance().getLastTranscriptCheckTime() > 604800000) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_SUCCESS);
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_FAIL);
            LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, intentFilter);
            Intent intent = new Intent(this, XKCDDownloader.class);
            intent.setAction(XKCDDownloader.DOWNLOAD_TRANSCRIPT);
            startService(intent);
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_SUCCESS);
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_FAIL);
            LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, intentFilter);
            Intent intent = new Intent(this, XKCDDownloader.class);
            intent.setAction(XKCDDownloader.DOWNLOAD_TODAY);
            startService(intent);
        }
    }

    private void setUpViews() {
        if (viewPager.getAdapter() == null) {
            FragmentsAdapter adapter = new FragmentsAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
        }
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DOWNLOAD_REQUEST) {
            if (resultCode == RESULT_OK) {
                SharedPrefs.getInstance().setFirstRun(false);
                setUpViews();
            } else {
                finish();
            }
        }
    }

    class FragmentsAdapter extends FragmentPagerAdapter {

        private String[] titles = getResources().getStringArray(R.array.tabs);

        public FragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override

        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FeedFragment();
                case 1:
                    return new RandomFragment();
                case 2:
                    return new FavouritesFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }
    }
}
