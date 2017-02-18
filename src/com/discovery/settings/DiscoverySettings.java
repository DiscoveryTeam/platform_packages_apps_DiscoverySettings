package com.discovery.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;

import com.discovery.settings.DiscoverySettingsTest;
import com.discovery.settings.fragments.BatterySettings;
import com.discovery.settings.fragments.ButtonsSettings;
import com.discovery.settings.fragments.ClockSettings;
import com.discovery.settings.fragments.LockscreenSettings;
import com.discovery.settings.notificationlight.BatteryLightSettings;
import com.discovery.settings.notificationlight.NotificationLightSettings;

import com.discovery.settings.PagerSlidingTabStrip;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;

public class DiscoverySettings extends SettingsPreferenceFragment {

    ViewPager mViewPager;
    String titleString[];
    ViewGroup mContainer;
    PagerSlidingTabStrip mTabs;

    static Bundle mSavedState;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;

        View view = inflater.inflate(R.layout.discovery_settings, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);

        StatusBarAdapter StatusBarAdapter = new StatusBarAdapter(getFragmentManager());
        mViewPager.setAdapter(StatusBarAdapter);

        mTabs.setViewPager(mViewPager);
        setHasOptionsMenu(true);
        return view;
    }

   @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    @Override
    public void onResume() {
        super.onResume();
     	mContainer.setPadding(30, 30, 30, 30);
    }

    class StatusBarAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public StatusBarAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new DiscoverySettingsTest();
            frags[1] = new ButtonsSettings();
            frags[2] = new ClockSettings();
            frags[3] = new LockscreenSettings();
            frags[4] = new BatterySettings();
            frags[5] = new BatteryLightSettings();
            frags[6] = new NotificationLightSettings();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }

    private String[] getTitles() {
        String titleString[];

        titleString = new String[] {
            getString(R.string.discovery_misc_settings),
            getString(R.string.discovery_button_settings),
            getString(R.string.discovery_clock_settings),
            getString(R.string.discovery_lockscreen_settings),
            getString(R.string.discovery_battery_settings),
            getString(R.string.discovery_battery_light_settings),
            getString(R.string.discovery_notification_light_settings)
        };

        return titleString;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISCOVERY;
    }
}

