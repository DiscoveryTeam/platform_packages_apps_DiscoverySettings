package com.discovery.settings.fragments;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.internal.util.discovery.DUtils;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;
import com.discovery.settings.preference.CustomSeekBarPreference;

public class ButtonsSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";
    private ListPreference mTorchPowerButton;

    //Keys
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_MENU_DOUBLE_TAP = "hardware_keys_menu_double_tap";
    private static final String KEY_BACK_PRESS = "hardware_keys_back_press";
    private static final String KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press";
    private static final String KEY_BACK_DOUBLE_TAP = "hardware_keys_back_double_tap";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP = "hardware_keys_app_switch_double_tap";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_ASSIST_DOUBLE_TAP = "hardware_keys_assist_double_tap";
    private static final String KEY_ENABLE_HW_KEYS = "enable_hw_keys";

    // category keys
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_APP_SWITCH = "app_switch_key";
    private static final String CATEGORY_ASSIST = "assist_key";

    // Available custom actions to perform on a key press.
    // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
    // frameworks/base/core/java/android/provider/Settings.java
    private static final int ACTION_NOTHING = 0;
    private static final int ACTION_MENU = 1;
    private static final int ACTION_APP_SWITCH = 2;
    private static final int ACTION_SEARCH = 3;
    private static final int ACTION_VOICE_SEARCH = 4;
    private static final int ACTION_IN_APP_SEARCH = 5;
    private static final int ACTION_LAUNCH_CAMERA = 6;
    private static final int ACTION_SLEEP = 7;
    private static final int ACTION_LAST_APP = 8;
    private static final int ACTION_SPLIT_SCREEN = 9;
    private static final int KEY_ACTION_SCREENSHOT = 10;
    private static final int KEY_ACTION_PARTIAL_SCREENSHOT = 11;
    private static final int KEY_ACTION_PIP = 12;
    private static final int KEY_ACTION_BACK = 13;

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;

    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mBackPressAction;
    private ListPreference mBackLongPressAction;
    private ListPreference mBackDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mMenuDoubleTapAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mAppSwitchDoubleTapAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAssistDoubleTapAction;
    private SwitchPreference mEnableHwKeys;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.buttons_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        if (!DUtils.deviceHasFlashlight(getContext())) {
            Preference toRemove = prefScreen.findPreference(TORCH_POWER_BUTTON_GESTURE);
            if (toRemove != null) {
                prefScreen.removePreference(toRemove);
            }
        } else {
            mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
            int mTorchPowerButtonValue = Settings.Secure.getInt(resolver,
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
            mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
            mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
            mTorchPowerButton.setOnPreferenceChangeListener(this);
        }

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        boolean hasAnyBindableKey = false;
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APP_SWITCH);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);

        mHandler = new Handler();

        mEnableHwKeys =
                (SwitchPreference) findPreference(KEY_ENABLE_HW_KEYS);

        if (hasHomeKey) {
            int defaultHomeLongPressAction = res.getInteger(
                    com.android.internal.R.integer.config_longPressOnHomeBehavior);
            if (defaultHomeLongPressAction < ACTION_NOTHING ||
                    defaultHomeLongPressAction > KEY_ACTION_SCREENSHOT) {
                defaultHomeLongPressAction = ACTION_NOTHING;
            }

            int defaultHomeDoubleTapAction = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnHomeBehavior);
            if (defaultHomeDoubleTapAction < ACTION_NOTHING ||
                    defaultHomeDoubleTapAction > KEY_ACTION_SCREENSHOT) {
                defaultHomeDoubleTapAction = ACTION_NOTHING;
            }

            int longPressHomeAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION,
                    defaultHomeLongPressAction);
            mHomeLongPressAction = initActionList(KEY_HOME_LONG_PRESS, longPressHomeAction);

            int doubleTapHomeAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                    defaultHomeDoubleTapAction);
            mHomeDoubleTapAction = initActionList(KEY_HOME_DOUBLE_TAP, doubleTapHomeAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(homeCategory);
        }

        if (hasMenuKey) {
            int defaultMenuPressAction = res.getInteger(
                    com.android.internal.R.integer.config_pressOnMenuBehavior);
            if (defaultMenuPressAction < ACTION_NOTHING ||
                    defaultMenuPressAction > KEY_ACTION_BACK) {
                defaultMenuPressAction = ACTION_MENU;
            }

            int defaultMenuLongPressAction = res.getInteger(
                    com.android.internal.R.integer.config_longPressOnMenuBehavior);
            if (defaultMenuLongPressAction < ACTION_NOTHING ||
                    defaultMenuLongPressAction > KEY_ACTION_SCREENSHOT) {
                defaultMenuLongPressAction = ACTION_NOTHING;
            }

            int defaultDoubleTapMenuAction = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnMenuBehavior);
            if (defaultDoubleTapMenuAction < ACTION_NOTHING ||
                    defaultDoubleTapMenuAction > KEY_ACTION_SCREENSHOT) {
                defaultDoubleTapMenuAction = ACTION_NOTHING;
            }

            int pressMenuAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_MENU_ACTION,
                    defaultMenuPressAction);
            mMenuPressAction = initActionList(KEY_MENU_PRESS, pressMenuAction);

            int longPressMenuAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION,
                    defaultMenuLongPressAction);
            if (((ListPreference) getPreferenceScreen().findPreference(KEY_MENU_LONG_PRESS)) != null) {
                mMenuLongPressAction = initActionList(KEY_MENU_LONG_PRESS, longPressMenuAction);
            }

            int doubleTapMenuAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_MENU_DOUBLE_TAP_ACTION,
                    defaultDoubleTapMenuAction);
            mMenuDoubleTapAction = initActionList(KEY_MENU_DOUBLE_TAP, doubleTapMenuAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(menuCategory);
        }

        if (hasBackKey) {
            int defaultBackPressAction = res.getInteger(
                    com.android.internal.R.integer.config_pressOnBackBehavior);
            if (defaultBackPressAction < ACTION_NOTHING ||
                    defaultBackPressAction > KEY_ACTION_BACK) {
                defaultBackPressAction = KEY_ACTION_BACK;
            }

            int defaultBackLongPressAction = res.getInteger(
                    com.android.internal.R.integer.config_longPressOnBackBehavior);
            if (defaultBackLongPressAction < ACTION_NOTHING ||
                    defaultBackLongPressAction > KEY_ACTION_SCREENSHOT) {
                defaultBackLongPressAction = ACTION_NOTHING;
            }

            int defaultDoubleTapBackAction = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnBackBehavior);
            if (defaultDoubleTapBackAction < ACTION_NOTHING ||
                    defaultDoubleTapBackAction > KEY_ACTION_SCREENSHOT) {
                defaultDoubleTapBackAction = ACTION_NOTHING;
            }

            int pressBackAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_BACK_ACTION,
                    defaultBackPressAction);
            mBackPressAction = initActionList(KEY_BACK_PRESS, pressBackAction);

            int longPressBackAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_BACK_LONG_PRESS_ACTION,
                    defaultBackLongPressAction);
            if (((ListPreference) getPreferenceScreen().findPreference(KEY_BACK_LONG_PRESS)) != null) {
                mBackLongPressAction = initActionList(KEY_BACK_LONG_PRESS, longPressBackAction);
            }

            int doubleTapBackAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_BACK_DOUBLE_TAP_ACTION,
                    defaultDoubleTapBackAction);
            mBackDoubleTapAction = initActionList(KEY_BACK_DOUBLE_TAP, doubleTapBackAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(backCategory);
        }

        if (hasAppSwitchKey) {
            int defaultAppSwitchPressAction = res.getInteger(
                    com.android.internal.R.integer.config_pressOnAppSwitchBehavior);
            if (defaultAppSwitchPressAction < ACTION_NOTHING ||
                    defaultAppSwitchPressAction > KEY_ACTION_BACK) {
                defaultAppSwitchPressAction = ACTION_APP_SWITCH;
            }

            int defaultAppSwitchLongPressAction = res.getInteger(
                    com.android.internal.R.integer.config_longPressOnAppSwitchBehavior);
            if (defaultAppSwitchLongPressAction < ACTION_NOTHING ||
                    defaultAppSwitchLongPressAction > KEY_ACTION_SCREENSHOT) {
                defaultAppSwitchLongPressAction = ACTION_NOTHING;
            }

            int defaultDoubleTapAppSwitchAction = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnAppSwitchBehavior);
            if (defaultDoubleTapAppSwitchAction < ACTION_NOTHING ||
                    defaultDoubleTapAppSwitchAction > KEY_ACTION_SCREENSHOT) {
                defaultDoubleTapAppSwitchAction = ACTION_NOTHING;
            }

            int pressAppSwitchAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_APP_SWITCH_ACTION,
                    defaultAppSwitchPressAction);
            mAppSwitchPressAction = initActionList(KEY_APP_SWITCH_PRESS, pressAppSwitchAction);

            int longPressAppSwitchAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                    defaultAppSwitchLongPressAction);
            if (((ListPreference) getPreferenceScreen().findPreference(KEY_APP_SWITCH_LONG_PRESS)) != null) {
                mAppSwitchLongPressAction = initActionList(KEY_APP_SWITCH_LONG_PRESS, longPressAppSwitchAction);
            }
            
            int doubleTapAppSwitchAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                    defaultDoubleTapAppSwitchAction);
            mAppSwitchDoubleTapAction = initActionList(KEY_APP_SWITCH_DOUBLE_TAP, doubleTapAppSwitchAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(appSwitchCategory);
        }

        if (hasAssistKey) {
            int defaultAssistPressAction = res.getInteger(
                    com.android.internal.R.integer.config_pressOnAssistBehavior);
            if (defaultAssistPressAction < ACTION_NOTHING ||
                    defaultAssistPressAction > KEY_ACTION_BACK) {
                defaultAssistPressAction = ACTION_SEARCH;
            }

            int defaultAssistLongPressAction = res.getInteger(
                    com.android.internal.R.integer.config_longPressOnAssistBehavior);
            if (defaultAssistLongPressAction < ACTION_NOTHING ||
                    defaultAssistLongPressAction > KEY_ACTION_SCREENSHOT) {
                defaultAssistLongPressAction = ACTION_NOTHING;
            }

            int defaultDoubleTapAssistAction = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnAssistBehavior);
            if (defaultDoubleTapAssistAction < ACTION_NOTHING ||
                    defaultDoubleTapAssistAction > KEY_ACTION_SCREENSHOT) {
                defaultDoubleTapAssistAction = ACTION_NOTHING;
            }

            int pressAssistAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_ASSIST_ACTION,
                    defaultAssistPressAction);
            mAssistPressAction = initActionList(KEY_ASSIST_PRESS, pressAssistAction);

            int longPressAssistAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION,
                    defaultAssistLongPressAction);
            if (((ListPreference) getPreferenceScreen().findPreference(KEY_ASSIST_LONG_PRESS)) != null) {
                mAssistLongPressAction = initActionList(KEY_ASSIST_LONG_PRESS, longPressAssistAction);
            }

            int doubleTapAssistAction = Settings.System.getInt(resolver,
                    Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION,
                    defaultDoubleTapAssistAction);
            if (((ListPreference) getPreferenceScreen().findPreference(KEY_ASSIST_DOUBLE_TAP)) != null) {
                        mAssistDoubleTapAction = initActionList(KEY_ASSIST_DOUBLE_TAP, doubleTapAssistAction);
            }

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(assistCategory);
        }

        if (hasMenuKey || hasHomeKey) {
            if (mEnableHwKeys != null) {
                mEnableHwKeys.setChecked((Settings.System.getInt(getContentResolver(),
                        Settings.System.ENABLE_HW_KEYS, 1) == 1));
                mEnableHwKeys.setOnPreferenceChangeListener(this);
            }
        } else {
            prefScreen.removePreference(mEnableHwKeys);
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public int getMetricsCategory() {
      return MetricsProto.MetricsEvent.DISCOVERY_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) objValue);
            int index = mTorchPowerButton.findIndexOfValue((String) objValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
            if (mTorchPowerButtonValue == 1) {
                //if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        1);
            }
            return true;
        }  else if (preference == mHomeLongPressAction) {
            handleActionListChange(mHomeLongPressAction, objValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleActionListChange(mHomeDoubleTapAction, objValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleActionListChange(mMenuPressAction, objValue,
                    Settings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleActionListChange(mMenuLongPressAction, objValue,
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mMenuDoubleTapAction) {
            handleActionListChange(mMenuDoubleTapAction, objValue,
                    Settings.System.KEY_MENU_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mBackPressAction) {
            handleActionListChange(mBackPressAction, objValue,
                    Settings.System.KEY_BACK_ACTION);
            return true;
        } else if (preference == mBackLongPressAction) {
            handleActionListChange(mBackLongPressAction, objValue,
                    Settings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mBackDoubleTapAction) {
            handleActionListChange(mBackDoubleTapAction, objValue,
                    Settings.System.KEY_BACK_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleActionListChange(mAppSwitchPressAction, objValue,
                    Settings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleActionListChange(mAppSwitchLongPressAction, objValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchDoubleTapAction) {
            handleActionListChange(mAppSwitchDoubleTapAction, objValue,
                    Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleActionListChange(mAssistPressAction, objValue,
                    Settings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {   
            handleActionListChange(mAssistLongPressAction, objValue,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistDoubleTapAction) {
            handleActionListChange(mAssistDoubleTapAction, objValue,
                    Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mEnableHwKeys) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ENABLE_HW_KEYS, value ? 1 : 0);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_ENABLED, value ? 0 : 1);    
            return true;
        }
        return false;
    }
}
