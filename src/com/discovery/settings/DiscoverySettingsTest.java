/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.discovery.settings;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscoverySettingsTest extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {
    private static final String TAG = "DiscoverySettings";

    public static final String VOLUME_ROCKER_WAKE = "volume_rocker_wake";
    public static final String VOLUME_ROCKER_MUSIC_CONTROLS = "volume_rocker_music_controls";
    private static final String KEY_NAVIGATION_BAR         = "navigation_bar";
    public static final String KEYGUARD_TOGGLE_TORCH = "keyguard_toggle_torch";
    public static final String FINGERPRINT_SUCCESS_VIB = "fingerprint_success_vib";
    public static final String SCREENSHOT_SOUND = "screenshot_sound";
    private static final String KEY_BATTERY_LIGHT = "battery_light";
    private static final String CATEGORY_BATTERY_LED = "battery_led_settings";
    public static final String TF_SCREENSHOT = "three_finger_gesture";
    public static final String DOUBLE_TAP_VIBRATE = "double_tap_vibrate";

    private static final String EMPTY_STRING = "";

    private Handler mHandler;

    private SwitchPreference mVolRockerWake;
    private SwitchPreference mVolRockerMusic;
    private SwitchPreference mNavigationBar;
    private SwitchPreference mKeyguardTorch;
    private SwitchPreference mFingerprintVib;
    private SwitchPreference mScreenshotSound;
    //private Preference mBattLedFragment;
    private SwitchPreference mThreeFingerScreenshot;
    private SwitchPreference mDoubleTapVibrate;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISCOVERY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.discovery_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();

        //final PreferenceCategory leds = (PreferenceCategory) findPreference(CATEGORY_BATTERY_LED);

        mVolRockerWake = (SwitchPreference) findPreference(VOLUME_ROCKER_WAKE);
        mVolRockerWake.setOnPreferenceChangeListener(this);

        mVolRockerMusic = (SwitchPreference) findPreference(VOLUME_ROCKER_MUSIC_CONTROLS);
        mVolRockerMusic.setOnPreferenceChangeListener(this);

        /* Navigation Bar */
        mNavigationBar = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR);
        if (mNavigationBar != null) {
            mNavigationBar.setOnPreferenceChangeListener(this);
        }

        mKeyguardTorch = (SwitchPreference) findPreference(KEYGUARD_TOGGLE_TORCH);
        mKeyguardTorch.setOnPreferenceChangeListener(this);

        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_SUCCESS_VIB);
        mFingerprintVib.setOnPreferenceChangeListener(this);

        mScreenshotSound = (SwitchPreference) findPreference(SCREENSHOT_SOUND);
        mScreenshotSound.setOnPreferenceChangeListener(this);

        mThreeFingerScreenshot = (SwitchPreference) findPreference(TF_SCREENSHOT);
        mThreeFingerScreenshot.setOnPreferenceChangeListener(this);

        mDoubleTapVibrate = (SwitchPreference) findPreference(DOUBLE_TAP_VIBRATE);
        mDoubleTapVibrate.setOnPreferenceChangeListener(this);


        //mBattLedFragment = findPreference(KEY_BATTERY_LIGHT);
        //remove battery led settings if device doesnt support it
        //if (!getResources().getBoolean(
        //        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
        //    leds.removePreference(findPreference(CATEGORY_BATTERY_LED));
        //}
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        final ContentResolver resolver = getActivity().getContentResolver();

        final boolean navigationBarEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        // Update volume rocker wake
        if (mVolRockerWake != null) {
            int value = Settings.System.getInt(getContentResolver(), VOLUME_ROCKER_WAKE, 0);
            mVolRockerWake.setChecked(value != 0);
        }

        // Update volume rocker music controls
        if (mVolRockerMusic != null) {
            int value = Settings.System.getInt(getContentResolver(), VOLUME_ROCKER_MUSIC_CONTROLS, 0);
            mVolRockerMusic.setChecked(value != 0);
        }

        // Update navigation bar preference
        if (mNavigationBar != null) {
            mNavigationBar.setChecked(navigationBarEnabled);
        }

        // Update keyguard torch
        if (mKeyguardTorch != null) {
            int value = Settings.System.getInt(getContentResolver(), KEYGUARD_TOGGLE_TORCH, 0);
            mKeyguardTorch.setChecked(value != 0);
        }

        // Update fingerprint authentication vibration
        if (mFingerprintVib != null) {
            int value = Settings.System.getInt(getContentResolver(), FINGERPRINT_SUCCESS_VIB, 0);
            mFingerprintVib.setChecked(value != 0);
        }

        // Update Screenshot Sound
        if (mScreenshotSound != null) {
            int value = Settings.System.getInt(getContentResolver(), SCREENSHOT_SOUND, 0);
            mScreenshotSound.setChecked(value != 0);
        }

        // Update three finger screenshot
        if (mThreeFingerScreenshot != null) {
            int value = Settings.System.getInt(getContentResolver(), TF_SCREENSHOT, 0);
            mThreeFingerScreenshot.setChecked(value != 0);
        }

        // Update double tap vibration
        if (mDoubleTapVibrate != null) {
            int value = Settings.System.getInt(getContentResolver(), DOUBLE_TAP_VIBRATE, 0);
            mDoubleTapVibrate.setChecked(value != 0);
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list != null) {
            list.setValue(Integer.toString(value));
            list.setSummary(list.getEntry());
            list.setOnPreferenceChangeListener(this);
        }
        return list;
    }

    private boolean handleOnPreferenceTreeClick(Preference preference) {
        if (preference != null && preference == mNavigationBar) {
            mNavigationBar.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNavigationBar.setEnabled(true);
                }
            }, 1000);
            return true;
        }
        return false;
    }

    private boolean handleOnPreferenceChange(Preference preference, Object newValue) {
        final String setting = getSystemPreferenceString(preference);

        if (TextUtils.isEmpty(setting)) {
            // No system setting.
            return false;
        }

        if (preference != null && preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            String value = (String) newValue;
            int index = listPref.findIndexOfValue(value);
            listPref.setSummary(listPref.getEntries()[index]);
            Settings.System.putIntForUser(getContentResolver(), setting, Integer.valueOf(value),
                    UserHandle.USER_CURRENT);
        } else if (preference != null && preference instanceof SwitchPreference) {
            boolean state = false;
            if (newValue instanceof Boolean) {
                state = (Boolean) newValue;
            } else if (newValue instanceof String) {
                state = Integer.valueOf((String) newValue) != 0;
            }
            Settings.System.putIntForUser(getContentResolver(), setting, state ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }

        return true;
    }

    private String getSystemPreferenceString(Preference preference) {
        if (preference == null) {
            return EMPTY_STRING;
        } else if (preference == mNavigationBar) {
            return Settings.System.NAVIGATION_BAR_ENABLED;
        }

        return EMPTY_STRING;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final boolean handled = handleOnPreferenceTreeClick(preference);
        // return super.onPreferenceTreeClick(preferenceScreen, preference);
        return handled;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final boolean handled = handleOnPreferenceChange(preference, objValue);
        final String key = preference.getKey();

		if (preference == mVolRockerWake) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), VOLUME_ROCKER_WAKE, value ? 1 : 0);
            return true;
        } else if (preference == mVolRockerMusic) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), VOLUME_ROCKER_MUSIC_CONTROLS, value ? 1 : 0);
            return true;
        } else if (preference == mKeyguardTorch) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), KEYGUARD_TOGGLE_TORCH, value ? 1 : 0);
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mScreenshotSound) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), SCREENSHOT_SOUND, value ? 1 : 0);
            return true;
        } else if (preference == mThreeFingerScreenshot) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), TF_SCREENSHOT, value ? 1 : 0);
            return true;
        } else if (preference == mDoubleTapVibrate) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), DOUBLE_TAP_VIBRATE, value ? 1 : 0);
            return true;
        }

        if (handled) {
            updateState();
        }
        return handled;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.discovery_settings;
                    result.add(sir);

                    return result;
                }
            };
}
