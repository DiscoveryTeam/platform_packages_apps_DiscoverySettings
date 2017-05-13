/*
 * Copyright (C) 2016-17 Discovery
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

package com.discovery.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.hardware.fingerprint.FingerprintManager;
import com.android.settings.Utils;


public class LockscreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "LockscreenSettings";

    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String DOUBLE_TAP_SLEEP_LOCK_SCREEN = "double_tap_sleep_lock_screen";
    private static final String KEY_LOCKSCREEN_QUICK_UNLOCK_CONTROL = "quick_unlock_control";

    private SwitchPreference mFpKeystore;
    private SwitchPreference mDt2sLockscreen;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mQuickUnlockScreen;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISCOVERY;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        addPreferencesFromResource(R.xml.ls_settings);
        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);


        mFpKeystore = (SwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        mFpKeystore.setChecked((Settings.System.getInt(getContentResolver(),
            Settings.System.FP_UNLOCK_KEYSTORE, 0) == 1));
        mFpKeystore.setOnPreferenceChangeListener(this);

        mDt2sLockscreen = (SwitchPreference) findPreference(DOUBLE_TAP_SLEEP_LOCK_SCREEN);
        mDt2sLockscreen.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_LOCK_SCREEN, 0) == 1));
        mDt2sLockscreen.setOnPreferenceChangeListener(this);

        // Quick Unlock Screen Control
        mQuickUnlockScreen = (SwitchPreference) findPreference(KEY_LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        mQuickUnlockScreen.setChecked((Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1));
        mQuickUnlockScreen.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue){
        if (preference == mFpKeystore) {
            boolean value = (Boolean) objValue;

            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.FP_UNLOCK_KEYSTORE, value ? 1 : 0);

            return true;
        } else if (preference == mDt2sLockscreen) {
            boolean value = (Boolean) objValue;

            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_LOCK_SCREEN, value ? 1 : 0);

            return true;
        } else if (preference == mQuickUnlockScreen) {
            boolean value = (Boolean) objValue;
            
            Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_QUICK_UNLOCK_CONTROL, value ? 1 : 0);

            return true;
        }
        return false;
    }
}
